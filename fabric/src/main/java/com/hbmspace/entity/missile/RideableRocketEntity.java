/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.entity.missile;

import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.client.ClientEffects;
import com.hbm.entity.missile.Missile;
import com.hbm.explosion.BlastDebris;
import com.hbm.explosion.CompositeExplosion;
import com.hbm.interfaces.EntityLookOverlayProvider;
import com.hbm.interfaces.LookOverlay;
import com.hbm.items.SatelliteFrequencyProvider;
import com.hbm.packet.ClientEventConstants;
import com.hbm.packet.PacketWire;
import com.hbm.saveddata.satellites.SatelliteDeployment;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.SoundHandle;
import com.hbm.sound.WorldSounds;
import com.hbm.util.ChunkUtil;
import com.hbm.util.GameTime;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.OrbitalStationBlockEntity;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.CelestialTeleporter;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.Target;
import com.hbmspace.dim.orbit.OrbitalStation.StationState;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.entity.RocketryEntitiesSpace;
import com.hbmspace.handler.RocketPart;
import com.hbmspace.handler.RocketStruct.RocketStage;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.items.VOTVdriveItem.Destination;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.items.weapon.CustomRocketItem;
import com.hbmspace.items.weapon.MissileKindsSpace;
import com.hbmspace.items.weapon.MissilePartsSpace;
import com.hbmspace.packet.layout.SpaceRocketTransfer;
import com.hbmspace.saveddata.satellites.SatelliteOrbit;
import com.hbmspace.sound.RocketrySoundsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RideableRocketEntity extends Missile implements EntityLookOverlayProvider {

    @Override
    public String sourceName() {
        return "EntityRideableRocket";
    }

    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(RideableRocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DRIVE =
            SynchedEntityData.defineId(
                    RideableRocketEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> TIMER =
            SynchedEntityData.defineId(RideableRocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> ROCKET =
            SynchedEntityData.defineId(
                    RideableRocketEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> MOTION_Y =
            SynchedEntityData.defineId(RideableRocketEntity.class, EntityDataSerializers.FLOAT);
    private static final Identifier CAMERA_DISTANCE =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "rocket_camera");

    public ItemStack navDrive = ItemStack.EMPTY;
    public @Nullable EntityRideableRocketDummy capDummy;

    private int stateTimer;
    public int decoupleTimer;
    public int shroudTimer;
    public int forceExitTimer;
    public boolean podBrakes;
    public long podBrakeStart;
    public boolean preview;

    private double rocketVelocity;
    private boolean checkedDock;
    private boolean willExplode;
    private int satFreq;
    private @Nullable SatelliteOrbit satOrbit;
    private @Nullable Destination destinationOverride;
    private @Nullable OrbitalStationBlockEntity targetPort;

    private RocketState lastState = RocketState.AWAITING;
    private boolean engineSoundActive;
    private @Nullable SoundHandle audio;

    public enum RocketState {
        AWAITING,
        LAUNCHING,
        LANDING,
        LANDED,
        TIPPING,
        DOCKING,
        UNDOCKING,
        NEEDSFUEL,
        TRANSFER,
    }

    public static void receiveTransfer(ByteBuf inputBuffer) {
        OrbitalStation.clientStation = OrbitalStation.deserialize(inputBuffer);
    }

    public RideableRocketEntity(EntityType<? extends RideableRocketEntity> type, Level level) {
        super(type, level);
        targetX = (int) getX() - 10_000;
        targetZ = (int) getZ();
    }

    public RideableRocketEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(RocketryEntitiesSpace.RIDEABLE_ROCKET.get(), level);
        launch(x, y, z, (int) x + 10_000, (int) z);
        satFreq = SatelliteFrequencyProvider.frequency(stack);
        satOrbit = stack.get(ModDataComponentsSpace.SATELLITE_ORBIT.get());
        setRocket(CustomRocketItem.get(stack));
    }

    public RideableRocketEntity withProgram(ItemStack drive) {
        this.navDrive = drive.copy();
        return this;
    }

    public RideableRocketEntity launchedBy(@Nullable LivingEntity entity) {
        setOwner(entity);
        return this;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, RocketState.AWAITING.ordinal());
        builder.define(DRIVE, ItemStack.EMPTY);
        builder.define(TIMER, 0);
        builder.define(ROCKET, ItemStack.EMPTY);
        builder.define(MOTION_Y, 0F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (ROCKET.equals(accessor)) refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(2F, (float) getRocket().getHeight() + 1F);
    }

    public void beginLandingSequence(Target from, Target to) {
        setDeltaMovement(Vec3.ZERO);

        RocketStruct rocket = getRocket();
        boolean expendStage = !rocket.stages.isEmpty();
        if (getState() == RocketState.UNDOCKING && from.body() == to.body()) expendStage = false;

        if (expendStage) {
            rocket.stages.removeFirst();
            setRocket(rocket);
        }

        setState(RocketState.LANDING);

        Destination destination = getDestination();
        if (destination == null) return;

        int x = destination.x();
        int y = 800;
        int z = destination.z();
        ResourceKey<Level> dimension = destination.body().getDimension();
        ServerLevel server = (ServerLevel) level();

        if (canRide()) {
            if (destination.body() == SolarSystem.Body.ORBIT) {
                setState(RocketState.DOCKING);
                x = x * OrbitalStation.STATION_SIZE + OrbitalStation.STATION_SIZE / 2;
                y = 0;
                z = z * OrbitalStation.STATION_SIZE + OrbitalStation.STATION_SIZE / 2;
            }

            if (level().dimension() != dimension) {
                CelestialTeleporter.teleport(this, dimension, x + 0.5, y, z + 0.5, false);
            } else {
                setPos(x + 0.5, y, z + 0.5);
            }

            if (destination.body() == SolarSystem.Body.ORBIT) {
                OrbitalStation.spawn(server.getServer().getLevel(dimension), x, z);
            }
            return;
        }

        if (rocket.capsule.item instanceof SatelliteFrequencyProvider
                && destination.body() != SolarSystem.Body.ORBIT) {
            orbit(server.getServer().getLevel(dimension), rocket.capsule.item);
        } else if (rocket.capsule.item == MissilePartsSpace.RP_STATION_CORE_20.get()) {
            OrbitalStation.addStation(x, z, CelestialBody.getBody(level()));
        }

        discard();
    }

    private void orbit(ServerLevel level, Item capsule) {
        ItemStack satellite = new ItemStack(capsule);
        if (satOrbit != null) satellite.set(ModDataComponentsSpace.SATELLITE_ORBIT.get(), satOrbit);
        SatelliteDeployment.launch(level, satellite, satFreq, getX(), getY(), getZ());
    }

    public void beginCelestialTransfer(Target from, Target to) {
        setDeltaMovement(Vec3.ZERO);
        setState(RocketState.TRANSFER);

        OrbitalStation station = SolarSystemWorldSavedData.get(level()).addStation(from.body());

        double distance =
                SolarSystem.calculateDistanceBetweenTwoBodies(level(), from.body(), to.body());
        station.setState(
                StationState.TRANSFER,
                OrbitalStation.calculateTransferTime(distance, 10, getRocket().getThrust()));
        station.orbiting = from.body();
        station.target = to.body();

        int x = station.dX * OrbitalStation.STATION_SIZE + OrbitalStation.STATION_SIZE / 2;
        int y = 128;
        int z = station.dZ * OrbitalStation.STATION_SIZE + OrbitalStation.STATION_SIZE / 2;

        if (level().dimension() != SpaceDimensions.ORBIT) {
            CelestialTeleporter.teleport(this, SpaceDimensions.ORBIT, x + 0.5, y, z + 0.5, false);
        } else {
            setPos(x + 0.5, y, z + 0.5);
        }
    }

    public void attemptLaunch() {
        Target from = CelestialBody.getTarget(level(), getBlockX(), getBlockZ());
        Target to = getTarget();

        RocketState transitionTo = from.inOrbit() ? RocketState.UNDOCKING : RocketState.LAUNCHING;

        targetX = (int) getX() - 10_000;
        targetZ = (int) getZ();

        if (getRocket().hasSufficientFuel(from.body(), to.body(), from.inOrbit(), to.inOrbit())) {
            setState(transitionTo);
        }
    }

    public boolean canExitCapsule() {
        RocketState state = getState();
        return state != RocketState.LANDING
                && state != RocketState.LAUNCHING
                && state != RocketState.DOCKING
                && state != RocketState.UNDOCKING
                && state != RocketState.TRANSFER;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) clientTick();
        else if (!isRemoved()) tickSoundVibrations();
        setStateTimer(++stateTimer);
    }

    @Override
    protected void fly() {
        moveFlight(getDeltaMovement().scale(motionMult()));
        if (isRemoved()) return;
        ChunkUtil.holdOwnChunk(this);
        serverTick();
    }

    @Override
    protected boolean shouldHitBlocks() {
        var state = getState();
        return state == RocketState.LANDING || state == RocketState.DOCKING;
    }

    @Override
    protected void onFlightImpact(BlockHitResult hit) {
        onRocketImpact();
    }

    private void serverTick() {
        RocketState state = getState();

        if (!checkedDock) {
            checkedDock = true;
            if (state == RocketState.LANDED
                    || state == RocketState.AWAITING
                    || state == RocketState.NEEDSFUEL) {
                BlockPos capsule =
                        BlockPos.containing(getX(), getY() + getBbHeight() - 1.0, getZ());
                BlockPos core = MultiblockSurface.coreOfAny(level(), capsule);
                if (core != null && level().getBlockEntity(core) instanceof RocketDock dock)
                    dock.dockRocket(this);
            }
        }

        Player rider = getFirstPassenger() instanceof Player player ? player : null;

        setYRot(-90.0F);

        if (navDrive.getItem() instanceof VOTVdriveItem) {
            VOTVdriveItem.publishStation(navDrive, level());
            if (!ItemStack.matches(navDrive, entityData.get(DRIVE))) setDrive(navDrive);
        }

        if (getOwner() == null && rider != null) setOwner(rider);

        if (state == RocketState.AWAITING && (jumping(rider) || !canRide())) {
            attemptLaunch();
            setOwner(rider);
        }

        exitControl(rider);

        switch (state) {
            case LAUNCHING -> {
                if (isReusable()) {
                    setXRot(Mth.clamp((stateTimer - 60) * 0.3F, 0.0F, 45.0F));
                    if (rocketVelocity < 4)
                        rocketVelocity += Mth.clamp(stateTimer / 120D * 0.05D, 0, 0.05);
                } else {
                    double acceleration = stateTimer / 120D;
                    setXRot(Mth.clamp((stateTimer - 80) * 0.3F, 0.0F, 45.0F));
                    if (rocketVelocity < 4) {
                        rocketVelocity += Mth.clamp(acceleration * acceleration * 0.05D, 0, 0.05);
                    }
                }
            }
            case LANDING -> land();
            case TIPPING -> tip();
            case DOCKING -> dock();
            case UNDOCKING -> {
                rocketVelocity = -0.1;
                setXRot(0);
            }
            case TRANSFER -> transfer(rider);
            default -> {
                rocketVelocity = 0;
                setXRot(0);
            }
        }

        if (state == RocketState.LAUNCHING) {
            setDeltaMovement(direction(getXRot() - 90.0F, 180.0F - getYRot(), rocketVelocity));
        } else {
            setDeltaMovement(0, rocketVelocity, 0);
        }
        entityData.set(MOTION_Y, (float) getDeltaMovement().y);

        if (state == RocketState.LANDING) {
            FluidState fluid = level().getFluidState(blockPosition());
            if (!fluid.isEmpty()) setState(RocketState.TIPPING);
            if (fluid.is(FluidTags.LAVA)) {
                lavaIgnite();
                willExplode = true;
            }
        }

        if ((state == RocketState.LAUNCHING && getY() > 900)
                || (state == RocketState.UNDOCKING && getY() < 32)) {
            Target from = CelestialBody.getTarget(level(), getBlockX(), getBlockZ());
            Target to = getTarget();

            if (!canRide() || from.body() == to.body()) {
                beginLandingSequence(from, to);
            } else {
                beginCelestialTransfer(from, to);
            }
        }

        if (isRemoved()) return;

        if (getBbHeight() > 8) {
            double offset = getBbHeight() - 4;
            if (capDummy == null || capDummy.isRemoved()) {
                capDummy = new EntityRideableRocketDummy(level(), this);
                capDummy.setPos(getX(), getY() + offset, getZ());
                level().addFreshEntity(capDummy);
            } else {
                capDummy.setPos(getX(), getY() + offset, getZ());
            }
        } else if (capDummy != null) {
            capDummy.discard();
            capDummy = null;
        }
    }

    private static boolean jumping(@Nullable Player rider) {
        return rider instanceof ServerPlayer player && player.getLastClientInput().jump();
    }

    private void exitControl(@Nullable Player rider) {
        if (rider instanceof ServerPlayer player && player.getLastClientInput().shift()) {
            if (canExitCapsule() || forceExitTimer >= 60) {
                player.stopRiding();
            } else {
                forceExitTimer++;
            }
            player.setShiftKeyDown(false);
        } else if (rider != null) {
            forceExitTimer = 0;
        }

        Entity owner = getOwner();
        if (owner != null
                && getFirstPassenger() == null
                && !canExitCapsule()
                && forceExitTimer < 60
                && owner.level() == level()) {
            owner.startRiding(this, true, false);
        }
    }

    private void land() {
        double targetHeight =
                level().getHeight(Heightmap.Types.MOTION_BLOCKING, getBlockX(), getBlockZ());
        setXRot(0);

        if (isReusable()) {
            rocketVelocity = Mth.clamp((targetHeight - getY()) * 0.01, -1.0, -0.005);
        } else {
            rocketVelocity = Mth.clamp((targetHeight - getY()) * 0.005, -0.5, -0.005);
        }

        if (destinationOverride == null && navDrive.getItem() instanceof VOTVdriveItem) {
            Destination destination = VOTVdriveItem.getDestination(navDrive);
            int x = destination.x();
            int z = destination.z();

            AABB below = getBoundingBox().setMinY(targetHeight);
            if (!level().getEntities(this, below, entity -> entity instanceof RideableRocketEntity)
                    .isEmpty()) {
                int distance = random.nextBoolean() ? -5 : 5;
                if (random.nextBoolean()) {
                    x += distance;
                } else {
                    z += distance;
                }
                VOTVdriveItem.setCoordinates(navDrive, x, z);
            }

            setPos(x + 0.5, getY(), z + 0.5);
        }
    }

    private void tip() {
        float tipTime = stateTimer * 0.1F;
        float pitch = tipTime * tipTime;

        if (pitch > 90) {
            pitch = 90;

            if (willExplode) {
                dropNDie();
                CompositeExplosion.detonate(
                        level(),
                        getX(),
                        getY(),
                        getZ(),
                        5F,
                        CompositeExplosion.CLOUD | CompositeExplosion.SHRAPNEL);
                Vec3 motion = getDeltaMovement();
                BlastDebris.shower(
                        level(), getX(), getY(), getZ(), motion.x, motion.y, motion.z, 15, 0.075);
            }
        }

        setXRot(pitch);
        rocketVelocity = 0;
    }

    private void dock() {
        if (stateTimer <= 20) {
            rocketVelocity = 0;
            setXRot(0);
            return;
        }

        rocketVelocity = 0.1;
        setXRot(0);

        if (targetPort == null) targetPort = OrbitalStation.getPort(getBlockX(), getBlockZ());

        if (targetPort == null) {
            rocketVelocity = 0;
            return;
        }

        BlockPos port = targetPort.getBlockPos();
        setPos(port.getX() + 0.5, getY(), port.getZ() + 0.5);

        targetPort.despawnRocket();
        targetPort.reservePort();

        if (getY() + getBbHeight() > port.getY() + 1.5) {
            setState(isReusable() ? RocketState.NEEDSFUEL : RocketState.LANDED);
            destinationOverride = null;
            setPos(getX(), port.getY() + 1.5 - getBbHeight(), getZ());

            targetPort.dockRocket(this);
            targetPort = null;
        }
    }

    private void transfer(@Nullable Player rider) {
        rocketVelocity = 0;
        setXRot(90);
        forceExitTimer = 0;

        OrbitalStation station = OrbitalStation.getStationFromPosition(getBlockX(), getBlockZ());
        station.update(level());

        if (station.getUnscaledProgress(0) > 0.99 || station.state == StationState.ARRIVING) {
            beginLandingSequence(
                    CelestialBody.getTarget(level(), getBlockX(), getBlockZ()), getTarget());
            SolarSystemWorldSavedData.get(level()).removeStation(station);
        }

        if (rider instanceof ServerPlayer player)
            PacketWire.sendTo(SpaceRocketTransfer.encode(station), player);
    }

    private void tickSoundVibrations() {
        RocketState state = getState();
        OrbitalStation station =
                state == RocketState.TRANSFER
                        ? OrbitalStation.getStationFromPosition(getBlockX(), getBlockZ())
                        : null;
        boolean active =
                state == RocketState.LAUNCHING
                        || state == RocketState.LANDING && entityData.get(MOTION_Y) > -0.4
                        || state == RocketState.TRANSFER && station.getUnscaledProgress(0) <= 0.15;
        if (active) WorldSounds.entityLoop(this, !engineSoundActive);
        engineSoundActive = active;
        if (state == RocketState.TRANSFER && station.getUnscaledProgress(0) > 0.2) {
            if (decoupleTimer == 0 || decoupleTimer == 100 && getRocket().stages.size() > 1) {
                WorldSounds.entityNoise(this, getX(), getY(), getZ(), 0.5F);
            }
            decoupleTimer++;
        } else if (state != RocketState.TRANSFER) {
            decoupleTimer = 0;
        }
    }

    private void clientTick() {
        RocketState state = getState();

        if (state != lastState) {
            if (state == RocketState.LAUNCHING) {
                SoundHandle ignition =
                        AudioSystem.getLoopedSound(
                                RocketrySoundsSpace.ROCKET_IGNITION.get(),
                                SoundSource.PLAYERS,
                                (float) getX(),
                                (float) getY(),
                                (float) getZ(),
                                1.0F,
                                250.0F,
                                1.0F,
                                5);
                if (ignition != null) {
                    ignition.setLooping(false);
                    ignition.start();
                }
            }

            lastState = state;
            stateTimer = 0;
        } else {
            if (state == RocketState.LAUNCHING
                    || (state == RocketState.LANDING && entityData.get(MOTION_Y) > -0.4)
                    || (state == RocketState.TRANSFER
                            && OrbitalStation.clientStation.getUnscaledProgress(0) <= 0.15)) {
                if (audio == null || !audio.isPlaying()) {
                    audio =
                            AudioSystem.getLoopedSound(
                                    getRocket().stages.size() <= 1
                                            ? RocketrySoundsSpace.ROCKET_FLY_LIGHT.get()
                                            : RocketrySoundsSpace.ROCKET_FLY_HEAVY.get(),
                                    SoundSource.PLAYERS,
                                    (float) getX(),
                                    (float) getY(),
                                    (float) getZ(),
                                    1.0F,
                                    250.0F,
                                    1.0F,
                                    5);
                    if (audio != null) audio.start();
                }

                if (audio != null) {
                    audio.moveTo((float) getX(), (float) getY(), (float) getZ());
                    audio.refresh();
                }
            } else if (audio != null) {
                audio.stop();
                audio = null;
            }
        }

        if (state == RocketState.LANDING) {
            if (entityData.get(MOTION_Y) > -0.4F) {
                if (podBrakes) {
                    podBrakes = false;
                    podBrakeStart = GameTime.millis(level());
                }
            } else {
                podBrakes = true;
            }
        }

        if (state == RocketState.TRANSFER) {
            OrbitalStation station = OrbitalStation.clientStation;
            station.update(level());

            if (station.getUnscaledProgress(0) > 0.2) {
                if (decoupleTimer == 0) stage(0.9F);

                if (decoupleTimer == 100 && getRocket().stages.size() > 1) {
                    stage(1.2F);
                } else if (decoupleTimer > 100) {
                    shroudTimer++;
                }

                decoupleTimer++;
            }
        } else {
            decoupleTimer = 0;
            shroudTimer = 0;
        }
    }

    private void stage(float pitch) {
        SoundHandle decouple =
                AudioSystem.getLoopedSound(
                        RocketrySoundsSpace.ROCKET_STAGE.get(),
                        SoundSource.PLAYERS,
                        (float) getX(),
                        (float) getY(),
                        (float) getZ(),
                        0.5F,
                        250.0F,
                        pitch + random.nextFloat() * 0.2F,
                        40);
        if (decouple != null) {
            decouple.setLooping(false);
            decouple.start();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (audio != null) {
            audio.stop();
            audio = null;
        }
        if (capDummy != null) capDummy.discard();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return motionMult() == 0;
    }

    private double motionMult() {
        RocketState state = getState();
        if (state == RocketState.AWAITING
                || state == RocketState.LANDED
                || state == RocketState.NEEDSFUEL) return 0;
        return 4;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (!canRide() || level().isClientSide()) return InteractionResult.PASS;

        if (player.isSecondaryUseActive()) {
            if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
            BlockPos capsule = BlockPos.containing(getX(), getY() + getBbHeight() - 1.0, getZ());
            BlockPos core = MultiblockSurface.coreOfAny(level(), capsule);
            if (core == null
                    || !(level().getBlockEntity(core) instanceof RocketDock dock)
                    || dock.getDocked() != this) return InteractionResult.PASS;
            return level().getBlockState(core)
                    .useWithoutItem(
                            level(),
                            player,
                            new BlockHitResult(Vec3.atCenterOf(core), Direction.UP, core, false));
        }

        Entity rider = getFirstPassenger();
        if (rider == null) {
            return player.startRiding(this) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return rider == player ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public boolean canRide() {
        RocketStruct rocket = getRocket();
        return rocket.capsule != null && rocket.capsule.warhead == MissileKindsSpace.APOLLO;
    }

    public boolean isReusable() {
        RocketStruct rocket = getRocket();
        return rocket.capsule != null && rocket.capsule.item == MissilePartsSpace.RP_POD_20.get();
    }

    public void recallPod(Destination destination) {
        setOwner((Entity) null);
        destinationOverride = destination;
        attemptLaunch();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        Vec3 seat = direction(getXRot() - 90.0F, 180.0F - getYRot(), getMountedYOffset() + 1.12);
        moveFunction.accept(passenger, getX() + seat.x, getY() + seat.y - 1.62, getZ() + seat.z);
    }

    public double getMountedYOffset() {
        if (isReusable()) return getBbHeight() - 2.5;
        return getBbHeight() - 3.0;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (!level().isClientSide() && passenger instanceof LivingEntity living) {
            living.getAttribute(Attributes.CAMERA_DISTANCE)
                    .addOrUpdateTransientModifier(
                            new AttributeModifier(
                                    CAMERA_DISTANCE, 8.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!level().isClientSide() && passenger instanceof LivingEntity living) {
            living.getAttribute(Attributes.CAMERA_DISTANCE).removeModifier(CAMERA_DISTANCE);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (CelestialBody.inOrbit(level()))
            return new Vec3(getX(), getBoundingBox().maxY + 1.0, getZ());
        return new Vec3(getX() + 2, getY() + 1, getZ());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isRemoved()) return true;
        if (isInvulnerableToBase(source)) return false;

        if (getFirstPassenger() == null && source.getEntity() instanceof Player player) {
            boolean bare = getRocket().stages.isEmpty() && !isReusable();
            if (bare
                    || getState() == RocketState.TIPPING
                    || player.getMainHandItem()
                            .isCorrectToolForDrops(Blocks.STONE.defaultBlockState())) {
                dropNDie();
            }
        }
        return true;
    }

    public ItemStack asItem() {
        RocketStruct rocket = getRocket();
        if (rocket.stages.isEmpty()) {
            ItemStack capsule = new ItemStack(rocket.capsule.item);
            if (capsule.getItem() instanceof SatelliteFrequencyProvider)
                SatelliteFrequencyProvider.setFrequency(capsule, satFreq);
            if (satOrbit != null)
                capsule.set(ModDataComponentsSpace.SATELLITE_ORBIT.get(), satOrbit);
            return capsule;
        }
        rocket.satFreq = satFreq;
        rocket.satOrbit = satOrbit;
        return CustomRocketItem.build(rocket, true);
    }

    public void retrieve(Player player) {
        ItemStack rocket = asItem();
        ItemStack drive = navDrive;
        navDrive = ItemStack.EMPTY;
        discard();
        player.getInventory().placeItemBackInInventory(rocket);
        if (!drive.isEmpty()) player.getInventory().placeItemBackInInventory(drive);
    }

    public void dropNDie() {
        if (level() instanceof ServerLevel server) {
            spawnAtLocation(server, asItem(), 0.0F);
            if (!navDrive.isEmpty()) spawnAtLocation(server, navDrive, 0.0F);
        }

        discard();
    }

    @Override
    protected void exhaust(Vec3 step) {
        RocketState state = getState();
        float motionY = entityData.get(MOTION_Y);

        if (state == RocketState.AWAITING
                || state == RocketState.LANDED
                || (state == RocketState.LANDING && motionY <= -0.4)
                || state == RocketState.DOCKING
                || state == RocketState.UNDOCKING
                || state == RocketState.NEEDSFUEL
                || (state == RocketState.TRANSFER
                        && OrbitalStation.clientStation.getUnscaledProgress(0) > 0.15)) {
            return;
        }

        double x = getX();
        double y = getY();
        double z = getZ();

        if (motionY > 0) {
            x = xo;
            y = yo;
            z = zo;
        }

        RocketStruct rocket = getRocket();
        if (rocket.stages.isEmpty()) {
            if (state == RocketState.TIPPING) return;

            if (isReusable()) {
                gasFlame(x + 0.5, y, z, 0, -1, 0);
                gasFlame(x - 0.5, y, z, 0, -1, 0);
                gasFlame(x, y, z + 0.5, 0, -1, 0);
                gasFlame(x, y, z - 0.5, 0, -1, 0);
            } else {
                double r = RocketPart.radius(rocket.capsule.bottom) * 0.5;
                gasFlame(x + r, y, z + r, 0.25, -0.75, 0.25);
                gasFlame(x - r, y, z + r, -0.25, -0.75, 0.25);
                gasFlame(x + r, y, z - r, 0.25, -0.75, -0.25);
                gasFlame(x - r, y, z - r, -0.25, -0.75, -0.25);
            }

            groundShock(x, y, z);
            return;
        }

        RocketStage stage = rocket.stages.getFirst();

        if (state == RocketState.LANDING) {
            gasFlame(x, y, z, 0.0, -1.0, 0.0);
            groundShock(x, y, z);
        } else if (state == RocketState.LAUNCHING || getStateTimer() < 200) {
            var nose = noseAxis();
            plume(step, nose, 0, 0, 0);

            int cluster = stage.getCluster();
            double radius = RocketPart.radius(stage.fuselage().bottom);
            for (int c = 1; c < cluster; c++) {
                float spin = (float) c / (float) (cluster - 1);
                plume(
                        step,
                        nose,
                        Math.cos(spin * Math.PI * 2) * radius,
                        0,
                        Math.sin(spin * Math.PI * 2) * radius);
            }
        }
    }

    @Override
    protected Vec3 noseAxis() {
        return new Vec3(0, 1, 0)
                .zRot(getXRot() * (float) Math.PI / 180F)
                .yRot((getYRot() + 90F) * (float) Math.PI / 180F);
    }

    private void gasFlame(double x, double y, double z, double mx, double my, double mz) {
        ClientEffects.spawnGasFlame(
                level(), x, y, z, mx, my, mz, ClientEventConstants.GAS_FLAME_DEFAULT_SCALE);
    }

    private void groundShock(double x, double y, double z) {
        double groundHeight =
                level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
        if (y - groundHeight < 10) {
            ClientEffects.spawnSmokeShock(
                    level(),
                    x,
                    groundHeight + 1.0,
                    z,
                    1 + random.nextInt(3),
                    1 + random.nextGaussian());
        }
    }

    public RocketStruct getRocket() {
        RocketStruct rocket = CustomRocketItem.get(entityData.get(ROCKET));
        return rocket != null ? rocket : new RocketStruct();
    }

    public void setRocket(RocketStruct rocket) {
        entityData.set(ROCKET, CustomRocketItem.build(rocket));
    }

    public RocketState getState() {
        return RocketState.values()[entityData.get(STATE)];
    }

    public void setState(RocketState state) {
        entityData.set(STATE, state.ordinal());
        entityData.set(TIMER, 0);
        stateTimer = 0;
    }

    public Target getTarget() {
        if (destinationOverride != null)
            return new Target(destinationOverride.body().getBody(), false, true);
        return VOTVdriveItem.getTarget(entityData.get(DRIVE), level());
    }

    public @Nullable Destination getDestination() {
        if (destinationOverride != null) return destinationOverride;
        ItemStack drive = entityData.get(DRIVE);
        return drive.getItem() instanceof VOTVdriveItem
                ? VOTVdriveItem.getDestination(drive)
                : null;
    }

    public void setDrive(ItemStack drive) {
        entityData.set(DRIVE, drive.copy());
    }

    public int getStateTimer() {
        return entityData.get(TIMER);
    }

    public void setStateTimer(int timer) {
        entityData.set(TIMER, timer);
    }

    private static Vec3 direction(float pitch, float yaw, double length) {
        float p = pitch * Mth.DEG_TO_RAD;
        float w = yaw * Mth.DEG_TO_RAD;
        return new Vec3(
                -Mth.sin(w) * Mth.cos(p) * length,
                -Mth.sin(p) * length,
                Mth.cos(w) * Mth.cos(p) * length);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setState(RocketState.values()[input.getIntOr("state", 0)]);
        input.read("rocket", RocketStruct.CODEC).ifPresent(this::setRocket);
        navDrive = input.read("drive", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        satFreq = input.getIntOr("freq", 0);
        satOrbit = input.read("satOrbit", SatelliteOrbit.CODEC).orElse(null);
        destinationOverride = input.read("override", Destination.CODEC).orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("state", getState().ordinal());
        output.store("rocket", RocketStruct.CODEC, getRocket());
        if (!navDrive.isEmpty()) output.store("drive", ItemStack.CODEC, navDrive);
        output.putInt("freq", satFreq);
        if (satOrbit != null) output.store("satOrbit", SatelliteOrbit.CODEC, satOrbit);
        if (destinationOverride != null)
            output.store("override", Destination.CODEC, destinationOverride);
    }

    @Override
    public void buildLookOverlay(Level level, LookOverlay info) {
        RocketState state = getState();
        if (state == RocketState.LAUNCHING
                || state == RocketState.LANDING
                || state == RocketState.TIPPING
                || state == RocketState.DOCKING
                || state == RocketState.UNDOCKING) {
            return;
        }

        Component title = getType().getDescription();

        if (state == RocketState.TRANSFER) {
            OrbitalStation station = OrbitalStation.clientStation;
            double progress = station.getUnscaledProgress(0);
            info.row(
                    Component.translatable("station.travelling")
                            .append(": ")
                            .withStyle(ChatFormatting.AQUA)
                            .append(
                                    Component.translatable("body." + station.target.name)
                                            .withStyle(ChatFormatting.WHITE)));
            info.row(
                    Component.translatable("station.progress")
                            .append(": ")
                            .withStyle(ChatFormatting.AQUA)
                            .append(
                                    Component.literal(Math.round(progress * 100) + "%")
                                            .withStyle(ChatFormatting.WHITE)));
            info.title(title);
            return;
        }

        RocketStruct rocket = getRocket();
        if (rocket.stages.isEmpty() && !CelestialBody.inOrbit(level) && !isReusable()) return;

        Target from = CelestialBody.getTarget(level, getBlockX(), getBlockZ());
        Target to = getTarget();

        boolean canLaunch = to.body() != null && state == RocketState.AWAITING;

        if (state == RocketState.NEEDSFUEL) {
            info.row(
                    Component.translatable("overlay.hbmspace.rocket.noFuel")
                            .withStyle(ChatFormatting.RED));
        } else if (canLaunch
                && !rocket.hasSufficientFuel(
                        from.body(), to.body(), from.inOrbit(), to.inOrbit())) {
            info.row(
                    Component.translatable("overlay.hbmspace.rocket.unreachable")
                            .withStyle(ChatFormatting.RED));
            canLaunch = false;
        }

        Entity rider = getFirstPassenger();
        if (rider == null) {
            info.row(Component.translatable("overlay.hbmspace.rocket.enter"));
        } else if (!(rider instanceof Player player && player.isLocalPlayer())) {
            info.row(Component.translatable("overlay.hbmspace.rocket.occupied"));
        } else {
            if (to.inOrbit()) {
                info.row(Component.translatable("desc.item.votvDrive.destinationStation"));
            } else if (to.body() != null) {
                info.row(
                        Component.translatable(
                                "desc.item.votvDrive.destination",
                                Component.translatable("body." + to.body().name)));
            } else {
                info.row(Component.translatable("overlay.hbmspace.rocket.noDrive"));
            }

            if (canLaunch) {
                info.row(Component.translatable("overlay.hbmspace.rocket.jump"));
            } else if (state == RocketState.LANDED) {
                info.row(Component.translatable("overlay.hbmspace.rocket.nextDrive"));
            }

            ItemStack held = player.getMainHandItem();
            if ((state == RocketState.LANDED || state == RocketState.AWAITING)
                    && held.getItem() instanceof VOTVdriveItem
                    && VOTVdriveItem.getProcessed(held)) {
                info.row(Component.translatable("overlay.hbmspace.rocket.swapDrive"));
            }
        }

        info.title(title);
    }

    @Override
    public Item missileItem() {
        return MissilePartsSpace.ROCKET_CUSTOM.get();
    }

    @Override
    public void detonate(@Nullable BlockHitResult mop) {}

    private void onRocketImpact() {
        RocketState state = getState();
        if (state != RocketState.LANDING && state != RocketState.DOCKING) return;

        setDeltaMovement(Vec3.ZERO);

        if (state == RocketState.DOCKING) return;

        RocketStruct rocket = getRocket();
        if (!rocket.stages.isEmpty() && rocket.stages.getFirst().fins() == null) {
            setState(RocketState.TIPPING);
            willExplode = true;
        } else {
            setState(RocketState.LANDED);
        }

        destinationOverride = null;

        setPos(
                getX(),
                level().getHeight(Heightmap.Types.MOTION_BLOCKING, getBlockX(), getBlockZ()),
                getZ());
    }

    public static class EntityRideableRocketDummy extends Entity
            implements EntityLookOverlayProvider {

        private static final EntityDataAccessor<Integer> PARENT_ID =
                SynchedEntityData.defineId(
                        EntityRideableRocketDummy.class, EntityDataSerializers.INT);

        public @Nullable RideableRocketEntity parent;

        public EntityRideableRocketDummy(
                EntityType<? extends EntityRideableRocketDummy> type, Level level) {
            super(type, level);
        }

        public EntityRideableRocketDummy(Level level, RideableRocketEntity parent) {
            this(RocketryEntitiesSpace.RIDEABLE_ROCKET_DUMMY.get(), level);
            this.parent = parent;
            entityData.set(PARENT_ID, parent.getId());
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
            builder.define(PARENT_ID, 0);
        }

        @Override
        public void tick() {
            if (!level().isClientSide()) {
                if (parent == null || parent.isRemoved()) discard();
            } else if (parent == null
                    && level().getEntity(entityData.get(PARENT_ID))
                            instanceof RideableRocketEntity rocket) {
                parent = rocket;
            }
        }

        @Override
        protected void readAdditionalSaveData(ValueInput input) {}

        @Override
        protected void addAdditionalSaveData(ValueOutput output) {}

        @Override
        public void buildLookOverlay(Level level, LookOverlay info) {
            if (parent != null) parent.buildLookOverlay(level, info);
        }

        @Override
        public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
            return parent == null
                    ? InteractionResult.PASS
                    : parent.interact(player, hand, location);
        }

        @Override
        public boolean isPickable() {
            return true;
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            return parent != null && parent.hurtServer(level, source, amount);
        }
    }
}
