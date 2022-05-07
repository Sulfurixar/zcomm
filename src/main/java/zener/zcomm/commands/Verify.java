package zener.zcomm.commands;

import java.util.function.Predicate;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;

public class Verify {

    private static Predicate<ServerCommandSource> require(String permission) {
        return CommandWrapper.require("verify"+(permission.equals("") ? "" : "."+permission), 2);
    }

    public static LiteralCommandNode<ServerCommandSource> verify() {
        return ((LiteralArgumentBuilder<ServerCommandSource>)CommandManager
        .literal("verify")
        .requires(require("verify"))
        .executes(context -> runVerify(
            context.getSource(),
            context.getSource().getPlayer(),
            false,
            null
        )).then(CommandManager
            .literal("verify.reset")
            .requires(require("reset"))
            .executes(context -> runVerify(
                context.getSource(),
                context.getSource().getPlayer(),
                true,
                null
            ))
        )
        .then(CommandManager
            .literal("help")
            .requires(require("verify.help"))
            .executes(context -> runVerify(
                context.getSource(),
                context.getSource().getPlayer(),
                false,
                null
            ))
        )
        .then(CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound())
            .requires(require("verify.nbt"))
            .executes(context -> runVerify(
                context.getSource(),
                context.getSource().getPlayer(),
                false,
                NbtCompoundArgumentType.getNbtCompound(context, "nbt")
            ))
        )
        ).build();
    }

    private static int runVerify(ServerCommandSource source, @Nullable ServerPlayerEntity player, @Nullable Boolean reset, @Nullable NbtCompound nbt) {
        if (player == null) { source.sendFeedback(TranslateLib.MUST_BE_PLAYER, false); return 0; }
        if (CommandWrapper.isValidTechnician(source, player)) { source.sendFeedback(TranslateLib.PERMISSIONS_LOW, false); return 0; }

        if (reset) {
            player.getItemsHand().iterator().forEachRemaining(stack -> {
                stack.setNbt(new NbtCompound());
                source.sendFeedback(TranslateLib.VERIFY_SUCCESS.append(stack.getName()), false);
            });
            return Command.SINGLE_SUCCESS;
        }

        if (nbt != null) {
            player.getItemsHand().iterator().forEachRemaining(stack -> {
                NbtCompound tag = stack.getOrCreateNbt();
                nbt.getKeys().forEach(key -> {
                    tag.put(key, nbt.get(key));
                });
                tag.putBoolean("v", true);
                source.sendFeedback(TranslateLib.VERIFY_SUCCESS.append(stack.getName()), false);
            });
            return Command.SINGLE_SUCCESS;
        }

        source.sendFeedback(new TranslatableText("command."+Main.ID+".verify"), false);
        return Command.SINGLE_SUCCESS;
    }
}
