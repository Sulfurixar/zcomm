package zener.zcomm.networking;

import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayChannelHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import zener.zcomm.entities.charmProjectile;

public class EntitySpawnPacketHandler implements PlayChannelHandler {
    
    public EntitySpawnPacketHandler(MinecraftClient mc, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        this.receive(mc, handler, buf, sender);
    }

    public void receive(MinecraftClient mc, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        EntityType<?> et = Registry.ENTITY_TYPE.get(buf.readVarInt());
        UUID uuid = buf.readUuid();
        int entityId = buf.readVarInt();
        UUID ownerId = buf.readUuid();
        Vec3d pos = EntitySpawnPacket.PacketBufUtil.readVec3d(buf);
        float pitch = EntitySpawnPacket.PacketBufUtil.readAngle(buf);
        float yaw = EntitySpawnPacket.PacketBufUtil.readAngle(buf);
        if (mc.world == null) {
            //throw new IllegalStateException("Tried to spawn entity in a null world!");
            return;
        }
        Entity e = et.create(mc.world);
        if (e == null)
            throw new IllegalStateException("Failed to create instance of entity \"" + Registry.ENTITY_TYPE.getId(et) + "\"!");
        e.updateTrackedPosition(pos);
        e.setPos(pos.x, pos.y, pos.z);
        e.setPitch(pitch);
        e.setYaw(yaw);
        e.setId(entityId);
        e.setUuid(uuid);
        PlayerEntity player = mc.world.getPlayerByUuid(ownerId);
        ((charmProjectile)e).setOwner(player);
        mc.world.addEntity(entityId, e);
    }

}
