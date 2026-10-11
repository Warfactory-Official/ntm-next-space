// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.AudioLoop;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blockentity.machine.storage.BlockEntityCrateTungsten;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorFire;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.items.ISatChip;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.AudioWrapper;
import com.hbmspace.blockentity.IDysonConverter;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import com.hbmspace.dim.trait.CBT_Dyson;
import com.hbmspace.explosion.vanillant.standard.CustomDamageHandlerDyson;
import com.hbmspace.particle.FlareParticleOptions;
import com.hbmspace.saveddata.satellites.SatelliteDysonRelay;
import com.hbmspace.sound.SkySoundsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class BlockEntityDysonReceiver extends BlockEntityMachineBase
        implements SyncUnitSchema, AudioLoop {

    private static final int MAX_LENGTH = 24;
    private static final int TOWER_TOP = 18;

    @SyncField(units = 1L)
    public boolean isReceiving;

    @SyncField(units = 1L)
    public int swarmId;

    @SyncField(units = 1L)
    public int swarmCount;

    @SyncField(units = 1L)
    public int swarmConsumers;

    @SyncField(units = 1L)
    public int beamLength;

    public BlockEntityDysonReceiver(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.DYSON_RECEIVER.get(), pos, state, 1);
    }

    public static long getEnergyOutput(int swarmCount) {
        double adjustedDensity = (double) swarmCount / 1024.0D;
        long maxOutput = Long.MAX_VALUE / 10;
        double b = 32.0D;
        double c = 1.3D;
        double gompertz = Math.exp(-b * Math.exp(-c * adjustedDensity));
        return (long) (maxOutput * gompertz) / 20;
    }

    @Override
    public void tickServer() {
        ServerLevel level = (ServerLevel) this.level;
        Direction dir = BlockMultiblockCore.coreFacing(getBlockState()).getOpposite();

        swarmId = ISatChip.getFreqS(getItem(0));

        Satellite sat =
                SatelliteSavedData.at(level, worldPosition.getX(), worldPosition.getZ())
                        .getSatFromFreq(swarmId);
        int sun =
                level.getBrightness(LightLayer.SKY, worldPosition.above(TOWER_TOP))
                        - level.getSkyDarken()
                        - 11;

        swarmCount = CBT_Dyson.count(level, swarmId);
        swarmConsumers = CBT_Dyson.consumers(level, swarmId);

        isReceiving =
                (sat instanceof SatelliteDysonRelay || sun > 0)
                        && swarmId > 0
                        && !occluded(level)
                        && swarmCount > 0
                        && swarmConsumers > 0;

        if (isReceiving) {
            long energyOutput = getEnergyOutput(swarmCount) / swarmConsumers;
            fire(level, dir, energyOutput);
        }

        networkPackNT(250);
    }

    @Override
    public void tickClient() {
        audioLoop(isReceiving, 0.75F, 0.85F);
        if (!isReceiving) return;

        RandomSource random = level.getRandom();
        if (random.nextInt(10) != 0) return;
        double x = worldPosition.getX() - 5 + random.nextDouble() * 10;
        double z = worldPosition.getZ() - 5 + random.nextDouble() * 10;
        double yd = 0.1 + random.nextFloat() * 0.1;
        float scale = 4F + random.nextFloat() * 2;
        level.addParticle(
                new FlareParticleOptions(scale),
                true,
                false,
                x,
                worldPosition.getY() + 11,
                z,
                0,
                yd,
                0);
    }

    @Override
    public AudioWrapper createAudioLoop() {
        Direction dir = BlockMultiblockCore.coreFacing(getBlockState()).getOpposite();
        return AudioSystem.getLoopedSound(
                SkySoundsSpace.DYSON_BEAM.get(),
                SoundSource.BLOCKS,
                worldPosition.getX() + dir.getStepX() * 8,
                worldPosition.getY(),
                worldPosition.getZ() + dir.getStepZ() * 8,
                0.75F,
                20F,
                1.0F,
                20);
    }

    private void fire(ServerLevel level, Direction dir, long energyOutput) {
        beamLength = MAX_LENGTH;
        for (int i = 9; i < MAX_LENGTH; i++) {
            BlockPos pos = worldPosition.relative(dir, i).above();
            BlockState state = level.getBlockState(pos);

            boolean detonate = true;
            BlockEntity be = null;
            if (i > 10) {
                BlockPos core = MultiblockSurface.coreOfAny(level, pos, state);
                be = level.getBlockEntity(core != null ? core : pos);

                if (be instanceof IDysonConverter converter) {
                    detonate = !converter.provideEnergy(pos, energyOutput);
                } else if (be instanceof BlockEntityCrateTungsten crate) {
                    crate.addEnergy(level, pos, energyOutput / 5000, Direction.NORTH);
                    detonate = false;
                }
            }

            if (state.isSolidRender() || be != null) {
                if (detonate) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                    ExplosionVNT vnt =
                            new ExplosionVNT(level, pos.getX(), pos.getY(), pos.getZ(), 3);
                    vnt.setBlockAllocator(new BlockAllocatorStandard());
                    vnt.setBlockProcessor(
                            new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
                    vnt.setEntityProcessor(new EntityProcessorStandard().allowSelfDamage());
                    vnt.setPlayerProcessor(new PlayerProcessorStandard());
                    vnt.setSFX(new ExplosionEffectStandard());
                    vnt.explode();
                }

                beamLength = i;
                break;
            }
        }

        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();
        AABB beam =
                new AABB(
                        Math.min(x, x + dir.getStepX() * beamLength) + 0.2,
                        y + 0.2,
                        Math.min(z, z + dir.getStepZ() * beamLength) + 0.2,
                        Math.max(x, x + dir.getStepX() * beamLength) + 0.8,
                        y + 1.8,
                        Math.max(z, z + dir.getStepZ() * beamLength) + 0.8);

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, beam)) {
            ExplosionVNT vnt =
                    new ExplosionVNT(
                            level,
                            entity.getX() - dir.getStepX(),
                            entity.getY() + 1.5,
                            entity.getZ() - dir.getStepZ(),
                            3);
            vnt.setBlockAllocator(new BlockAllocatorStandard());
            vnt.setBlockProcessor(
                    new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
            vnt.setEntityProcessor(
                    new EntityProcessorStandard()
                            .withDamageMod(new CustomDamageHandlerDyson(energyOutput)));
            vnt.setPlayerProcessor(new PlayerProcessorStandard());
            vnt.setSFX(new ExplosionEffectStandard());
            vnt.explode();
        }
    }

    private boolean occluded(ServerLevel level) {
        int floor = worldPosition.getY() + 10;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int x = worldPosition.getX() + dx;
                int z = worldPosition.getZ() + dz;
                for (int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                        y >= floor;
                        y--) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (state.getLightDampening() == 0) continue;
                    if (state.getBlock() instanceof BlockMultiblockCore
                            || BlockMultiblockCore.isCell(state)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public long syncUnitMask() {
        return 1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeBoolean(isReceiving);
        output.writeInt(swarmId);
        output.writeInt(swarmCount);
        output.writeInt(swarmConsumers);
        output.writeInt(beamLength);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        isReceiving = input.readBoolean();
        swarmId = input.readInt();
        swarmCount = input.readInt();
        swarmConsumers = input.readInt();
        beamLength = input.readInt();
    }
}
