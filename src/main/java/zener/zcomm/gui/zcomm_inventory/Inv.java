package zener.zcomm.gui.zcomm_inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import zener.zcomm.data.dataHandler;
import zener.zcomm.data.playerData;
import zener.zcomm.util.inventoryUtils;
import zener.zcomm.util.nrCheck;

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

    @Override
    public void markDirty() {
        if (playerInventory.player.world.isClient()) {
            return;
        }
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
            String uuid = tag.getString("UUID");
            playerData playerData = dataHandler.data.commData.get(uuid);
            if (playerData == null) {
                int nr = zcommItemStack.getOrCreateNbt().getInt("NR");
                dataHandler.addEntry(uuid, new playerData(playerInventory.player.getUuidAsString(), new nrCheck(nr).getNrStr(), "", "", new String[] { "", "", "", "", "", ""}));
            } else {
                playerData newPlayerData = new playerData(zcommItemStack, playerData.USER_ID);
                dataHandler.updateEntry(uuid, newPlayerData);
            }
        }
        
        super.markDirty();
    }
}
