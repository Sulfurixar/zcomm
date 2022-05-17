package zener.zcomm.components;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import blue.endless.jankson.annotation.Nullable;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import lombok.Data;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Box;
import zener.zcomm.Main;
import zener.zcomm.entities.CharmProjectile;

@Data
public class PlayerCharmComponent implements IPlayerCharmComponent, AutoSyncedComponent {
    private final PlayerEntity player;
    private List<CharmProjectile> charms = new ArrayList<>();

    public PlayerCharmComponent(PlayerEntity player) {
        this.player = player;
    }

    private int getCustomData(ItemStack charm) {
        NbtCompound ctag = charm.getOrCreateNbt();
        if (ctag.contains("CustomModelData")) {
            NbtElement customData = ctag.get("CustomModelData");
            if (customData.getType() == NbtType.BYTE || customData.getType() == NbtType.INT) {
                return ctag.getInt("CustomModelData");
            }
        }
        return 0;
    }

    public static ItemStack getCharmStack(ItemStack comm) {
        ItemStack charmStack;
        if (comm.getOrCreateNbt().contains("Inventory")) {
            NbtList tag = comm.getNbt().getList("Inventory", NbtType.COMPOUND);
            NbtCompound stackTag = (NbtCompound) tag.getCompound(0);
            if (stackTag != null) {
                charmStack = ItemStack.fromNbt(stackTag.getCompound("Stack"));
                if (charmStack.isOf(Main.CHARM)) {
                    return charmStack;
                }
            }
        }
        return null;
    }

    @Data
    private class fBool {
        boolean found = false;
        public void toggle() {
            found = !found;
        }
    }

    public CharmProjectile updateCharms(@Nullable PlayerEntity entity) {
        List<CharmProjectile> charmChecker = new ArrayList<>();
        List<CharmProjectile> newCharms = new ArrayList<>();
        fBool found = new fBool();
        CharmProjectile charm = new CharmProjectile(player.getEntityWorld(), (LivingEntity) player);
        charms.forEach(c -> {
            charmChecker.add(c);
        });

        PlayerEntity player;
        if (entity != null) {
            player = entity;
        } else {
            player = this.player;
        }

        player.getItemsHand().forEach(comm -> {

            final ItemStack charmStack = getCharmStack(comm);
            int prevSize = newCharms.size();
            if (charmStack != null) {
                charmChecker.forEach(c -> {
                    if (prevSize == newCharms.size()) {
                        int cdat = getCustomData(c.getStack());
                        int charmDat = getCustomData(charmStack);
                        if (cdat == charmDat) {
                            if (!newCharms.contains(c)) newCharms.add(c);
                        }
                    }
                });
                if (prevSize == newCharms.size()) {
                    charm.setItem(charmStack);
                    if (player.getEntityWorld().spawnEntity(charm)) {
                        CharmComponent charmComponent = ComponentHandler.CHARM_KEY.get(charm);
                        charmComponent.setOwner(player);
                        newCharms.add(charm);
                        found.toggle();
                    }
                }
            }
        });

        charms = newCharms;
        if(found.isFound()) {
            return charm;
        } else {
            return null;
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.get("charms") == null) return;
        NbtList charms = tag.getList("charms", NbtType.INT_ARRAY);
        charms.forEach(charm -> {
            UUID uuid = NbtHelper.toUuid(charm);
            double x1 = player.getPos().getX()-10, x2 = player.getPos().getX()+10, y1 = player.getPos().getY()-10, y2 = player.getPos().getY()+10,
            z1 = player.getPos().getZ()-10, z2 = player.getPos().getZ()+10;
            List<CharmProjectile> foundCharms = player.world.getEntitiesByType(Main.charmProjectileEntityType, new Box(x1, y1, z1, x2, y2, z2), entity -> entity.getUuid().equals(uuid));
            if (!foundCharms.isEmpty()) {
                List<CharmProjectile> hadCharm = this.charms.stream().filter(c -> c.getUuid().equals(uuid)).toList();
                if (hadCharm.isEmpty()) {
                    this.charms.add(foundCharms.get(0));
                }
            }
        });
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (charms.isEmpty()) return;
        NbtList charms = new NbtList();
        this.charms.forEach(charm -> {
            charms.add(NbtHelper.fromUuid(charm.getUuid()));
        });
        tag.put("charms", charms);
    }
}
