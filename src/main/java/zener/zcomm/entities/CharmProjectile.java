package zener.zcomm.entities;

import java.util.UUID;

import com.google.common.base.MoreObjects;

import blue.endless.jankson.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import zener.zcomm.Main;
import zener.zcomm.components.CharmComponent;
import zener.zcomm.components.ComponentHandler;
import zener.zcomm.components.PlayerCharmComponent;

public class CharmProjectile extends Entity implements FlyingItemEntity{

    @Nullable
    private UUID ownerUuid;
    @Nullable
    private Entity owner;
    private boolean leftOwner;
    private boolean shot;
    private int despawnTimer = 30;

    public CharmProjectile(EntityType<? extends CharmProjectile> type, World world) {
        super(type, world);
    }

    public CharmProjectile(World world, LivingEntity owner) {
        this(Main.charmProjectileEntityType, owner, world);
    }

    public CharmProjectile(World world, double x, double y, double z) {
        this(Main.charmProjectileEntityType, x, y, z, world);
    }

    protected Item getDefaultItem() {
        return Main.CHARM;
    }

    @Environment(EnvType.CLIENT)
    private ParticleEffect getParticleParameters() {
        ItemStack itemStack = this.getItem();
        ParticleEffect particle = (ParticleEffect)(itemStack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack));
        return particle;
    }

    @Environment(EnvType.CLIENT)
    public void handleStatus(byte status) {
        if (status == 3) {
            ParticleEffect particleEffect = this.getParticleParameters();
            for (int i = 0; i < 8; i++) {
                this.world.addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void tick() {
        CharmComponent charm = ComponentHandler.CHARM_KEY.get(this);
        if (charm.isSynced()) {
            PlayerEntity player = charm.getPlayer();
            if (!exists(player)) {
                System.out.println("Exists: "+exists(player));
                discard();
                return;
            }
            doMovement(player);
            _tick();
            
        } else {
            despawnTimer--;
            if (despawnTimer < 1) {
                discard();
                return;
            }
        }
    }

    private boolean exists(PlayerEntity player) {
        if (player == null) { return false; }
        PlayerCharmComponent playerCharms = ComponentHandler.PLAYER_CHARM_KEY.get(player);
        boolean has_charm = !(playerCharms.getCharms().stream().filter(charm -> {
            return charm.getUuid().equals(getUuid());
        }).toList().isEmpty());
        return has_charm;
    }

    private void doMovement(PlayerEntity player) {

        double dist = 0.5;

        double rad = ((player.bodyYaw + 180)) % 360 * Math.PI / 180;
        double rotatedX = dist * Math.cos(rad) - 0*dist * Math.sin(rad);
        double rotatedZ = 0*dist * Math.cos(rad) + dist * Math.sin(rad);
        if (player.getMainHandStack().isOf(Main.ZCOMM)) {
            ItemStack charmItem = PlayerCharmComponent.getCharmStack(player.getMainHandStack());
            if (charmItem != null) {
                if (getStack().isItemEqual(charmItem)) {
                    rad = ((player.bodyYaw + 180)-60) % 360 * Math.PI / 180;
                    rotatedX = (dist * Math.cos(rad) + 0*dist * Math.sin(rad));
                    rotatedZ = (0*dist * Math.cos(rad) - dist * Math.sin(rad));
                }
            }
        }

        /// POSITION WE WANT TO REACH
        double posx = player.getX() + rotatedX;
        double posy = player.getY() + 0.6D * player.getHeight() / 2;
        double posz = player.getZ() + rotatedZ;

        /// DISTANCE FROM POSITION
        double x = (posx - getX());
        double y = (posy - getY());
        double z = (posz - getZ());
        
        float speed = (float)Math.pow(Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z))), 2);
        if (speed < 0.005 && (getX() != posx || getY() != posy || getZ() != posz)) {
            //speed = 0.001f;
        }
        if (Math.abs(getX()-x) < 0.1 && Math.abs(getY()-y) < 0.1 && Math.abs(getZ() - z) < 0.1 ) {
            setVelocity(x, y, z, 0.1f, 0);
        } else {
            setVelocity(x, y, z, speed, 0.00F);
        }

        float g;
        Object blockPos;
        blockPos = this.getVelocity();
        double blockState = this.getX() + ((Vec3d)blockPos).x;
        double d = this.getY() + ((Vec3d)blockPos).y;
        double e = this.getZ() + ((Vec3d)blockPos).z;
        this.updateRotation();

        if (this.isTouchingWater()) {
            for (int i = 0; i < 4; ++i) {
                float f = 0.25f;
                this.world.addParticle(ParticleTypes.BUBBLE, blockState - ((Vec3d)blockPos).x * 0.25, d - ((Vec3d)blockPos).y * 0.25, e - ((Vec3d)blockPos).z * 0.25, ((Vec3d)blockPos).x, ((Vec3d)blockPos).y, ((Vec3d)blockPos).z);
            }
            g = 0.8f;
        } else {
            g = 0.99f;
        }

        this.setVelocity(((Vec3d)blockPos).multiply(g));

        this.setPosition(blockState, d, e);

    }

    // FROM ThrownItemEntity.class
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(CharmProjectile.class, TrackedDataHandlerRegistry.ITEM_STACK);

    public void setItem(ItemStack item) {
        if (!item.isOf(this.getDefaultItem()) || item.hasNbt()) {
            this.getDataTracker().set(ITEM, Util.make(item.copy(), stack -> stack.setCount(1)));
        }
    }

    protected ItemStack getItem() {
        return this.getDataTracker().get(ITEM);
    }

    @Override
    public ItemStack getStack() {
        ItemStack itemStack = this.getItem();
        return itemStack.isEmpty() ? new ItemStack(this.getDefaultItem()) : itemStack;
    }

    @Override
    protected void initDataTracker() {
        this.getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        _writeCustomDataToNbt(nbt);
        ItemStack itemStack = this.getItem();
        if (!itemStack.isEmpty()) {
            nbt.put("Item", itemStack.writeNbt(new NbtCompound()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        _readCustomDataFromNbt(nbt);
        ItemStack itemStack = ItemStack.fromNbt(nbt.getCompound("Item"));
        this.setItem(itemStack);
    }

    // FROM ThrownEntity.class

    public CharmProjectile(EntityType<? extends CharmProjectile> type, double x, double y, double z, World world) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    public CharmProjectile(EntityType<? extends CharmProjectile> type, LivingEntity owner, World world) {
        this(type, owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ(), world);
        this.setOwner(owner);
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(d)) {
            d = 4.0;
        }
        return distance < (d *= 64.0) * d;
    }

    protected float getGravity() {
        return 0.00f;
    }

    // FROM ProjectileEntity.class
    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUuid = entity.getUuid();
            this.owner = entity;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.owner != null && !this.owner.isRemoved()) {
            return this.owner;
        }
        if (this.ownerUuid != null && this.world instanceof ServerWorld) {
            this.owner = ((ServerWorld)this.world).getEntity(this.ownerUuid);
            return this.owner;
        }
        return null;
    }

    /**
     * {@return the cause entity of any effect applied by this projectile} If this
     * projectile has an owner, the effect is attributed to the owner; otherwise, it
     * is attributed to this projectile itself.
     */
    public Entity getEffectCause() {
        return MoreObjects.firstNonNull(this.getOwner(), this);
    }

    protected void _writeCustomDataToNbt(NbtCompound nbt) {
        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", this.ownerUuid);
        }
        if (this.leftOwner) {
            nbt.putBoolean("LeftOwner", true);
        }
        nbt.putBoolean("HasBeenShot", this.shot);
    }

    protected boolean isOwner(Entity entity) {
        return entity.getUuid().equals(this.ownerUuid);
    }

    protected void _readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
        }
        this.leftOwner = nbt.getBoolean("LeftOwner");
        this.shot = nbt.getBoolean("HasBeenShot");
    }

    public void _tick() {

        if (!this.shot) {
            this.emitGameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner(), this.getBlockPos());
            this.shot = true;
        }
        if (!this.leftOwner) {
            this.leftOwner = this.shouldLeaveOwner();
        }
        super.tick();
    }

    private boolean shouldLeaveOwner() {
        Entity entity2 = this.getOwner();
        if (entity2 != null) {
            for (Entity entity22 : this.world.getOtherEntities(this, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0), entity -> !entity.isSpectator() && entity.collides())) {
                if (entity22.getRootVehicle() != entity2.getRootVehicle()) continue;
                return false;
            }
        }
        return true;
    }

    public void setVelocity(double x, double y, double z, float speed, float divergence) {
        Vec3d vec3d = new Vec3d(x, y, z).normalize().add(this.random.nextGaussian() * (double)0.0075f * (double)divergence, this.random.nextGaussian() * (double)0.0075f * (double)divergence, this.random.nextGaussian() * (double)0.0075f * (double)divergence).multiply(speed);
        this.setVelocity(vec3d);
        double d = vec3d.horizontalLength();
        this.setYaw((float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875));
        this.setPitch((float)(MathHelper.atan2(vec3d.y, d) * 57.2957763671875));
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
    }

    public void setProperties(Entity user, float pitch, float yaw, float roll, float modifierZ, float modifierXYZ) {
        float f = -MathHelper.sin(yaw * ((float)Math.PI / 180)) * MathHelper.cos(pitch * ((float)Math.PI / 180));
        float g = -MathHelper.sin((pitch + roll) * ((float)Math.PI / 180));
        float h = MathHelper.cos(yaw * ((float)Math.PI / 180)) * MathHelper.cos(pitch * ((float)Math.PI / 180));
        this.setVelocity(f, g, h, modifierZ, modifierXYZ);
        Vec3d vec3d = user.getVelocity();
        this.setVelocity(this.getVelocity().add(vec3d.x, user.isOnGround() ? 0.0 : vec3d.y, vec3d.z));
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        this.setVelocity(x, y, z);
        if (this.prevPitch == 0.0f && this.prevYaw == 0.0f) {
            double d = Math.sqrt(x * x + z * z);
            this.setPitch((float)(MathHelper.atan2(y, d) * 57.2957763671875));
            this.setYaw((float)(MathHelper.atan2(x, z) * 57.2957763671875));
            this.prevPitch = this.getPitch();
            this.prevYaw = this.getYaw();
            this.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        }
    }

    protected void updateRotation() {
        Vec3d vec3d = this.getVelocity();
        double d = vec3d.horizontalLength();
        this.setPitch(updateRotation(this.prevPitch, (float)(MathHelper.atan2(vec3d.y, d) * 57.2957763671875)));
        this.setYaw(updateRotation(this.prevYaw, (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875)));
    }

    protected static float updateRotation(float prevRot, float newRot) {
        while (newRot - prevRot < -180.0f) {
            prevRot -= 360.0f;
        }
        while (newRot - prevRot >= 180.0f) {
            prevRot += 360.0f;
        }
        return MathHelper.lerp(0.2f, prevRot, newRot);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        Entity entity = this.getOwner();
        return new EntitySpawnS2CPacket(this, entity == null ? 0 : entity.getId());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        Entity entity = this.world.getEntityById(packet.getEntityData());
        if (entity != null) {
            this.setOwner(entity);
        }
    }

    @Override
    public boolean canModifyAt(World world, BlockPos pos) {
        Entity entity = this.getOwner();
        if (entity instanceof PlayerEntity) {
            return entity.canModifyAt(world, pos);
        }
        return entity == null || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
    }
    
}
