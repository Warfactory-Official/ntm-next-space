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

package com.hbmspace.client.sky;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import java.nio.ByteBuffer;
import net.minecraft.client.renderer.DynamicUniformStorage.DynamicUniform;

public final class PlanetInfo implements DynamicUniform {

    public static final int MAX_SHOCKS = 4;
    public static final int SIZE =
            new Std140SizeCalculator()
                    .putVec4()
                    .putVec4()
                    .putVec4()
                    .putVec4()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putFloat()
                    .putInt()
                    .putInt()
                    .putInt()
                    .putInt()
                    .putInt()
                    .get();

    public final float[] nukeShockTime = {-1, -1, -1, -1};
    public final float[] nukeShockCenterX = {0.5F, 0.5F, 0.5F, 0.5F};
    public final float[] nukeShockCenterY = {0.5F, 0.5F, 0.5F, 0.5F};
    public final float[] nukeShockStrength = new float[MAX_SHOCKS];
    public float atmosphereColorR;
    public float atmosphereColorG;
    public float atmosphereColorB;
    public float cloudColorR;
    public float cloudColorG;
    public float cloudColorB;
    public float cloudTintStrength;
    public float cloudStormDarkness;
    public float atmosphereAlpha;
    public float atmosphereTime;
    public float patternOffset;
    public float impactTime;
    public float phase;
    public float offset;
    public float atmosphereDensity;
    public float cloudLightningStrength;
    public float eveFlashStrength;
    public int nukeShockCount;
    public int atmosphereStyle;
    public int lightningMode;
    public int blackouts;
    public int useBodyAlphaMask;

    @Override
    public void write(ByteBuffer buffer) {
        Std140Builder.intoBuffer(buffer)
                .putVec4(nukeShockTime[0], nukeShockTime[1], nukeShockTime[2], nukeShockTime[3])
                .putVec4(
                        nukeShockCenterX[0],
                        nukeShockCenterX[1],
                        nukeShockCenterX[2],
                        nukeShockCenterX[3])
                .putVec4(
                        nukeShockCenterY[0],
                        nukeShockCenterY[1],
                        nukeShockCenterY[2],
                        nukeShockCenterY[3])
                .putVec4(
                        nukeShockStrength[0],
                        nukeShockStrength[1],
                        nukeShockStrength[2],
                        nukeShockStrength[3])
                .putFloat(atmosphereColorR)
                .putFloat(atmosphereColorG)
                .putFloat(atmosphereColorB)
                .putFloat(cloudColorR)
                .putFloat(cloudColorG)
                .putFloat(cloudColorB)
                .putFloat(cloudTintStrength)
                .putFloat(cloudStormDarkness)
                .putFloat(atmosphereAlpha)
                .putFloat(atmosphereTime)
                .putFloat(patternOffset)
                .putFloat(impactTime)
                .putFloat(phase)
                .putFloat(offset)
                .putFloat(atmosphereDensity)
                .putFloat(cloudLightningStrength)
                .putFloat(eveFlashStrength)
                .putInt(nukeShockCount)
                .putInt(atmosphereStyle)
                .putInt(lightningMode)
                .putInt(blackouts)
                .putInt(useBodyAlphaMask);
    }
}
