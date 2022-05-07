package zener.zcomm.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mojang.brigadier.tree.LiteralCommandNode;

import blue.endless.jankson.annotation.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;
import zener.zcomm.commands.SuggestionProviders.CommNRProvider;
import zener.zcomm.commands.SuggestionProviders.CommUUIDProvider;
import zener.zcomm.components.CommRegistryComponent;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.components.ICommRegistryComponent.Comm;
import zener.zcomm.components.ICommRegistryComponent.OldComm;

public class Retrieve {
    private static Predicate<ServerCommandSource> require(String permission) {
        return CommandWrapper.require("retrieve"+(permission.equals("") ? "" : "."+permission), 2);
    }

    public static LiteralCommandNode<ServerCommandSource> retrieve() {
        return ((LiteralArgumentBuilder<ServerCommandSource>)CommandManager
        .literal("retrieve")
        .requires(require("retrieve"))
        .executes(context -> runRetrieve(
            context.getSource(),
            context.getSource().getPlayer(),
            null,
            null,
            null
        ))
        .then(CommandManager.argument("nr", IntegerArgumentType.integer(0, 999))
            .requires(require("retrieve.nr"))
            .suggests(new CommNRProvider())
            .executes(context -> runRetrieve(
                context.getSource(),
                context.getSource().getPlayer(),
                IntegerArgumentType.getInteger(context, "nr"),
                null,
                null
            ))
            .then(CommandManager.literal("for")
                .then((RequiredArgumentBuilder<ServerCommandSource, EntitySelector>)
                    (CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> runRetrieve(
                        context.getSource(),
                        context.getSource().getPlayer(),
                        IntegerArgumentType.getInteger(context, "nr"),
                        EntityArgumentType.getPlayer(context, "player"),
                        null
                    )))
                )
            )
        )
        .then(CommandManager.argument("uuid", UuidArgumentType.uuid())
            .requires(require("retrieve.uuid"))
            .suggests(new CommUUIDProvider())
            .executes(context -> runRetrieve(
                context.getSource(),
                context.getSource().getPlayer(),
                null,
                null,
                UuidArgumentType.getUuid(context, "uuid")
            ))
            .then(CommandManager.literal("for")
                .then((RequiredArgumentBuilder<ServerCommandSource, EntitySelector>)
                    (CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> runRetrieve(
                        context.getSource(),
                        context.getSource().getPlayer(),
                        null,
                        EntityArgumentType.getPlayer(context, "player"),
                        UuidArgumentType.getUuid(context, "uuid")
                    )))
                )
            )
        )
        ).build();
    }

    private static <C extends Comm> int runRetrieve(ServerCommandSource source, @Nullable ServerPlayerEntity player, @Nullable Integer nr, @Nullable ServerPlayerEntity forPlayer, @Nullable UUID uuidArg) {
        if (player == null) { source.sendFeedback(TranslateLib.MUST_BE_PLAYER, false); return 0; }
        if (CommandWrapper.isValidTechnician(source, player)) { source.sendFeedback(TranslateLib.PERMISSIONS_LOW, false); return 0; }

        if (nr != null || uuidArg != null) {
            Map<String, C> commList = new HashMap<>();
            if (nr != null) {
                commList = getComms(source, new NRPredicate<>(nr));
                if (commList == null || commList.isEmpty()) {
                    source.sendFeedback(new TranslatableText("command."+Main.ID+".retrieve.no_comm"), false);
                    return 0;
                }
                if (commList.size() > 1) {
                    commList.forEach((k, v) -> {
                        source.sendFeedback(new TranslatableText("command."+Main.ID+".retrieve.found_comm")
                        .append("NR: "+v.getNR()+"; UUID: "+k), false);
                    });
                    source.sendFeedback(new TranslatableText("command."+Main.ID+".retrieve.multiple_comms"), false);
                    return Command.SINGLE_SUCCESS;
                }
            }

            if (uuidArg != null) {
                commList = getComms(source, new CommUUIDPredicate<>(uuidArg));
                
                if (commList == null || commList.isEmpty()) {
                    source.sendFeedback(new TranslatableText("command."+Main.ID+".retrieve.no_comm"), false);
                    return 0;
                }
            }

            Comm comm = commList.values().toArray(new Comm[1])[0];
            ItemStack commItem = comm.getCOMM().copy();
            UUID comm_id = ComponentHandler.createUUID(player);
            commItem.getOrCreateNbt().putUuid("UUID", comm_id);
            if (forPlayer != null) {
                if (!player.getInventory().insertStack(commItem)) {
                    source.sendFeedback(TranslateLib.NO_SPACE, false);
                    return 0;
                }
                ComponentHandler.COMM_REGISTRY.get(source.getServer().getOverworld()).addEntry(comm_id, new Comm(forPlayer.getUuid(), nr, commItem));
            } else {
                UUID UUID = comm.getUSER_UUID();
                if (UUID != null) {
                    if (!player.getInventory().insertStack(commItem)) {
                        source.sendFeedback(TranslateLib.NO_SPACE, false);
                        return 0;
                    }
                    ComponentHandler.COMM_REGISTRY.get(source.getServer().getOverworld()).addEntry(comm_id, new Comm(UUID, nr, commItem));
                    ComponentHandler.removeCommEntry(player, commList.keySet().toArray(new String[1])[0]);
                    return Command.SINGLE_SUCCESS;
                } else {
                    OldComm oldComm = (OldComm) comm;
                    String uuid = oldComm.getSTRING_UUID();
                    List<ServerPlayerEntity> players = source.getServer().getPlayerManager().getPlayerList().stream().filter(p -> p.getUuid().toString().equals(uuid)).toList();
                    if (players.isEmpty()) {
                        source.sendFeedback(new TranslatableText("command."+Main.ID+".retrieve.no_player").append(uuid), false);
                        return 0;
                    }
                    forPlayer = players.get(0);
                    if (!player.getInventory().insertStack(commItem)) {
                        source.sendFeedback(TranslateLib.NO_SPACE, false);
                        return 0;
                    }
                    ComponentHandler.COMM_REGISTRY.get(source.getServer().getOverworld()).addEntry(comm_id, new Comm(forPlayer.getUuid(), nr, commItem));
                    ComponentHandler.removeCommEntry(player, commList.keySet().toArray(new String[1])[0]);
                    return Command.SINGLE_SUCCESS;
                }
            }
        }

        source.sendFeedback(new TranslatableText("command."+Main.ID+".retrieve"), false);
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("unchecked")
    public static <C extends Comm, E extends Map.Entry<?, C>, P extends Predicate<E>> Map<String, C> getComms(ServerCommandSource source, P predicate) {
        CommRegistryComponent commRegistry = ComponentHandler.COMM_REGISTRY.get(source.getServer().getOverworld());
        Main.LOGGER.info("Comm count: "+(commRegistry.getCOMMLIST().size()+commRegistry.getOLD_COMMS().size()));
        Map<String, C> commList = commRegistry.getCOMMLIST().entrySet().stream()
                .filter(entry -> predicate.test((E)entry)).map(entry -> Map.entry(entry.getKey().toString(), (C)entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            commRegistry.getOLD_COMMS().entrySet().stream()
                .filter(entry -> predicate.test((E)entry)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach((k, v) -> commList.put(k, (C)v));
        return commList;
    }

    private static class NRPredicate<C extends Comm> implements Predicate<Map.Entry<?, C>> {

        private final int nr;
        NRPredicate(int nr) {
            this.nr = nr;
        }

        @Override
        public boolean test(Map.Entry<?, C> t) {
            return t.getValue().getNR() == nr;
        }
        
    }

    public static class CommUUIDPredicate<C extends Comm> implements Predicate<Map.Entry<?, C>> {

        private final UUID UUID;
        CommUUIDPredicate(UUID UUID) {
            this.UUID = UUID;
        }

        @Override
        public boolean test(Map.Entry<?, C> t) {
            if (t.getKey() instanceof UUID) {
                return t.getKey().equals(UUID);
            } else {
                return t.getKey().equals(UUID.toString());
            }
        }
        
    }

    public static class PlayerUUIDPredicate<C extends Comm> implements Predicate<Map.Entry<?, C>> {

        private final UUID UUID;
        public PlayerUUIDPredicate(UUID UUID) {
            this.UUID = UUID;
        }

        @Override
        public boolean test(Entry<?, C> t) {
            UUID _UUID = t.getValue().getUSER_UUID();
            if (_UUID != null) {
                Main.LOGGER.info(_UUID+" == "+UUID);
                return _UUID.equals(UUID);
            }
            String uuid = ((OldComm)t.getValue()).getSTRING_UUID();
            Main.LOGGER.info(uuid+" == "+UUID);
            return uuid.equals(UUID.toString());
        }

    }
}
