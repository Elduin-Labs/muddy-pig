package com.elduin.muddy_pig.entity;

import com.elduin.muddy_pig.MuddyPig;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModEntities {

	public static final ResourceKey<EntityType<?>> MUDDY_PIG_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, MuddyPig.id("muddy_pig"));

	// Same size as a normal pig. Spawns on grass like every other farm animal.
	public static final EntityType<MuddyPigEntity> MUDDY_PIG = Registry.register(BuiltInRegistries.ENTITY_TYPE, MUDDY_PIG_KEY,
			FabricEntityType.Builder.createMob(MuddyPigEntity::new, MobCategory.CREATURE, mob -> mob
							//? if >=26 {
							/*.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules)
							*///? } else {
							.spawnRestriction(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules)
							//? }
							.defaultAttributes(MuddyPigEntity::createAttributes))
					.sized(0.9F, 0.9F)
					.clientTrackingRange(10)
					.build(MUDDY_PIG_KEY));

	public static final ResourceKey<Item> SPAWN_EGG_KEY =
			ResourceKey.create(Registries.ITEM, MuddyPig.id("muddy_pig_spawn_egg"));

	public static final Item MUDDY_PIG_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, SPAWN_EGG_KEY,
			new SpawnEggItem(new Item.Properties().spawnEgg(MUDDY_PIG).setId(SPAWN_EGG_KEY)));

	/** Muddy Pigs were "epic" in Minecraft Earth. Plain pigs have a weight of 10 in plains. */
	private static final int SPAWN_WEIGHT = 2;

	private ModEntities() {
	}

	public static void register() {
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS),
				MobCategory.CREATURE, MUDDY_PIG, SPAWN_WEIGHT, 1, 2);
	}
}
