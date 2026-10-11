// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.explosion.ExplosionNukeCustom;
import com.hbmspace.entity.BombEntitiesSpace;
import com.hbmspace.entity.effect.TorexAntimatter;
import com.hbmspace.entity.logic.EntityAntimatterBlast;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExplosionNukeCustom.class)
public abstract class MixinExplosionNukeCustom {

    @WrapOperation(
            method = "explodeCustom",
            at = @At(value = "NEW", target = "com/hbm/entity/logic/EntityBalefire"))
    private static EntityBalefire hbmspace$antimatterBlast(
            EntityType<? extends EntityBalefire> type,
            Level level,
            Operation<EntityBalefire> original) {
        return new EntityAntimatterBlast(BombEntitiesSpace.ANTIMATTER_BLAST.get(), level);
    }

    @WrapOperation(
            method = "explodeCustom",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/entity/effect/EntityNukeTorex;statFacBale(Lnet/minecraft/world/level/Level;DDDF)Lcom/hbm/entity/effect/EntityNukeTorex;"))
    private static EntityNukeTorex hbmspace$antimatterTorex(
            Level level,
            double x,
            double y,
            double z,
            float scale,
            Operation<EntityNukeTorex> original) {
        return TorexAntimatter.startFacAnti(level, x, y, z, scale);
    }
}
