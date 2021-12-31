package zener.zcomm.gui.zcomm_main;

import org.lwjgl.glfw.GLFW;

import blue.endless.jankson.annotation.Nullable;
import io.github.cottonmc.cotton.gui.widget.WTextField;

public class InputField extends WTextField {

    @Nullable private Runnable enterEvent;

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
			super.setCursorPos(0);
			if (enterEvent!=null) enterEvent.run();
		} else {
			super.onKeyPressed(ch, key, modifiers);
		}
	}
    
}
