package zener.zcomm.data;

import java.util.HashMap;

import lombok.Data;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import zener.zcomm.entities.charmProjectile;

public class charmData {
    
    private static final charmData INSTANCE = new charmData();

    @Data public class playerInfo {
        ItemStack stack;
        ModelTransformation.Mode renderMode;
        boolean leftHanded;
        MatrixStack matrices;
        VertexConsumerProvider vertexConsumers;
        int light;
        int overlay;
        BakedModel model;

        public void setAll(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
            this.stack = stack;
            this.renderMode = renderMode;
            this.leftHanded = leftHanded;
            this.matrices = matrices;
            this.vertexConsumers = vertexConsumers;
            this.light = light;
            this.overlay = overlay;
            this.model = model;
        }
    }

    public static charmData getInstance() {
        return INSTANCE;
    }

    public playerInfo pInfo = new playerInfo();

    private HashMap<String, charmProjectile> activeCharms = new HashMap<>();

    public HashMap<String, charmProjectile> getActiveCharms() {
        return activeCharms;
    }

    public void setCharm(String uuid, charmProjectile charm) {
        activeCharms.put(uuid, charm);
    }

    public void removeCharm(String uuid) {
        activeCharms.remove(uuid);
    }
}
