package zener.zcomm.components;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import zener.zcomm.Main;
import zener.zcomm.data.playerData;
import zener.zcomm.util.inventoryUtils;

@SuppressWarnings("deprecation")
public interface ICommRegistryComponent extends ComponentV3 {
    
    Map<UUID, Comm> getCOMMLIST();
    World getWorld();

    Map<String, OldComm> getOLD_COMMS();

    boolean isSyncedOldData();

    public class Comm {
        @Getter private final UUID USER_UUID;
        @Getter private final int NR;
        @Getter @Setter private ItemStack COMM;

        public Comm(@Nullable UUID user_uuid, int nr, ItemStack comm) {
            USER_UUID = user_uuid;
            NR = nr;
            COMM = comm;
        }
    }

    @Deprecated
    public class OldComm extends Comm {

        private static ItemStack getcomm(String UUID, playerData player) {
            ItemStack comm = new ItemStack(Main.ZCOMM);
            comm.setNbt(new NbtCompound() {{
                put("Inventory", inventoryUtils.fromPlayerData(player));
                putInt("NR", Integer.parseInt(player.COMM_NR));
                putString("UUID", UUID);
            }});
            return comm;
        }

        @Getter private final String STRING_UUID;

        public OldComm(String UUID, playerData player) {
            super(
                null, 
                Integer.parseInt(player.COMM_NR), 
                getcomm(UUID, player)
            );
            STRING_UUID = player.USER_ID;
        }

        public OldComm(String UUID, int nr, ItemStack comm) {
            super(null, nr, comm);
            STRING_UUID = UUID;
        }

        public OldComm(UUID UUID, int nr, ItemStack comm) {
            super(UUID, nr, comm);
            STRING_UUID = UUID.toString();
        }
    }

}
