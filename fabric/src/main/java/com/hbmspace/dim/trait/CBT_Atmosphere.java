// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluidProperty;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.trait.FluidTrait;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public class CBT_Atmosphere extends CelestialBodyTrait {

    public ArrayList<FluidEntry> fluids;

    public static class FluidEntry {
        public Fluid fluid;
        public double pressure;

        public FluidEntry(Fluid fluid, double percentage) {
            this.fluid = fluid;
            this.pressure = percentage;
        }
    }

    public CBT_Atmosphere() {
        fluids = new ArrayList<>();
    }

    public CBT_Atmosphere(Fluid fluid, double pressure) {
        fluids = new ArrayList<>();
        fluids.add(new FluidEntry(fluid, pressure));
    }

    public CBT_Atmosphere and(Fluid fluid, double pressure) {
        fluids.add(new FluidEntry(fluid, pressure));
        return this;
    }

    @Override
    public CBT_Atmosphere clone() {
        CBT_Atmosphere clone = new CBT_Atmosphere();
        for (FluidEntry entry : fluids) {
            clone.fluids.add(new FluidEntry(entry.fluid, entry.pressure));
        }
        return clone;
    }

    public void add(Fluid fluid, double pressure) {
        for (FluidEntry entry : fluids) {
            if (entry.fluid == fluid) {
                entry.pressure += pressure;
                return;
            }
        }

        fluids.add(new FluidEntry(fluid, pressure));
    }

    public void reduce(double pressure) {
        double totalPressure = getPressure();
        if (pressure >= totalPressure) {
            fluids = new ArrayList<>();
            return;
        }

        double pressureRatio = pressure / totalPressure;

        for (FluidEntry entry : fluids) {
            entry.pressure *= pressureRatio;
        }
    }

    public boolean hasFluid(Fluid fluid) {
        return hasFluid(fluid, 0.0001);
    }

    public boolean hasFluid(Fluid fluid, double abovePressure) {
        for (FluidEntry entry : fluids) {
            if (entry.fluid == fluid) {
                return entry.pressure >= abovePressure;
            }
        }

        return false;
    }

    public boolean hasTrait(Class<? extends FluidTrait> trait) {
        return hasTrait(trait, 0.0001);
    }

    public boolean hasTrait(Class<? extends FluidTrait> trait, double abovePressure) {
        for (FluidEntry entry : fluids) {
            if (entry.pressure >= abovePressure
                    && NTMFluidProperties.hasTrait(entry.fluid, trait)) {
                return true;
            }
        }

        return false;
    }

    public Fluid getMainFluid() {
        sortDescending();
        return fluids.isEmpty() ? NTMFluids.NONE : fluids.getFirst().fluid;
    }

    public void sortDescending() {
        fluids.sort((a, b) -> Double.compare(b.pressure, a.pressure));
    }

    public double getPressure() {
        double pressure = 0;
        for (int i = 0; i < fluids.size(); i++) {
            pressure += fluids.get(i).pressure;
        }

        return pressure;
    }

    public double getPressure(Fluid fluid) {
        for (FluidEntry entry : fluids) {
            if (entry.fluid == fluid) {
                return entry.pressure;
            }
        }

        return 0;
    }

    public List<Integer> getFluidColors() {
        List<Integer> colors = new ArrayList<>();
        for (FluidEntry entry : fluids) {
            NTMFluidProperty property = NTMFluidProperties.get(entry.fluid);
            colors.add(property == null ? 0 : property.color());
        }
        return colors;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        ListTag fluidList = new ListTag();
        for (FluidEntry entry : fluids) {
            CompoundTag fluidTag = new CompoundTag();
            fluidTag.putString("type", BuiltInRegistries.FLUID.getKey(entry.fluid).toString());
            fluidTag.putDouble("percentage", entry.pressure);
            fluidList.add(fluidTag);
        }
        nbt.put("fluids", fluidList);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        fluids = new ArrayList<>();
        ListTag fluidList = nbt.getListOrEmpty("fluids");
        for (int i = 0; i < fluidList.size(); i++) {
            CompoundTag fluidTag = fluidList.getCompoundOrEmpty(i);
            Fluid fluid =
                    BuiltInRegistries.FLUID.getValue(
                            Identifier.parse(fluidTag.getStringOr("type", "")));
            fluids.add(new FluidEntry(fluid, fluidTag.getDoubleOr("percentage", 0)));
        }
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeInt(fluids.size());
        for (FluidEntry entry : fluids) {
            buf.writeInt(BuiltInRegistries.FLUID.getId(entry.fluid));
            buf.writeFloat((float) entry.pressure);
        }
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        int size = buf.readInt();
        fluids = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Fluid fluid = BuiltInRegistries.FLUID.byId(buf.readInt());
            double percentage = buf.readFloat();
            fluids.add(new FluidEntry(fluid, percentage));
        }
    }
}
