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

import com.hbm.blocks.machine.EPressBlock;
import com.hbm.blocks.machine.PressBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.interfaces.ToolType;
import com.hbm.items.tool.ToolInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ToolInteractions.class)
public abstract class MixinToolUse {
    @Inject(
            method =
                    "useOn(Lnet/minecraft/world/item/context/UseOnContext;Lcom/hbm/interfaces/ToolType;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void hbmspace$pressDummyDrill(
            UseOnContext ctx, ToolType tool, CallbackInfoReturnable<Boolean> cir) {
        if (tool != ToolType.HAND_DRILL) return;
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockPos core = MultiblockSurface.coreOfAny(level, pos);
        if (core == null || core.equals(pos)) return;
        var owner = level.getBlockState(core).getBlock();
        if (!(owner instanceof PressBlock || owner instanceof EPressBlock)) return;
        MultiblockCoreBlock.withoutTeardown(() -> level.removeBlock(pos, false));
        cir.setReturnValue(true);
    }
}
