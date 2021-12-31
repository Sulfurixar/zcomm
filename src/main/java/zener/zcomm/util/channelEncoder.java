package zener.zcomm.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import zener.zcomm.Main;
import zener.zcomm.chat.ListenerRunnable.Type;

public class channelEncoder {

    public class Encoding {

        private final String codec;
        @Getter private final BitSet bits;

        public String getCodec() {
            return compress(codec);
        }

        private final Map<Integer, Integer> int2vig = new HashMap<Integer, Integer>() {{
            put((int)'0', 0);
            put((int)'1', 1);
            put((int)'2', 2);
            put((int)'3', 3);
            put((int)'4', 4);
            put((int)'5', 5);
            put((int)'6', 6);
            put((int)'7', 7);
            put((int)'8', 8);
            put((int)'9', 9);
            put((int)'a', 10);
            put((int)'b', 11);
            put((int)'c', 12);
            put((int)'d', 13);
            put((int)'e', 14);
            put((int)'f', 15);
            put((int)'g', 16);
            put((int)'h', 17);
            put((int)'i', 18);
            put((int)'j', 19);
            put((int)'k', 20);
            put((int)'l', 21);
            put((int)'m', 22);
            put((int)'n', 23);
            put((int)'o', 24);
            put((int)'p', 25);
            put((int)'q', 26);
            put((int)'r', 27);
            put((int)'s', 28);
            put((int)'t', 29);
            put((int)'u', 30);
            put((int)'v', 31);
        }};

        private final Map<Integer, Integer> vig2int = new HashMap<Integer, Integer>() {{
            put(0, (int)'0');
            put(1, (int)'1');
            put(2, (int)'2');
            put(3, (int)'3');
            put(4, (int)'4');
            put(5, (int)'5');
            put(6, (int)'6');
            put(7, (int)'7');
            put(8, (int)'8');
            put(9, (int)'9');
            put(10, (int)'a');
            put(11, (int)'b');
            put(12, (int)'c');
            put(13, (int)'d');
            put(14, (int)'e');
            put(15, (int)'f');
            put(16, (int)'g');
            put(17, (int)'h');
            put(18, (int)'i');
            put(19, (int)'j');
            put(20, (int)'k');
            put(21, (int)'l');
            put(22, (int)'m');
            put(23, (int)'n');
            put(24, (int)'o');
            put(25, (int)'p');
            put(26, (int)'q');
            put(27, (int)'r');
            put(28, (int)'s');
            put(29, (int)'t');
            put(30, (int)'u');
            put(31, (int)'v');
        }};

        private Encoding(String codec) {
            this.codec = depress(codec);
            this.bits = generateBitField(this.codec);
        }

        private Encoding(int[] listenValues) {
            this.bits = generateBitField(listenValues);
            this.codec = generateCodec(this.bits);
        }

        private Encoding(BitSet bitfield) {
            this.bits = bitfield;
            this.codec = generateCodec(bitfield);
        }

        private String generateCodec(BitSet bitfield) {
            String codec = "";

            for (int i = 0, pos = 0, buff = 0; i < 1000; i++) {
                int b =  bitfield.get(i) ? 1 : 0;

                buff += b << (4-pos);

                pos += 1;
                if (pos % 5 == 0) {
                    codec += (char) vig2int.get(buff).intValue();
                    buff = 0;
                    pos = 0;
                }
            }
            codec = codec.replaceAll("0+$", "");
            return codec;
        }

        private BitSet generateBitField(int[] listenValues) {
            BitSet bitfield = new BitSet(1000);

            for (int i : listenValues) {
                if (i >= 0 && i < 1000) {
                    bitfield.set(i);
                }
            }

            return bitfield;
        }

        private BitSet generateBitField(String codec) {
            BitSet bitfield = new BitSet(1000);
            int pos = 0;
            for (int c : codec.toLowerCase().chars().toArray()) {
                if (pos > 999) break;
                if (!int2vig.containsKey(c)) {
                    pos += 5; continue;
                }
                c  = int2vig.get(c);
                bitfield.set(pos, (c&16)>0);
                bitfield.set(pos+1, (c&8)>0);
                bitfield.set(pos+2, (c&4)>0);
                bitfield.set(pos+3, (c&2)>0);
                bitfield.set(pos+4, (c&1)>0);
                pos += 5;
            }
            return bitfield;
        }

        private String compress(String codec) {

            String compress = codec;
            Pattern p = Pattern.compile("(.)\\1{4,}");
            Matcher m = p.matcher(codec);
            while (m.find()) {
                int l = Math.abs(m.start() - m.end());
                compress = compress.replaceFirst(m.group(), m.group().toCharArray()[0] + "{" + Integer.toString(l) + "}");
            }

            return compress;
        }

        private String depress(String codec) {
            String depress = codec;

            Pattern p = Pattern.compile(".\\{\\d+\\}");
            Pattern p2 = Pattern.compile("(?<=\\{)\\d+");
            Matcher m = p.matcher(codec);
            while (m.find()) {
                char c = m.group().toCharArray()[0];
                Matcher m2 = p2.matcher(m.group());
                m2.find();
                int l = Integer.parseInt(m2.group());
                char[] r = new char[l];
                Arrays.fill(r, c);
                String sr = new String(r);
                depress = depress.replace(m.group(), sr);
            }

            return depress;
        }

        public boolean check_channel(int ch) {
            return bits.get(ch);
        }

    }

    public boolean can_listen(PlayerInventory playerInventory, ItemStack item, int recipient, Type type) {

        NbtCompound tag = item.getOrCreateNbt();
        if (!tag.contains("Inventory") || tag.get("Inventory").getType() != NbtType.LIST) {
            return false;
        }

        NbtList inventory = tag.getList("Inventory", NbtType.COMPOUND);
        for (int i = 0; i < inventory.size(); i++) {

            NbtElement cmp = inventory.get(i);
            
            if (cmp.getType() != NbtType.COMPOUND) continue;

            NbtCompound c = (NbtCompound)cmp;
            if (!c.contains("Slot") || !c.contains("Stack") || c.get("Stack").getType() != NbtType.COMPOUND) continue;

            NbtCompound s = c.getCompound("Stack");
            if (!s.contains("Count") || !s.contains("id") || !s.contains("tag")) continue;

            if (s.get("id").getType() != NbtType.STRING) continue;

            if (!s.getString("id").equals(Main.UPGRADE_IDENTIFIER.toString())) continue;
            if (s.get("Count").getType() != NbtType.BYTE || s.getByte("Count") != 1) continue;

            if (s.get("tag").getType() != NbtType.COMPOUND) continue;
            
            NbtCompound t = s.getCompound("tag");
            if (!t.contains("CustomModelData") || !t.contains("v") || !t.contains("l") || !t.contains("Damage")) continue;
            if (t.get("CustomModelData").getType() != NbtType.INT || t.get("v").getType() != NbtType.BYTE || t.get("l").getType() != NbtType.STRING || t.get("Damage").getType() != NbtType.INT) continue;
            if (t.getInt("CustomModelData") != 1 || t.getByte("v") != 1) continue;

            boolean unbreakable = (t.contains("Unbreakable") && t.get("Unbreakable").getType() == NbtType.BYTE && t.getByte("Unbreakable") > 0);

            if (getEncoding(t.getString("l")).check_channel(recipient)) {
                if (unbreakable || type == Type.CHATTER) return true;


                int dmg = t.getInt("Damage") + 1;
                if (dmg >= Main.UPGRADE_MAXIMUM_DAMAGE || dmg < 0) {
                    NbtCompound new_slot = new NbtCompound();
                    new_slot.putByte("Slot", c.getByte("Slot"));
                    NbtCompound new_stack = new NbtCompound();
                    new_stack.putByte("Count", (byte)1);
                    new_stack.putString("id", "minecraft:air");
                    new_slot.put("Stack", new_stack);
                    inventory.set(i, new_slot);
                } else {
                    t.putInt("Damage", dmg);
                    s.put("tag", t);
                    c.put("Stack", s);
                    inventory.set(i, c);
                }
                tag.put("Inventory", inventory);
                item.setNbt(tag);
                //playerInventory.markDirty();
                return true;
            }
        }

        return false;
    }

    public Encoding getEncoding(String codec) {
        Encoding encoding = new Encoding(codec);

        return encoding;
    }

    public Encoding getEncoding(int[] listenValues) {
        Encoding encoding = new Encoding(listenValues);

        return encoding;
    }

    public Encoding getEncoding(BitSet bitfield) {
        Encoding encoding = new Encoding(bitfield);

        return encoding;
    }
    
}
