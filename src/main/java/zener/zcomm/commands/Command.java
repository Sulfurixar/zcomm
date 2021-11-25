package zener.zcomm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class Command{
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {

        //stitch commands together
        LiteralCommandNode<ServerCommandSource> zcommsNode = CommandManager.literal("zcomms").requires(source -> source.hasPermissionLevel(4)).executes(Help::help).build();
        dispatcher.getRoot().addChild(zcommsNode);

            // zcomms help
            LiteralCommandNode<ServerCommandSource> helpNode = CommandManager.literal("help").requires(source -> source.hasPermissionLevel(4)).executes(Help::help).build();
            zcommsNode.addChild(helpNode);

                // zcomms help data
                LiteralCommandNode<ServerCommandSource> helpDataNode = CommandManager.literal("data").requires(source -> source.hasPermissionLevel(4)).executes(Data::help).build();
                helpNode.addChild(helpDataNode);

                    // zcomms help data get
                    LiteralCommandNode<ServerCommandSource> helpDataGetNode = CommandManager.literal("get").requires(source -> source.hasPermissionLevel(4)).executes(Data::getHelp).build();
                    helpDataNode.addChild(helpDataGetNode);

                    // zcomms help data add
                    LiteralCommandNode<ServerCommandSource> helpDataAddNode = CommandManager.literal("add").requires(source -> source.hasPermissionLevel(4)).executes(Data::addHelp).build();
                    helpDataNode.addChild(helpDataAddNode);

                        // zcomms help data add technician
                        LiteralCommandNode<ServerCommandSource> helpDataAddTechnicianNode = CommandManager.literal("technician").requires(source -> source.hasPermissionLevel(4)).executes(Data::addTechnician).build();
                        helpDataAddNode.addChild(helpDataAddTechnicianNode);

            // zcomms data
            LiteralCommandNode<ServerCommandSource> dataNode = CommandManager.literal("data").requires(source -> source.hasPermissionLevel(4)).executes(Data::data).build();
            zcommsNode.addChild(dataNode);

                // zcomms data help
                LiteralCommandNode<ServerCommandSource> dataHelpNode = CommandManager.literal("help").requires(source -> source.hasPermissionLevel(4)).executes(Data::help).build();
                dataNode.addChild(dataHelpNode);

                // zcomms data get
                LiteralCommandNode<ServerCommandSource> dataGetNode = CommandManager.literal("get").requires(source -> source.hasPermissionLevel(4)).executes(Data::get).build();
                dataNode.addChild(dataGetNode);

                    // zcomms data get help
                    LiteralCommandNode<ServerCommandSource> dataGetHelpNode = CommandManager.literal("help").requires(source -> source.hasPermissionLevel(4)).executes(Data::getHelp).build();
                    dataGetNode.addChild(dataGetHelpNode);

                // zcomms data add
                LiteralCommandNode<ServerCommandSource> dataAddNode = CommandManager.literal("add").requires(source -> source.hasPermissionLevel(4)).executes(Data::add).build();
                dataNode.addChild(dataAddNode);

                    // zcomms data add help
                    LiteralCommandNode<ServerCommandSource> dataAddHelpNode = CommandManager.literal("help").requires(source -> source.hasPermissionLevel(4)).executes(Data::addHelp).build();
                    dataAddNode.addChild(dataAddHelpNode);

                    // zcomms data add technician
                    LiteralCommandNode<ServerCommandSource> dataAddTechnicianNode = CommandManager.literal("technician").requires(source -> source.hasPermissionLevel(4)).executes(Data::addTechnician).build();
                    dataAddNode.addChild(dataAddTechnicianNode);

                        // zcomms data add technician help
                        LiteralCommandNode<ServerCommandSource> dataAddTechnicianHelpNode = CommandManager.literal("help").requires(source -> source.hasPermissionLevel(4)).executes(Data::addTechnician).build();
                        dataAddTechnicianNode.addChild(dataAddTechnicianHelpNode);

                        // zcomms data add technician player
                        ArgumentCommandNode<ServerCommandSource, EntitySelector> dataAddTechnicianArgNode = CommandManager.argument("player", EntityArgumentType.player()).requires(source -> source.hasPermissionLevel(4)).executes(Data::addTechnicianArg).build();
                        dataAddTechnicianNode.addChild(dataAddTechnicianArgNode);



    }

}
