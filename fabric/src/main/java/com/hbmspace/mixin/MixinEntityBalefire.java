// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.entity.logic.EntityBalefire;
import com.hbm.explosion.ExplosionBalefire;
import com.hbmspace.entity.logic.EntityAntimatterBlast;
import com.hbmspace.explosion.ExplosionAntimatter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityBalefire.class)
public abstract class MixinEntityBalefire {
    @WrapOperation(
            method = {"tick", "readAdditionalSaveData"},
            at = @At(value = "NEW", target = "com/hbm/explosion/ExplosionBalefire"))
    private ExplosionBalefire hbmspace$antimatterCrater(
            int x, int y, int z, Level level, int range, Operation<ExplosionBalefire> original) {
        return (Object) this instanceof EntityAntimatterBlast
                ? new ExplosionAntimatter(x, y, z, level, range)
                : original.call(x, y, z, level, range);
    }

    @Inject(
            method = "tick",
            at = @At(value = "JUMP", opcode = Opcodes.IFNE, ordinal = 0),
            slice =
                    @Slice(
                            from =
                                    @At(
                                            value = "INVOKE",
                                            target =
                                                    "Lcom/hbm/explosion/ExplosionBalefire;update()Z")))
    private void hbmspace$stepEffects(CallbackInfo ci, @Local(ordinal = 0) boolean finished) {
        if ((Object) this instanceof EntityAntimatterBlast blast) blast.flash();
        EntityAntimatterBlast.stepEffects((EntityBalefire) (Object) this, finished);
    }
}
