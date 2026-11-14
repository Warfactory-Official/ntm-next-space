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

package com.hbmspace.client.render.mob;

import com.hbm.client.render.flywheel.MobObjVisual;
import com.hbm.client.render.flywheel.ObjEntityVisual;
import com.hbm.render.loader.HFRWavefrontObject;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public record ObjMobRig<T extends LivingEntity>(
        Supplier<HFRWavefrontObject> mesh,
        List<Identifier> skins,
        ToIntFunction<T> skin,
        Map<String, Identifier> groupSkins,
        ObjEntityVisual.Pose<T> pose,
        List<MobObjVisual.Part<T>> parts,
        float shadowRadius) {

    MobObjVisual.Spec<T> spec(int skinIndex, Predicate<T> instanced) {
        List<MobObjVisual.Part<T>> sheeted = new ArrayList<>(parts.size());
        for (MobObjVisual.Part<T> part : parts) {
            Identifier own = groupSkins.get(part.name());
            sheeted.add(own == null ? part : part.material(() -> material(own)));
        }
        Identifier texture = skins.get(skinIndex);
        return new MobObjVisual.Spec<>(
                mesh,
                () -> material(texture),
                List.copyOf(sheeted),
                pose,
                shadowRadius,
                1.0F,
                instanced);
    }

    private static Material material(Identifier texture) {
        return SimpleMaterial.builderOf(Materials.CUTOUT)
                .texture(texture)
                .mipmap(false)
                .cutout(CutoutShaders.ONE_TENTH)
                .cardinalLightingMode(CardinalLightingMode.ENTITY)
                .backfaceCulling(false)
                .build();
    }
}
