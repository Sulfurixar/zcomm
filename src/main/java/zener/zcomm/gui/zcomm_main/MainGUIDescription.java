package zener.zcomm.gui.zcomm_main;

import java.util.UUID;


import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.networking.NetworkSide;
import io.github.cottonmc.cotton.gui.networking.ScreenNetworking;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WText;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import zener.zcomm.Main;
import zener.zcomm.chat.ChatHistory;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.items.zcomm.comm;
import zener.zcomm.util.nrCheck;

public class MainGUIDescription extends SyncedGuiDescription {


    public MainGUIDescription(int synchronizationID, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
        this(synchronizationID, playerInventory, packetByteBuf.readItemStack());
    }

    public MainGUIDescription(int syncId, PlayerInventory playerInventory, ItemStack zcommItemStack) {
        super(Main.ZCOMM_MAIN_SCREEN_TYPE, syncId, playerInventory);

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

    @SuppressWarnings("deprecation")
    private void setupDisplay(PlayerInventory playerInventory, ItemStack zcommItemStack, WGridPanel root) {


        NbtCompound tag = zcommItemStack.getOrCreateNbt();
        PlayerEntity player = playerInventory.player;
        String name = player.getName().asString();
        int nr = zcommItemStack.getOrCreateNbt().getInt("NR");

        if (tag.get("UUID").getType() == NbtType.STRING) {
            ScreenNetworking.of(this, NetworkSide.CLIENT).send(new Identifier(Main.ID, Main.ID+"_check_owner_deprecated"), buf -> {
                buf.writeString(tag.getString("UUID"));
                buf.writeString(player.getUuidAsString());
                buf.writeString(name);
            });
        } else {
            ScreenNetworking.of(this, NetworkSide.CLIENT).send(new Identifier(Main.ID, Main.ID+"_check_owner"), buf -> {
                buf.writeUuid(tag.getUuid("UUID"));
                buf.writeUuid(player.getUuid());
                buf.writeString(name);
            });
        }

        ScreenNetworking.of(this, NetworkSide.SERVER).receive(new Identifier(Main.ID, Main.ID+"_check_owner_deprecated"), buf -> {
            String uuid = buf.readString();
            String owner_id = buf.readString();
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            boolean isOwner = ComponentHandler.COMM_REGISTRY.get(serverPlayer.getServer().getOverworld()).isOwner(uuid, owner_id);
            
            if (isOwner) {
                String _name = buf.readString();
                if (tag.getString("Owner").compareTo(_name) != 0) {
                    tag.put("Owner", NbtString.of(_name));
                    playerInventory.markDirty();
                }
            }
        });

        ScreenNetworking.of(this, NetworkSide.SERVER).receive(new Identifier(Main.ID, Main.ID+"_check_owner"), buf -> {
            UUID uuid = buf.readUuid();
            UUID owner_id = buf.readUuid();
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            boolean isOwner = ComponentHandler.COMM_REGISTRY.get(serverPlayer.getServer().getOverworld()).isOwner(uuid, owner_id);
            
            if (isOwner) {
                String _name = buf.readString();
                if (tag.getString("Owner").compareTo(_name) != 0) {
                    tag.put("Owner", NbtString.of(_name));
                    playerInventory.markDirty();
                }
            }
        });

        ScreenNetworking.of(this, NetworkSide.SERVER).receive(new Identifier(Main.ID, Main.ID+"_message"), buf -> {
            playerInventory.player.getServer().getPlayerManager().broadcastChatMessage(new LiteralText(buf.readString()), MessageType.CHAT, buf.readUuid());
        });

        if (player.world.isClient()) {

            String _owner = tag.getString("Owner");

            WText owner = new WText(new LiteralText("Owner: " + _owner));
            owner.setVerticalAlignment(VerticalAlignment.TOP);
            owner.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            root.add(owner, 7, 0, 15, 1);

            ChatWidget chat = new ChatWidget(nr);
            chat.setSize(10, 10);
            root.add(chat, 0, 2, 22, 9);

            InputField nrfield = new InputField();
            nrfield.setSize(54, 54);
            nrfield.setMaxLength(3);
            if (ChatHistory.getInstance().getLast_channel(nr) == Main.GLOBAL_CHANNEL_NR) {
                nrfield.setSuggestion(" G");
            } else {
                nrfield.setSuggestion(new LiteralText(String.format("%s", new nrCheck(ChatHistory.getInstance().getLast_channel(nr)).getNrStr())));
            }
            nrfield.setEnterEvent(new NRConfirm(this, nr, nrfield));
            root.add(nrfield, 0, 11, 2, 1);

            InputField chatfield = new InputField();
            chatfield.setSize(54, 54);
            chatfield.setMaxLength(57);
            
            chatfield.setEnterEvent(new MessageHandler(chatfield, nr, nrfield, player, this));
            root.add(chatfield, 2, 11, 20, 1);

            TabButton chn_tab = new TabButton(nr, nrfield);
            chn_tab.setSize(186, 16);
            root.add(chn_tab, 0, 1, 20, 1);


            root.validate(this);

        }

    }

    public void closeScreen(PlayerInventory playerInventory) {
        this.close(playerInventory.player);
    }

}
