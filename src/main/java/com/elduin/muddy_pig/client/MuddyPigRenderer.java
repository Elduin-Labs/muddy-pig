package com.elduin.muddy_pig.client;

import com.elduin.muddy_pig.MuddyPig;
import com.elduin.muddy_pig.entity.MuddyPigEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
//? if >=26 {
/*import net.minecraft.client.renderer.state.level.CameraRenderState;
*///? } else {
import net.minecraft.client.renderer.state.CameraRenderState;
//? }
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class MuddyPigRenderer extends MobRenderer<MuddyPigEntity, MuddyPigRenderState, MuddyPigModel> {

	private static final Identifier MUDDY = MuddyPig.id("textures/entity/muddy_pig/muddy_pig.png");
	private static final Identifier DRIED = MuddyPig.id("textures/entity/muddy_pig/dried_muddy_pig.png");

	private final MuddyPigModel adult;
	private final MuddyPigModel baby;

	public MuddyPigRenderer(EntityRendererProvider.Context context) {
		super(context, new MuddyPigModel(context.bakeLayer(MuddyPigClient.MUDDY_PIG)), 0.7F);
		this.adult = this.model;
		this.baby = new MuddyPigModel(context.bakeLayer(MuddyPigClient.MUDDY_PIG_BABY));
	}

	@Override
	public void submit(MuddyPigRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		this.model = state.isBaby ? this.baby : this.adult;
		super.submit(state, poseStack, collector, camera);
	}

	@Override
	public Identifier getTextureLocation(MuddyPigRenderState state) {
		// Rolling in mud is wet mud, even if it was dry a moment ago.
		return state.dry && !state.wallowing ? DRIED : MUDDY;
	}

	@Override
	public MuddyPigRenderState createRenderState() {
		return new MuddyPigRenderState();
	}

	@Override
	public void extractRenderState(MuddyPigEntity pig, MuddyPigRenderState state, float partialTick) {
		super.extractRenderState(pig, state, partialTick);
		state.dry = pig.isDry();
		state.wallowing = pig.isWallowing();
		state.wallowTime = pig.getWallowTicks() + partialTick;
	}

	@Override
	protected void setupRotations(MuddyPigRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		super.setupRotations(state, poseStack, bodyRot, scale);
		if (!state.wallowing) {
			return;
		}
		// Roll around the middle of the body, and sink down so the side or back is in the mud.
		float roll = rollAngle(state.wallowTime);
		float size = state.isBaby ? 0.5F : 1.0F;
		float middle = 0.625F * size;
		float sink = 0.34F * size * Math.min(1.0F, roll / 90.0F);
		poseStack.translate(0.0F, middle - sink, 0.0F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
		poseStack.translate(0.0F, -middle, 0.0F);
	}

	/**
	 * How far over the pig is, in degrees: 0 is standing, 90 is on its side, 180 is on its back with
	 * its feet in the air. It flops over, rolls side to side across its back twice, then gets up.
	 */
	static float rollAngle(float t) {
		float flop = 8.0F;
		float total = MuddyPigEntity.WALLOW_TIME;
		if (t < flop) {
			return 90.0F * smooth(t / flop);
		}
		if (t > total - flop) {
			return 90.0F * smooth((total - t) / flop);
		}
		float k = (t - flop) / (total - 2.0F * flop);
		return 180.0F - 90.0F * Mth.cos(k * Mth.TWO_PI * 2.0F);
	}

	private static float smooth(float x) {
		x = Mth.clamp(x, 0.0F, 1.0F);
		return x * x * (3.0F - 2.0F * x);
	}
}
