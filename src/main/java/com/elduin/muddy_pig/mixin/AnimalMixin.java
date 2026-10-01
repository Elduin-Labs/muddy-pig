package com.elduin.muddy_pig.mixin;

import com.elduin.muddy_pig.entity.MuddyPigEntity;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "If a pig was in the mud, it became a muddy pig." A normal pig that stands in mud for a moment
 * rolls around in it and comes out a Muddy Pig.
 *
 * Pig itself has no per-tick method to hook, so this goes on Animal and checks for pigs.
 */
@Mixin(Animal.class)
@MixinEnvironment(type = MixinEnvironment.Env.MAIN)
public abstract class AnimalMixin {

	@Unique
	private int muddy_pig$ticksInMud;

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void muddy_pig$pigsLoveMud(CallbackInfo ci) {
		if (!((Object) this instanceof Pig pig) || !(pig.level() instanceof ServerLevel)) {
			return;
		}
		// A saddled pig or one being ridden stays a normal pig, so nobody loses their ride.
		boolean canRoll = pig.isAlive() && pig.onGround() && !pig.isSaddled() && !pig.isVehicle();
		if (canRoll && MuddyPigEntity.isInMud(pig)) {
			if (++this.muddy_pig$ticksInMud >= MuddyPigEntity.PIG_MUD_TIME) {
				this.muddy_pig$ticksInMud = 0;
				MuddyPigEntity.turnMuddy(pig);
			}
		} else {
			this.muddy_pig$ticksInMud = 0;
		}
	}
}
