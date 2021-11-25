package zener.zcomm.gui.zcomm_main;

import org.lwjgl.glfw.GLFW;

import blue.endless.jankson.annotation.Nullable;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class InputField extends WTextField {

    @Nullable private Runnable enterEvent;

	private int select = -1;
	private int cursor = 0;
	private int maxLength = 16;

    public InputField setEnterEvent(@Nullable Runnable enterEvent) {
        this.enterEvent = enterEvent;
        return this;
    }

    public Runnable getEnterEvent() {
        return this.enterEvent;
    }

	@Override
	public void onKeyPressed(int ch, int key, int modifiers) {
		MinecraftClient mc = MinecraftClient.getInstance();
        if (!this.isEditable()) return;

		if (Screen.isCopy(ch)) {
			String selection = getSelection();
			if (selection!=null) {
				mc.keyboard.setClipboard(selection);
			}
			
			return;
		} else if (Screen.isPaste(ch)) {
			if (select!=-1) {
				int a = select;
				int b = cursor;
				if (b<a) {
					int tmp = b;
					b = a;
					a = tmp;
				}
				String before = this.getText().substring(0, a);
				String after = this.getText().substring(b);
				
				String clip = mc.keyboard.getClipboard();
				this.setText(before+clip+after);
				select = -1;
				cursor = (before+clip).length();
			} else {
				String before = this.getText().substring(0, cursor);
				String after = this.getText().substring(cursor, this.getText().length());
				
				String clip = mc.keyboard.getClipboard();
				this.setText(before + clip + after);
				cursor += clip.length();
				if (this.getText().length()>this.maxLength) {
					this.setText(this.getText().substring(0, maxLength));
					if (cursor>this.getText().length()) cursor = this.getText().length();
				}
			}
			return;
		} else if (Screen.isSelectAll(ch)) {
			select = 0;
			cursor = this.getText().length();
			return;
		}
		
		if (modifiers==0) {
			if (ch==GLFW.GLFW_KEY_DELETE || ch==GLFW.GLFW_KEY_BACKSPACE) {
				if (this.getText().length()>0 && cursor>0) {
					if (select>=0 && select!=cursor) {
						int a = select;
						int b = cursor;
						if (b<a) {
							int tmp = b;
							b = a;
							a = tmp;
						}
						String before = this.getText().substring(0, a);
						String after = this.getText().substring(b);
						this.setText(before+after);
						if (cursor==b) cursor = a;
						select = -1;
					} else {
						String before = this.getText().substring(0, cursor);
						String after = this.getText().substring(cursor, this.getText().length());
						
						before = before.substring(0,before.length()-1);
						this.setText(before+after);
						cursor--;
					}
				}
			} else if (ch==GLFW.GLFW_KEY_LEFT) {
				if (select!=-1) {
					cursor = Math.min(cursor, select);
					select = -1; //Clear the selection anchor
				} else {
					if (cursor>0) cursor--;
				}
			} else if (ch==GLFW.GLFW_KEY_RIGHT) {
				if (select!=-1) {
					cursor = Math.max(cursor, select);
					select = -1; //Clear the selection anchor
				} else {
					if (cursor<this.getText().length()) cursor++;
				}
            } else if (ch==GLFW.GLFW_KEY_ENTER) {
                if (select!=-1) {
                    cursor = Math.max(cursor, select);
                    select = -1;
                } else {
                    if (enterEvent!=null) enterEvent.run();
                }
            } else {
				//System.out.println("Ch: "+ch+", Key: "+key);
			}
		} else {
			if (modifiers==GLFW.GLFW_MOD_SHIFT) {
				if (ch==GLFW.GLFW_KEY_LEFT) {
					if (select==-1) select = cursor;
					if (cursor>0) cursor--;
					if (select==cursor) select = -1;
				} else if (ch==GLFW.GLFW_KEY_RIGHT) {
					if (select==-1) select = cursor;
					if (cursor<this.getText().length()) cursor++;
					if (select==cursor) select = -1;
				}
			}
		}
	}
    
}
