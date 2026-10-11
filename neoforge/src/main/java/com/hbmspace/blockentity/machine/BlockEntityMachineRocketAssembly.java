// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.control.IControlReceiver;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.items.ISatChip;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.blocks.machine.MachineRocketAssembly;
import com.hbmspace.handler.RocketStruct.RocketStage;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.container.MenuMachineRocketAssembly;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.items.weapon.ItemCustomRocket;
import com.hbmspace.saveddata.satellites.SatelliteOrbit;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityMachineRocketAssembly extends BlockEntityMachineBase
        implements MenuProvider, IControlReceiver, SyncUnitSchema {

    public static final int SLOT_CAPSULE = 0;
    public static final int SLOT_RESULT = 1 + RocketStruct.MAX_STAGES * 3;
    public static final int SLOT_COUNT = SLOT_RESULT + 1 + RocketStruct.MAX_STAGES * 2;
    public static final int STACK_LIMIT = 8;

    @SyncField(units = 1L << 0)
    public RocketStruct rocket = new RocketStruct();

    public int currentStage;

    private int previousHeight;
    private boolean platformFailed;
    private int spire;
    private int[] platforms = new int[0];

    public BlockEntityMachineRocketAssembly(BlockPos pos, BlockState state) {
        super(RocketryBlocksSpace.ROCKET_ASSEMBLY.get(), pos, state, SLOT_COUNT);
    }

    public static int fuselage(int stage) {
        return 1 + stage * 3;
    }

    public static int fins(int stage) {
        return 2 + stage * 3;
    }

    public static int thruster(int stage) {
        return 3 + stage * 3;
    }

    public static int driveFrom(int stage) {
        return SLOT_RESULT + 1 + stage * 2;
    }

    public static int driveTo(int stage) {
        return SLOT_RESULT + 2 + stage * 2;
    }

    public int spireHeight() {
        return spire;
    }

    public int[] platforms() {
        return platforms;
    }

    @Override
    public void tickServer() {
        ItemStack from = getItem(driveFrom(currentStage));
        ItemStack to = getItem(driveTo(currentStage));
        if (!from.isEmpty()) ItemVOTVdrive.getTarget(from, level);
        if (!to.isEmpty()) ItemVOTVdrive.getTarget(to, level);

        RocketStruct next = assemble(this, SLOT_CAPSULE);

        int height = (int) next.getHeight();
        if (height != previousHeight) {
            raise(next);
            previousHeight = height;
        }

        if (platformFailed) {
            next.addIssue(
                    Component.translatable("desc.gui.rocket.ceilingTooLow")
                            .withStyle(ChatFormatting.RED));
        }

        rocket = next;
        networkPackNT(250);
    }

    public static RocketStruct assemble(Container slots, int capsuleSlot) {
        ItemStack capsule = slots.getItem(capsuleSlot);
        RocketStruct rocket = new RocketStruct(capsule);
        if (capsule.getItem() instanceof ISatChip) rocket.satFreq = ISatChip.getFreqS(capsule);
        rocket.satOrbit = capsule.get(ModDataComponentsSpace.SATELLITE_ORBIT.get());

        int end = capsuleSlot + 1 + RocketStruct.MAX_STAGES * 3;
        for (int i = capsuleSlot + 1; i < end; i += 3) {
            if (emptyGroup(slots, i)) {
                if (i + 3 >= end || emptyGroup(slots, i + 3)) break;
                for (int j = 0; j < 3; j++) {
                    slots.setItem(i + j, slots.getItem(i + 3 + j));
                    slots.setItem(i + 3 + j, ItemStack.EMPTY);
                }
            }
            rocket.addStage(slots.getItem(i), slots.getItem(i + 1), slots.getItem(i + 2));
        }
        return rocket;
    }

    private static boolean emptyGroup(Container slots, int first) {
        return slots.getItem(first).isEmpty()
                && slots.getItem(first + 1).isEmpty()
                && slots.getItem(first + 2).isEmpty();
    }

    private void raise(RocketStruct next) {
        int oldSpire = spire;
        int[] oldPlatforms = platforms;

        int maxHeight = Integer.MAX_VALUE;
        for (int h = 1; worldPosition.getY() + h < level.getMaxY(); h++) {
            BlockState above = level.getBlockState(worldPosition.above(h));
            if (!above.canBeReplaced() && !above.is(getBlockState().getBlock())) {
                maxHeight = h;
                break;
            }
        }

        double checkHeight = next.getHeight();
        if (next.capsule != null) checkHeight -= next.capsule.height;
        if (!next.stages.isEmpty() && next.stages.getFirst().thruster() != null) {
            checkHeight -= next.stages.getFirst().thruster().height;
        }

        List<Integer> raised = new ArrayList<>();
        if (checkHeight < maxHeight) {
            int targetHeight = 0;
            for (int i = 0; i < next.stages.size(); i++) {
                RocketStage stage = next.stages.get(i);
                RocketStage nextStage = i < next.stages.size() - 1 ? next.stages.get(i + 1) : null;

                if (stage.fuselage() != null)
                    targetHeight += stage.fuselage().height * stage.getStack();
                if (nextStage != null && nextStage.thruster() != null)
                    targetHeight += nextStage.thruster().height;

                int platform = Math.round(targetHeight);
                if (platform > 0) raised.add(platform);
            }
            spire = Math.min(targetHeight, level.getMaxY() - worldPosition.getY());
            platformFailed = false;
        } else {
            platformFailed = true;
        }
        platforms = raised.stream().mapToInt(Integer::intValue).toArray();

        BlockMultiblockCore.withoutTeardown(
                () -> {
                    BlockPos.MutableBlockPos kept = new BlockPos.MutableBlockPos();
                    List<BlockPos> removed = new ArrayList<>();
                    int[] gone =
                            Arrays.stream(oldPlatforms)
                                    .filter(y -> Arrays.stream(platforms).noneMatch(n -> n == y))
                                    .toArray();
                    MachineRocketAssembly.visitRaised(
                            worldPosition,
                            oldSpire,
                            gone,
                            (pos, mask) -> {
                                if (pos.getY() - worldPosition.getY() <= spire && isColumn(pos))
                                    return;
                                removed.add(kept.set(pos).immutable());
                            });
                    long core = worldPosition.asLong();
                    for (BlockPos pos : removed) {
                        if (!level.getBlockState(pos).is(getBlockState().getBlock())) continue;
                        if (MultiblockSurface.recordedCorePacked(
                                        (ServerLevel) level, pos.getX(), pos.getY(), pos.getZ())
                                != core) {
                            continue;
                        }
                        level.removeBlock(pos, false);
                    }
                });

        Direction facing = getBlockState().getValue(BlockMultiblockCore.FACING);
        ((MachineRocketAssembly) getBlockState().getBlock())
                .fillSpace(level, worldPosition, facing);
        setChanged();
    }

    private boolean isColumn(BlockPos pos) {
        int x = pos.getX() - worldPosition.getX(), z = pos.getZ() - worldPosition.getZ();
        return x == 0 && z == 0 || Math.abs(x) == 4 && Math.abs(z) == 4;
    }

    public void construct() {
        if (!rocket.validate()) return;
        if (!getItem(SLOT_RESULT).isEmpty()) return;

        setItem(SLOT_RESULT, ItemCustomRocket.build(rocket));
        for (int i = 0; i < SLOT_RESULT; i++) setItem(i, ItemStack.EMPTY);
    }

    public boolean canDeconstruct() {
        if (ItemCustomRocket.get(getItem(SLOT_RESULT)) == null) return false;
        for (int i = 0; i < SLOT_RESULT; i++) {
            if (!getItem(i).isEmpty()) return false;
        }
        return true;
    }

    public void deconstruct() {
        if (!canDeconstruct()) return;
        ItemStack source = getItem(SLOT_RESULT);
        int satFreq = ISatChip.getFreqS(source);
        RocketStruct built = ItemCustomRocket.get(source);

        ItemStack capsule = new ItemStack(built.capsule.item);
        if (capsule.getItem() instanceof ISatChip) {
            ISatChip.setFreqS(capsule, satFreq);
            SatelliteOrbit orbit = source.get(ModDataComponentsSpace.SATELLITE_ORBIT.get());
            if (orbit != null) capsule.set(ModDataComponentsSpace.SATELLITE_ORBIT.get(), orbit);
        }
        setItem(SLOT_CAPSULE, capsule);

        for (int i = 0; i < built.stages.size(); i++) {
            RocketStage stage = built.stages.get(built.stages.size() - 1 - i);
            int o = i * 3;
            setItem(o + 1, new ItemStack(stage.fuselage().item, stage.fuselageCount()));
            if (stage.fins() != null) setItem(o + 2, new ItemStack(stage.fins().item));
            setItem(o + 3, new ItemStack(stage.thruster().item, stage.thrusterCount()));
        }

        setItem(SLOT_RESULT, ItemStack.EMPTY);
    }

    @Override
    public int getMaxStackSize() {
        return STACK_LIMIT;
    }

    @Override
    public boolean dropsSlot(int slot) {
        return slot <= SLOT_RESULT;
    }

    @Override
    public boolean hasPermission(Player player) {
        return level.getBlockEntity(worldPosition) == this;
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if (data.getBooleanOr("construct", false)) construct();
        if (data.getBooleanOr("deconstruct", false)) deconstruct();
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new MenuMachineRocketAssembly(containerId, playerInventory, this);
    }

    @Override
    public long syncUnitMask() {
        return 1L << 0;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        RocketStruct.STREAM_CODEC.encode(
                new RegistryFriendlyByteBuf(output, level.registryAccess()), rocket);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        rocket =
                RocketStruct.STREAM_CODEC.decode(
                        new RegistryFriendlyByteBuf(input, level.registryAccess()));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        platforms = input.getIntArray("platforms").orElse(new int[0]);
        spire = input.getIntOr("spire", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putIntArray("platforms", platforms);
        output.putInt("spire", spire);
    }
}
