package zener.zcomm.gui.zcomm_inventory;

import java.util.UUID;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.util.inventoryUtils;

public class Inv extends SimpleInventory {

    private final PlayerInventory playerInventory;
    private final ItemStack zcommItemStack;
    private final NbtCompound tag;

    public Inv(PlayerInventory playerInventory, ItemStack zcommItemStack, int size) {
        super(size);
        this.playerInventory = playerInventory;
        this.zcommItemStack = zcommItemStack;
        this.tag = zcommItemStack.getOrCreateNbt();
    }

    public Inv(PlayerInventory playerInventory, ItemStack zcommItemStack) {
        this(playerInventory, zcommItemStack, 8);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void markDirty() {
        if (playerInventory.player.world.isClient()) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) playerInventory.player;
        tag.put("Inventory", inventoryUtils.toTag(this));
        NbtCompound coverTag = this.getStack(1).getOrCreateNbt();
        if (!coverTag.contains("v") || coverTag.getBoolean("v") == false) {
            if (tag.contains("CustomModelData")){
                tag.remove("CustomModelData");
            }
        } else { 
            if (coverTag.contains("CustomModelData")) {
                tag.putInt("CustomModelData", coverTag.getInt("CustomModelData"));
            }
        }

        if (tag.contains("UUID")){
            if (tag.get("UUID").getType() == NbtType.STRING) {
                String uuid = tag.getString("UUID");
                ComponentHandler.updateCommEntry(player, uuid, zcommItemStack);
            }
            if (tag.get("UUID").getType() == NbtType.INT_ARRAY) {
                UUID uuid = tag.getUuid("UUID");
                ComponentHandler.updateCommEntry(player, uuid, zcommItemStack);
            }
        }
        
        super.markDirty();
    }
}
