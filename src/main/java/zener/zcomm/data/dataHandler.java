package zener.zcomm.data;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import zener.zcomm.util.nrCheck;

public class dataHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static data data;

    public static void serverLoad(MinecraftServer server) {
        data = new data(server);
        reloadData();
    }

    public static void reloadData() {
        data.load();
    }

    public static void addEntry(String uuid, playerData playerData) {
        data.commData.put(uuid, playerData);
        data.save();
        reloadData();
    }

    public static void updateEntry(String uuid, playerData playerData) {
        data.commData.replace(uuid, playerData);
        data.save();
        reloadData();
    }

    public static void addEntry(String uuid, tData tData) {
        data.techData.put(uuid, tData);
        data.save();
        reloadData();
    }

    public static void updateEntry(String uuid, tData tData) {
        data.techData.replace(uuid, tData);
        data.save();
        reloadData();
    }

    public static void removeTEntry(String uuid) {
        data.techData.remove(uuid);
        data.save();
        reloadData();
    }

    public static boolean checkDUserEntry(String uuid) {
        return data.commData.entrySet().stream().filter(x -> x.getValue().USER_ID.compareTo(uuid) == 0).map(x -> x.getValue()).collect(Collectors.toList()).size() > 0;
    }

    public static boolean checkTEntry(String uuid) {
        return data.techData.containsKey(uuid);
    }

    public static Map<String, playerData> get_user_data(String uuid) {
        return data.commData.entrySet().stream().filter(x -> x.getValue().USER_ID.compareTo(uuid) == 0).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    public static boolean check_comm(String nr) {
        return data.commData.entrySet().stream().filter(x -> x.getValue().COMM_NR.compareTo(nr) == 0).map(x -> x.getValue()).collect(Collectors.toList()).size() > 0;
    }

    public static Map<String, playerData> get_comm(String nr) {
        return data.commData.entrySet().stream().filter(x -> x.getValue().COMM_NR.compareTo(nr) == 0).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }
    
    public static boolean checkNR(int nr, data data) {

        if (data == null) return false;

        playerData[] vals = data.commData.values().toArray(new playerData[data.commData.size()]);
        String _nr = new nrCheck(nr).getNrStr();
        for (int i = 0; i < data.commData.size(); i++) {
            if (vals[i].COMM_NR.compareTo(_nr) == 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkOwner(String uuid, String owner_id) {
        return data.commData.get(uuid).USER_ID.compareTo(owner_id) == 0;
    }

    public dataHandler(ServerLoginNetworkHandler networkHandler, MinecraftServer server) {
        data = new data(server);
    }

    public static PacketByteBuf writeNrTransmitter(PacketByteBuf buf) {

        Map<String, playerData> commData = dataHandler.data.commData;
        // How many entries there are in commData
        buf.writeInt(commData.size());
        for (String key : commData.keySet()) {
            buf.writeString(key);
            playerData playerData = commData.get(key);
            buf.writeString(playerData.COMM_NR);
        }

        return buf;
    }

    public static data readNrTransmitter(PacketByteBuf buf) {
        data _data = data;
        if (_data == null) {
            _data = new data();
        
            int buf_size = buf.readInt();
            for (int i = 0; i < buf_size; i++) {
                String UUID = buf.readString();
                playerData playerData = new playerData("", buf.readString(), "", "", new String[] {"", "", "", "", "", ""});
                data.commData.put(UUID, playerData);
            }
        }
        return _data;
    }

}
