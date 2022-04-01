package zener.zcomm.items.zcomm;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import zener.zcomm.Main;
import zener.zcomm.data.dataHandler;

public class craftingComponent extends Item {
    
    public craftingComponent (Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        // check if the item has a generated ID
        ItemStack zcommItemStack = user.getStackInHand(hand);
        NbtCompound tag = zcommItemStack.getOrCreateNbt();

        //check item verification
        if (!tag.contains("v") || tag.getBoolean("v") == false) {
            user.sendMessage(new TranslatableText(Main.identifier+".item_not_verified"), true);
            return super.use(world, user, hand);
        }

        return super.use(world, user, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (!entity.isPlayer() || world.isClient()) {
            return;
        }

        NbtCompound _tag = stack.getOrCreateNbt();
        if (_tag.contains("CustomModelData")) {
            if (!_tag.contains("v") || _tag.getBoolean("v") == false) {
                if (!dataHandler.checkTEntry(((ServerPlayerEntity)entity).getUuidAsString())) {
                    ((ServerPlayerEntity)entity).getInventory().removeStack(slot);
                    ((ServerPlayerEntity)entity).getInventory().markDirty();
                }
                return;
            }
        }
        
    }

}
