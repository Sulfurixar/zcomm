package zener.zcomm.gui.zcomm_inventory;

import java.util.function.Predicate;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WPlayerInvPanel;
import io.github.cottonmc.cotton.gui.widget.WText;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;
import zener.zcomm.data.dataHandler;
import zener.zcomm.data.playerData;
import zener.zcomm.items.zcomm.comm;
import zener.zcomm.util.inventoryUtils;

public class InvGUIDescription extends SyncedGuiDescription {
    
    /*
    *   Casing
    *   Charm
    *   Upgrades: 6
    */
    private static final int INVENTORY_SIZE = 8;


    public InvGUIDescription(int synchronizationID, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
        this(synchronizationID, playerInventory, packetByteBuf.readItemStack());
    }

    public InvGUIDescription(int syncId, PlayerInventory playerInventory, ItemStack zcommItemStack) {
        super(Main.ZCOMM_INV_SCREEN_TYPE, syncId, playerInventory);

        WGridPanel root = new WGridPanel();
        root.setInsets(new Insets(10, 10, 10, 10));
        setRootPanel(root);
        if (zcommItemStack.getItem() instanceof comm) {
            setupContainer(playerInventory, zcommItemStack, root);
        } else {
            PlayerEntity player = playerInventory.player;
            this.close(player);
        }

    }

    private void setupContainer(PlayerInventory playerInventory, ItemStack zcommItemStack, WGridPanel root) {

        NbtList tag = zcommItemStack.getOrCreateNbt().getList("Inventory", NbtType.COMPOUND);
        SimpleInventory inventory = new SimpleInventory(INVENTORY_SIZE) {
            @Override
            public void markDirty() {
                zcommItemStack.getOrCreateNbt().put("Inventory", inventoryUtils.toTag(this));
                NbtCompound coverTag = this.getStack(1).getOrCreateNbt();
                if (coverTag.contains("CustomModelData")) {
                    zcommItemStack.getOrCreateNbt().putInt("CustomModelData", coverTag.getInt("CustomModelData"));
                } else if (zcommItemStack.getOrCreateNbt().contains("CustomModelData")){
                    zcommItemStack.getOrCreateNbt().remove("CustomModelData");
                }
                try {
                    String uuid = zcommItemStack.getNbt().getString("UUID");
                    playerData playerData = dataHandler.data.commData.get(uuid);
                    if (playerData == null) {
                        int nr = zcommItemStack.getOrCreateNbt().getInt("NR");
                        dataHandler.addEntry(uuid, new playerData(playerInventory.player.getUuidAsString(), String.format("%03d", nr), "", "", new String[] { "", "", "", "", "", ""}));
                    } else {
                        playerData newPlayerData = new playerData(zcommItemStack, playerData.USER_ID);
                        dataHandler.updateEntry(uuid, newPlayerData);
                    }
                } catch(NullPointerException e) {
                    System.out.println(e);
                }
                super.markDirty();
            }
        };

        inventoryUtils.fromTag(tag, inventory);

        WText charmText = new WText(new TranslatableText("gui.zcomm.charmslot"));
        charmText.setVerticalAlignment(VerticalAlignment.BOTTOM);
        charmText.setHorizontalAlignment(HorizontalAlignment.CENTER);
        root.add(charmText, 0, 1, 3, 1);

        WItemSlot charmSlot = new WItemSlot(inventory, 0, 1, 1, false);
        Predicate<ItemStack> charmPredicate = c -> Item.getRawId(c.getItem()) == Item.getRawId(Main.CHARM);
        charmSlot.setFilter(charmPredicate);
        
        root.add(charmSlot, 1, 2);

        WText casingText = new WText(new TranslatableText("gui.zcomm.casingslot"));
        casingText.setVerticalAlignment(VerticalAlignment.BOTTOM);
        casingText.setHorizontalAlignment(HorizontalAlignment.CENTER);
        root.add(casingText, 0, 3, 3, 1);

        WItemSlot casingSlot = new WItemSlot(inventory, 1, 1, 1, false);
        Predicate<ItemStack> casingPredicate = c -> Item.getRawId(c.getItem()) == Item.getRawId(Main.CASING);
        casingSlot.setFilter(casingPredicate);

        root.add(casingSlot, 1, 4);

        WText upgradeText = new WText(new TranslatableText("gui.zcomm.upgradeslot"));
        upgradeText.setVerticalAlignment(VerticalAlignment.BOTTOM);
        upgradeText.setHorizontalAlignment(HorizontalAlignment.CENTER);
        root.add(upgradeText, 5, 1, 4, 1);

        WItemSlot upgradeSlots = new WItemSlot(inventory, 2, 2, 3, false);
        Predicate<ItemStack> upgradePredicate = c -> Item.getRawId(c.getItem()) == Item.getRawId(Main.UPGRADE);
        upgradeSlots.setFilter(upgradePredicate);

        root.add(upgradeSlots, 6, 2);

        ZItem commDisplay = new ZItem(zcommItemStack);
        commDisplay.setSize(3, 3);
        root.add(commDisplay, 3, 3, 3, 3);

        WPlayerInvPanel playerInvPanel = new WPlayerInvPanel(playerInventory);
        root.add(playerInvPanel, 0, 6);

        root.validate(this);

    }
}