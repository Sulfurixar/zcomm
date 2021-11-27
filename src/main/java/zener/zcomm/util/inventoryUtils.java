package zener.zcomm.util;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import zener.zcomm.Main;
import zener.zcomm.data.playerData;

public class inventoryUtils {
    
    public static NbtList toTag(SimpleInventory inventory) {
        NbtList tag = new NbtList();

        for (int i = 0; i < inventory.size(); i++) {
            NbtCompound stackTag = new NbtCompound();
            stackTag.putInt("Slot", i);
            stackTag.put("Stack", inventory.getStack(i).writeNbt(new NbtCompound()));
            tag.add(stackTag);
        }

        return tag;
    }

    public static void fromTag(NbtList tag, SimpleInventory inventory) {
        inventory.clear();

        tag.forEach(element -> {
            NbtCompound stackTag = (NbtCompound) element;
            int slot = stackTag.getInt("Slot");
            ItemStack stack = ItemStack.fromNbt(stackTag.getCompound("Stack"));
            inventory.setStack(slot, stack);
        });
    }

    private static boolean has_in_tag(JsonObject obj, String tag) {
        return obj.get("Stack").getAsJsonObject().has("tag") && obj.get("Stack").getAsJsonObject().get("tag").getAsJsonObject().has(tag);
    }

    private static NbtCompound createInventoryItem(byte slot, JsonObject obj, Identifier identifier) {
        NbtCompound obj_nbt = new NbtCompound();
        obj_nbt.putByte("Slot", slot);
        NbtCompound stack = new NbtCompound();
        stack.putString("id", identifier.toString());
        stack.putByte("Count", (byte)1);
        NbtCompound obj_tag = new NbtCompound();
        if (has_in_tag(obj, "CustomModelData")) {
            obj_tag.putInt("CustomModelData", obj.get("Stack").getAsJsonObject().getAsJsonObject().get("tag").getAsJsonObject().get("CustomModelData").getAsInt());
        }
        if (has_in_tag(obj, "v")) {
            obj_tag.putBoolean("v", obj.get("Stack").getAsJsonObject().getAsJsonObject().get("tag").getAsJsonObject().get("v").getAsBoolean());
        }

        stack.put("tag", obj_tag);

        obj_nbt.put("Stack", stack);
        return obj_nbt;
    }

    private static boolean hasItem(JsonObject obj) {
        if (obj == null) return false;
        return !(obj.get("Stack") == null || obj.get("Stack").getAsJsonObject().get("id") == null || obj.get("Stack").getAsJsonObject().get("id").getAsString().compareTo("minecraft:air") == 0);
    }

    public static NbtList fromPlayerData(playerData pdata) {
        NbtList inventory = new NbtList();
        
        JsonObject charm = pdata.getCharm();
        if (hasItem(charm)){
            inventory.add(createInventoryItem((byte)0, charm, Main.CHARM_IDENTIFIER));
        }

        JsonObject casing = pdata.getCasing();
        if (hasItem(casing)) {
            inventory.add(createInventoryItem((byte)1, casing, Main.CASING_IDENTIFIER));
        }

        JsonArray upgrades = pdata.getUpgrades();
        for (byte i = 2; i < upgrades.size()+2; i++) {
            if (!upgrades.get(i-2).isJsonObject()) {continue;}
            JsonObject upgrade = upgrades.get(i-2).getAsJsonObject();
            if (hasItem(upgrade)) {
                inventory.add(createInventoryItem(i, upgrade, Main.UPGRADE_IDENTIFIER));
            }
        }

        return inventory;
    }
}
