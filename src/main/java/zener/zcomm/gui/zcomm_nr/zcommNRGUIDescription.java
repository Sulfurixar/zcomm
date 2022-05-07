package zener.zcomm.gui.zcomm_nr;

import java.util.UUID;


import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.networking.NetworkSide;
import io.github.cottonmc.cotton.gui.networking.ScreenNetworking;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import zener.zcomm.Main;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.components.ICommRegistryComponent.Comm;
import zener.zcomm.gui.zcomm_inventory.ZItem;
import zener.zcomm.items.zcomm.comm;
import zener.zcomm.util.nrCheck;

public class zcommNRGUIDescription extends SyncedGuiDescription {


    public zcommNRGUIDescription(int synchronizationID, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
        this(synchronizationID, playerInventory, packetByteBuf.readItemStack());
    }

    public zcommNRGUIDescription(int syncId, PlayerInventory playerInventory, ItemStack zcommItemStack) {
        super(Main.ZCOMM_NR_SCREEN_TYPE, syncId, playerInventory);
        
        WGridPanel root = new WGridPanel();
        root.setInsets(new Insets(10, 10, 10, 10));
        setRootPanel(root);

        if (zcommItemStack.getItem() instanceof comm) {
            setupDisplay(playerInventory, zcommItemStack, root);
        } else {
            this.closeScreen(playerInventory);
        }

    }

    @SuppressWarnings("deprecation")
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

        ScreenNetworking.of(this, NetworkSide.SERVER).receive(new Identifier(Main.ID, "set_nr"), buf -> {
            ServerPlayerEntity player = (ServerPlayerEntity)playerInventory.player;
            int nr = buf.readInt();

            nrCheck nrcheck = new nrCheck(nr);

            if (nrcheck.nrTaken(player.getServer())) {
                ScreenNetworking.of(this, NetworkSide.SERVER).send(new Identifier(Main.ID, "nr_taken"), buf2 -> {});
            } else {
                NbtCompound tag = zcommItemStack.getOrCreateNbt();
                tag.put("NR", NbtInt.of(nr));
                tag.put("Owner", NbtString.of(player.getName().asString()));
                // try to get playerData for this comm
                if (!tag.contains("UUID")) {
                    UUID uuid = ComponentHandler.createUUID(player);
                    tag.putUuid("UUID", uuid);
                    ComponentHandler.COMM_REGISTRY.get(player.getServer().getOverworld()).addEntry(uuid, new Comm(player.getUuid(), nr, zcommItemStack));
                } else {
                    if (tag.get("UUID").getType() == NbtType.STRING) {
                        String uuid = tag.getString("UUID");
                        ComponentHandler.updateCommEntry(player, uuid, zcommItemStack);
                    } else {
                        UUID uuid = tag.getUuid("UUID");
                        ComponentHandler.updateCommEntry(player, uuid, zcommItemStack);   
                    }
                }
                
                ScreenNetworking.of(this, NetworkSide.SERVER).send(new Identifier(Main.ID, "available"), buf2 -> {});
            }
        });

        ScreenNetworking.of(this, NetworkSide.CLIENT).receive(new Identifier(Main.ID, "nr_taken"), buf -> {
            NR_Field.setText("");
            NR_Field.releaseFocus();
            NR_Field.setSuggestion("NR Taken");
        });

        ScreenNetworking.of(this, NetworkSide.CLIENT).receive(new Identifier(Main.ID, "available"), buf -> {
            NR_Field.setText("");
            NR_Field.releaseFocus();
            NR_Field.setSuggestion("NR Set");
            playerInventory.markDirty();
        });

    }

    public void closeScreen(PlayerInventory playerInventory) {
        this.close(playerInventory.player);
    }

}
