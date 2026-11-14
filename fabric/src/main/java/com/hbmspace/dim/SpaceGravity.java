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

package com.hbmspace.dim;

import com.hbmspace.HbmSpace;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import com.hbmspace.items.armor.ModHeavyBootsItem;
import com.hbmspace.util.AstronomyUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class SpaceGravity {

    public static final Identifier BODY =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "body_gravity");

    private SpaceGravity() {}

    static void onLoad(Entity entity, ServerLevel level) {
        if (!(entity instanceof LivingEntity living)) return;
        if (CelestialBody.inOrbit(level)) {
            SpaceLivingProps.setAtmosphere(
                    living, ChunkAtmosphereManager.proxy.getAtmosphere(living));
        } else {
            measure(living);
        }
    }

    public static void measure(LivingEntity entity) {
        apply(entity, CelestialBody.getGravity(entity));
    }

    public static void apply(LivingEntity entity, float gravity) {
        float pull =
                gravity >= 1.5F && gravity <= 1.7F
                        ? AstronomyUtil.STANDARD_GRAVITY
                        : gravity == 0 ? 0 : Math.max(gravity, 0.2F);
        scale(entity, Attributes.GRAVITY, (double) pull / AstronomyUtil.STANDARD_GRAVITY);

        double fall =
                gravity < 0.3F
                        ? 0
                        : gravity < 1.5F ? (double) gravity / AstronomyUtil.STANDARD_GRAVITY : 1;
        scale(entity, Attributes.FALL_DAMAGE_MULTIPLIER, fall);
        scale(entity, Attributes.SAFE_FALL_DISTANCE, fall == 0 ? 1 : 1 / fall);
    }

    private static void scale(LivingEntity entity, Holder<Attribute> attribute, double factor) {
        AttributeMap attributes = entity.getAttributes();
        boolean held = attributes.hasModifier(attribute, BODY);
        if (held && attributes.getModifierValue(attribute, BODY) == factor - 1) return;
        if (factor != 1) {
            entity.getAttribute(attribute)
                    .addOrUpdateTransientModifier(
                            new AttributeModifier(
                                    BODY,
                                    factor - 1,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (held) {
            entity.getAttribute(attribute).removeModifier(BODY);
        }
    }

    public static double effective(LivingEntity entity, double gravity) {
        AttributeMap attributes = entity.getAttributes();
        if (!attributes.hasModifier(Attributes.GRAVITY, BODY)) return gravity;
        double ratio = 1 + attributes.getModifierValue(Attributes.GRAVITY, BODY);
        if (ratio == 0) {
            if (!(entity instanceof Player player)) return gravity;
            return (player.isShiftKeyDown() ? 0.01F : 0F) - (player.isJumping() ? 0.01F : 0F);
        }
        boolean standard =
                ratio > 1
                        ? entity.getDeltaMovement().y >= 0
                        : entity instanceof Player player && ModHeavyBootsItem.pulls(player);
        return standard ? gravity / ratio : gravity;
    }

    public static boolean weightless(LivingEntity entity) {
        AttributeMap attributes = entity.getAttributes();
        return attributes.hasModifier(Attributes.GRAVITY, BODY)
                && attributes.getModifierValue(Attributes.GRAVITY, BODY) == -1;
    }
}
