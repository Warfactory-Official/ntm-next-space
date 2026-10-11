// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.items.armor.ItemModMedal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemModMedal.class)
public abstract class MixinItemModMedal {
    @Redirect(
            method = "modUpdate",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/util/ContaminationUtil;"
                                            + "neutronActivateInventory(Lnet/minecraft/world/entity/player/Player;FF)Z"))
    private boolean hbmspace$keepActivation(Player player, float radiation, float decay) {
        return false;
    }
}
