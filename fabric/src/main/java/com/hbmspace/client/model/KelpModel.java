// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.model;

import com.hbm.client.model.Boxes;
import com.hbm.client.model.SimpleBlockModel;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.BlockKelp;
import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class KelpModel implements BlockStateModel {

    private static final Identifier[] MODELS = {model("laythe_kelp"), model("laythe_kelp_top")};
    private static final int STEM = 0;
    private static final int TOP = 1;

    public static Factory FACTORY = KelpModel::new;

    private final BlockStateModelPart[] parts = new BlockStateModelPart[MODELS.length];
    private final Material.Baked particle;

    public KelpModel(ModelBaker baker) {
        for (int i = 0; i < MODELS.length; i++) {
            ResolvedModel resolved = baker.getModel(MODELS[i]);
            Material.Baked material =
                    resolved.resolveParticleMaterial(resolved.getTopTextureSlots(), baker);
            parts[i] =
                    new SimpleModelWrapper(
                            resolved.bakeTopGeometry(
                                    resolved.getTopTextureSlots(), baker, Boxes.IDENTITY),
                            resolved.getTopAmbientOcclusion(),
                            material);
        }
        particle = parts[TOP].particleMaterial();
    }

    private static KelpModel create(ModelBaker baker) {
        return FACTORY.create(baker);
    }

    private static Identifier model(String name) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "block/" + name);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        output.add(parts[TOP]);
    }

    public void collectParts(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            List<BlockStateModelPart> output) {
        output.add(parts[geometryKey(level, pos, state)]);
    }

    public int geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return BlockKelp.isStem(level, pos, state) ? STEM : TOP;
    }

    @Override
    public Material.Baked particleMaterial() {
        return particle;
    }

    @Override
    public int materialFlags() {
        int flags = 0;
        for (BlockStateModelPart part : parts) flags |= part.materialFlags();
        return flags;
    }

    public interface Factory {
        KelpModel create(ModelBaker baker);
    }

    public record Family() implements SimpleBlockModel {
        @Override
        public BlockStateModel.UnbakedRoot root(Block block, BlockState state) {
            return new Root();
        }
    }

    public record Root() implements BlockStateModel.UnbakedRoot {
        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            for (Identifier model : MODELS) resolver.markDependency(model);
        }

        @Override
        public BlockStateModel bake(BlockState state, ModelBaker baker) {
            return baker.compute(new SharedKey(this));
        }

        @Override
        public Object visualEqualityGroup(BlockState state) {
            return this;
        }
    }

    private record SharedKey(Root root) implements ModelBaker.SharedOperationKey<BlockStateModel> {
        @Override
        public BlockStateModel compute(ModelBaker baker) {
            return create(baker);
        }
    }
}
