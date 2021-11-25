package zener.zcomm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zener.zcomm.Main;
import zener.zcomm.gui.zcomm_inventory.ZItem;

@Environment(EnvType.CLIENT)
@Mixin(value = ItemRenderer.class, priority = 1050)
public class ZItemRendererMixin {

    @Inject(method = "renderGuiItemModel(Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At(value="INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", shift = At.Shift.AFTER, ordinal=1))
    private void ifinject(ItemStack stack, int x, int y, BakedModel model, CallbackInfo ci) {
        if (Item.getRawId(stack.getItem()) == Item.getRawId(Main.ZCOMM) && stack.getDamage() == 19) {
            ZItem.renderSizeModifier();
        }
    }
    
}
