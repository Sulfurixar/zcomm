package zener.zcomm.commands;

import java.util.function.Predicate;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;
import zener.zcomm.commands.data.Add;
import zener.zcomm.commands.data.Get;
import zener.zcomm.commands.data.Remove;
import zener.zcomm.components.ICommRegistryComponent.Comm;

public class Data {
    private static Predicate<ServerCommandSource> require(String permission) {
        return CommandWrapper.require("data"+(permission.equals("") ? "" : "."+permission), 2);
    }

    public static LiteralCommandNode<ServerCommandSource> data() {
        return ((LiteralArgumentBuilder<ServerCommandSource>)CommandManager
        .literal("data")
        .requires(require("help"))
        .executes(context -> runData(
            context.getSource(),
            context.getSource().getPlayer(),
            false,
            null,
            null,
            false,
            false,
            null,
            false
        ))
        .then(CommandManager.literal("add")
            .requires(require("add"))
            .executes(context -> runData(
                context.getSource(),
                context.getSource().getPlayer(),
                false,
                null,
                null,
                true,
                false,
                null,
                false
            ))
            .then(CommandManager.literal("technician")
                .requires(require("add.technician"))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .then(CommandManager.argument("head", BoolArgumentType.bool())
                        .then(CommandManager.argument("passcode", StringArgumentType.string())
                            .executes(context -> runData(
                                context.getSource(),
                                context.getSource().getPlayer(),
                                false,
                                EntityArgumentType.getPlayer(context, "player"),
                                null,
                                true,
                                BoolArgumentType.getBool(context, "head"),
                                StringArgumentType.getString(context, "passcode"),
                                false
                            ))
                        )
                    )
                    .then(CommandManager.argument("passcode", StringArgumentType.string())
                        .executes(context -> runData(
                            context.getSource(),
                            context.getSource().getPlayer(),
                            false,
                            EntityArgumentType.getPlayer(context, "player"),
                            null,
                            true,
                            false,
                            StringArgumentType.getString(context, "passcode"),
                            false
                        ))
                    )
                )
            )
        )
        .then(CommandManager.literal("remove")
            .requires(require("remove"))
            .executes(context -> runData(
                context.getSource(),
                context.getSource().getPlayer(),
                false,
                null,
                null,
                false,
                false,
                null,
                true
            ))
            .then(CommandManager.literal("technician")
                .requires(require("remove.technician"))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> runData(
                        context.getSource(),
                        context.getSource().getPlayer(),
                        false,
                        EntityArgumentType.getPlayer(context, "player"),
                        null,
                        false,
                        false,
                        null,
                        true
                    ))
                )
            )
        )
        .then(CommandManager.literal("get")
            .requires(require("get"))
            .then(CommandManager.literal("help")
                .requires(require("get.help"))
                .executes(context -> runData(
                    context.getSource(),
                    context.getSource().getPlayer(),
                    true,
                    null,
                    null,
                    false,
                    false,
                    null,
                    false
                ))
            )
            .then(CommandManager.literal("user")
                .requires(require("get.user"))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> runData(
                        context.getSource(),
                        context.getSource().getPlayer(),
                        true,
                        EntityArgumentType.getPlayer(context, "player"),
                        null,
                        false,
                        false,
                        null,
                        false
                    ))
                )
            )
            .then(CommandManager.literal("listener")
                .requires(require("get.listener"))
                .then(CommandManager.argument("ranges", NbtElementArgumentType.nbtElement())
                    .executes(context -> runData(
                        context.getSource(),
                        context.getSource().getPlayer(),
                        true,
                        null,
                        NbtElementArgumentType.getNbtElement(context, "ranges"),
                        false,
                        false,
                        null,
                        false
                    ))
                )
            )
        )
        ).build();
    }

    private static <C extends Comm> int runData(
        ServerCommandSource source, @Nullable ServerPlayerEntity player, 
        @Nullable Boolean get, @Nullable ServerPlayerEntity argPlayer, 
        @Nullable NbtElement listener, @Nullable Boolean add,
        @Nullable Boolean head, @Nullable String passcode,
        @Nullable Boolean remove
        ) {

        if (player == null) { source.sendFeedback(TranslateLib.MUST_BE_PLAYER, false); return 0; }

        if (add) {

            if (passcode != null) {

                return Add.addTechnician(source, player, argPlayer, passcode, head);

            }

            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.add.help"), false);
            return Command.SINGLE_SUCCESS;
        }
        if (CommandWrapper.isValidTechnician(source, player)) { source.sendFeedback(TranslateLib.PERMISSIONS_LOW, false); return 0; }

        if (get) {

            if (argPlayer != null) {

                return Get.getPlayer(source, argPlayer);
            }

            if (listener != null) {
                return Get.getListener(source, listener);
            }

            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.help"), false);
            return Command.SINGLE_SUCCESS;
        }

        if (remove) {

            if (argPlayer != null) {
                return Remove.removeTechnician(source, player, argPlayer);
            }

            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.remove.help"), false);
            return Command.SINGLE_SUCCESS;
        }

        source.sendFeedback(new TranslatableText("command."+Main.ID+".data.help"), false);
        return Command.SINGLE_SUCCESS;
    }

}
