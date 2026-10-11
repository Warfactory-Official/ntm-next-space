// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2024-2026 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import com.hbmspace.dim.CelestialBody;
import net.minecraft.nbt.CompoundTag;

public class CBT_Destroyed extends CelestialBodyTrait {

    public float destProgress;

    public CBT_Destroyed() {}

    public CBT_Destroyed(float destProgress) {
        this.destProgress = destProgress;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putFloat("destProgress", destProgress);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        destProgress = nbt.getFloatOr("destProgress", 0);
    }

    @Override
    public void update(boolean isRemote, CelestialBody body) {
        if (isRemote) {
            destProgress =
                    Math.min(201.0f, destProgress + 0.0025f * (201.0f - destProgress) * 0.15f);
            if (destProgress >= 200) {
                destProgress = 0;
            }
        }
    }
}
