package zener.zcomm.commands;

import java.util.Map;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;
import zener.zcomm.data.dataHandler;
import zener.zcomm.data.playerData;
import zener.zcomm.data.tData;

public class Data {

    private static void removePerms(ServerPlayerEntity player, ServerAdvancementLoader loader) {
        player.getAdvancementTracker().revokeCriterion(loader.get(Main.ZCOMM_ROOT_ADVANCEMENT), Main.identifier);
        player.getAdvancementTracker().revokeCriterion(loader.get(Main.ZCOMM_CERT_ADVANCEMENT), Main.identifier);
        player.getAdvancementTracker().revokeCriterion(loader.get(Main.ZCOMM_HEAD_ADVANCEMENT), Main.identifier);
    }

    // ROOT COMMAND, displays subcommands
    public static int data(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("command."+Main.identifier+".data"), false);
        return Command.SINGLE_SUCCESS;
    }

        // GET COMMAND, displays subcommands
        public static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            ServerCommandSource source = context.getSource();
            source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.help"), false);
            return Command.SINGLE_SUCCESS;
        }

            // GET PLAYER COMM DATA
            public static int getUser(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                ServerCommandSource source = context.getSource();

                source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.comm_data"), false);
                return Command.SINGLE_SUCCESS;
            }


                private static boolean check_model_data(JsonObject x) {
                    if (x.get("Stack") == null || x.get("Stack").getAsJsonObject().get("tag") == null || x.get("Stack").getAsJsonObject().get("tag").getAsJsonObject().get("CustomModelData") == null){
                        return false;
                    }
                    return true;
                }
                public static int getUserArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity technician = source.getPlayer();
                    String technician_uuid = technician.getUuidAsString();
                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                    String player_uuid = player.getUuidAsString();

                    if (!dataHandler.checkTEntry(technician_uuid)) {
                        source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.permissions_too_low"), false);
                        return Command.SINGLE_SUCCESS;
                    }

                    if (!dataHandler.checkDUserEntry(player_uuid)) {
                        source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.user.no_user"), false);
                        return Command.SINGLE_SUCCESS;
                    }

                    Map<String, playerData> commData = dataHandler.get_user_data(player_uuid);
                    source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.comm_data.found_data"), false);
                    commData.forEach((uuid, pdata) -> {
                        source.sendFeedback(new LiteralText("Comm ID: " + uuid), false);
                        source.sendFeedback(new LiteralText("    User ID: " + pdata.USER_ID), false);
                        source.sendFeedback(new LiteralText("    Comm NR: " + pdata.COMM_NR), false);
                        
                        JsonObject charm = pdata.getCharm();
                        if (check_model_data(charm)) {
                            String id = Integer.toString(charm.get("Stack").getAsJsonObject().get("tag").getAsJsonObject().get("CustomModelData").getAsInt());
                            source.sendFeedback(new LiteralText("    Charm: " + id), false);
                        }
                        JsonObject casing = pdata.getCasing();
                        if (check_model_data(casing)) {
                            String id = Integer.toString(casing.get("Stack").getAsJsonObject().get("tag").getAsJsonObject().get("CustomModelData").getAsInt());
                            source.sendFeedback(new LiteralText("    Casing: " + id), false);
                        }
                    });
                    return Command.SINGLE_SUCCESS;
                }

            // GET KEY COMMAND, displays help about key command
            public static int getTechnicianKey(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                ServerCommandSource source = context.getSource();
                ServerPlayerEntity technician = source.getPlayer();
                String technician_uuid = technician.getUuidAsString();

                if (!dataHandler.checkTEntry(technician_uuid)) {
                    source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.permissions_too_low"), false);
                    return Command.SINGLE_SUCCESS;
                }
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key"), false);
                return Command.SINGLE_SUCCESS;
            }

                // GET KEY PLAYER COMMAND, displays player key as technician
                public static int getTechnicianKeyArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity technician = source.getPlayer();
                    String technician_uuid = technician.getUuidAsString();
                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                    String player_uuid = player.getUuidAsString();

                    if (!dataHandler.checkTEntry(technician_uuid)) {
                        source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.permissions_too_low"), false);
                        return Command.SINGLE_SUCCESS;
                    }

                    tData technician_data = dataHandler.data.techData.get(technician_uuid);
                    if (!technician_data.isHeadTechnician) {
                        source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.permissions_too_low"), false);
                        return Command.SINGLE_SUCCESS;
                    }

                    if (dataHandler.checkTEntry(player_uuid)) {
                        source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.player_exists"), false);
                        return Command.SINGLE_SUCCESS;
                    }

                    tData tdata = new tData((PlayerEntity)player, (byte)2);

                    source.sendFeedback(new LiteralText("Player: " + player.getName().toString() + "; Passcode: " + tdata.get_hash()), false);
                    return Command.SINGLE_SUCCESS;
                }

                    // GET KEY PLAYER LEVEL COMMAND, displays player key as specified technician level (technician or head)
                    public static int getTechnicianKeyArgArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                        ServerCommandSource source = context.getSource();
                        ServerPlayerEntity technician = source.getPlayer();
                        String technician_uuid = technician.getUuidAsString();
                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        String player_uuid = player.getUuidAsString();
                        Integer id = IntegerArgumentType.getInteger(context, "level");

                        if (!dataHandler.checkTEntry(technician_uuid)) {
                            source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.permissions_too_low"), false);
                            return Command.SINGLE_SUCCESS;
                        }

                        tData technician_data = dataHandler.data.techData.get(technician_uuid);
                        if (!technician_data.isHeadTechnician) {
                            source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.permissions_too_low"), false);
                            return Command.SINGLE_SUCCESS;
                        }

                        if (dataHandler.checkTEntry(player_uuid)) {
                            source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.player_exists"), false);
                            return Command.SINGLE_SUCCESS;
                        }

                        tData tdata = new tData((PlayerEntity)player, (byte)id.intValue());

                        source.sendFeedback(new LiteralText("Passcode: " + tdata.get_hash()), false);
                        return Command.SINGLE_SUCCESS;
                    }

        public static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            ServerCommandSource source = context.getSource();
            source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.add.help"), false);
            return Command.SINGLE_SUCCESS;
        }

            public static int addTechnician(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                ServerCommandSource source = context.getSource();
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.add.technician.help"), false);
                return Command.SINGLE_SUCCESS;
            }

                public static int addTechnicianArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    ServerCommandSource source = context.getSource();
                    source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.add.technician.arg"), false);
                    return Command.SINGLE_SUCCESS;
                }

                    public static int addTechnicianArgArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                        ServerCommandSource source = context.getSource();
                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        String uuid = player.getUuidAsString();
                        String arg = context.getArgument("passcode", String.class);
                        boolean head = false;
                        ServerAdvancementLoader loader = source.getServer().getAdvancementLoader();

                        if (dataHandler.checkTEntry(uuid)) {
                            source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.add.technician.already_exist"), false);
                            return Command.SINGLE_SUCCESS;
                        }

                        if (dataHandler.data.techData.size() == 0 && arg.compareTo("Zener") == 0) {
                            head = true;
                        }

                        tData tdata;
                        if (head) {
                            tdata = new tData(player, (byte)2);
                            dataHandler.addEntry(uuid, tdata);
                            source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.add.technician.set_headtechnician"), false);
                            player.getAdvancementTracker().grantCriterion(loader.get(Main.ZCOMM_ROOT_ADVANCEMENT), Main.identifier);
                            player.getAdvancementTracker().grantCriterion(loader.get(Main.ZCOMM_CERT_ADVANCEMENT), Main.identifier);
                            player.getAdvancementTracker().grantCriterion(loader.get(Main.ZCOMM_HEAD_ADVANCEMENT), Main.identifier);
                            return Command.SINGLE_SUCCESS;
                        } else {
                            tdata = new tData(player, (byte)1);
                            String hash = tdata.get_hash();
                            if (hash.compareToIgnoreCase(arg) == 0) {
                                dataHandler.addEntry(uuid, tdata);
                                source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.add.technician.set_technician"), false);
                                player.getAdvancementTracker().grantCriterion(loader.get(Main.ZCOMM_ROOT_ADVANCEMENT), Main.identifier);
                                player.getAdvancementTracker().grantCriterion(loader.get(Main.ZCOMM_CERT_ADVANCEMENT), Main.identifier);
                                return Command.SINGLE_SUCCESS;
                            }
                        }
                        source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.add.technician.incorrect_code"), false);
                        return Command.SINGLE_SUCCESS;
                    }
        public static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            ServerCommandSource source = context.getSource();
            ServerPlayerEntity technician = source.getPlayer();
            String technician_uuid = technician.getUuidAsString();

            if (!dataHandler.checkTEntry(technician_uuid)) {
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.permissions_too_low"), false);
                return Command.SINGLE_SUCCESS;
            }
            source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.remove"), false);
            return Command.SINGLE_SUCCESS;
        }

            public static int removeTechnician(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                ServerCommandSource source = context.getSource();
                ServerPlayerEntity technician = source.getPlayer();
                String technician_uuid = technician.getUuidAsString();

                if (!dataHandler.checkTEntry(technician_uuid)) {
                    source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.get.key.permissions_too_low"), false);
                    return Command.SINGLE_SUCCESS;
                }
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.remove.technician"), false);
                return Command.SINGLE_SUCCESS;
            }

            public static int removeTechnicianArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                ServerCommandSource source = context.getSource();
                ServerPlayerEntity technician = source.getPlayer();
                String technician_uuid = technician.getUuidAsString();
                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                String player_uuid = player.getUuidAsString();
                ServerAdvancementLoader loader = source.getServer().getAdvancementLoader();

                if (!dataHandler.checkTEntry(technician_uuid)) {
                    source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.remove.technician.permissions_too_low"), false);
                    return Command.SINGLE_SUCCESS;
                }

                if (!dataHandler.checkTEntry(player_uuid)) {
                    source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.remove.technician.target_not_technician"), false);
                    return Command.SINGLE_SUCCESS;
                }

                if (technician_uuid.compareTo(player_uuid) == 0) {
                    
                    dataHandler.removeTEntry(player_uuid);
                    removePerms(player, loader);
                    source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.remove.technician.remove_success"), false);
                    return Command.SINGLE_SUCCESS;
                }

                tData technician_data = dataHandler.data.techData.get(technician_uuid);
                if (!technician_data.isHeadTechnician) {
                    source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.remove.technician.permissions_too_low"), false);
                    return Command.SINGLE_SUCCESS;
                }

                dataHandler.removeTEntry(player_uuid);
                removePerms(player, loader);
                source.sendFeedback(new TranslatableText("command."+Main.identifier+".data.remove.technician.remove_success"), false);

                return Command.SINGLE_SUCCESS;
            }

}
