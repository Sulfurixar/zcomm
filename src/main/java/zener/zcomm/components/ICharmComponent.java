package zener.zcomm.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.entity.player.PlayerEntity;
import zener.zcomm.entities.CharmProjectile;

public interface ICharmComponent extends ComponentV3 {
    
    PlayerEntity getPlayer();
    CharmProjectile getCharm();
    boolean isSynced();

}
