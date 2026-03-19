package com.diffusehyperion.dmm.features.ghostseek;

import net.minecraft.core.BlockPos;

public record GhostSeekPing(BlockPos origin, int minDistance, int maxDistance) {
}
