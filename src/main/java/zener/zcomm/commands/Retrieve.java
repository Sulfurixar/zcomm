package zener.zcomm.commands;

import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;
import zener.zcomm.data.dataHandler;
import zener.zcomm.data.playerData;
import zener.zcomm.util.inventoryUtils;
import zener.zcomm.util.nrCheck;

public class Retrieve {
    
    public static int retrieve(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity technician = source.getPlayer();
        String technician_uuid = technician.getUuidAsString();

        if (!dataHandler.checkTEntry(technician_uuid)) {
            source.sendFeedback(new TranslatableText("command."+Main.identifier+".retrieve.permissions_too_low"), false);
            return Command.SINGLE_SUCCESS;
        }

        source.sendFeedback(new TranslatableText("command."+Main.identifier+".retrieve"), false);

        return Command.SINGLE_SUCCESS;

    }

        public static int retrieveArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            ServerCommandSource source = context.getSource();
            ServerPlayerEntity technician = source.getPlayer();
            String technician_uuid = technician.getUuidAsString();
            Integer nr = IntegerArgumentType.getInteger(context, "nr");
            nrCheck nrcheck = new nrCheck(nr);

            if (!dataHandler.checkTEntry(technician_uuid)) {
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".retrieve.permissions_too_low"), false);
                return Command.SINGLE_SUCCESS;
            }

            if (!nrcheck.isValid()) {
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".retrieve.invalid_nr"), false);
                return Command.SINGLE_SUCCESS;
            }

            if (!dataHandler.check_comm(nrcheck.getNrStr())) {
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".retrieve.no_comm"), false);
                return Command.SINGLE_SUCCESS;
            }


            Map<String, playerData> comm_data = dataHandler.get_comm(nrcheck.getNrStr());
            if (comm_data.size() == 0) {
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".retrieve.no_comm"), false);
                return Command.SINGLE_SUCCESS;
            }
            playerData pdata = (playerData)comm_data.values().toArray()[0];
            String comm_id = (String)comm_data.keySet().toArray()[0];

            ItemStack zcommItemStack = new ItemStack(Main.ZCOMM);
            NbtCompound tag = new NbtCompound();
            tag.put("Inventory", inventoryUtils.fromPlayerData(pdata));
            tag.putInt("NR", Integer.parseInt(pdata.COMM_NR));
            tag.putString("UUID", comm_id);
            zcommItemStack.setNbt(tag);

            if (!technician.getInventory().insertStack(zcommItemStack)) {
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".retrieve.no_space"), false);
                return Command.SINGLE_SUCCESS;
            }

            source.sendFeedback(new TranslatableText("command."+Main.identifier+".retrieve.success"), false);

            return Command.SINGLE_SUCCESS;
        }

}
