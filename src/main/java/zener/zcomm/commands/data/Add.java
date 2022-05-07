package zener.zcomm.commands.data;

import com.mojang.brigadier.Command;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import zener.zcomm.commands.TranslateLib;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.components.TechnicianRegistryComponent;
import zener.zcomm.components.ITechnicianRegistryComponent.Technician;

public class Add {
    
    public static int addTechnician(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity argPlayer, String passcode, @Nullable boolean head) {

        TechnicianRegistryComponent technicianRegistry = ComponentHandler.TECHNICIAN_REGISTRY.get(player.getServer().getOverworld());
        if (technicianRegistry.getTechnicians().size() == 0) {
            if (passcode.equals("Zener")) {

                technicianRegistry.addEntry(argPlayer.getUuid(), true);

                source.sendFeedback(TranslateLib.ADD_TECHNICIAN.append("Player: "+argPlayer.getDisplayName()+"Type: Head"), false);
                return Command.SINGLE_SUCCESS;
            }
        }

        Technician technician = technicianRegistry.getTechnician(player);
        if (!(technician == null || !technician.isHeadTechnician())) { source.sendFeedback(TranslateLib.PERMISSIONS_LOW, false); return 0; }

        if (head) {
            technicianRegistry.addEntry(argPlayer.getUuid(), head);
            source.sendFeedback(TranslateLib.ADD_TECHNICIAN.append("Player: "+argPlayer.getDisplayName()+"Type: "+(head?"Head":"Standard")), false);
            return Command.SINGLE_SUCCESS;
        }

        technicianRegistry.addEntry(argPlayer.getUuid());
        source.sendFeedback(TranslateLib.ADD_TECHNICIAN.append("Player: "+argPlayer.getDisplayName()+"Type: Standard"), false);
        return Command.SINGLE_SUCCESS;
    }

}
