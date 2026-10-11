// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.items.ModDataComponents;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.blocks.RocketryBlocksSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityDishControl extends BlockEntityMachineBase implements SyncUnitSchema {

    public BlockPos linkPosition = BlockPos.ZERO;
    public boolean isLinked;
    private boolean foundLink;

    @SyncField(units = 1L << 0)
    public boolean starDarHasDisk;

    public BlockEntityDishControl(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.DISH_CONTROL.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        if (!foundLink && isLinked) {
            isLinked = establishLink(linkPosition);
            foundLink = true;
        }

        boolean disk =
                isLinked
                        && level.isLoaded(linkPosition)
                        && level.getBlockEntity(linkPosition)
                                instanceof BlockEntityMachineStardar stardar
                        && !stardar.getItem(0).isEmpty();
        if (disk != starDarHasDisk) {
            starDarHasDisk = disk;
            setChanged();
        }

        networkPackNT(15);
    }

    public @Nullable BlockEntityMachineStardar linkedStardar() {
        if (!isLinked || !level.isLoaded(linkPosition)) return null;
        return level.getBlockEntity(linkPosition) instanceof BlockEntityMachineStardar stardar
                ? stardar
                : null;
    }

    public void tryLink(ItemStack sensor) {
        Long packed = sensor.get(ModDataComponents.REACTOR_POS.get());
        isLinked = packed != null && establishLink(BlockPos.of(packed));
        setChanged();
    }

    private boolean establishLink(BlockPos pos) {
        if (!level.isLoaded(pos)) return false;
        BlockPos core = MultiblockSurface.coreOfAny(level, pos);
        if (core == null
                || !level.getBlockState(core).is(RocketryBlocksSpace.MACHINE_STARDAR.get()))
            return false;
        if (!(level.getBlockEntity(core) instanceof BlockEntityMachineStardar)) return false;
        linkPosition = core.immutable();
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        isLinked = input.getBooleanOr("isLinked", false);
        linkPosition =
                new BlockPos(
                        input.getIntOr("linkPosX", 0),
                        input.getIntOr("linkPosY", 0),
                        input.getIntOr("linkPosZ", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("isLinked", isLinked);
        output.putInt("linkPosX", linkPosition.getX());
        output.putInt("linkPosY", linkPosition.getY());
        output.putInt("linkPosZ", linkPosition.getZ());
    }

    @Override
    public long syncUnitMask() {
        return 0x1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeBoolean(starDarHasDisk);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        starDarHasDisk = input.readBoolean();
    }
}
