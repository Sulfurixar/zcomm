package zener.zcomm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import zener.zcomm.chat.ChatHistory;

@Environment(EnvType.CLIENT)
@Mixin(value = ChatHud.class, priority = 1050)
public class zcommsMixin {
    

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;I)V", at = @At("HEAD"), cancellable = true)
    private void addMessage(Text text, int id, CallbackInfo ci) {
        if (ChatHistory.getInstance().handleText(text)) {
            ci.cancel();
            ChatHistory.getInstance().resendMessage(text);
        }
    }
}
