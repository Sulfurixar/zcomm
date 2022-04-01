package zener.zcomm.items.zcomm;

import java.util.HashMap;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
import zener.zcomm.data.charmData;
import zener.zcomm.data.dataHandler;
import zener.zcomm.entities.charmProjectile;
import zener.zcomm.gui.zcomm_inventory.InvGUIDescription;
import zener.zcomm.gui.zcomm_main.MainGUIDescription;
import zener.zcomm.gui.zcomm_nr.zcommNRGUIDescription;

public class comm extends Item {

    public comm(Settings settings) {
        super(settings);
    }

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
                String uuid = tag.getString("UUID");
                // ILLEGAL
                if (uuid != null && !uuid.isEmpty() && dataHandler.data.commData.get(uuid) == null) {
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
            user.sendMessage(new TranslatableText(Main.identifier+".item_not_verified"), true);
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
                ((PlayerEntity)entity).sendMessage(new TranslatableText(Main.identifier+".item_not_verified"), true);
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
                                ((PlayerEntity)entity).sendMessage(new TranslatableText(Main.identifier+".item_not_verified"), true);
                            }
                        }
                    }
                }
            }

            HashMap<String, charmProjectile> charms = charmData.getInstance().getActiveCharms();
            String uuid = stack.getNbt().getString("UUID");
            if ((charms.get(uuid) != null && !charms.get(uuid).isAlive()) || (((PlayerEntity)entity).getMainHandStack().equals(stack) || ((PlayerEntity)entity).getOffHandStack().equals(stack))) {
                if (!charms.keySet().contains(uuid)) {
                    if (charmStack != null) {
                        charmProjectile charm = new charmProjectile(world, (LivingEntity) entity);
                        charm.setItem(charmStack);
                        charm.setProperties(entity, 0.0F, 0.0F);
                        charmData.getInstance().setCharm(uuid, charm);
                        world.spawnEntity(charm);
                    }
                } else {
                    charmProjectile charm = charms.get(uuid);
                    if (charm != null && charm.isAlive()) {
                        double dist = 0.3;
                        double rad = (((PlayerEntity)entity).bodyYaw + 90) % 360 * Math.PI / 180;
                        double rotatedX = ((PlayerEntity)entity).getMainHandStack().equals(stack) ? dist * Math.cos(rad) - 0*dist * Math.sin(rad) : (dist * Math.cos(rad) + 0*dist * Math.sin(rad));
                        double rotatedZ = ((PlayerEntity)entity).getMainHandStack().equals(stack) ? 0*dist * Math.cos(rad) + dist * Math.sin(rad) : (0*dist * Math.cos(rad) - dist * Math.sin(rad));
                        double posx = entity.getX() + rotatedX;
                        double posy = entity.getY() + 0.6D * entity.getHeight() / 2;
                        double posz = entity.getZ() + rotatedZ;
                        double x = posx - charm.getX();
                        double y = posy - charm.getY();
                        double z = posz - charm.getZ();
                        float speed = (float)Math.pow(Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z))), 2);
                        if (speed < 0.005 && (charm.getX() != posx || charm.getY() != posy || charm.getZ() != posz)) {
                            speed = 0.005f;
                        }
                        charm.setVelocity(x, y, z, speed, 0.01F);
                    }
                }
            } else {
                if (charms.keySet().contains(uuid)) {
                    charmProjectile charm = charms.get(uuid);
                    if (charm != null && charm.isAlive()) {
                        charm.kill();
                    }
                    charmData.getInstance().removeCharm(uuid);
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
