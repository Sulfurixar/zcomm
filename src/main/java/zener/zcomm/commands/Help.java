package zener.zcomm.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;

public class Help {

    public static int help(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("command."+Main.identifier+".help"), false);
        return Command.SINGLE_SUCCESS;
    }
    
}
