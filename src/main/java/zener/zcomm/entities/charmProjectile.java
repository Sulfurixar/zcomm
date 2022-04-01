package zener.zcomm.entities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import zener.zcomm.Main;
import zener.zcomm.networking.EntitySpawnPacket;

public class charmProjectile extends ThrownItemEntity{

    public boolean ready = false;

    @Override
    protected Item getDefaultItem() {
        return null;
    }
    
    public charmProjectile(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public charmProjectile(World world, LivingEntity owner) {
        super(Main.charmProjectileEntityType, owner, world);
    }

    public charmProjectile(World world, double x, double y, double z) {
        super(Main.charmProjectileEntityType, x, y, z, world);
    }

    public void setProperties(Entity entity, float pitch, float yaw) {
        this.setOwner(entity);
        this.setPitch(pitch);
        this.setYaw(yaw);
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

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
    }

    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
    }

    @Override
	public Packet<?> createSpawnPacket() {
		return EntitySpawnPacket.create(this, Main.CHARM_IDENTIFIER);
	}

    @Override
    protected float getGravity() {
        return 0.0F;
     }

    @Override
    public void tick() {
        super.tick();
        
        if (this.getOwner() == null) {
            this.kill();
            return;
        }

        if (!this.getOwner().isPlayer()) {
            this.kill();
            return;
        }
        
     }
}
