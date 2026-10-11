// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.api.environment.ISuffocationImmune;
import com.hbm.entity.mob.EntityCyberCrab;
import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.lib.ModDamageTypes;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.entity.missile.EntityRideableRocket;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import com.hbmspace.items.armor.ArmorUtilSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class EntityEffectHandlerSpace {

    private EntityEffectHandlerSpace() {}

    public static void onUpdate(LivingEntity entity) {
        CBT_Atmosphere atmosphere = getAtmosphereCached(entity);

        handleOxy(entity, atmosphere);
        handleCorrosion(entity, atmosphere);

        if (entity instanceof ServerPlayer player) SpaceLivingProps.sync(player);
    }

    private static @Nullable CBT_Atmosphere getAtmosphereCached(LivingEntity entity) {
        if (entity instanceof ServerPlayer || entity.tickCount % 20 == 0) {
            CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(entity);
            SpaceLivingProps.setAtmosphere(entity, atmosphere);
            return atmosphere;
        }

        return SpaceLivingProps.getAtmosphere(entity);
    }

    public static void handleOxy(LivingEntity entity, @Nullable CBT_Atmosphere atmosphere) {
        if (entity instanceof ISuffocationImmune) return;
        if (entity.getVehicle() instanceof EntityRideableRocket) {
            SpaceLivingProps.setTankInUse(entity, false);
            return;
        }

        if (!ArmorUtilSpace.checkForOxy(entity, atmosphere)) {
            SpaceLivingProps.setOxy(entity, SpaceLivingProps.getOxy(entity) - 1);
        } else {
            SpaceLivingProps.setOxy(entity, SpaceLivingProps.FULL_BREATH);
        }
    }

    public static void handleCorrosion(LivingEntity entity, @Nullable CBT_Atmosphere atmosphere) {
        if (entity instanceof EntityGlyphid) return;
        if (entity instanceof EntityCyberCrab) return;
        if (entity.getVehicle() instanceof EntityRideableRocket) return;

        if (ArmorUtilSpace.checkForCorrosion(entity, atmosphere)) {
            ServerLevel level = (ServerLevel) entity.level();
            entity.hurtServer(level, level.damageSources().source(ModDamageTypes.ACID), 1);
        }
    }
}
