package com.elduin.muddy_pig;

//? if >=26 {
/*import net.minecraft.world.entity.animal.pig.PigSoundVariant;
import net.minecraft.world.entity.animal.pig.PigSoundVariants;
*///? }

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * The handful of things Mojang moved between the versions this mod supports. Keeping them in one
 * place means the rest of the mod reads the same on every version.
 */
public final class Compat {

	private Compat() {
	}

	// 26 gave pigs sound variants (classic, mini, big) and separate baby sounds. The Muddy Pig
	// always uses the classic oink.

	//? if >=26 {
	/*private static PigSoundVariant.PigSoundSet pigSounds(boolean baby) {
		PigSoundVariant classic = SoundEvents.PIG_SOUNDS.get(PigSoundVariants.SoundSet.CLASSIC);
		return baby ? classic.babySounds() : classic.adultSounds();
	}

	public static SoundEvent pigAmbient(boolean baby) {
		return pigSounds(baby).ambientSound().value();
	}

	public static SoundEvent pigHurt(boolean baby) {
		return pigSounds(baby).hurtSound().value();
	}

	public static SoundEvent pigDeath(boolean baby) {
		return pigSounds(baby).deathSound().value();
	}

	public static SoundEvent pigStep(boolean baby) {
		return pigSounds(baby).stepSound().value();
	}
	*///? } else {
	public static SoundEvent pigAmbient(boolean baby) {
		return SoundEvents.PIG_AMBIENT;
	}

	public static SoundEvent pigHurt(boolean baby) {
		return SoundEvents.PIG_HURT;
	}

	public static SoundEvent pigDeath(boolean baby) {
		return SoundEvents.PIG_DEATH;
	}

	public static SoundEvent pigStep(boolean baby) {
		return SoundEvents.PIG_STEP;
	}
	//? }
}
