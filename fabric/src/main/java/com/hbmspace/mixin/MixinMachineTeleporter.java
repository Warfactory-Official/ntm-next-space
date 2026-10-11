// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityMachineTeleporter;
import com.hbm.blocks.machine.MachineTeleporter;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay.LookInfo;
import com.hbmspace.blockentity.machine.SpaceTeleporterTank;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MachineTeleporter.class)
public abstract class MixinMachineTeleporter {

    @Inject(method = "caps", at = @At("RETURN"), cancellable = true)
    private void hbmspace$receiveNmass(CallbackInfoReturnable<MachineCaps> cir) {
        cir.setReturnValue(cir.getReturnValue().fluidIn());
    }

    @Inject(method = "buildLookOverlay", at = @At("HEAD"), cancellable = true)
    private void hbmspace$crossingFuelLine(
            Level level, BlockPos pos, LookInfo info, CallbackInfo ci) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineTeleporter tele)
                || tele.target == null
                || level.dimension().equals(tele.target.dimension())) return;

        info.title(((MachineTeleporter) (Object) this).getName().getString(), 0xFFFF00, 0x404000);
        info.line(
                (tele.power >= BlockEntityMachineTeleporter.CONSUMPTION
                                ? ChatFormatting.GREEN
                                : ChatFormatting.RED)
                        + String.format(Locale.US, "%,d", tele.power)
                        + " / "
                        + String.format(Locale.US, "%,d", BlockEntityMachineTeleporter.MAX_POWER));
        var tank = ((SpaceTeleporterTank) tele).hbmspace$tank();
        info.line(
                Component.translatable("block.hbmspace.nmass").getString()
                        + ": "
                        + tank.getFill()
                        + "/"
                        + tank.getMaxFill()
                        + "mB");
        info.line(
                "Destination: "
                        + tele.target.pos().getX()
                        + " / "
                        + tele.target.pos().getY()
                        + " / "
                        + tele.target.pos().getZ()
                        + " (D: "
                        + tele.target.dimension().identifier()
                        + ")");
        ci.cancel();
    }
}
