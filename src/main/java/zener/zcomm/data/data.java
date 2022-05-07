package zener.zcomm.data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import zener.zcomm.Main;
import zener.zcomm.components.CommRegistryComponent;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.components.TechnicianRegistryComponent;

@Deprecated
public class data {
    
    public File Data;
    public File TData;
    private final MinecraftServer server;


    /*  Structure of commData
    *   Data {                                              // commData
    *      "UUID": {                                        // playerData
    *           "USER_UUID": STR,   // UUID
    *           "NR": STR,          // "000" to "999"
    *           "CHARM": STR,       // ITEM_ID
    *           "CASING": STR,      // ITEM_ID
    *           "UPGRADES": STR[]   // ITEM_ID []
    *       }
    *   }
    */
    public Map<String, playerData> commData = new HashMap<>();

    /*  Structure of configData
    *   Data {                                              // configData
    *      
    *   }
    */
    public Map<String, configData> configData = new HashMap<>();

    /*  Structure of techData
    *   Data {
    *       UUIDSTRING: tData 
    *   }
    */
    public Map<String, tData> techData = new HashMap<>();

    public data(MinecraftServer server) {
        Path dataPath = server.getSavePath(WorldSavePath.ROOT);
        this.server = server;
        
        File dataDir = dataPath.resolve(Main.ID).toFile();
        try {
            if (!dataDir.exists())
                dataDir.mkdirs();
            this.Data = new File(dataDir, Main.ID+"_data.json");
            this.TData = new File(dataDir, "tdata.dat");
            if (!this.Data.exists()) {
                this.Data.createNewFile();
                this.save();
            }
            if (!this.TData.exists()) {
                this.TData.createNewFile();
                this.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            FileReader reader = new FileReader(this.Data);
            JsonObject obj = dataHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            reader = new FileReader(this.TData);
            JsonObject obj2 = dataHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            
            if (obj != null) {
                JsonObject commdata = obj.get("comm_data").getAsJsonObject();
                commdata.entrySet().iterator().forEachRemaining(entry -> {
                    JsonObject playerdata = entry.getValue().getAsJsonObject();
                    List<String> upgrade = new ArrayList<String>();
                    playerdata.getAsJsonArray("upgrades").forEach(upgrades -> {
                        upgrade.add(upgrades.getAsString());
                    });
                    
                    playerData playerData = new playerData(
                        playerdata.get("id").getAsString(), 
                        playerdata.get("nr").getAsString(), 
                        playerdata.get("charm").getAsString(), 
                        playerdata.get("casing").getAsString(),
                        upgrade.toArray(new String[6])
                    );
                    commData.put(entry.getKey(), playerData);

                    CommRegistryComponent comms = ComponentHandler.COMM_REGISTRY.get(server.getOverworld());
                    if (comms.getComm(entry.getKey()) == null) {
                        comms.addEntry(entry.getKey(), playerData);
                    }
                });
            }
            
            if (obj2 != null) {
            JsonObject tdata = obj2.get("tdata").getAsJsonObject();

                tdata.entrySet().iterator().forEachRemaining(entry -> {
                    JsonObject _tdata = entry.getValue().getAsJsonObject();
                    List<Byte> a = new ArrayList<>();
                    List<Byte> b = new ArrayList<>();
                    _tdata.getAsJsonArray("a").forEach(ba -> {
                        a.add(ba.getAsByte());
                    });
                    _tdata.getAsJsonArray("b").forEach(bb -> {
                        b.add(bb.getAsByte());
                    });
                    tData newTData = new tData(ArrayUtils.toPrimitive(a.toArray(new Byte[a.size()])), ArrayUtils.toPrimitive(b.toArray(new Byte[b.size()])));
                    techData.put(entry.getKey(), newTData);

                    TechnicianRegistryComponent technicians = ComponentHandler.TECHNICIAN_REGISTRY.get(server.getOverworld());
                    if (technicians.getTechnician(newTData.UUID) == null) {
                        technicians.addEntry(newTData.UUID, newTData.isTechnician, newTData.isHeadTechnician);
                    }
                });
            }
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // set to do nothing, to avoid new file creation.
    public void save() {
        /*JsonObject obj = new JsonObject();
        obj.addProperty("__comment", "TODO");
        JsonObject obj2 = new JsonObject();
        obj2.addProperty("__comment", "TODO");
        JsonObject commData = new JsonObject();
        JsonObject tdata = new JsonObject();

        this.commData.forEach((uuid, _playerdata) -> {
            JsonObject playerdata = new JsonObject();
            playerdata.addProperty("casing", _playerdata.CASING);
            playerdata.addProperty("charm", _playerdata.CHARM);
            playerdata.addProperty("id", _playerdata.USER_ID);
            playerdata.addProperty("nr", _playerdata.COMM_NR);
            JsonArray upgrades = new JsonArray();
            for (String upgrade : _playerdata.UPGRADES) {
                upgrades.add(upgrade);
            }
            playerdata.add("upgrades", upgrades);
            commData.add(uuid, playerdata);
        });
        obj.add("comm_data", commData);

        
        this.techData.forEach((uuid, _tdata) -> {
            JsonObject __tdata = new JsonObject();
            JsonArray a = new JsonArray();
            JsonArray b = new JsonArray();
            List<Byte[]> c = _tdata.getBytes();
            for (Byte ba : c.get(0)) {
                a.add(ba);
            }
            for (Byte bb : c.get(1)) {
                b.add(bb);
            }
            __tdata.add("a", a);
            __tdata.add("b", b);
            tdata.add(uuid, __tdata);
        });
        obj2.add("tdata", tdata);
        

        try {
            FileWriter writer = new FileWriter(this.Data);
            dataHandler.GSON.toJson(obj, writer);
            writer.close();

            writer = new FileWriter(this.TData);
            dataHandler.GSON.toJson(obj2, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    public static <V, K> Map<V, K> createHashMap(Consumer<Map<V, K>> cons) {
        Map<V, K> map = new HashMap<>();
        cons.accept(map);
        return map;
    }

    public static <V, K> Map<V, K> createLinkedHashMap(Consumer<Map<V, K>> cons) {
        Map<V, K> map = new LinkedHashMap<>();
        cons.accept(map);
        return map;
    }
}
