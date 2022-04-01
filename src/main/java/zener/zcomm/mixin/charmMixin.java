package zener.zcomm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import zener.zcomm.Main;
import zener.zcomm.data.charmData;

@Environment(EnvType.CLIENT)
@Mixin(value = ItemRenderer.class, priority = 1050)
public class charmMixin {

    private MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    public void renderItem(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (stack.getItem().equals(Main.ZCOMM) && stack.getHolder() != null && stack.getHolder().getType() != null && stack.getHolder().getType().toString().equals("entity.minecraft.player")) {
            PlayerEntity player = (PlayerEntity)stack.getHolder();
            if (client.player.equals(player)) {
                charmData.getInstance().pInfo.setAll(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
            }
        }   
    }
    
}
