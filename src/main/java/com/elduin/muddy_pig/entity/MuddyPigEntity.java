package com.elduin.muddy_pig.entity;

import com.elduin.muddy_pig.Compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Muddy Pig from Minecraft Earth. It is either muddy (wet mud, flower open) or dried (cracked
 * mud, flower shut). A dried one goes looking for a mud block, jumps in and rolls around, and comes
 * out muddy again with its flower open.
 */
public class MuddyPigEntity extends Animal {

	/** How long one roll in the mud lasts, in ticks. */
	public static final int WALLOW_TIME = 60;

	/** How long a normal pig has to stand in mud before it rolls in it, in ticks. */
	public static final int PIG_MUD_TIME = 20;

	private static final EntityDataAccessor<Boolean> DATA_DRY =
			SynchedEntityData.defineId(MuddyPigEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_WALLOWING =
			SynchedEntityData.defineId(MuddyPigEntity.class, EntityDataSerializers.BOOLEAN);

	private static final BlockParticleOption MUD = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState());
	private static final BlockParticleOption DRIED_MUD = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_MUD.defaultBlockState());

	/** Ticks until the mud dries. Only counted on the server. */
	private int dryTimer;
	/** How far into a roll we are. Counted on both sides so the client can animate it. */
	private int wallowTicks;

	public MuddyPigEntity(EntityType<? extends MuddyPigEntity> type, Level level) {
		super(type, level);
		this.dryTimer = this.newDryTime();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
		this.goalSelector.addGoal(2, new WallowGoal(this));
		this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, stack -> stack.is(ItemTags.PIG_FOOD), false));
		this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
		this.goalSelector.addGoal(6, new FindMudGoal(this, 1.1));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_DRY, false);
		builder.define(DATA_WALLOWING, false);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("dry", this.isDry());
		output.putInt("dry_timer", this.dryTimer);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setDry(input.getBooleanOr("dry", false));
		this.dryTimer = input.getIntOr("dry_timer", this.newDryTime());
	}

	// ---- mud ----

	public boolean isDry() {
		return this.entityData.get(DATA_DRY);
	}

	private void setDry(boolean dry) {
		this.entityData.set(DATA_DRY, dry);
	}

	public boolean isWallowing() {
		return this.entityData.get(DATA_WALLOWING);
	}

	public int getWallowTicks() {
		return this.wallowTicks;
	}

	/** Between a minute and a half and three minutes. */
	private int newDryTime() {
		return 1800 + this.random.nextInt(1800);
	}

	/** Standing in a mud block. Mud is a little lower than a full block, so your feet sink into it. */
	public static boolean isInMud(Entity entity) {
		return entity.level().getBlockState(entity.blockPosition()).is(Blocks.MUD)
				|| entity.getBlockStateOn().is(Blocks.MUD);
	}

	/** A normal pig stood in mud long enough: it becomes a Muddy Pig and has a good roll. */
	public static void turnMuddy(Pig pig) {
		pig.convertTo(ModEntities.MUDDY_PIG, ConversionParams.single(pig, false, false), MuddyPigEntity::startWallowing);
	}

	/** Jump into the mud and start rolling around. */
	public void startWallowing() {
		if (this.isWallowing()) {
			return;
		}
		this.entityData.set(DATA_WALLOWING, true);
		this.wallowTicks = 0;
		this.getNavigation().stop();
		if (this.onGround()) {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.3, 0.0));
		}
		this.playSound(SoundEvents.MUD_FALL, 1.0F, 0.8F);
	}

	/** Something interrupted the roll (it got hurt, say). No mud this time. */
	public void stopWallowing() {
		this.entityData.set(DATA_WALLOWING, false);
		this.wallowTicks = 0;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.isWallowing()) {
			this.wallowTicks++;
		} else {
			this.wallowTicks = 0;
		}

		if (!(this.level() instanceof ServerLevel server) || !this.isAlive()) {
			return;
		}

		if (this.isWallowing()) {
			this.wallowEffects(server);
			if (this.wallowTicks >= WALLOW_TIME) {
				this.finishWallowing(server);
			}
		} else if (this.isDry()) {
			if (this.onGround() && isInMud(this)) {
				this.startWallowing();
			}
		} else if (isInMud(this)) {
			// sitting in mud keeps you muddy
			this.dryTimer = this.newDryTime();
		} else if (!this.isInWaterOrRain() && --this.dryTimer <= 0) {
			this.dryOut(server);
		}
	}

	private void wallowEffects(ServerLevel server) {
		if (this.wallowTicks % 4 == 0) {
			server.sendParticles(MUD, this.getX(), this.getY() + 0.2, this.getZ(), 6, 0.4, 0.1, 0.4, 0.15);
		}
		if (this.wallowTicks % 12 == 6) {
			this.playSound(SoundEvents.MUD_STEP, 1.0F, 0.7F + this.random.nextFloat() * 0.4F);
		}
		if (this.wallowTicks == WALLOW_TIME / 2) {
			// a happy oink
			this.playSound(Compat.pigAmbient(this.isBaby()), 1.0F, 1.3F);
		}
	}

	/** All muddy again, and the flower opens. */
	private void finishWallowing(ServerLevel server) {
		this.stopWallowing();
		this.setDry(false);
		this.dryTimer = this.newDryTime();
		this.playSound(SoundEvents.BONE_MEAL_USE, 1.0F, 1.0F);
		Vec3 head = this.headPosition();
		server.sendParticles(ParticleTypes.HAPPY_VILLAGER, head.x, head.y, head.z, 8, 0.25, 0.2, 0.25, 0.0);
	}

	/** The mud has dried out. The flower shuts and the pig wants another mud bath. */
	private void dryOut(ServerLevel server) {
		this.setDry(true);
		this.playSound(SoundEvents.MUD_BREAK, 0.6F, 1.5F);
		server.sendParticles(DRIED_MUD, this.getX(), this.getY() + 0.5, this.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
	}

	/** Where the flower is: on top of the head. */
	private Vec3 headPosition() {
		float size = this.isBaby() ? 0.5F : 1.0F;
		Vec3 forward = Vec3.directionFromRotation(0.0F, this.yBodyRot).scale(0.6 * size);
		return this.position().add(forward).add(0.0, (this.isBaby() ? 0.9 : 1.4) * size, 0.0);
	}

	// ---- pig things ----

	@Override
	public boolean isFood(ItemStack stack) {
		return stack.is(ItemTags.PIG_FOOD);
	}

	@Override
	public @Nullable MuddyPigEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return ModEntities.MUDDY_PIG.create(level, EntitySpawnReason.BREEDING);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return Compat.pigAmbient(this.isBaby());
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return Compat.pigHurt(this.isBaby());
	}

	@Override
	protected SoundEvent getDeathSound() {
		return Compat.pigDeath(this.isBaby());
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(Compat.pigStep(this.isBaby()), 0.15F, 1.0F);
	}

	@Override
	public Vec3 getLeashOffset() {
		return new Vec3(0.0, 0.6F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
	}
}
