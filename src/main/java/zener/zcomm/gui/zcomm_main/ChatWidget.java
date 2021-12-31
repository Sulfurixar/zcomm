package zener.zcomm.gui.zcomm_main;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import zener.zcomm.Main;
import zener.zcomm.chat.ChatHistory;
import zener.zcomm.util.nrCheck;

public class ChatWidget extends WWidget {

    private final MinecraftClient client;
    private final int comm_nr;
    private int scroll = 0;
    private int text_padding = 2;
    private int padding_left = 4;
    private int padding_right = 2;

    public ChatWidget(int comm_nr) {
        client = MinecraftClient.getInstance();
        this.comm_nr = comm_nr;
    }

    @Override
	public InputResult onMouseScroll(int x, int y, double amount) {
        scroll += amount;
        if (scroll < 0) 
            scroll = 0;
        return InputResult.PROCESSED;
	}
    
    @Override
    public boolean canResize() {
        return true;
    }


    @Environment(EnvType.CLIENT)
    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        ScreenDrawing.coloredRect(matrices, x-1, y-1, width+2, height+2, 0xFF_FFFFA0);
		ScreenDrawing.coloredRect(matrices, x, y, width, height, 0xFF000000);

        ChatHistory hist = ChatHistory.getInstance();
        int max_text = height / (client.textRenderer.fontHeight+3);
        int offset = Integer.max(0, hist.getSize(comm_nr, hist.getLast_channel(comm_nr))-1-max_text);
        if (scroll > hist.getSize(comm_nr, hist.getLast_channel(comm_nr)) - 1 - max_text)
            scroll = Integer.max(0, hist.getSize(comm_nr, hist.getLast_channel(comm_nr))-1-max_text);
        int c = max_text;
        for (int i = hist.getSize(comm_nr, hist.getLast_channel(comm_nr)) - 1; i >= offset; i--) {
            Text text = hist.getMessages(comm_nr, hist.getLast_channel(comm_nr)).get(i-scroll);
            String message = hist.getMessage(text);
            String sender = hist.getSender(text);
            String recipient = hist.getReceiver(text);
            String as = hist.getAs(text);
            nrCheck sender_nr = new nrCheck(sender);
            nrCheck recipient_nr = new nrCheck(recipient);
            nrCheck as_nr = new nrCheck(as);
            if (sender_nr.getNr() != comm_nr && recipient_nr.getNr() != comm_nr) {
                if (sender_nr.getNr() != Main.GLOBAL_CHANNEL_NR && recipient_nr.getNr() != Main.GLOBAL_CHANNEL_NR)
                    continue;
            }
            String sent_to = "";
            if (hist.getLast_channel(comm_nr) == Main.GLOBAL_CHANNEL_NR) {
                if (recipient_nr.getNr() != Main.GLOBAL_CHANNEL_NR) {
                    sent_to = "->"+recipient_nr.getNrStr();
                } else {
                    sent_to = "->G";
                }
            }
            String new_message;
            if (as == null || !as_nr.isValid()) {
                new_message = String.format("[%s%s]: %s", sender_nr.getNrStr(), sent_to, message);
            } else {
                if (!sent_to.equals("")){
                    new_message = String.format("[%s%s](as %s): %s", sender_nr.getNrStr(), sent_to, as, message);
                } else {
                    new_message = String.format("[%s->%s]: %s", sender_nr.getNrStr(), as, message);
                }
            }
            int length = new_message.length();
            if (sender_nr.getNr() == comm_nr) {
                new_message = "§6" + new_message;
            }

            if (length > width - padding_left - padding_right) {
                DrawableHelper.drawTextWithShadow(
                    matrices, client.textRenderer, new LiteralText("Too Long."), x+padding_left, y+(client.textRenderer.fontHeight+text_padding)*(c)+text_padding, 0xFFFFFFFF);
                c--;
            } else {
                DrawableHelper.drawTextWithShadow(
                    matrices, client.textRenderer, new LiteralText(new_message), x+padding_left, y+(client.textRenderer.fontHeight+text_padding)*(c)+text_padding, 0xFFFFFFFF);
                c--;
            }
            if (c < 0) {
                break;
            }
        }
    }

}
