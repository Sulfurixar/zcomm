package zener.zcomm.gui.zcomm_inventory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.cottonmc.cotton.gui.widget.WItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;

public class ZItem extends WItem {

    private List<ItemStack> items;
    private int current = 0;
    private int ticks = 0;
    private int duration = 25;

    private static float x = 2f;
    private static float y = 2f;
    private static float z = 2f;

    public ZItem(List<ItemStack> items) {
        super(items);
		setItems(items);
	}

	public ZItem(Tag<? extends ItemConvertible> tag) {
        super(tag);
	}

	public ZItem(ItemStack stack) {
		this(Collections.singletonList(stack));
	}

    public static void renderSizeModifier() {
		MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.scale(x, y, z);
    }

    @Environment(EnvType.CLIENT)
	@Override
	public void tick() {
		if (ticks++ >= duration) {
			ticks = 0;
			current = (current + 1) % items.size();
		}
	}

    @Override
    public WItem setItems(List<ItemStack> items) {
		Objects.requireNonNull(items, "stacks == null!");
		if (items.isEmpty()) throw new IllegalArgumentException("The stack list is empty!");

		this.items = items;

		// Reset the state
		current = 0;
		ticks = 0;

		return this;
	}

    @Environment(EnvType.CLIENT)
	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		RenderSystem.enableDepthTest();

        ItemStack copiedItemStack = items.get(current).copy();
        copiedItemStack.setDamage(19);

		MinecraftClient mc = MinecraftClient.getInstance();
        ItemRenderer renderer = mc.getItemRenderer();
		renderer.zOffset = 100f;
		renderer.renderInGui(copiedItemStack, x + getWidth() / 2 - 9, y + getHeight() / 2 - 9);
		renderer.zOffset = 0f;
	}
    
}
