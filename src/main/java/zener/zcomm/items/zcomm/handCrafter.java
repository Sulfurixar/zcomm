package zener.zcomm.items.zcomm;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import zener.zcomm.Main;
import zener.zcomm.util.handCraft;

public class handCrafter extends Item  {
    
    public handCrafter (Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if (!world.isClient()) {

            if (user.getStackInHand(Hand.OFF_HAND).getItem() == Main.HANDCRAFTER) {
                handCraft.craft(world, user);
            }

        }

        return super.use(world, user, hand);
    }

}
