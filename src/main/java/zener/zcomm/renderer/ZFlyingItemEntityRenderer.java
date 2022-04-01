package zener.zcomm.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import zener.zcomm.entities.charmProjectile;

public class ZFlyingItemEntityRenderer extends FlyingItemEntityRenderer<charmProjectile> {
    
    public ZFlyingItemEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }
}
