package zener.zcomm.gui.zcomm_main;

import org.lwjgl.glfw.GLFW;

import blue.endless.jankson.annotation.Nullable;
import io.github.cottonmc.cotton.gui.widget.WTextField;

public class InputField extends WTextField {

    @Nullable private Runnable enterEvent;

	private int select = -1;
	private int cursor = 0;

    public InputField setEnterEvent(@Nullable Runnable enterEvent) {
        this.enterEvent = enterEvent;
        return this;
    }

    public Runnable getEnterEvent() {
        return this.enterEvent;
    }

	
	@Override
	public void onKeyPressed(int ch, int key, int modifiers) {

		if (ch==GLFW.GLFW_KEY_ENTER) {
			if (select!=-1) {
				cursor = Math.max(cursor, select);
				select = -1;
			} else {
				if (enterEvent!=null) enterEvent.run();
			}
		} else {
			super.onKeyPressed(ch, key, modifiers);
		}
	}
    
}
