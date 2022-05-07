package zener.zcomm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import zener.zcomm.entities.CharmProjectile;
import zener.zcomm.gui.zcomm_inventory.InvGUI;
import zener.zcomm.gui.zcomm_main.MainGUI;
import zener.zcomm.gui.zcomm_nr.zcommNRGUI;

@Environment(EnvType.CLIENT)
public class MainClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(Main.ZCOMM_INV_SCREEN_TYPE, InvGUI::new);
        ScreenRegistry.register(Main.ZCOMM_NR_SCREEN_TYPE, zcommNRGUI::new);
        ScreenRegistry.register(Main.ZCOMM_MAIN_SCREEN_TYPE, MainGUI::new);
        EntityRendererRegistry.register(Main.charmProjectileEntityType, (context) -> new FlyingItemEntityRenderer<CharmProjectile>(context));
    }
}
