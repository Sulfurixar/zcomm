package zener.zcomm.items.zcomm;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import zener.zcomm.Main;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.entities.CharmProjectile;
import zener.zcomm.gui.zcomm_inventory.InvGUIDescription;
import zener.zcomm.gui.zcomm_main.MainGUIDescription;
import zener.zcomm.gui.zcomm_nr.zcommNRGUIDescription;

public class comm extends Item {

    public comm(Settings settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        // check if the item has a generated ID
        ItemStack zcommItemStack = user.getStackInHand(hand);
        NbtCompound tag = zcommItemStack.getOrCreateNbt();

        
        // DO A CLEAN-UP IN CASE NO UUID
        if (!tag.contains("UUID")) {
            if (!world.isClient()) {
                if (tag.contains("Owner")) {
                    tag.putString("dOwner", tag.getString("Owner"));
                    tag.remove("Owner");
                }
                if (tag.contains("NR")) {
                    tag.putString("dNR", tag.getString("NR"));
                    tag.remove("NR");
                }
                zcommItemStack.setNbt(tag);
                user.getInventory().markDirty();
            }
        } else {
            // CHECK IF IT'S A VALID UUID
            if (!world.isClient()) {
                boolean illegal;
                if (tag.get("UUID").getType() == NbtType.STRING) {
                    String uuid = tag.getString("UUID");
                    // ILLEGAL
                    illegal = uuid == null || uuid.isEmpty() || ComponentHandler.getComm((ServerPlayerEntity)user, uuid) == null;
                } else {
                    UUID uuid = tag.getUuid("UUID");
                    illegal = uuid == null || ComponentHandler.getComm((ServerPlayerEntity)user, uuid) == null;
                }

                if (illegal) {
                    if (tag.contains("Owner")) {
                        tag.putString("dOwner", tag.getString("Owner"));
                        tag.remove("Owner");
                    }
                    if (tag.contains("v")) {
                        tag.remove("v");
                    }
                    if (tag.contains("NR")) {
                        tag.putString("dNR", tag.getString("NR"));
                        tag.remove("NR");
                    }
                    zcommItemStack.setNbt(tag);
                    user.getInventory().markDirty();
                }
                
            }
        }

        //check item verification
        if (!tag.contains("v") || tag.getBoolean("v") == false) {
            user.sendMessage(new TranslatableText(Main.ID+".item_not_verified"), true);
            return super.use(world, user, hand);
        }

        // open interface
        
        if (user.isSneaking()) {
            openInvScreen(user, zcommItemStack);
        } else {
            if (!tag.contains("NR")) {
                openNRScreen(user, zcommItemStack);
            } else {
                openMainScreen(user, zcommItemStack);
            }
        }


        return super.use(world, user, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        //check item verification
        if (entity.isPlayer()) {
            NbtCompound _tag = stack.getOrCreateNbt();
            if (!_tag.contains("v") || _tag.getBoolean("v") == false) {
                ((PlayerEntity)entity).sendMessage(new TranslatableText(Main.ID+".item_not_verified"), true);
                return;
            }
        }
        if (
            !world.isClient && 
            stack.getOrCreateNbt().contains("UUID") && 
            entity.getType().toString().equals("entity.minecraft.player")
            ) {
            // get the charm itemStack
            ItemStack charmStack = null;
            if (stack.getNbt().contains("Inventory")) {
                NbtList tag = stack.getOrCreateNbt().getList("Inventory", NbtType.COMPOUND);
                NbtCompound stackTag = (NbtCompound) tag.getCompound(0);
                if (stackTag != null) {
                    charmStack = ItemStack.fromNbt(stackTag.getCompound("Stack"));
                    if (charmStack.getItem() == Main.CHARM) {
                        // check item verification
                        NbtCompound _tag = charmStack.getOrCreateNbt();
                        if (!_tag.contains("v") || _tag.getBoolean("v") == false) {
                            if (entity.isPlayer()) {
                                ((PlayerEntity)entity).sendMessage(new TranslatableText(Main.ID+".item_not_verified"), true);
                            }
                        } else {
                            CharmProjectile charm = ComponentHandler.PLAYER_CHARM_KEY.get((PlayerEntity)entity).updateCharms((PlayerEntity)entity);
                            ComponentHandler.PLAYER_CHARM_KEY.sync((PlayerEntity)entity);
                            if (charm != null) {
                                ComponentHandler.CHARM_KEY.sync(charm);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void openInvScreen(PlayerEntity player, ItemStack zcommItemStack) {
        if (player.world != null && !player.world.isClient) {
            player.openHandledScreen(new ExtendedScreenHandlerFactory(){

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
                    packetByteBuf.writeItemStack(zcommItemStack);
                }

                @Override
                public Text getDisplayName() {
                    return new TranslatableText(zcommItemStack.getItem().getTranslationKey());
                }

                @Override
                public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new InvGUIDescription(syncId, inv, zcommItemStack);
                }
                
            });
        }
    }

    public static void openNRScreen(PlayerEntity player, ItemStack zcommItemStack) {
        if (player.world != null && !player.world.isClient) {
            player.openHandledScreen(new ExtendedScreenHandlerFactory(){

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
                    packetByteBuf.writeItemStack(zcommItemStack);
                }

                @Override
                public Text getDisplayName() {
                    return new TranslatableText(zcommItemStack.getItem().getTranslationKey());
                }

                @Override
                public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new zcommNRGUIDescription(syncId, inv, zcommItemStack);
                }
                
            });
        }
    }

    public static void openMainScreen(PlayerEntity player, ItemStack zcommItemStack) {
        if (player.world != null && !player.world.isClient) {
            player.openHandledScreen(new ExtendedScreenHandlerFactory(){

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
                    packetByteBuf.writeItemStack(zcommItemStack);
                }

                @Override
                public Text getDisplayName() {
                    return new TranslatableText(zcommItemStack.getItem().getTranslationKey());
                }

                @Override
                public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new MainGUIDescription(syncId, inv, zcommItemStack);
                }
                
            });
        }
    }

}
