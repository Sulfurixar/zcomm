package zener.zcomm.gui.zcomm_main;

import java.util.ArrayList;
import java.util.List;

import blue.endless.jankson.annotation.Nullable;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import zener.zcomm.Main;
import zener.zcomm.chat.ChatHistory;

public class TabButton extends WButton {

    protected int color = WLabel.DEFAULT_TEXT_COLOR;
	protected int darkmodeColor = WLabel.DEFAULT_TEXT_COLOR;
	private boolean enabled = true;
	protected HorizontalAlignment alignment = HorizontalAlignment.CENTER;
    int box_width = 30;
	
    private List<int[]> boxes;

	@Nullable private Runnable onClick;
	@Nullable private Icon icon = null;
    
    private InputField nrfield;
    private int comm_nr;

    public TabButton(int comm_nr, InputField nrfield) {
        this.nrfield = nrfield;
        this.comm_nr = comm_nr;
    }

    @Environment(EnvType.CLIENT)
	@Override
	public InputResult onClick(int x, int y, int button) {
		super.onClick(x, y, button);
		if (enabled && isWithinBounds(x, y)) {
            for (int i = 0; i < boxes.size(); i++) {
                int[] box = boxes.get(i);
                if (box[0] <= x && x <= box[0]+box_width && box[1] <= y && y <= box[1]+height) {
                    ChatHistory.getInstance().setLast_channel(comm_nr, box[2]);
                    if (box[2] == Main.GLOBAL_CHANNEL_NR)
                        nrfield.setSuggestion(new LiteralText(" G"));
                    else {
                        nrfield.setSuggestion(new LiteralText(String.format("%03d", box[2])));
                    }
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    break;
                }
            }

			if (onClick!=null) onClick.run();
		}
        return InputResult.PROCESSED;
	}

    @Override
	public void setSize(int x, int y) {
		super.setSize(x, 16);
	}

    @Override
    public boolean canFocus() {
		return false;
	}

    private void draw_tab(MatrixStack matrices, int x, int y, int mouseX, int mouseY, Text label, boolean selected) {
        int halfWidth = getWidth()/2;
		if (halfWidth>198) halfWidth=198;
		
		int offset = 0;
        if (!selected)
            offset = 3;
        ScreenDrawing.coloredRect(matrices, x-1, y-1, box_width+2, height-1, 0xFF_FFFFA0);
		ScreenDrawing.coloredRect(matrices, x, y, box_width, height-offset, 0xFF000000);

		if (icon != null) {
			icon.paint(matrices, x + 1, y + 1, 16);
		}
		
		if (label!=null) {
			int color = 0xE0E0E0;
			if (!enabled) {
				color = 0xA0A0A0;
			} /*else if (hovered) {
				color = 0xFFFFA0;
			}*/

			int xOffset = (icon != null && alignment == HorizontalAlignment.LEFT) ? 18 : 0;
			ScreenDrawing.drawStringWithShadow(matrices, label.asOrderedText(), alignment, x + xOffset, y + ((height - 8) / 2), box_width, color); //LibGuiClient.config.darkMode ? darkmodeColor : color);
		}
    }

    @Environment(EnvType.CLIENT)
	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		
        boxes = new ArrayList<>();

        draw_tab(matrices, x, y, mouseX, mouseY, new LiteralText("G"), ChatHistory.getInstance().getLast_channel(comm_nr) == Main.GLOBAL_CHANNEL_NR);
		boxes.add(new int[] {0, 0, 71});

        int[] channels = ChatHistory.getInstance().getLast_channels(comm_nr);
        for (int i = 0; i < channels.length; i++) {
            if (channels[i] < 0)
                continue;
            draw_tab(matrices, x+box_width*(i+1), y, mouseX, mouseY, new LiteralText(String.format("%03d", channels[i])), ChatHistory.getInstance().getLast_channel(comm_nr) == channels[i]);
            boxes.add(new int[] {0+box_width*(i+1)-1, 0, channels[i]});
        }

	}
}
