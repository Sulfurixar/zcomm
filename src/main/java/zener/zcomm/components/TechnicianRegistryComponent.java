package zener.zcomm.components;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import blue.endless.jankson.annotation.Nullable;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import lombok.Data;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.World;
import zener.zcomm.Main;

@Data
public class TechnicianRegistryComponent implements ITechnicianRegistryComponent, AutoSyncedComponent {

    private final Map<UUID, Technician> technicians = new HashMap<>();
    private final World world;

    TechnicianRegistryComponent(World world) {
        this.world = world;
    }

    public void addEntry(UUID uuid) {
        addEntry(uuid, false);
    }

    public void addEntry(UUID uuid, boolean isHeadTechnician) {
        addEntry(uuid, true, isHeadTechnician);
    }

    public void addEntry(UUID uuid, boolean isTechnician, boolean isHeadTechnician) {
        Main.LOGGER.info("Added Technician: "+uuid+"Technician: "+(isTechnician?"True":"False")+"; Head Technician: "+(isHeadTechnician?"True":"False"));
        technicians.put(uuid, new Technician(isTechnician, isHeadTechnician));
    }

    public void removeEntry(UUID uuid) {
        Main.LOGGER.info("Removed Technician: "+uuid);
        technicians.remove(uuid);   
    }

    public @Nullable Technician getTechnician(UUID uuid) {
        return technicians.get(uuid);
    }

    public @Nullable Technician getTechnician(PlayerEntity player) {
        return technicians.get(player.getUuid());
    }

    public boolean isTechnician(UUID uuid) {
        return getTechnician(uuid).technician;
    }

    public boolean isTechnician(PlayerEntity player) {
        return getTechnician(player.getUuid()).technician;
    }

    public boolean isHeadTechnician(UUID uuid) {
        return getTechnician(uuid).headTechnician;
    }

    public boolean isHeadTechnician(PlayerEntity player) {
        return getTechnician(player.getUuid()).headTechnician;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.get("technician_count") == null || tag.get("technician_count").getType() != NbtType.INT) { return; }
        int technicianCount = tag.getInt("technician_count");
        NbtList list = tag.getList("technicians", NbtType.COMPOUND);
        for (int i = 0; i < technicianCount; i++) {
            NbtCompound t = list.getCompound(i);
            UUID player = t.getUuid("user_id");
            boolean technician = t.getBoolean("technician");
            boolean headTechnician = t.getBoolean("head_technician");

            Technician technician2 = new Technician(technician, headTechnician);
            technicians.put(player, technician2);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("technician_count", technicians.size());
        NbtList list = new NbtList();
        technicians.forEach((player, technician) -> {
            NbtCompound t = new NbtCompound();
            t.putUuid("user_id", player);
            t.putBoolean("technician", technician.technician);
            t.putBoolean("head_technician", technician.headTechnician);
            list.add(t);
        });
        tag.put("technicians", list);
    }
    
}
