// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.fluid.BlockFluidClassicBase;
import com.hbm.blocks.fluid.BlockSchrabidic;
import com.hbm.blocks.fluid.ClassicFluid;
import com.hbm.lib.ModDamageTypes;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.ContaminationUtil;
import com.hbmspace.blocks.fluid.SchrabidicReactions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockSchrabidic.class)
public abstract class MixinBlockSchrabidic extends BlockFluidClassicBase {

    protected MixinBlockSchrabidic(ClassicFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void hbmspace$acidInsteadOfWeb(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effects,
            boolean precise,
            CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            Vec3 motion = entity.getDeltaMovement();
            if (motion.y < -0.2D)
                entity.setDeltaMovement(new Vec3(motion.x, motion.y * 0.5D, motion.z));
            if (level instanceof ServerLevel server) {
                entity.hurtServer(server, level.damageSources().source(ModDamageTypes.ACID), 10F);
                ContaminationUtil.contaminate(
                        living, HazardType.RADIATION, ContaminationType.CREATIVE, 1.0F);
            }
        }
        ci.cancel();
    }

    @Inject(
            method = "neighborChanged",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/Level;"
                                            + "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
            cancellable = true)
    private void hbmspace$noNeighborReaction(
            BlockState state,
            Level level,
            BlockPos pos,
            Block block,
            @Nullable Orientation orientation,
            boolean movedByPiston,
            CallbackInfo ci) {
        ci.cancel();
    }

    @Override
    protected void flowTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.VALUES) {
            SchrabidicReactions.react(level, pos.relative(direction), random);
        }
    }
}
