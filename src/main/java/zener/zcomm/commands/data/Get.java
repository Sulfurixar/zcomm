package zener.zcomm.commands.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.brigadier.Command;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import zener.zcomm.Main;
import zener.zcomm.commands.Retrieve;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.components.ICommRegistryComponent.Comm;
import zener.zcomm.components.ITechnicianRegistryComponent.Technician;
import zener.zcomm.util.channelEncoder;

public class Get {
    public static <C extends Comm> int getPlayer(ServerCommandSource source, ServerPlayerEntity argPlayer) {
        Map<String, C> commList = Retrieve.getComms(source, new Retrieve.PlayerUUIDPredicate<C>(argPlayer.getUuid()));
        Technician argTechnician = ComponentHandler.TECHNICIAN_REGISTRY.get(source.getServer().getOverworld()).getTechnician(argPlayer);

        source.sendFeedback(new TranslatableText("command."+Main.ID+".technician_status")
            .append(
                "Technician: "+(argTechnician.isTechnician() ? "True" : "False")+
                "; Head Technician: "+(argTechnician.isHeadTechnician() ? "True" : "False"
            )), false);
        source.sendFeedback(new TranslatableText("command."+Main.ID+".technician_key")
            .append(Technician.get_hash(argPlayer.getUuid())), 
            false);
        commList.forEach((k, v) -> {
            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.found_comm")
            .append("NR: "+v.getNR()+"; UUID: "+k), false);
        });
        return Command.SINGLE_SUCCESS;
    }  
    
    public static int getListener(ServerCommandSource source, NbtElement listener) {
        if (listener.getType() != NbtType.LIST && listener.getType() != NbtType.INT && listener.getType() != NbtType.INT_ARRAY && listener.getType() != NbtType.STRING) {
            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.incorrect_datatype"), false);
            return Command.SINGLE_SUCCESS;
        }

        List<Integer> range = new ArrayList<Integer>();

        if (listener.getType() == NbtType.STRING) {
            String data = listener.asString();
            int dt = determineDatatype(data);
            switch (dt) {

                case 1:
                    NumberRange<Integer> _range = getNumberRange(data);
                    List<Integer> __range = IntStream.range(getContainedInt(_range.getMin()), getContainedInt(_range.getMax()+1)).boxed().collect(Collectors.toList());
                    range = Stream.of(range, __range).flatMap(Collection::stream).collect(Collectors.toList());
                    break;

                case 2:
                    range.add(Integer.parseInt(data));
                    break;

                default:
                    source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.incorrect_datatype").append(": " + data), false);
                    return Command.SINGLE_SUCCESS;
            }

            channelEncoder.Encoding cencoder = new channelEncoder().getEncoding(range.stream().mapToInt(i->i).toArray());
            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.result").append(": " + cencoder.getCodec()), false);
            return Command.SINGLE_SUCCESS;
        }

        if (listener.getType() == NbtType.INT) {
            int v = ((NbtInt)listener).intValue();
            if (!isValidNr(v)) {
                source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.incorrect_datatype").append(": " + v), false);
                return Command.SINGLE_SUCCESS;
            }
            range.add(v);
            channelEncoder.Encoding cencoder = new channelEncoder().getEncoding(range.stream().mapToInt(i->i).toArray());
            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.result").append(": " + cencoder.getCodec()), false);
            return Command.SINGLE_SUCCESS;
        }

        if (listener.getType() == NbtType.INT_ARRAY) {
            range = Arrays.stream(((NbtIntArray)listener).stream().mapToInt(i->i.intValue()).toArray()).filter(i -> i >= 0 && i < 1000).boxed().collect(Collectors.toList());
            channelEncoder.Encoding cencoder = new channelEncoder().getEncoding(range.stream().mapToInt(i->i).toArray());
            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.result").append(": " + cencoder.getCodec()), false);
            return Command.SINGLE_SUCCESS;
        }

        if (listener.getType() == NbtType.LIST) {
            for (NbtElement e : (NbtList)listener) {
                if (e.getType() != NbtType.INT && e.getType() != NbtType.STRING) {
                    source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.incorrect_datatype").append(": " + e.asString()), false);
                    return Command.SINGLE_SUCCESS;
                }
                if (e.getType() == NbtType.INT) {
                    int v = ((NbtInt)e).intValue();
                    if (!isValidNr(v)) {
                        source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.incorrect_datatype").append(": " + v), false);
                        return Command.SINGLE_SUCCESS;
                    }
                    range.add(v);
                }
                
                if (e.getType() == NbtType.STRING) {
                    String data = e.asString();
                    int dt = determineDatatype(data);
                    switch (dt) {

                        case 1:
                            NumberRange<Integer> _range = getNumberRange(data);
                            List<Integer> __range = IntStream.range(getContainedInt(_range.getMin()), getContainedInt(_range.getMax()+1)).boxed().collect(Collectors.toList());
                            range = Stream.of(range, __range).flatMap(Collection::stream).collect(Collectors.toList());
                            break;

                        case 2:
                            range.add(Integer.parseInt(data));
                            break;

                        default:
                            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.incorrect_datatype").append(": " + data), false);
                            return Command.SINGLE_SUCCESS;
                    }
                }
            }
            channelEncoder.Encoding cencoder = new channelEncoder().getEncoding(range.stream().mapToInt(i->i).toArray());
            source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener.result").append(": " + cencoder.getCodec()), false);
            return Command.SINGLE_SUCCESS;
        }

        source.sendFeedback(new TranslatableText("command."+Main.ID+".data.get.listener"), false);
        return Command.SINGLE_SUCCESS;
    }

    // 0 - not a NumberRange or Integer, 1 - NumberRange, 2 - Integer
    private static int determineDatatype(String data) {
        if (data.contains("..")) {
            String[] nrs = data.split("\\.\\.");
            if (nrs.length != 2) return 0;
            for (String nr : nrs) {
                try {
                    int a = Integer.parseInt(nr);
                    if (!isValidNr(a)) return 0;
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 1;
        }
        try {
            int a = Integer.parseInt(data);
            if (!isValidNr(a)) return 0;
            return 2;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static NumberRange<Integer> getNumberRange(String data) {
        String[] nrs = data.split("\\.\\.");
        int nr0 = Integer.parseInt(nrs[0]);
        int nr1 = Integer.parseInt(nrs[1]);
        NumberRange<Integer> range = new NumberRange<Integer>(nr0, nr1) {};
        return range;
    }

    private static int getContainedInt(int val) {
        return  Math.max(Math.min(val, 999), 0);
    }

    private static boolean isValidNr(int nr) {
        return nr >= 0 && nr < 1000;
    }

}
