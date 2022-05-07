package zener.zcomm.components;

import java.util.UUID;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import lombok.Data;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import zener.zcomm.entities.CharmProjectile;

@Data
public class CharmComponent implements ICharmComponent, AutoSyncedComponent {
    
    private PlayerEntity player;
    private final CharmProjectile charm;
    private boolean synced = false;

    public CharmComponent(CharmProjectile charm) {
        this.charm = charm;
    }

    public void setOwner(PlayerEntity player) {
        this.player = player;
        this.synced = true;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.get("player") != null && tag.get("player").getType() == NbtType.INT_ARRAY) {
            UUID uuid = tag.getUuid("player");
            if (uuid != null && charm != null) {
                if (charm.getServer() != null) {
                    player = charm.getServer().getPlayerManager().getPlayer(uuid);
                } else {
                    if (charm.getEntityWorld().isClient()) {
                        player = charm.getEntityWorld().getPlayerByUuid(uuid);
                    }
                }
            }
        }
        if (tag.get("synced") != null) {
            synced = tag.getBoolean("synced");
        }
    }
    @Override
    public void writeToNbt(NbtCompound tag) {
        if (player != null) { 
            tag.putUuid("player", player.getUuid());
            tag.putBoolean("synced", true);
        }
    }

}
