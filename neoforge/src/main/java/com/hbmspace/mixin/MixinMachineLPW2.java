/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.mixin;

import com.hbm.blocks.ModBlockEntities;
import com.hbm.blocks.TickingEntityBlock;
import com.hbm.blocks.machine.LPW2Block;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.Blink;
import com.hbmspace.blockentity.machine.LPW2SpaceBlockEntity;
import com.hbmspace.dim.CelestialBody;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LPW2Block.class)
abstract class MixinMachineLPW2 extends MultiblockCoreBlock
        implements TickingEntityBlock, CapabilityBlock, LookOverlayProvider {

    protected MixinMachineLPW2(Properties properties) {
        super(properties);
    }

    @Override
    public Direction getDirModified(Direction dir) {
        return dir.getCounterClockWise();
    }

    @Inject(method = "newBlockEntity", at = @At("HEAD"), cancellable = true)
    private void hbmspace$engine(
            BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        cir.setReturnValue(new LPW2SpaceBlockEntity(pos, state));
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(ModBlockEntities.LPW2)
                .fluidIn()
                .fluidFaces(
                        LPW2SpaceBlockEntity.class,
                        (be, face) -> CelestialBody.inOrbit(be.getLevel()));
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof LPW2SpaceBlockEntity thruster)) return;
        info.title(getName());
        if (!thruster.isFacingPrograde()) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.engineFacing"))
                            .append(" ! ! !"),
                    Blink.lit() ? 0xFF0000 : 0xFFFF00);
            return;
        }
        for (FluidTankNTM tank : thruster.tanks) {
            info.row(
                    Component.literal("-> ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(
                                    Component.translatable(
                                                    "overlay.hbmspace.launchPadRocket.tank",
                                                    NTMFluidProperties.getDisplayName(
                                                            tank.getTankType()),
                                                    tank.getFill(),
                                                    tank.getMaxFill())
                                            .withStyle(ChatFormatting.WHITE)));
        }
    }
}
