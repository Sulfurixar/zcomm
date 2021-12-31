package zener.zcomm.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import zener.zcomm.util.nrCheck;

public class ListenerRunnable implements Runnable {

    private MinecraftClient client = MinecraftClient.getInstance();
    private ChatHistory ch = ChatHistory.getInstance();

    public static enum Type {
        LOGGER,
        CHATTER,
        CHATTER2
    }

    private class logger extends chatter {

        private Text get_new_text(Text text, nrCheck nrcheck, nrCheck receiver) {
            String[] split = text.asString().split(";");
            String[] flags = split[0].split("=");
            flags[flags.length-1] = nrcheck.getNrStr() + ",AS=" + receiver.getNrStr();
            split[0] = String.join("=", flags);
            return new LiteralText(String.join(";", split));
        }

        @Override
        public void run(nrCheck sender, nrCheck nrcheck, nrCheck receiver, Text text) {
            Text new_text = get_new_text(text, nrcheck, receiver);
            ch.insertMessage(sender.getNr(), nrcheck.getNr(), new_text);
            ch.messagesAdd(new_text);

        }
    }

    private class chatter{
        public void run(nrCheck sender, nrCheck nrcheck, nrCheck receiver, Text text) {
            client.inGameHud.getChatHud().addMessage(new LiteralText(String.format("§7[%s->%s](as %s): %s", sender.getNrStr(), nrcheck.getNrStr(), receiver.getNrStr(), ch.getMessage(text))));
        }
    }

    private class chatter2 extends chatter{
        @Override
        public void run(nrCheck sender, nrCheck nrcheck, nrCheck receiver, Text text) {
            client.inGameHud.getChatHud().addMessage(new LiteralText(String.format("§7[%s->%s]: %s", sender.getNrStr(), receiver.getNrStr(), ch.getMessage(text))));
        }
    }

    private final nrCheck sender;
    private final nrCheck nrcheck;
    private final nrCheck receiver;
    private final Text text;
    private final Type type;
    private final Object[] types = new Object[] {
        new logger(),
        new chatter(),
        new chatter2()
    };

    public ListenerRunnable(nrCheck sender, nrCheck nrcheck, nrCheck receiver, Text text, Type type) {
        this.sender = sender;
        this.nrcheck = nrcheck;
        this.receiver = receiver;
        this.text = text;
        this.type = type;
    }

    @Override
    public void run() {
        ((chatter) types[type.ordinal()]).run(sender, nrcheck, receiver, text);
    }

}
