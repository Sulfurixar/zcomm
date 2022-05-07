package zener.zcomm.commands;

import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import org.jetbrains.annotations.NotNull;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import zener.zcomm.Main;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.components.TechnicianRegistryComponent;
import zener.zcomm.components.ITechnicianRegistryComponent.Technician;

public class CommandWrapper {

    public static @NotNull Predicate<ServerCommandSource> require(String permission, int defaultRequireLevel) {
        return player -> check(player, Main.ID+(permission.equals("") ? "" : "."+permission), defaultRequireLevel);
    }

    private static boolean check(@NotNull CommandSource source, @NotNull String permission, int defaultRequireLevel) {
        if (source.hasPermissionLevel(defaultRequireLevel)) { return true; }
        return Permissions.getPermissionValue(source, permission).orElse(false);
    }

    private static LiteralCommandNode<ServerCommandSource> literalBuilder(String id, String name, com.mojang.brigadier.Command<ServerCommandSource> cmd) {
        return CommandManager.literal(id).requires(require(name, 2)).executes(cmd).build();
    }

    public static boolean isValidTechnician(ServerCommandSource source, ServerPlayerEntity player) {
        TechnicianRegistryComponent technicianRegistry = ComponentHandler.TECHNICIAN_REGISTRY.get(source.getServer().getOverworld());
        Technician technician = technicianRegistry.getTechnician(player);
        return !(technician == null || !(!technician.isTechnician() && !technician.isHeadTechnician()));
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralCommandNode<ServerCommandSource> zcommsNode = literalBuilder(Main.ID, "", Help::help);
        dispatcher.getRoot().addChild(zcommsNode);
        zcommsNode.addChild(Verify.verify());
        zcommsNode.addChild(Retrieve.retrieve());
        zcommsNode.addChild(Data.data());
    }
    
}
