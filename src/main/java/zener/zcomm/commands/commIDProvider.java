package zener.zcomm.commands;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import zener.zcomm.data.dataHandler;

public class commIDProvider implements SuggestionProvider<ServerCommandSource> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        
            ServerCommandSource source = context.getSource();
            if (source.getEntity().isPlayer() && !source.getEntity().world.isClient()) {
                ServerPlayerEntity technician = (ServerPlayerEntity) source.getEntity();
                if (dataHandler.checkTEntry(technician.getUuidAsString())) {
                    dataHandler.data.commData.forEach((id, data) -> {
                        builder.suggest(id);
                    });
                }    
            }

            return builder.buildFuture();   
        }

    }