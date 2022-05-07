package zener.zcomm.gui.zcomm_nr;

import io.github.cottonmc.cotton.gui.networking.NetworkSide;
import io.github.cottonmc.cotton.gui.networking.ScreenNetworking;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import zener.zcomm.Main;
import zener.zcomm.util.nrCheck;

public class NRConfirm implements Runnable {

    public PlayerInventory playerInventory;
    public ItemStack zcommItemStack;
    public WTextField NR_Field;
    public zcommNRGUIDescription zcommNRGUIDescription;

    public NRConfirm(zcommNRGUIDescription zcommNRGUIDescription, PlayerInventory playerInventory, ItemStack zcommItemStack, WTextField NR_Field) {
        this.playerInventory = playerInventory;
        this.zcommItemStack = zcommItemStack;
        this.NR_Field = NR_Field;
        this.zcommNRGUIDescription = zcommNRGUIDescription;
    }

    public void run() {

        String text = NR_Field.getText();
        nrCheck nrcheck = new nrCheck(text);
        if (!nrcheck.isNr()) {
            NR_Field.setSuggestion("Not a number");
            NR_Field.setText("");
            return;
        }

        if (nrcheck.isLengthLong()) {
            NR_Field.setSuggestion("Too long");
            NR_Field.setText("");
            return;
        }

        if (nrcheck.isLengthShort()) {
            NR_Field.setSuggestion("Too short");
            NR_Field.setText("");
            return;
        }


        if (nrcheck.isNegative()) {
            NR_Field.setSuggestion("Negative");
            NR_Field.setText("");
            return;
        }

        ScreenNetworking.of(zcommNRGUIDescription, NetworkSide.CLIENT).send(new Identifier(Main.ID, "set_nr"), buf -> buf.writeInt(nrcheck.getNr()));
    }
    
}
