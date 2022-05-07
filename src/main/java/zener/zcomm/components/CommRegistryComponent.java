package zener.zcomm.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


import blue.endless.jankson.annotation.Nullable;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import lombok.Data;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import zener.zcomm.Main;
import zener.zcomm.data.playerData;

@SuppressWarnings("deprecation")
@Data
public class CommRegistryComponent implements ICommRegistryComponent, AutoSyncedComponent {

    private final Map<UUID, Comm> COMMLIST = new HashMap<>();
    private final World world;

    private boolean syncedOldData;

    @Deprecated
    private final Map<String, OldComm> OLD_COMMS = new HashMap<>();

    public CommRegistryComponent(World world) {
        this.world = world;
    }

    public boolean isNrFree(int nr) {
        List<? extends Comm> comms = COMMLIST.values().stream().filter(comm -> comm.getNR() == nr).toList();
        if (!comms.isEmpty()) {
            comms = OLD_COMMS.values().stream().filter(comm -> comm.getNR() == nr).toList();
            return !comms.isEmpty();
        }
        return comms.isEmpty();
    }

    public void removeComm(String uuid) {
        List<UUID> matches = COMMLIST.keySet().stream().filter(key -> key.toString().equals(uuid)).toList();
        if (matches != null && !matches.isEmpty()) {
            COMMLIST.remove(matches.get(0));
            return;
        }
        List<String> matches2 = OLD_COMMS.keySet().stream().filter(key -> key.equals(uuid)).toList();
        if (matches2 != null && !matches2.isEmpty()) {
            OLD_COMMS.remove(matches2.get(0));
        }
    }

    @Deprecated
    private @Nullable <C extends Comm, D> C getCommByString(Map<D, C> list, String UUID) {
        List<C> matches = list.entrySet().stream().filter(entry -> entry.getKey().toString().equals(UUID)).map(Map.Entry::getValue).collect(Collectors.toList());
        if (!matches.isEmpty()) {
            return matches.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public @Nullable <C extends Comm> C getComm(String uuid) {

        C match = (C) getCommByString(COMMLIST, uuid);
        if (match != null) return match;
        match = (C) getCommByString(OLD_COMMS, uuid);
        return match;
    }

    @Deprecated
    public void addEntry(String uuid, playerData player) {
        Main.LOGGER.info("Added Comm: "+uuid);
        OLD_COMMS.put(uuid, (new OldComm(uuid, player)));
    }

    @Deprecated
    private @Nullable <C extends Comm, D> C getCommByUUID(Map<UUID, C> list, UUID UUID) {
        List<C> matches = list.entrySet().stream().filter(entry -> entry.getKey().equals(UUID)).map(Map.Entry::getValue).collect(Collectors.toList());
        if (!matches.isEmpty()) {
            return matches.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public @Nullable <C extends Comm> C getComm(UUID uuid) {
        C match = (C) getCommByUUID(COMMLIST, uuid);
        if (match != null) return match;
        match = (C) getCommByString(OLD_COMMS, uuid.toString());
        return match;
    }

    public void addEntry(UUID uuid, Comm comm) {
        Main.LOGGER.info("Added Comm: "+uuid);
        COMMLIST.put(uuid, comm);
    }

    public UUID findUniqueEntry() {
        while (true) {
            UUID uuid = UUID.randomUUID();
            if (getComm(uuid) == null) {
                return uuid;
            }
        }
    }

    @Deprecated
    public void updateEntry(String uuid, ServerPlayerEntity player, ItemStack stack) {
        OldComm match = getCommByString(OLD_COMMS, uuid);
        if (match == null) {
            UUID UUID = findUniqueEntry();
            stack.getNbt().putUuid("UUID", UUID);
            int nr = stack.getNbt().getInt("NR");
            Comm new_comm = new Comm(player.getUuid(), nr, stack);
            COMMLIST.put(UUID, new_comm);
            return;
        }
        match.setCOMM(stack);
        OLD_COMMS.replace(uuid, match);
    }

    public void updateEntry(UUID uuid, ServerPlayerEntity player, ItemStack stack) {
        Comm match = getComm(uuid);
        if (match == null) {
            UUID UUID = findUniqueEntry();
            stack.getNbt().putUuid("UUID", UUID);
            int nr = stack.getNbt().getInt("NR");
            Comm new_comm = new Comm(player.getUuid(), nr, stack);
            COMMLIST.put(UUID, new_comm);
            return;
        }
        match.setCOMM(stack);
        COMMLIST.replace(uuid, match);
    }

    @Deprecated
    public boolean isOwner(String commID, String playerID) {
        OldComm match = getComm(commID);
        if (match.getUSER_UUID() != null) {
            return playerID.equals(match.getUSER_UUID().toString());
        }
        return playerID.equals(match.getSTRING_UUID());
    }

    @Deprecated
    public boolean isOwner(String commID, UUID playerID) {
        OldComm match = getComm(commID);
        if (match == null) { return false; }
        if (match.getUSER_UUID() != null) {
            return playerID.equals(match.getUSER_UUID());
        }
        return playerID.toString().equals(match.getSTRING_UUID());
    }

    public boolean isOwner(UUID commID, UUID playerID) {
        Comm match = getComm(commID);
        if (match == null) { return false; }
        return playerID.equals(match.getUSER_UUID());
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.get("comm_count") != null && tag.get("comm_count").getType() == NbtType.INT) {
            int commCount = tag.getInt("comm_count");
            NbtList list = tag.getList("comms", NbtType.COMPOUND);
            for (int i = 0; i < commCount; i++) {
                NbtCompound t = list.getCompound(i);
                UUID commID = t.getUuid("comm_id");
                UUID userID = t.getUuid("user_id");
                int nr = t.getInt("nr");
                NbtCompound commPound = t.getCompound("comm_item");
                ItemStack commItem = ItemStack.fromNbt(commPound);
                
                Comm COMM = new Comm(userID, nr, commItem);
                COMMLIST.put(commID, COMM);
            }
            syncedOldData = tag.getBoolean("is_synced");
        }
        if (tag.get("legacy_comm_count") != null && tag.get("legacy_comm_count").getType() == NbtType.INT) { 
            int commCount = tag.getInt("legacy_comm_count");
            NbtList legacy_list = tag.getList("legacy_comms", NbtType.COMPOUND);
            for (int i = 0; i < commCount; i++) {
                NbtCompound t = legacy_list.getCompound(i);
                String commID = t.getString("comm_id");
                
                int nr = t.getInt("nr");
                NbtCompound commPound = t.getCompound("comm_item");
                ItemStack commItem = ItemStack.fromNbt(commPound);
                
                OldComm COMM;
                if (t.get("user_id").getType() == NbtType.STRING) {
                    String userID = t.getString("user_id");
                    COMM = new OldComm(userID, nr, commItem);
                } else {
                    UUID userID = t.getUuid("user_id");
                    COMM = new OldComm(userID, nr, commItem);
                }
                OLD_COMMS.put(commID, COMM);
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("comm_count", COMMLIST.size());
        tag.putBoolean("is_synced", syncedOldData);
        NbtList list = new NbtList();
        COMMLIST.forEach((uuid, comm) -> {
            NbtCompound t = new NbtCompound();
            t.putUuid("comm_id", uuid);
            t.putUuid("user_id", comm.getUSER_UUID());
            t.putInt("nr", comm.getNR());
            NbtCompound commItem = new NbtCompound();
            comm.getCOMM().writeNbt(commItem);
            t.put("comm_item", commItem);
            list.add(t);
        });
        tag.put("comms", list);
        tag.putInt("legacy_comm_count", OLD_COMMS.size());
        NbtList legacy_list = new NbtList();
        OLD_COMMS.forEach((uuid, comm) -> {
            NbtCompound t = new NbtCompound();
            t.putString("comm_id", uuid);
            if (comm.getUSER_UUID() == null) {
                t.putString("user_id", comm.getSTRING_UUID());
            } else {
                t.putUuid("user_id", comm.getUSER_UUID());
            }
            t.putInt("nr", comm.getNR());
            NbtCompound commItem = new NbtCompound();
            comm.getCOMM().writeNbt(commItem);
            t.put("comm_item", commItem);
            legacy_list.add(t);
        });
        tag.put("legacy_comms", legacy_list);
    }

}
