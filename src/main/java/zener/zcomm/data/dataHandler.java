package zener.zcomm.data;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;

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
    
    public static boolean checkNR(int nr, data data) {

        if (data == null) return false;

        playerData[] vals = data.commData.values().toArray(new playerData[data.commData.size()]);
        String _nr = String.format("%03d", nr);
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

        System.out.println("writeNrTransmitter");

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
        System.out.println("readNrTransmitter");
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
