package com.elduin.muddy_pig.client;

import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A pig with a flower growing out of the top of its head. The flower is two flat crossed cards,
 * like a flower block. There are two of them, open and shut, and only one shows at a time.
 */
public class MuddyPigModel extends QuadrupedModel<MuddyPigRenderState> {

	/** Babies get a full-size head on a half-size body, same as normal pigs. */
	public static final MeshTransformer BABY_TRANSFORMER = PigModel.BABY_TRANSFORMER;

	private final ModelPart flower;
	private final ModelPart bud;

	public MuddyPigModel(ModelPart root) {
		super(root);
		this.flower = this.head.getChild("flower");
		this.bud = this.head.getChild("bud");
	}

	public static LayerDefinition createBodyLayer() {
		// The pig's own shape: body and legs from the four-legged animal mesh, then the head and snout.
		MeshDefinition mesh = QuadrupedModel.createBodyMesh(6, true, false, CubeDeformation.NONE);
		PartDefinition head = mesh.getRoot().addOrReplaceChild(
				"head",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F)
						.texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F),
				PartPose.offset(0.0F, 12.0F, -6.0F));

		// Each card is 6 wide and 7 tall. The two cards of one flower share the same picture:
		// the first card's faces start at (u, v), the second card's start 6 lower.
		PartPose onTop = PartPose.offset(1.5F, -4.0F, -4.5F);
		head.addOrReplaceChild("flower", crossedCards(0, 40), onTop);
		head.addOrReplaceChild("bud", crossedCards(16, 40), onTop);

		return LayerDefinition.create(mesh, 64, 64);
	}

	private static CubeListBuilder crossedCards(int u, int v) {
		return CubeListBuilder.create()
				.texOffs(u, v).addBox(-3.0F, -7.0F, 0.0F, 6.0F, 7.0F, 0.0F)
				.texOffs(u, v - 6).addBox(0.0F, -7.0F, -3.0F, 0.0F, 7.0F, 6.0F);
	}

	@Override
	public void setupAnim(MuddyPigRenderState state) {
		super.setupAnim(state);

		boolean open = !state.dry && !state.wallowing;
		this.flower.visible = open;
		this.bud.visible = !open;

		if (state.wallowing) {
			// legs paddle in the air while it rolls
			float kick = Mth.sin(state.ageInTicks * 0.9F) * 0.8F;
			this.rightHindLeg.xRot = kick;
			this.leftHindLeg.xRot = -kick;
			this.rightFrontLeg.xRot = -kick;
			this.leftFrontLeg.xRot = kick;
			this.head.xRot = 0.0F;
		}
	}
}
