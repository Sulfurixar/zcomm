package zener.zcomm.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import zener.zcomm.Main;
import zener.zcomm.chat.ListenerRunnable.Type;
import zener.zcomm.util.channelEncoder;
import zener.zcomm.util.nrCheck;

public class ListenerCheck {

    private MinecraftClient mc = MinecraftClient.getInstance();
    private ChatHistory ch = ChatHistory.getInstance();
    private final Type type;

    public ListenerCheck(Type type) {
        this.type = type;
    }

    private boolean executor(PlayerInventory playerInventory, ItemStack stack, channelEncoder chnEnc, nrCheck receiver, nrCheck sender, Text text) {
        NbtCompound tag = stack.getOrCreateNbt();
        if (tag.contains("NR")) {
            int nr = tag.getInt("NR");
            nrCheck nrcheck = new nrCheck(nr);
            if (nrcheck.isValid()) {
                if (chnEnc.can_listen(playerInventory, stack, receiver.getNr(), type) && receiver.getNr() != nrcheck.getNr() && sender.getNr() != nrcheck.getNr()) {
                    ListenerRunnable runnable = new ListenerRunnable(sender, nrcheck, receiver, text, this.type);
                    runnable.run();
                    return true;
                } else if (type == Type.CHATTER && (sender.getNr() == nrcheck.getNr() || receiver.getNr() == nrcheck.getNr())) {
                    ListenerRunnable runnable = new ListenerRunnable(sender, nrcheck, receiver, text, Type.CHATTER2);
                    runnable.run();
                    return true;
                }
            }
        }
        return false;
    }

    public void listen(Text text) {
        String recipient = ch.getReceiver(text);
        if (recipient == null) return;

        nrCheck receiver = new nrCheck(recipient);
        if (!receiver.isValid()) return;

        nrCheck sender = new nrCheck(ch.getSender(text));
        channelEncoder chnEnc = new channelEncoder();

        // check hotbar slots for comms
        int[] commslots = new int[9];
        int c = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (PlayerInventory.isValidHotbarIndex(i) && Item.getRawId(mc.player.getInventory().getStack(i).getItem()) == Item.getRawId(Main.ZCOMM)) {
                commslots[c] = i;
                c++;
            }
        }

        // check offhand
        ItemStack offhandStack = mc.player.getOffHandStack();
        ItemStack mainhandStack = mc.player.getMainHandStack();
        boolean isOffHand = Item.getRawId(offhandStack.getItem()) == Item.getRawId(Main.ZCOMM);
        boolean isMainHand = Item.getRawId(mainhandStack.getItem()) == Item.getRawId(Main.ZCOMM);

        // found a comm
        if (isOffHand || isMainHand || c != 0) {
            if (isOffHand) {
                if (executor(mc.player.getInventory(), offhandStack, chnEnc, receiver, sender, text)) return;
            }

            if (isMainHand) {
                if (executor(mc.player.getInventory(), mainhandStack, chnEnc, receiver, sender, text)) return;
            }

            if (c > 0 && c < 9) {
                for (int i = 0; i < c; i++) {
                    ItemStack hotbarStack = mc.player.getInventory().getStack(commslots[i]);
                    if (executor(mc.player.getInventory(), hotbarStack, chnEnc, receiver, sender, text)) return;
                }
            }

            if (type == Type.CHATTER) {
                // if we have a comm in inventory, and message is global and in general inventory
                if (receiver.getNr() == Main.GLOBAL_CHANNEL_NR) {
                    mc.inGameHud.getChatHud().addMessage(new LiteralText(String.format("§7[%s]: %s", new nrCheck(ch.getSender(text)).getNrStr(), ch.getMessage(text))));
                    return;
                }
            }
        }

    }
    
}
