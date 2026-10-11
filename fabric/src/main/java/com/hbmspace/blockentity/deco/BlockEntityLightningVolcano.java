// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.deco;

import com.hbm.blockentity.Synced;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.lib.ModDamageTypes;
import com.hbm.packet.PacketWire;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.packet.toclient.PlasmaBlastPayload;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class BlockEntityLightningVolcano extends BlockEntity implements Synced, SyncUnitSchema {

    public static final int RANGE = 256;

    private static final int UNCHARGED = -1;

    @SyncField(units = 1L)
    public int chargetime = UNCHARGED;

    public float flashd;

    public BlockEntityLightningVolcano(BlockPos pos, BlockState state) {
        super(TerrainBlockEntitiesSpace.LIGHTNING_VOLCANO.get(), pos, state);
    }

    public void charge(int roll) {
        chargetime = roll + 100;
        setChanged();
    }

    private void integrateFlash() {
        if (chargetime > 0) {
            flashd = 0;
        } else {
            flashd += 0.3F;
            flashd = Math.min(100.0F, flashd + 0.3F * (100.0F - flashd) * 0.15F);
        }
    }

    public static void tickClient(
            Level level, BlockPos pos, BlockState state, BlockEntityLightningVolcano be) {
        be.integrateFlash();
    }

    public static void tickServer(
            Level level, BlockPos pos, BlockState state, BlockEntityLightningVolcano be) {
        if (be.chargetime == UNCHARGED) be.charge(level.getRandom().nextInt(400));
        be.integrateFlash();

        if (be.chargetime == 1) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 20.5;
            double z = pos.getZ() + 0.5;
            PacketWire.sendToAllAround(
                    new PlasmaBlastPayload(x, y, z, 1F, 1F, 1F, 0F, 90F, 40F),
                    new TargetPoint((ServerLevel) level, x, y, z, RANGE));
            level.playSound(
                    null,
                    x,
                    y,
                    z,
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER,
                    200F,
                    0.8F + level.getRandom().nextFloat() * 0.2F);
            be.vapor((ServerLevel) level, pos);
        }

        if (be.chargetime > 0) be.chargetime--;
        if (be.flashd >= 100) be.chargetime = level.getRandom().nextInt(400) + 100;

        be.networkPackNT(RANGE);
    }

    private void vapor(ServerLevel level, BlockPos pos) {
        AABB column =
                new AABB(
                        pos.getX() - 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() - 0.5,
                        pos.getX() + 1.5,
                        pos.getY() + 60,
                        pos.getZ() + 1.5);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, column)) {
            float damage = Mth.clamp(living.getMaxHealth(), 3, 20);
            if (living.hurtServer(
                    level, level.damageSources().source(ModDamageTypes.ELECTRICITY), damage)) {
                EntitySounds.play(
                        living,
                        living.getX(),
                        living.getY(),
                        living.getZ(),
                        ModSounds.TESLA_ZAP.get(),
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("charge", chargetime);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        chargetime = in.getIntOr("charge", UNCHARGED);
    }

    @Override
    public long syncUnitMask() {
        return 1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeInt(chargetime);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        chargetime = input.readInt();
    }
}
