package zener.zcomm.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;
import zener.zcomm.data.dataHandler;

public class Verify {

    public static int verify(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity technician = source.getPlayer();
        String technician_uuid = technician.getUuidAsString();

        if (!dataHandler.checkTEntry(technician_uuid)) {
            source.sendFeedback(new TranslatableText("command."+Main.identifier+".verify.permissions_too_low"), false);
            return Command.SINGLE_SUCCESS;
        }

        Iterable<ItemStack> stacks = technician.getItemsHand();
        stacks.iterator().forEachRemaining(stack -> {
            for (Item item : Main.ITEMS) {
                if (stack.getItem() == item) {
                    NbtCompound tag = stack.getOrCreateNbt();
                    if (!tag.contains("v")) {
                        tag.putBoolean("v", true);
                        stack.setNbt(tag);
                        technician.getInventory().markDirty();
                        source.sendFeedback(new TranslatableText("command."+Main.identifier+".verify.success").append(stack.getName()), false);
                    } else {
                        tag.putBoolean("v", true);
                        stack.setNbt(tag);
                        technician.getInventory().markDirty();
                        source.sendFeedback(new TranslatableText("command."+Main.identifier+".verify.success").append(stack.getName()), false);
                    }
                }
            }
        });

        source.sendFeedback(new TranslatableText("command."+Main.identifier+".verify"), false);

        return Command.SINGLE_SUCCESS;

    }

    public static int verifyNbt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity technician = source.getPlayer();
        String technician_uuid = technician.getUuidAsString();

        if (!dataHandler.checkTEntry(technician_uuid)) {
            source.sendFeedback(new TranslatableText("command."+Main.identifier+".verify.permissions_too_low"), false);
            return Command.SINGLE_SUCCESS;
        }

        if (!dataHandler.data.techData.get(technician_uuid).isHeadTechnician) {
            source.sendFeedback(new TranslatableText("command."+Main.identifier+".verify.permissions_too_low"), false);
            return Command.SINGLE_SUCCESS;
        }

        NbtCompound nbt = NbtCompoundArgumentType.getNbtCompound(context, "nbt");
        Iterable<ItemStack> stacks = technician.getItemsHand();
        stacks.iterator().forEachRemaining(stack -> {
            NbtCompound tag = stack.getOrCreateNbt();
            for (String key : nbt.getKeys()) {
                tag.put(key, nbt.get(key));
            }
            stack.setNbt(tag);
            source.sendFeedback(new TranslatableText("command."+Main.identifier+".verify.success").append(stack.getName()), false);
        });


        return Command.SINGLE_SUCCESS;
    }

    public static int verifyHelp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity technician = source.getPlayer();
        String technician_uuid = technician.getUuidAsString();

        if (!dataHandler.checkTEntry(technician_uuid)) {
            source.sendFeedback(new TranslatableText("command."+Main.identifier+".verify.permissions_too_low"), false);
            return Command.SINGLE_SUCCESS;
        }

        source.sendFeedback(new TranslatableText("command."+Main.identifier+".verify"), false);

        return Command.SINGLE_SUCCESS;

    }
    
}
