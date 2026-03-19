package com.diffusehyperion.dmm.features.ghostseek;
//
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.LocalPlayerResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mutable;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GhostSeekPingSequence {
    private final List<GhostSeekPing> ghostSeekPings = new ArrayList<>();
    public final Color colour = Color.getHSBColor((float) Math.random(), 1, 1);

    public List<GhostSeekPing> getGhostSeekPings() {
        return ghostSeekPings;
    }

    public void addPing(GhostSeekPing ghostSeekPing) {
        ghostSeekPings.add(ghostSeekPing);

        // TODO: variable naming sucks in this file lol
        Stream<BlockPos> blockPosList = getShell(ghostSeekPing.origin(), ghostSeekPing.minDistance(), ghostSeekPing.maxDistance());
        List<BlockPos> possibleBlocks = blockPosList.filter(this::getSpawnAvailability).toList();
    }

    private Stream<BlockPos> getShell(BlockPos center, int r, int R) {
        List<BlockPos> result = new ArrayList<>();

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        int r2 = r * r;
        int R2 = R * R;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -R; dx <= R; dx++) {
            int dx2 = dx * dx;

            for (int dy = -R; dy <= R; dy++) {
                int dxy2 = dx2 + dy * dy;

                if (dxy2 > R2) continue;

                int maxDz = (int) Math.floor(Math.sqrt(R2 - dxy2));

                int minDz = 0;
                if (dxy2 < r2) {
                    minDz = (int) Math.ceil(Math.sqrt(r2 - dxy2));
                }

                for (int dz = -maxDz; dz <= -minDz; dz++) {
                    mutable.set(cx + dx, cy + dy, cz + dz);
                    result.add(mutable.immutable());
                }

                for (int dz = minDz; dz <= maxDz; dz++) {
                    mutable.set(cx + dx, cy + dy, cz + dz);
                    result.add(mutable.immutable());
                }
            }
        }

        return result.stream();
    }

    private boolean getSpawnAvailability(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            throw new RuntimeException("Level was null when trying to determine possible skeleton spawning positions");
        }

        if (!level.getBlockState(pos).isValidSpawn(level, pos, EntityType.ITEM_DISPLAY)) {
            return false;
        }
        Stream<BlockState> blockStates = level.getBlockStates(new AABB(Vec3.atBottomCenterOf(pos.mutable().move(2, 1, 2)), Vec3.atBottomCenterOf(pos.mutable().move(-2, 1, -2))));
        return blockStates.allMatch(BlockBehaviour.BlockStateBase::isAir);
    }
}
