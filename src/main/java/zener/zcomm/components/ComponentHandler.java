package zener.zcomm.components;

import java.util.UUID;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer.Factory;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import dev.onyxstudios.cca.internal.world.ComponentPersistentState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import zener.zcomm.Main;
import zener.zcomm.components.ICommRegistryComponent.Comm;
import zener.zcomm.entities.CharmProjectile;

public class ComponentHandler implements WorldComponentInitializer, EntityComponentInitializer {

    public static final ComponentKey<CommRegistryComponent> COMM_REGISTRY = ComponentRegistryV3.INSTANCE
    .getOrCreate(new Identifier(Main.ID, "comms"), CommRegistryComponent.class);

    public static final ComponentKey<TechnicianRegistryComponent> TECHNICIAN_REGISTRY = 
    ComponentRegistryV3.INSTANCE
    .getOrCreate(new Identifier(Main.ID, "technicians"), TechnicianRegistryComponent.class);

    public static final ComponentKey<CharmComponent> CHARM_KEY = ComponentRegistryV3.INSTANCE
    .getOrCreate(new Identifier(Main.ID, "charm"), CharmComponent.class);

    public static final ComponentKey<PlayerCharmComponent> PLAYER_CHARM_KEY = ComponentRegistryV3.INSTANCE
    .getOrCreate(new Identifier(Main.ID, "player_charm"), PlayerCharmComponent.class);

    public static ComponentPersistentState COMM_PERSISTENCE;
    public static ComponentPersistentState TECHNICIAN_PERSISTENCE;

    @SuppressWarnings("unchecked")
    public static <C extends ComponentV3> ComponentPersistentState createPersistence(C component) {
        Factory<C> factory = (Factory<C>) Factory.builder(component.getClass()).build();
        ComponentContainer container = factory.createContainer(component);
        return new ComponentPersistentState(container);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(COMM_REGISTRY, (world) -> { 
            CommRegistryComponent commRegistry = new CommRegistryComponent(world);
            COMM_PERSISTENCE = createPersistence(commRegistry);
            return commRegistry; 
        });
        registry.register(TECHNICIAN_REGISTRY, (world) -> new TechnicianRegistryComponent(world));
        registry.register(TECHNICIAN_REGISTRY, (world) -> {
            TechnicianRegistryComponent technicianRegistry = new TechnicianRegistryComponent(world);
            TECHNICIAN_PERSISTENCE = createPersistence(technicianRegistry);
            return technicianRegistry;
        });
    }

    @Deprecated
    public static <C extends Comm> C getComm(ServerPlayerEntity player, String uuid) {
        return COMM_REGISTRY.get(player.getServer().getOverworld()).getComm(uuid);
    }

    public static <C extends Comm> C getComm(ServerPlayerEntity player, UUID uuid) {
        return COMM_REGISTRY.get(player.getServer().getOverworld()).getComm(uuid);
    }

    public static UUID createUUID(ServerPlayerEntity player) {
        return COMM_REGISTRY.get(player.getServer().getOverworld()).findUniqueEntry();
    }

    @Deprecated
    public static void updateCommEntry(ServerPlayerEntity player, String uuid, ItemStack stack) {
        COMM_REGISTRY.get(player.getServer().getOverworld()).updateEntry(uuid, player, stack);
    }

    public static void updateCommEntry(ServerPlayerEntity player, UUID uuid, ItemStack stack) {
        COMM_REGISTRY.get(player.getServer().getOverworld()).updateEntry(uuid, player, stack);
    }

    public static boolean isNrFree(MinecraftServer server, int nr) {
        return COMM_REGISTRY.get(server.getOverworld()).isNrFree(nr);
    }

    public static void removeCommEntry(ServerPlayerEntity player, String uuid) {
        COMM_REGISTRY.get(player.getServer().getOverworld()).removeComm(uuid);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER_CHARM_KEY, player -> new PlayerCharmComponent(player), RespawnCopyStrategy.NEVER_COPY);
        registry.registerFor(CharmProjectile.class, CHARM_KEY, entity -> new CharmComponent(entity));     
    }
    
}
