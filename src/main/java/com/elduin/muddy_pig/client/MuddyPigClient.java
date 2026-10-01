package com.elduin.muddy_pig.client;

import com.elduin.muddy_pig.MuddyPig;
import com.elduin.muddy_pig.entity.ModEntities;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
//? if >=26 {
/*import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
*///? } else {
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
//? }
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class MuddyPigClient {

	public static final ModelLayerLocation MUDDY_PIG = new ModelLayerLocation(MuddyPig.id("muddy_pig"), "main");
	public static final ModelLayerLocation MUDDY_PIG_BABY = new ModelLayerLocation(MuddyPig.id("muddy_pig_baby"), "main");

	private MuddyPigClient() {
	}

	public static void register() {
		// Fabric renamed its model layer registry in 26.
		//? if >=26 {
		/*ModelLayerRegistry.registerModelLayer(MUDDY_PIG, MuddyPigModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(MUDDY_PIG_BABY,
				() -> MuddyPigModel.createBodyLayer().apply(MuddyPigModel.BABY_TRANSFORMER));
		*///? } else {
		EntityModelLayerRegistry.registerModelLayer(MUDDY_PIG, MuddyPigModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(MUDDY_PIG_BABY,
				() -> MuddyPigModel.createBodyLayer().apply(MuddyPigModel.BABY_TRANSFORMER));
		//? }
		EntityRendererRegistry.register(ModEntities.MUDDY_PIG, MuddyPigRenderer::new);
	}
}
