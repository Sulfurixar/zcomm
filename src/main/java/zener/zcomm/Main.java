package zener.zcomm;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import zener.zcomm.commands.Command;
import zener.zcomm.data.dataHandler;
import zener.zcomm.entities.charmProjectile;
import zener.zcomm.gui.zcomm_inventory.InvGUIDescription;
import zener.zcomm.gui.zcomm_main.MainGUIDescription;
import zener.zcomm.gui.zcomm_nr.zcommNRGUIDescription;
import zener.zcomm.items.zcomm.casing;
import zener.zcomm.items.zcomm.charm;
import zener.zcomm.items.zcomm.comm;
import zener.zcomm.items.zcomm.craftingComponent;
import zener.zcomm.items.zcomm.handCrafter;
import zener.zcomm.items.zcomm.infuser;
import zener.zcomm.items.zcomm.upgrade;
import zener.zcomm.recipes.RecipeTypesRegistry;



public class Main implements ModInitializer {

	public static final String identifier = "zcomm";

    @Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		Registry.register(Registry.ITEM, ZCOMM_IDENTIFIER, ZCOMM);
		Registry.register(Registry.ITEM, CHARM_IDENTIFIER, CHARM);
		Registry.register(Registry.ITEM, CASING_IDENTIFIER, CASING);
		Registry.register(Registry.ITEM, UPGRADE_IDENTIFIER, UPGRADE);
		Registry.register(Registry.ITEM, CRAFTING_COMPONENT_IDENTIFIER, CRAFTING_COMPONENT);
		Registry.register(Registry.ITEM, INFUSER_IDENTIFIER, INFUSER);
		Registry.register(Registry.ITEM, HAND_CRAFTER_IDENTIFIER, HANDCRAFTER);

		RecipeTypesRegistry.registerAll();

		CommandRegistrationCallback.EVENT.register(Command::register);

		ServerLifecycleEvents.SERVER_STARTING.register(Main::serverLoad);

	}

	//// INTERFACE
	public static final Identifier ZCOMM_INVIDENTIFIER = new Identifier(identifier, "comm_inventory");
	public static final Identifier ZCOMM_NRIDENTIFIER = new Identifier(identifier, "comm_nr");
	public static final Identifier ZCOMM_MAINIDENTIFIER = new Identifier(identifier, "comm_main");
	public static final ScreenHandlerType<InvGUIDescription> ZCOMM_INV_SCREEN_TYPE = ScreenHandlerRegistry.registerExtended(ZCOMM_INVIDENTIFIER, InvGUIDescription::new);
	public static final ScreenHandlerType<zcommNRGUIDescription> ZCOMM_NR_SCREEN_TYPE = ScreenHandlerRegistry.registerExtended(ZCOMM_NRIDENTIFIER, zcommNRGUIDescription::new);
	public static final ScreenHandlerType<MainGUIDescription> ZCOMM_MAIN_SCREEN_TYPE = ScreenHandlerRegistry.registerExtended(ZCOMM_MAINIDENTIFIER, MainGUIDescription::new);

	//// UPGRADE STUFF
	public static int UPGRADE_MAXIMUM_DAMAGE = 100;

	//// ITEMS
	public static final Identifier ZCOMM_IDENTIFIER = new Identifier(identifier, "comm");
	public static final Identifier CHARM_IDENTIFIER = new Identifier(identifier, "charm");
	public static final Identifier CASING_IDENTIFIER = new Identifier(identifier, "casing");
	public static final Identifier UPGRADE_IDENTIFIER = new Identifier(identifier, "upgrade");
	public static final Identifier CRAFTING_COMPONENT_IDENTIFIER = new Identifier(identifier, "crafting_component");
	public static final Identifier INFUSER_IDENTIFIER = new Identifier(identifier, "infuser");
	public static final Identifier HAND_CRAFTER_IDENTIFIER = new Identifier(identifier, "handcrafter");
	public static final String ZCOMM_TRANSLATION_KEY = Util.createTranslationKey("container", ZCOMM_IDENTIFIER);
	public static final ItemGroup ZCOMM_GROUP = FabricItemGroupBuilder.create(
		new Identifier(identifier, "general"))
	.icon(() -> new ItemStack(new comm(new Item.Settings())))
	.build();

	public static Item ZCOMM = new comm(new Item.Settings().group(ZCOMM_GROUP));
	public static Item CHARM = new charm(new Item.Settings().group(ZCOMM_GROUP));
	public static Item CASING = new casing(new Item.Settings().group(ZCOMM_GROUP));
	public static Item UPGRADE = new upgrade(new Item.Settings().group(ZCOMM_GROUP).maxDamageIfAbsent(UPGRADE_MAXIMUM_DAMAGE));
	public static Item CRAFTING_COMPONENT = new craftingComponent(new Item.Settings().group(ZCOMM_GROUP));
	public static Item INFUSER = new infuser(new Item.Settings().group(ZCOMM_GROUP));
	public static Item HANDCRAFTER = new handCrafter(new Item.Settings().group(ZCOMM_GROUP));

	public static Item[] ITEMS = new Item[] {
		ZCOMM, CHARM, CASING, UPGRADE, CRAFTING_COMPONENT, INFUSER
	};

	//// GLOBAL VAR FOR MANAGING CHANNELS
	public static int GLOBAL_CHANNEL_NR = 71;
	public static String ZCOMM_COMMUNICATION_IDENTIFIER = "ZC0MM_1D";

	//// CHARM STUFF
	public static final EntityType<charmProjectile> charmProjectileEntityType = Registry.register(
		Registry.ENTITY_TYPE, 
		new Identifier(identifier, "charm_projectile"), 
		FabricEntityTypeBuilder.<charmProjectile>create(SpawnGroup.MISC, charmProjectile::new)
			.dimensions(EntityDimensions.fixed(0.25F, 0.25F)
		)
		.trackRangeBlocks(4).trackedUpdateRate(10).build()
	);
	public static final Identifier CHARM_SPAWN_PACKET = new Identifier(identifier, "charm_spawn_packet");

	//// ADVANCEMENTS
	public static final Identifier ZCOMM_ROOT_ADVANCEMENT = new Identifier(identifier, "root");
	public static final Identifier ZCOMM_CERT_ADVANCEMENT = new Identifier(identifier, "zcomm_cert");
	public static final Identifier ZCOMM_HEAD_ADVANCEMENT = new Identifier(identifier, "zcomm_head");
	
	public static void serverLoad(MinecraftServer server) {
		dataHandler.serverLoad(server);
	}
	
}


