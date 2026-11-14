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
