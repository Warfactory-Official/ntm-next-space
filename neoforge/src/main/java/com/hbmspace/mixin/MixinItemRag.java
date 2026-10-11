// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.special.ItemRag;
import com.hbmspace.items.PartsItemsSpace;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemRag.class)
public abstract class MixinItemRag {
    @ModifyArg(
            method = "use",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;)V"))
    private ItemLike hbmspace$bloodRag(ItemLike rag, @Local(argsOnly = true) Player player) {
        return HbmLivingProps.getRadiation(player) > 500 ? PartsItemsSpace.RAG_BLOOD.get() : rag;
    }
}
