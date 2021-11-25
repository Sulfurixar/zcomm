package zener.zcomm.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.EntitySelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class Data {

    public static int data(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("command.zcomms.data"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int help(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("command.zcomms.data.help"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("command.zcomms.data.get.help"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("command.zcomms.data.add.help"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int addTechnician(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("command.zcomms.data.add.technician.help"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int addTechnicianArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        
        PlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(source);
        System.out.println(player.getName().toString());
        source.sendFeedback(new LiteralText("asd"), true);
        //source.sendFeedback(new TranslatableText("command.zcomms.data.add.technician.arg"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int addHelp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("command.zcomms.data.add.help"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int getHelp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("command.zcomms.data.get.help"), true);
        return Command.SINGLE_SUCCESS;
    }
    
}
