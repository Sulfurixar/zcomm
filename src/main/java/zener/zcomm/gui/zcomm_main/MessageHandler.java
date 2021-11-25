package zener.zcomm.gui.zcomm_main;

import io.github.cottonmc.cotton.gui.networking.NetworkSide;
import io.github.cottonmc.cotton.gui.networking.ScreenNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import zener.zcomm.Main;
import zener.zcomm.util.nrCheck;

public class MessageHandler implements Runnable {

    public InputField textField;
    public int nr;
    public InputField nrField;
    public MainGUIDescription zcommMainGUIDescription;
    public PlayerEntity playerEntity;

    public MessageHandler(InputField textField, int nr, InputField nrField, PlayerEntity playerEntity, MainGUIDescription zcommMainGUIDescription) {
        this.textField = textField;
        this.nr = nr;
        this.nrField = nrField;
        this.zcommMainGUIDescription = zcommMainGUIDescription;
        this.playerEntity = playerEntity;
    }

    public void run() {

        ScreenNetworking.of(zcommMainGUIDescription, NetworkSide.CLIENT).send(new Identifier("zcomm", "zcomm_message"), buf -> {
            String message = "";
            nrCheck nrCheck = new nrCheck(((String) nrField.getSuggestion().asString()).trim());
            if (nrField.getSuggestion() == null || !nrCheck.isValid()) {
                return;            
            }

            message += String.format("%s=%d,TO=%d;%s", Main.ZCOMM_COMMUNICATION_IDENTIFIER, this.nr, nrCheck.getNr(), textField.getText());
            textField.setText("");
            buf.writeString(message);
            buf.writeUuid(playerEntity.getUuid());
        });
        
    }
    
}
