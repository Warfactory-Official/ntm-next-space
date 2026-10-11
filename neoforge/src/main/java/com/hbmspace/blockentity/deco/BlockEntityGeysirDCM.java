// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.deco;

import com.hbm.lib.ModDamageTypes;
import com.hbm.sound.EntitySounds;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BlockEntityGeysirDCM extends BlockEntity {

    public static Consumer<BlockEntityGeysirDCM> CLIENT_EFFECTS = be -> {};

    public BlockEntityGeysirDCM(BlockPos pos, BlockState state) {
        super(TerrainBlockEntitiesSpace.GEYSIR_DCM.get(), pos, state);
    }

    public static void tickClient(
            Level level, BlockPos pos, BlockState state, BlockEntityGeysirDCM be) {
        CLIENT_EFFECTS.accept(be);
    }

    public static void tickServer(
            Level level, BlockPos pos, BlockState state, BlockEntityGeysirDCM be) {
        AABB column =
                new AABB(
                        pos.getX() - 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() - 0.5,
                        pos.getX() + 1.5,
                        pos.getY() + 60,
                        pos.getZ() + 1.5);
        ServerLevel server = (ServerLevel) level;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, column)) {
            float damage = Mth.clamp(living.getMaxHealth() * 0.1F, 3, 20);
            if (living.hurtServer(
                    server, level.damageSources().source(ModDamageTypes.ACID), damage)) {
                EntitySounds.play(
                        living,
                        living.getX(),
                        living.getY(),
                        living.getZ(),
                        SoundEvents.FIRE_EXTINGUISH,
                        living.getSoundSource(),
                        1.0F,
                        1.0F);
            }
        }
    }
}
