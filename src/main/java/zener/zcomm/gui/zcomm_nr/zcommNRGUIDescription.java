package zener.zcomm.gui.zcomm_nr;

import java.util.UUID;


import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.networking.NetworkSide;
import io.github.cottonmc.cotton.gui.networking.ScreenNetworking;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import zener.zcomm.Main;
import zener.zcomm.data.dataHandler;
import zener.zcomm.data.playerData;
import zener.zcomm.gui.zcomm_inventory.ZItem;
import zener.zcomm.items.zcomm.comm;
import zener.zcomm.util.nrCheck;

public class zcommNRGUIDescription extends SyncedGuiDescription {


    public zcommNRGUIDescription(int synchronizationID, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
        this(synchronizationID, playerInventory, packetByteBuf.readItemStack());
    }

    public zcommNRGUIDescription(int syncId, PlayerInventory playerInventory, ItemStack zcommItemStack) {
        super(Main.ZCOMM_NR_SCREEN_TYPE, syncId, playerInventory);

        NbtCompound tag = zcommItemStack.getOrCreateNbt();  
        if (!tag.contains("UUID")) {
            UUID uuid = UUID.randomUUID();
            // check for database in case of UUID conflicts
            tag.put("UUID", NbtString.of(uuid.toString()));
            zcommItemStack.setNbt(tag);
        }

        WGridPanel root = new WGridPanel();
        root.setInsets(new Insets(10, 10, 10, 10));
        setRootPanel(root);

        if (zcommItemStack.getItem() instanceof comm) {
            setupDisplay(playerInventory, zcommItemStack, root);
        } else {
            this.closeScreen(playerInventory);
        }

    }

    private void setupDisplay(PlayerInventory playerInventory, ItemStack zcommItemStack, WGridPanel root) {

        ZItem commDisplay = new ZItem(zcommItemStack);
        commDisplay.setSize(72, 72);
        root.add(commDisplay, 1, 1, 3, 3);

        WTextField NR_Field = new WTextField(new LiteralText("Comm number"));
        NR_Field.setSize(36, 18);
        NR_Field.setMaxLength(3);
        root.add(NR_Field, 0, 4, 5, 1);

        WButton confirm = new WButton(new LiteralText("Set number"));
        NRConfirm nrConfirm = new NRConfirm(this, playerInventory, zcommItemStack, NR_Field);
            
        confirm.setOnClick(nrConfirm);
        root.add(confirm, 0, 6, 5, 1);


        root.validate(this);

        ScreenNetworking.of(this, NetworkSide.SERVER).receive(new Identifier("zcomm", "set_nr"), buf -> {
            int nr = buf.readInt();

            nrCheck nrcheck = new nrCheck(nr);

            if (nrcheck.nrTaken()) {
                ScreenNetworking.of(this, NetworkSide.SERVER).send(new Identifier("zcomm", "nr_taken"), buf2 -> {});
            } else {
                zcommItemStack.getOrCreateNbt().put("NR", NbtInt.of(nr));
                zcommItemStack.getOrCreateNbt().put("Owner", NbtString.of(playerInventory.player.getName().asString()));
                // try to get playerData for this comm
                String uuid = zcommItemStack.getNbt().getString("UUID");
                try {
                    playerData playerData = dataHandler.data.commData.get(uuid);
                    if (playerData == null) {
                        dataHandler.addEntry(uuid, new playerData(playerInventory.player.getUuidAsString(), String.format("%03d", nr), "", "", new String[] { "", "", "", "", "", ""}));
                    } else {
                        playerData newPlayerData = new playerData(playerInventory.player.getUuidAsString(), String.format("%03d", nr), playerData.CHARM, playerData.CASING, playerData.UPGRADES);
                        dataHandler.updateEntry(uuid, newPlayerData);
                    }
                } catch(NullPointerException e) {
                    System.out.println(e);
                }
                
                ScreenNetworking.of(this, NetworkSide.SERVER).send(new Identifier("zcomm", "available"), buf2 -> {});
            }
        });

        ScreenNetworking.of(this, NetworkSide.CLIENT).receive(new Identifier("zcomm", "nr_taken"), buf -> {
            NR_Field.setText("");
            NR_Field.setSuggestion("NR Taken");
        });

        ScreenNetworking.of(this, NetworkSide.CLIENT).receive(new Identifier("zcomm", "available"), buf -> {
            NR_Field.setText("");
            NR_Field.setSuggestion("NR Set");
            playerInventory.markDirty();
        });

    }

    public void closeScreen(PlayerInventory playerInventory) {
        this.close(playerInventory.player);
    }

}
