package net.iamaprogrammer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.iamaprogrammer.config.CoreConfig;
import net.iamaprogrammer.config.core.ConfigRegistry;
import net.iamaprogrammer.entity.DyeEntity;
import net.iamaprogrammer.event.UseItemHandler;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ThrowableDye implements ModInitializer {
	public static final String MOD_ID = "throwabledye";
	public static final Logger LOGGER = LoggerFactory.getLogger("throwabledye");

	public static CoreConfig CONFIG;
	public static final EntityType<DyeEntity> DYE = Registry.register(Registry.ENTITY_TYPE, new Identifier(ThrowableDye.MOD_ID, "packed_snowball"), FabricEntityTypeBuilder.<DyeEntity>create(SpawnGroup.MISC, DyeEntity::new).dimensions(EntityDimensions.fixed(0.25F, 0.25F)).trackRangeChunks(4).trackedUpdateRate(10).build());


	@Override
	public void onInitialize() {

		CoreConfig defaultConfig = new CoreConfig();
		defaultConfig.setOutliers(new HashMap<>());

		Map<String, String> outliers = defaultConfig.getOutliers();
		outliers.put("minecraft:glass", "{color}_stained_glass");
		outliers.put("minecraft:glass_pane", "{color}_stained_glass_pane");

		CONFIG = new ConfigRegistry.Builder<CoreConfig>(MOD_ID, CoreConfig.class)
				.withDefaultConfig(defaultConfig)
				.register();

		UseItemCallback.EVENT.register(new UseItemHandler());
		LOGGER.info("Mod Loaded.");
	}
}