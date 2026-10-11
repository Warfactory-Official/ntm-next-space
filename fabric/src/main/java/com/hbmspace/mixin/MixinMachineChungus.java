// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityTurbineBase;
import com.hbm.blocks.machine.MachineChungus;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbm.client.RepairLookOverlay;
import com.hbm.interfaces.ILookOverlay.LookInfo;
import com.hbm.interfaces.IRepairable;
import com.hbm.interfaces.IToolable;
import com.hbm.sound.ModSounds;
import com.hbmspace.blockentity.machine.BlockEntityChungusSpace;
import com.hbmspace.blockentity.machine.ThermalMachineDamage;
import com.hbmspace.blocks.machine.ChungusPlacementSpace;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MachineChungus.class)
public abstract class MixinMachineChungus extends BlockMultiblockCore implements IToolable {
    protected MixinMachineChungus(Properties properties) {
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
        cir.setReturnValue(new BlockEntityChungusSpace(pos, state));
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
        var be = (BlockEntityChungusSpace) level.getBlockEntity(core);
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
        if (level.getBlockEntity(core) instanceof BlockEntityChungusSpace be && be.isDamaged()) {
            RepairLookOverlay.build(level, core, info);
            ci.cancel();
        }
    }

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void hbmspace$wholeClearance(CallbackInfoReturnable<int[]> cir) {
        cir.setReturnValue(ChungusPlacementSpace.DIMENSIONS);
    }

    @Redirect(
            method = "visitCells",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/blocks/multiblock/BlockMultiblockCore;visitCells(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lcom/hbm/blocks/multiblock/BlockMultiblockCore$CellVisitor;)V"))
    private void hbmspace$mainBox(
            BlockMultiblockCore block, BlockPos core, Direction facing, CellVisitor visitor) {
        MultiblockHandlerXR.visitBox(core, ChungusPlacementSpace.BOXES[0], facing, visitor);
    }

    @Inject(method = "checkRequirement", at = @At("HEAD"), cancellable = true)
    private void hbmspace$allBoxes(
            Level level,
            BlockPos placed,
            Direction facing,
            int offset,
            CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ChungusPlacementSpace.check(level, placed, facing, offset));
    }

    @Inject(method = "useAtCore", at = @At("HEAD"), cancellable = true)
    private void hbmspace$brokenLever(
            BlockState state,
            Level level,
            BlockPos core,
            Player player,
            net.minecraft.world.phys.BlockHitResult hit,
            CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        if (player.isShiftKeyDown() || state.getValue(ThermalMachineDamage.DAMAGED)) {
            cir.setReturnValue(net.minecraft.world.InteractionResult.PASS);
        }
    }

    @Redirect(
            method = "useAtCore",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/blocks/machine/MachineIndustrialTurbine;pullLever(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lcom/hbm/blockentity/machine/BlockEntityTurbineBase;)Lnet/minecraft/world/InteractionResult;"))
    private net.minecraft.world.InteractionResult hbmspace$liveLever(
            Level level, BlockPos pos, Player player, BlockEntityTurbineBase turbine) {
        if (!level.isClientSide()) {
            level.playSound(null, pos, ModSounds.CHUNGUS_LEVER.get(), SoundSource.BLOCKS, 1.5F, 1F);
            ((BlockEntityChungusSpace) turbine).onLeverPull();
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }
}
