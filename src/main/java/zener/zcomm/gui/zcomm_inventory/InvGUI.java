package zener.zcomm.gui.zcomm_inventory;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class InvGUI extends CottonInventoryScreen<InvGUIDescription> {
    
    public InvGUI(InvGUIDescription gui, PlayerInventory inventory, Text title) {
        
        super(gui, inventory.player, title);

    }

    

}
