package zener.zcomm.gui.zcomm_nr;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class zcommNRGUI extends CottonInventoryScreen<zcommNRGUIDescription> {
    
    public zcommNRGUI(zcommNRGUIDescription gui, PlayerInventory inventory, Text title) {
        
        super(gui, inventory.player, title);

    }

    

}
