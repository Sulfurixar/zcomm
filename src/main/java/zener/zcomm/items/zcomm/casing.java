package zener.zcomm.items.zcomm;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import zener.zcomm.Main;

public class casing extends Item {
    
    public casing (Settings settings) {
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
    
}
