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
import net.minecraft.item.Items;
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
import zener.zcomm.items.zcomm.upgrade;



public class Main implements ModInitializer {

    @Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		Registry.register(Registry.ITEM, ZCOMM_IDENTIFIER, ZCOMM);
		Registry.register(Registry.ITEM, CHARM_IDENTIFIER, CHARM);
		Registry.register(Registry.ITEM, CASING_IDENTIFIER, CASING);
		Registry.register(Registry.ITEM, UPGRADE_IDENTIFIER, UPGRADE);

		CommandRegistrationCallback.EVENT.register(Command::register);

		ServerLifecycleEvents.SERVER_STARTING.register(Main::serverLoad);

	}

	//// INTERFACE
	public static final Identifier ZCOMM_INVIDENTIFIER = new Identifier("zcomm", "comm_inventory");
	public static final Identifier ZCOMM_NRIDENTIFIER = new Identifier("zcomm", "comm_nr");
	public static final Identifier ZCOMM_MAINIDENTIFIER = new Identifier("zcomm", "comm_main");
	public static final ScreenHandlerType<InvGUIDescription> ZCOMM_INV_SCREEN_TYPE = ScreenHandlerRegistry.registerExtended(ZCOMM_INVIDENTIFIER, InvGUIDescription::new);
	public static final ScreenHandlerType<zcommNRGUIDescription> ZCOMM_NR_SCREEN_TYPE = ScreenHandlerRegistry.registerExtended(ZCOMM_NRIDENTIFIER, zcommNRGUIDescription::new);
	public static final ScreenHandlerType<MainGUIDescription> ZCOMM_MAIN_SCREEN_TYPE = ScreenHandlerRegistry.registerExtended(ZCOMM_MAINIDENTIFIER, MainGUIDescription::new);


	//// ITEMS
	public static final Identifier ZCOMM_IDENTIFIER = new Identifier("zcomm", "comm");
	public static final Identifier CHARM_IDENTIFIER = new Identifier("zcomm", "charm");
	public static final Identifier CASING_IDENTIFIER = new Identifier("zcomm", "casing");
	public static final Identifier UPGRADE_IDENTIFIER = new Identifier("zcomm", "upgrade");
	public static final String ZCOMM_TRANSLATION_KEY = Util.createTranslationKey("container", ZCOMM_IDENTIFIER);
	public static final ItemGroup ZCOMM_GROUP = FabricItemGroupBuilder.create(
		new Identifier("zcomm", "general"))
	.icon(() -> new ItemStack(Items.BOWL))
	.build();

	public static Item ZCOMM = new comm(new Item.Settings().group(ZCOMM_GROUP));
	public static Item CHARM = new charm(new Item.Settings().group(ZCOMM_GROUP));
	public static Item CASING = new casing(new Item.Settings().group(ZCOMM_GROUP));
	public static Item UPGRADE = new upgrade(new Item.Settings().group(ZCOMM_GROUP));

	//// GLOBAL VAR FOR MANAGING CHANNELS
	public static int GLOBAL_CHANNEL_NR = 71;
	public static String ZCOMM_COMMUNICATION_IDENTIFIER = "ZC0MM_1D";

	//// CHARM STUFF
	public static final EntityType<charmProjectile> charmProjectileEntityType = Registry.register(
		Registry.ENTITY_TYPE, 
		new Identifier("zcomm", "charm_projectile"), 
		FabricEntityTypeBuilder.<charmProjectile>create(SpawnGroup.MISC, charmProjectile::new)
			.dimensions(EntityDimensions.fixed(0.25F, 0.25F)
		)
		.trackRangeBlocks(4).trackedUpdateRate(10).build()
	);
	public static final Identifier CHARM_SPAWN_PACKET = new Identifier("zcomm", "charm_spawn_packet");

	public static void serverLoad(MinecraftServer server) {
		dataHandler.serverLoad(server);
	}

	public static final void receiveCommDataPacket() {

	}
}

