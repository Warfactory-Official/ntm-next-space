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

package com.hbmspace.client.gui;

import com.hbm.util.GameTime;
import com.hbmspace.entity.RocketryEntitiesSpace;
import com.hbmspace.entity.missile.RideableRocketEntity;
import com.hbmspace.handler.RocketStruct;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

final class RocketPreview {

    private final int left, top, width, height;
    private final float originX, originY;
    private @Nullable RideableRocketEntity entity;
    private @Nullable RocketStruct shown;

    RocketPreview(int left, int top, int width, int height, int originX, int originY) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.originX = originX - left - width / 2F;
        this.originY = originY - top - height / 2F;
    }

    void draw(Submitter submitter, RocketStruct rocket, float scale, float lift) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        if (entity == null) {
            entity =
                    new RideableRocketEntity(
                            RocketryEntitiesSpace.RIDEABLE_ROCKET.get(), minecraft.level);
            entity.preview = true;
        }
        if (!Objects.equals(shown, rocket)) {
            entity.setRocket(rocket);
            shown = rocket.copy();
        }

        float angle = GameTime.now() / 10 % 360 * Mth.DEG_TO_RAD;
        Quaternionf rotation = new Quaternionf().rotateZ(Mth.PI).rotateY(-angle);
        Vector3f translation = new Vector3f(originX / scale, originY / scale + lift, 0F);

        EntityRenderState state =
                minecraft
                        .getEntityRenderDispatcher()
                        .getRenderer(entity)
                        .createRenderState(entity, 0F);
        state.shadowPieces.clear();

        submitter.submit(state, scale, translation, rotation, left, top, width, height);
    }

    @FunctionalInterface
    interface Submitter {
        void submit(
                EntityRenderState state,
                float scale,
                Vector3f translation,
                Quaternionf rotation,
                int left,
                int top,
                int width,
                int height);
    }
}
