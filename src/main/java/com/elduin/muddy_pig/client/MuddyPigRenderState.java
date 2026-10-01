package com.elduin.muddy_pig.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class MuddyPigRenderState extends LivingEntityRenderState {
	public boolean dry;
	public boolean wallowing;
	/** Ticks into the current roll, including the part-tick, so the roll is smooth. */
	public float wallowTime;
}
