package zener.zcomm.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import zener.zcomm.Main;
import zener.zcomm.util.nrCheck;

@Environment(EnvType.SERVER)
@Mixin(value = MinecraftServer.class, priority = 1050)
public class mcserverMixin {

    @SuppressWarnings("unused")
    private boolean validText(Text text) {
        String _text = text.asString();
        if (!_text.startsWith(Main.ZCOMM_COMMUNICATION_IDENTIFIER)) 
            return false;
        
        //register message
        Integer recipient = new nrCheck(getReceiver(text)).getNr();
        Integer sender = new nrCheck(getSender(text)).getNr();
        if (recipient == null || sender == null) return false;
        return true;
    }

    private String flagGetter(Text text, String flag) {
        String data = text.asString().split(";")[0];
        String[] datas = data.split(",");
        Map<String, String> args = new HashMap<>();
        for (String s : datas) {
            String[] separated = s.split("=");
            args.put(separated[0], separated[1]);
        }
        if (args.containsKey(flag)) {
            return args.get(flag);
        }
        return null;
    }

    private String getSender(Text text) {
        return flagGetter(text, Main.ZCOMM_COMMUNICATION_IDENTIFIER);
    }

    private String getReceiver(Text text) {
        return flagGetter(text, "TO");
    }

    // We'll be using this when we actually need to hide messages from server console
    @Inject(method = "sendSystemMessage(Lnet/minecraft/text/Text;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    public void sendSystemMessage(Text message, UUID sender, CallbackInfo ci) {
        /*if (validText(message)) {
            ci.cancel();
        }*/
    }

}
