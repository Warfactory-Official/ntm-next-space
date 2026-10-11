// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.entity.mob.EntityCreeperBase;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockMutatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.particle.ParticleGiblet;
import com.hbm.particle.helper.ParticleCreators;
import com.hbmspace.blocks.TerrainBlocksSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityCreeperFlesh extends EntityCreeperBase {

    public EntityCreeperFlesh(EntityType<? extends EntityCreeperFlesh> type, Level level) {
        super(type, level);
    }

    @Override
    protected void detonate(ServerLevel level) {
        ParticleCreators.giblets(level, this, ParticleGiblet.TYPE_MEAT, 2);
        ExplosionVNT vnt =
                new ExplosionVNT(level, getX(), getY(), getZ(), isPowered() ? 7 : 4, this);
        vnt.setBlockAllocator(new BlockAllocatorBulkie(30, isPowered() ? 16 : 8));
        vnt.setBlockProcessor(
                new BlockProcessorStandard()
                        .withBlockEffect(
                                new BlockMutatorBulkie(
                                        () -> TerrainBlocksSpace.TUMOR.get().defaultBlockState())));
        vnt.setEntityProcessor(new EntityProcessorStandard().withRangeMod(0.25F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectStandard());
        vnt.explode();
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        MobDrops.dropFew(level, this, source, Items.GUNPOWDER);
    }
}
