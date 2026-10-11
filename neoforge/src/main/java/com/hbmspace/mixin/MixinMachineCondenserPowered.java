// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.machine.MachineCondenserPowered;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.RepairLookOverlay;
import com.hbm.interfaces.ILookOverlay.LookInfo;
import com.hbm.interfaces.IRepairable;
import com.hbm.interfaces.IToolable;
import com.hbmspace.blockentity.machine.BlockEntityCondenserPoweredSpace;
import com.hbmspace.blockentity.machine.ThermalMachineDamage;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MachineCondenserPowered.class)
public abstract class MixinMachineCondenserPowered extends BlockMultiblockCore
        implements IToolable {
    protected MixinMachineCondenserPowered(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ThermalMachineDamage.DAMAGED);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hbmspace$intactDefault(Properties properties, CallbackInfo ci) {
        registerDefaultState(defaultBlockState().setValue(ThermalMachineDamage.DAMAGED, false));
    }

    @Inject(method = "newBlockEntity", at = @At("HEAD"), cancellable = true)
    private void hbmspace$repairable(
            BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        cir.setReturnValue(new BlockEntityCondenserPoweredSpace(pos, state));
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void onExplosionHit(
            BlockState state,
            ServerLevel level,
            BlockPos core,
            Explosion explosion,
            BiConsumer<ItemStack, BlockPos> onHit) {
        var be = (BlockEntityCondenserPoweredSpace) level.getBlockEntity(core);
        if (be.lastExplosion == explosion) return;
        be.lastExplosion = explosion;
        if (be.isDamaged()) level.removeBlock(core, false);
        else be.damage();
    }

    @Override
    public boolean onScrew(
            Level level,
            Player player,
            BlockPos core,
            @Nullable Direction side,
            Vec3 hit,
            ToolType tool) {
        if (tool != ToolType.TORCH) return false;
        return IRepairable.tryRepairMultiblock(level, core, player);
    }

    @Inject(method = "buildLookOverlay", at = @At("HEAD"), cancellable = true)
    private void hbmspace$repairOverlay(
            Level level, BlockPos core, LookInfo info, CallbackInfo ci) {
        if (level.getBlockEntity(core) instanceof BlockEntityCondenserPoweredSpace be
                && be.isDamaged()) {
            RepairLookOverlay.build(level, core, info);
            ci.cancel();
        }
    }
}
