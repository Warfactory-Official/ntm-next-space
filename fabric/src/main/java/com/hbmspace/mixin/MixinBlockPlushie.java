// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.generic.BlockPlushie;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockPlushie.class)
public abstract class MixinBlockPlushie implements ITickingBlock {}
