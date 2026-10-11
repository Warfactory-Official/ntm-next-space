// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.ore;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

final class MorkiteSoundType extends SoundType {

    static final SoundType INSTANCE = new MorkiteSoundType();

    private static final ThreadLocal<Boolean> BREAKING =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private MorkiteSoundType() {
        super(
                0.85F,
                0.85F,
                SoundType.STONE.getBreakSound(),
                SoundType.STONE.getStepSound(),
                SoundType.STONE.getPlaceSound(),
                SoundType.STONE.getHitSound(),
                SoundType.STONE.getFallSound());
    }

    @Override
    public SoundEvent getBreakSound() {
        BREAKING.set(Boolean.TRUE);
        return CelestialOresSpace.STEP_MORKITE.get();
    }

    @Override
    public SoundEvent getPlaceSound() {
        BREAKING.set(Boolean.FALSE);
        return super.getPlaceSound();
    }

    @Override
    public SoundEvent getStepSound() {
        BREAKING.set(Boolean.FALSE);
        return super.getStepSound();
    }

    @Override
    public float getPitch() {
        float base = BREAKING.get() ? super.getPitch() - 0.15F : super.getPitch();
        return base + ThreadLocalRandom.current().nextFloat() * 0.2F;
    }
}
