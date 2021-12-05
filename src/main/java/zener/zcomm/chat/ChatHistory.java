package zener.zcomm.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import zener.zcomm.Main;
import zener.zcomm.util.nrCheck;

@Environment(EnvType.CLIENT)
public class ChatHistory {
    
    private final static ChatHistory INSTANCE = new ChatHistory();
    private HashMap<Integer, HashMap<Integer, ArrayList<Text>>> comms = new HashMap<>();
    private ArrayList<Text> messages = new ArrayList<>();
    private MinecraftClient client = MinecraftClient.getInstance();
    private HashMap<Integer, int[]> comm_last_channels = new HashMap<>();
    private HashMap<Integer, Integer> comm_last_channel = new HashMap<>();
    public static ChatHistory getInstance() {
        return INSTANCE;
    }

    public int[] getLast_channels(int comm_nr) {
        if (!comm_last_channels.containsKey(comm_nr)){
            comm_last_channels.put(comm_nr, new int[] { -1, -1, -1, -1, -1 });
        }
        return comm_last_channels.get(comm_nr);
    }

    public void add_last_channel(int comm_nr, int channel) {
        if (!comm_last_channels.containsKey(comm_nr)){
            comm_last_channels.put(comm_nr, new int[] { -1, -1, -1, -1, -1 });
        }
        int[] last_channels = comm_last_channels.get(comm_nr);
        boolean found = false;
        for (int i = 0; i < last_channels.length; i++) {
            if (last_channels[i] == channel) {
                for (int j = i-1; j >= 0; j--) {
                    last_channels[j+1] = last_channels[j];
                }
                found = true;
                break;
            }
        }
        if (!found) {
            for (int i = last_channels.length-2; i >= 0; i--) {
                last_channels[i+1] = last_channels[i];
            }
        }
        last_channels[0] = channel;
        comm_last_channels.put(comm_nr, last_channels);
    }

    public int getLast_channel(int comm_nr) {
        if (!comm_last_channel.containsKey(comm_nr)) {
            comm_last_channel.put(comm_nr, Main.GLOBAL_CHANNEL_NR);
        }
        return comm_last_channel.get(comm_nr);
    }

    public void setLast_channel(int comm_nr, int channel_nr) {
        comm_last_channel.put(comm_nr, channel_nr);
    }

    private void insertMessage(int sender, int recipient, Text text){
        if (!comms.containsKey(recipient)){
            comms.put(recipient, new HashMap<Integer, ArrayList<Text>>());
        }
        HashMap<Integer, ArrayList<Text>> recip_comm = comms.get(recipient);
        if (!recip_comm.containsKey(sender)) {
            recip_comm.put(sender, new ArrayList<Text>());
        }
        ArrayList<Text> recip_channel = recip_comm.get(sender);
        recip_channel.add(text);
        if (recip_channel.size() > 1000) {
            recip_channel.remove(0);
        }
    }

    public boolean handleText(Text text) {
        String _text = text.asString();

        if (!_text.startsWith(Main.ZCOMM_COMMUNICATION_IDENTIFIER)) 
            return false;
        
        //register message
        //LogManager.getLogger().info("[ZCOMM] {}", text.getString());
        int recipient = new nrCheck(getReceiver(text)).getNr();
        int sender = new nrCheck(getSender(text)).getNr();
        
        insertMessage(sender, recipient, text);
        insertMessage(recipient, sender, text);
        insertMessage(sender, 71, text);
        insertMessage(recipient, 71, text);

        messages.add(text);

        return true;

    }

    public int getSize() {
        return messages.size();
    }

    public int getSize(int comm_nr, int channel_nr) {
        if (channel_nr == Main.GLOBAL_CHANNEL_NR) {
            return getSize();
        }
        if (comms.get(comm_nr) == null) {
            return 0;
        }
        if (comms.get(comm_nr).get(channel_nr) == null) {
            return 0;
        }
        return comms.get(comm_nr).get(channel_nr).size();
    }

    public ArrayList<Text> getMessages() {
        return messages;
    }

    public ArrayList<Text> getMessages(int comm_nr, int channel_nr) {
        if (channel_nr == Main.GLOBAL_CHANNEL_NR) {
            return getMessages();
        }
        if (comms.get(comm_nr) == null) {
            return new ArrayList<>();
        }
        if (comms.get(comm_nr).get(channel_nr) == null) {
            return new ArrayList<>();
        }
        return comms.get(comm_nr).get(channel_nr);
    }

    public String getMessage(Text text) {
        String[] sides = text.asString().split(";");
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < sides.length; i++) {
            sb.append(sides[i]);
            if (i < sides.length - 1)
                sb.append(";");
        }
        return sb.toString();
    }

    public String getSender(Text text) {
        String data = text.asString().split(";")[0];
        String[] datas = data.split(",");
        Map<String, String> args = new HashMap<>();
        for (String s : datas) {
            String[] separated = s.split("=");
            args.put(separated[0], separated[1]);
        }
        if (args.containsKey(Main.ZCOMM_COMMUNICATION_IDENTIFIER)) {
            return args.get(Main.ZCOMM_COMMUNICATION_IDENTIFIER);
        }
        return null;
    }

    public String getReceiver(Text text) {
        String data = text.asString().split(";")[0];
        String[] datas = data.split(",");
        Map<String, String> args = new HashMap<>();
        for (String s : datas) {
            String[] separated = s.split("=");
            args.put(separated[0], separated[1]);
        }
        if (args.containsKey("TO")) {
            return args.get("TO");
        }
        return null;
    }

    public void resendMessage(Text text) {
        String recipient = getReceiver(text);
        if (recipient == null)
            return;

        // ADD IN check for comms in inventory
        MinecraftClient mc = MinecraftClient.getInstance();
        
        int[] commslots = new int[9];
        int c = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (PlayerInventory.isValidHotbarIndex(i) && Item.getRawId(mc.player.getInventory().getStack(i).getItem()) == Item.getRawId(Main.ZCOMM)) {
                commslots[0] = i;
                c++;
            }
        }

        boolean isOffHand = Item.getRawId(mc.player.getOffHandStack().getItem()) == Item.getRawId(Main.ZCOMM);
        if (isOffHand || c != 0) {
            // if we're here, it means there's a comm in either hand or hotbar
            nrCheck nrcheck = new nrCheck(recipient);
            if (!nrcheck.isValid()) return;
            if (nrcheck.getNr() == Main.GLOBAL_CHANNEL_NR) {
                client.inGameHud.getChatHud().addMessage(new LiteralText(String.format("§7[%s]: %s", new nrCheck(getSender(text)).getNrStr(), getMessage(text))));
                return;
            }
            // handle off-hand item
            if (isOffHand) {
                NbtCompound tag = mc.player.getOffHandStack().getOrCreateNbt();
                if (tag.contains("NR")) {
                    int nr = tag.getInt("NR");
                    nrcheck = new nrCheck(nr);
                    if (nrcheck.isValid()) {
                        client.inGameHud.getChatHud().addMessage(new LiteralText(String.format("§7[%s]: %s", new nrCheck(getSender(text)).getNrStr(), getMessage(text))));
                        return;
                    }
                }
            }
            // handle hotbar items
            if (c > 0) {
                for (int i = 0; i < c; i++) {
                    NbtCompound tag = mc.player.getInventory().getStack(i).getOrCreateNbt();
                    if (tag.contains("NR")) {
                        int nr = tag.getInt("NR");
                        nrcheck = new nrCheck(nr);
                        if (nrcheck.isValid()) {
                            client.inGameHud.getChatHud().addMessage(new LiteralText(String.format("§7[%s->%s]: %s", new nrCheck(getSender(text)).getNrStr(), nrcheck.getNrStr(), getMessage(text))));
                            return;
                        }
                    }
                }
            }
        }
    }
}
