package com.elduin.muddy_pig.entity;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;

/** Keeps the pig still while it rolls, so it doesn't wander off mid-roll. */
public class WallowGoal extends Goal {

	private final MuddyPigEntity pig;

	public WallowGoal(MuddyPigEntity pig) {
		this.pig = pig;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return this.pig.isWallowing();
	}

	@Override
	public void start() {
		this.pig.getNavigation().stop();
	}

	@Override
	public void stop() {
		// Something more important (like getting hurt) took over before the roll was done.
		if (this.pig.isWallowing()) {
			this.pig.stopWallowing();
		}
	}
}
