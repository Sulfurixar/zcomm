package zener.zcomm.gui.zcomm_main;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class MainGUI extends CottonInventoryScreen<MainGUIDescription> {
    
    public MainGUI(MainGUIDescription gui, PlayerInventory inventory, Text title) {
        
        super(gui, inventory.player, title);

    }

    

}
