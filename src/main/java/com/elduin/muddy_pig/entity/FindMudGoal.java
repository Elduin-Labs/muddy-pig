package com.elduin.muddy_pig.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** A dried Muddy Pig walks to the nearest mud block. Stepping into it starts the roll. */
public class FindMudGoal extends MoveToBlockGoal {

	private final MuddyPigEntity pig;

	public FindMudGoal(MuddyPigEntity pig, double speed) {
		super(pig, speed, 16, 4);
		this.pig = pig;
	}

	@Override
	public boolean canUse() {
		return this.pig.isDry() && !this.pig.isWallowing() && super.canUse();
	}

	@Override
	public boolean canContinueToUse() {
		return this.pig.isDry() && !this.pig.isWallowing() && super.canContinueToUse();
	}

	/** Look again every few seconds, not every ten or twenty like most animals. */
	@Override
	protected int nextStartTick(PathfinderMob mob) {
		return reducedTickDelay(40 + mob.getRandom().nextInt(40));
	}

	/** Mud with room on top for a pig, and no water on it. */
	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		if (!level.getBlockState(pos).is(Blocks.MUD)) {
			return false;
		}
		BlockPos above = pos.above();
		BlockState state = level.getBlockState(above);
		return state.getFluidState().isEmpty() && state.getCollisionShape(level, above).isEmpty();
	}
}
