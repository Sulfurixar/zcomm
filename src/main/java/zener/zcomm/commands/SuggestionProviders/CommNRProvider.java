package zener.zcomm.commands.SuggestionProviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.ServerCommandSource;
import zener.zcomm.commands.CommandWrapper;
import zener.zcomm.components.CommRegistryComponent;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.components.ICommRegistryComponent.Comm;

public class CommNRProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
        CommandContext<ServerCommandSource> context, 
        SuggestionsBuilder builder
    ) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) { return builder.buildFuture(); }
        if (CommandWrapper.isValidTechnician(source, source.getPlayer())) { return builder.buildFuture(); }
        
        CommRegistryComponent commRegistry = ComponentHandler.COMM_REGISTRY.get(source.getServer().getOverworld());
        List<Integer> uniqueNRs = addNRsFromMap(addNRsFromMap(new ArrayList<>(), commRegistry.getCOMMLIST()), commRegistry.getOLD_COMMS());
        uniqueNRs.forEach(nr -> {
            builder.suggest(nr);
        });
        return builder.buildFuture();
    }

    private <C extends Comm> List<Integer> addNRsFromMap(List<Integer> list, Map<?, C> map) {
        map.values().forEach(c -> {
            int nr = c.getNR();
            if (!list.contains(nr)) { list.add(nr); }
        });
        return list;
    }
    
}
