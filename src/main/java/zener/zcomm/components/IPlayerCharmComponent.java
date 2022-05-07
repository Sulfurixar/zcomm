package zener.zcomm.components;

import java.util.List;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.entity.player.PlayerEntity;
import zener.zcomm.entities.CharmProjectile;

public interface IPlayerCharmComponent extends ComponentV3 {
    
    PlayerEntity getPlayer();
    List<CharmProjectile> getCharms();

}