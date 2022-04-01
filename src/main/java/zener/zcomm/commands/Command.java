package zener.zcomm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import static com.mojang.brigadier.arguments.StringArgumentType.word;

import java.util.function.Predicate;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import org.jetbrains.annotations.NotNull;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import zener.zcomm.Main;

public class Command{

    private static @NotNull Predicate<ServerCommandSource> require(String permission, int defaultRequireLevel) {
        return player -> check(player, permission, defaultRequireLevel);
    }

    private static boolean check(@NotNull CommandSource source, @NotNull String permission, int defaultRequireLevel) {
        if (source.hasPermissionLevel(defaultRequireLevel)) {return true; }
        return Permissions.getPermissionValue(source, permission).orElse(false);
    }

    private static LiteralCommandNode<ServerCommandSource> literalBuilder(String id, String name, com.mojang.brigadier.Command<ServerCommandSource> cmd) {
        return CommandManager.literal(id).requires(require(name, 4)).executes(cmd).build();
    }
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {

        //stitch commands together
        LiteralCommandNode<ServerCommandSource> zcommsNode = literalBuilder(Main.identifier, Main.identifier, Help::help);
        dispatcher.getRoot().addChild(zcommsNode);

            // zcomms verify
            LiteralCommandNode<ServerCommandSource> verifyNode = literalBuilder("verify", Main.identifier+".verify", Verify::verify);
            zcommsNode.addChild(verifyNode);

                // zcomms verify help
                LiteralCommandNode<ServerCommandSource> verifyHelpNode = literalBuilder("help", Main.identifier+".verify", Verify::verifyHelp);
                verifyNode.addChild(verifyHelpNode);

                // zcomms verify reset
                LiteralCommandNode<ServerCommandSource> verifyResetNode = literalBuilder("reset", Main.identifier+".verify", Verify::verifyReset);
                verifyNode.addChild(verifyResetNode);

                ArgumentCommandNode<ServerCommandSource, NbtCompound> verifyNbtNode = CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound()).requires(require(Main.identifier+".retrieve", 4)).executes(Verify::verifyNbt).build();
                verifyNode.addChild(verifyNbtNode);

            // zcomms retrieve
            LiteralCommandNode<ServerCommandSource> retrieveNode = literalBuilder("retrieve", Main.identifier+".retrieve", Retrieve::retrieve);
            zcommsNode.addChild(retrieveNode);

                // zcomms retrieve nr
                ArgumentCommandNode<ServerCommandSource, Integer> retrieveArgNode = CommandManager.argument("nr", IntegerArgumentType.integer(0, 999)).requires(require(Main.identifier+".retrieve", 4)).executes(Retrieve::retrieveArg).build();
                retrieveNode.addChild(retrieveArgNode);

                // zcomms retrieve id
                ArgumentCommandNode<ServerCommandSource, String> retrieveArgIDNode = CommandManager.argument("id", StringArgumentType.word()).suggests(new commIDProvider()).requires(require(Main.identifier+".retrieve", 4)).executes(Retrieve::retrieveArgID).build();
                retrieveNode.addChild(retrieveArgIDNode);

            // zcomms help
            LiteralCommandNode<ServerCommandSource> helpNode = literalBuilder("help", Main.identifier+".help", Help::help);
            zcommsNode.addChild(helpNode);

                // zcomms verify
                LiteralCommandNode<ServerCommandSource> helpVerifyNode = literalBuilder("help", Main.identifier+".help.verify", Verify::verifyHelp);
                helpNode.addChild(helpVerifyNode);

                // zcomms help retrieve
                LiteralCommandNode<ServerCommandSource> helpRetrieveNode = literalBuilder("help", Main.identifier+".help.retrieve", Retrieve::retrieve);
                helpNode.addChild(helpRetrieveNode);

                // zcomms help data
                LiteralCommandNode<ServerCommandSource> helpDataNode = literalBuilder("data", Main.identifier+".help.data", Data::data);
                helpNode.addChild(helpDataNode);

                    // zcomms help data get
                    LiteralCommandNode<ServerCommandSource> helpDataGetNode = literalBuilder("get", Main.identifier+".help.data.get", Data::get);
                    helpDataNode.addChild(helpDataGetNode);

                        // zcomms help data get user
                        LiteralCommandNode<ServerCommandSource> helpDataGetUserNode = literalBuilder("user", Main.identifier+".data.get.user", Data::getUser);
                        helpDataGetNode.addChild(helpDataGetUserNode);

                        // zcomms help data get key
                        LiteralCommandNode<ServerCommandSource> helpDataGetKeyNode = literalBuilder("key", Main.identifier+".help.data.get.key", Data::getTechnicianKey);
                        helpDataGetNode.addChild(helpDataGetKeyNode);

                    // zcomms help data add
                    LiteralCommandNode<ServerCommandSource> helpDataAddNode = literalBuilder("add", Main.identifier+".help.data.add", Data::add);
                    helpDataNode.addChild(helpDataAddNode);

                        // zcomms help data add technician
                        LiteralCommandNode<ServerCommandSource> helpDataAddTechnicianNode = literalBuilder("technician", Main.identifier+".help.data.add.technician", Data::addTechnician);
                        helpDataAddNode.addChild(helpDataAddTechnicianNode);

                    //zcomms help data remove
                    LiteralCommandNode<ServerCommandSource> helpDataRemoveNode = literalBuilder("remove", Main.identifier+".help.data.remove", Data::remove);
                    helpDataNode.addChild(helpDataRemoveNode);

                        //zcomms data remove technician
                        LiteralCommandNode<ServerCommandSource> helpDataRemoveTechnicianNode = literalBuilder("remove", Main.identifier+".help.data.remove.technician", Data::removeTechnician);
                        helpDataRemoveTechnicianNode.addChild(helpDataRemoveNode);

            // zcomms data
            LiteralCommandNode<ServerCommandSource> dataNode = literalBuilder("data", Main.identifier+".data", Data::data);
            zcommsNode.addChild(dataNode);

                // zcomms data help
                LiteralCommandNode<ServerCommandSource> dataHelpNode = literalBuilder("help", Main.identifier+".data", Data::data);
                dataNode.addChild(dataHelpNode);

                // zcomms data get
                LiteralCommandNode<ServerCommandSource> dataGetNode = literalBuilder("get", Main.identifier+".data.get", Data::get);
                dataNode.addChild(dataGetNode);

                    // zcomms data get help
                    LiteralCommandNode<ServerCommandSource> dataGetHelpNode = literalBuilder("help", Main.identifier+".data.get", Data::get);
                    dataGetNode.addChild(dataGetHelpNode);

                    // zcomms data get user
                    LiteralCommandNode<ServerCommandSource> dataGetUserNode = literalBuilder("user", Main.identifier+".data.get.user", Data::getUser);
                    dataGetNode.addChild(dataGetUserNode);

                        //zcomms data get user player
                        ArgumentCommandNode<ServerCommandSource, EntitySelector> dataGetUserArgNode = CommandManager.argument("player", EntityArgumentType.player()).requires(require(Main.identifier+".data.get.user", 4)).executes(Data::getUserArg).build();
                        dataGetUserNode.addChild(dataGetUserArgNode);

                    // zcomms data get key
                    LiteralCommandNode<ServerCommandSource> dataGetTechnicianKeyNode = literalBuilder("key", Main.identifier+".data.get.key", Data::getTechnicianKey);
                    dataGetNode.addChild(dataGetTechnicianKeyNode);

                        //zcomms data get key player
                        ArgumentCommandNode<ServerCommandSource, EntitySelector> dataGetTechnicianKeyArgNode = CommandManager.argument("player", EntityArgumentType.player()).requires(require(Main.identifier+".data.get.key", 4)).executes(Data::getTechnicianKeyArg).build();
                        dataGetTechnicianKeyNode.addChild(dataGetTechnicianKeyArgNode);

                            //zcomms data get key player level
                            ArgumentCommandNode<ServerCommandSource, Integer> dataGetTechnicianKeyArgArgNode = CommandManager.argument("level", IntegerArgumentType.integer(1, 2)).requires(require(Main.identifier+".data.get.key", 4)).executes(Data::getTechnicianKeyArgArg).build();
                            dataGetTechnicianKeyArgNode.addChild(dataGetTechnicianKeyArgArgNode);

                     // zcomms data get listener
                     LiteralCommandNode<ServerCommandSource> dataGetListenerNode = literalBuilder("listener", Main.identifier+".data.get.listener", Data::getListener);
                     dataGetNode.addChild(dataGetListenerNode);

                        // zcomms data get listener [args]
                        ArgumentCommandNode<ServerCommandSource, NbtElement> dataGetListenerArgsNode = CommandManager.argument("comm_nr_array", NbtElementArgumentType.nbtElement()).requires(require(Main.identifier+".data.get.listener", 4)).executes(Data::getListenerArgs).build();
                        dataGetListenerNode.addChild(dataGetListenerArgsNode);


                // zcomms data add
                LiteralCommandNode<ServerCommandSource> dataAddNode = literalBuilder("add", Main.identifier+".add", Data::add);
                dataNode.addChild(dataAddNode);

                    // zcomms data add help
                    LiteralCommandNode<ServerCommandSource> dataAddHelpNode = literalBuilder("help", Main.identifier+".add", Data::add);
                    dataAddNode.addChild(dataAddHelpNode);

                    // zcomms data add technician
                    LiteralCommandNode<ServerCommandSource> dataAddTechnicianNode = literalBuilder("technician", Main.identifier+".add.technician", Data::addTechnician);
                    dataAddNode.addChild(dataAddTechnicianNode);

                        // zcomms data add technician help
                        LiteralCommandNode<ServerCommandSource> dataAddTechnicianHelpNode = literalBuilder("help", Main.identifier+".add.technician", Data::addTechnician);
                        dataAddTechnicianNode.addChild(dataAddTechnicianHelpNode);

                        // zcomms data add technician player
                        ArgumentCommandNode<ServerCommandSource, EntitySelector> dataAddTechnicianArgNode = CommandManager.argument("player", EntityArgumentType.player()).requires(require(Main.identifier+".add.technician", 4)).executes(Data::addTechnicianArg).build();
                        dataAddTechnicianNode.addChild(dataAddTechnicianArgNode);

                            // zcomms data add technician player password
                            ArgumentCommandNode<ServerCommandSource, String> dataAddTechnicianArgArgNode = CommandManager.argument("passcode", word()).requires(require(Main.identifier+".add.technician", 4)).executes(Data::addTechnicianArgArg).build();
                            dataAddTechnicianArgNode.addChild(dataAddTechnicianArgArgNode);

                //zcomms data remove
                LiteralCommandNode<ServerCommandSource> dataRemoveNode = literalBuilder("remove", Main.identifier+".data.remove", Data::remove);
                dataNode.addChild(dataRemoveNode);

                    //zcomms data remove help
                    LiteralCommandNode<ServerCommandSource> dataRemoveHelpNode = literalBuilder("help", Main.identifier+".data.remove", Data::remove);
                    dataRemoveNode.addChild(dataRemoveHelpNode);

                    // zcomms data remove technician player
                    ArgumentCommandNode<ServerCommandSource, EntitySelector> dataRemoveTechnicianArgNode = CommandManager.argument("player", EntityArgumentType.player()).requires(require(Main.identifier+".data.remove.technician", 4)).executes(Data::removeTechnicianArg).build();
                    dataRemoveNode.addChild(dataRemoveTechnicianArgNode);


    }

}
