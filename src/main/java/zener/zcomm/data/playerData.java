package zener.zcomm.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import zener.zcomm.util.nrCheck;

@Deprecated
public class playerData {
    
    public final String USER_ID;
    public final String COMM_NR;
    public final String CHARM;
    public final String CASING;
    public String[] UPGRADES;


    public playerData(String user_id, String comm_nr, String charm, String casing, String[] upgrades) {
        this.USER_ID = user_id;
        this.COMM_NR = comm_nr;
        this.CHARM = charm;
        this.CASING = casing;
        this.UPGRADES = upgrades;
    }

    public playerData(ItemStack zcommItemStack, String user_id) {
        NbtCompound tag = zcommItemStack.getOrCreateNbt();
        this.USER_ID = user_id;
        this.COMM_NR = new nrCheck(tag.getInt("NR")).getNrStr();
        NbtList invtag = zcommItemStack.getOrCreateNbt().getList("Inventory", NbtType.COMPOUND);

        //CHARM
        NbtCompound charmTag = (NbtCompound) invtag.getCompound(0);
        this.CHARM = charmTag.asString();
        //CASING
        NbtCompound casingTag = (NbtCompound) invtag.getCompound(1);
        this.CASING = casingTag.asString();
        this.UPGRADES = new String[] { "", "", "", "", "", ""};
    }

    public JsonObject getCharm() {
        JsonObject charm = dataHandler.GSON.fromJson(this.CHARM, JsonObject.class);
        return charm;
    }

    public JsonObject getCasing() {
        JsonObject casing = dataHandler.GSON.fromJson(this.CASING, JsonObject.class);
        return casing;
    }

    public JsonArray getUpgrades() {
        JsonArray upgrades = new JsonArray();
        for (String upgrade : this.UPGRADES) {
            upgrades.add(dataHandler.GSON.fromJson(upgrade, JsonObject.class));
        }
        return upgrades;
    }
    
}
