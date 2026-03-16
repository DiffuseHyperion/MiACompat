package com.diffusehyperion.dmm.features.ghostseek;

import net.minecraft.world.phys.Vec3;

public record GhostSeekPing(Vec3 origin, double minDistance, double maxDistance) {
}
