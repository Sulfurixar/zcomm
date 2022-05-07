package zener.zcomm.commands.data;

import com.mojang.brigadier.Command;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;
import zener.zcomm.commands.TranslateLib;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.components.TechnicianRegistryComponent;
import zener.zcomm.components.ITechnicianRegistryComponent.Technician;

public class Remove {
    
    public static int removeTechnician(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity argPlayer) {

        TechnicianRegistryComponent technicianRegistry = ComponentHandler.TECHNICIAN_REGISTRY.get(player.getServer().getOverworld());
        Technician technician = technicianRegistry.getTechnician(player);
        if (technician == null || !technician.isHeadTechnician()) { source.sendFeedback(TranslateLib.PERMISSIONS_LOW, false); return 0; }
        
        Technician argTechnician = technicianRegistry.getTechnician(argPlayer);
        if (argTechnician == null) { source.sendFeedback(new TranslatableText("command."+Main.ID+".remove.technician.no_player"), false); return 0;}

        technicianRegistry.removeEntry(argPlayer.getUuid());
        source.sendFeedback(new TranslatableText("command."+Main.ID+".remove.technician.removed").append(argPlayer.getDisplayName()), false);

        return Command.SINGLE_SUCCESS;
    }

}
