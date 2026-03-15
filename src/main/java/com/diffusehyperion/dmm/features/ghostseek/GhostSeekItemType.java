package com.diffusehyperion.dmm.features.ghostseek;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.diffusehyperion.dmm.DMM.LOGGER;

public enum GhostSeekItemType {
    MAKESHIFT("makeshift", 20, new int[] {150, 100, 50, 25}),
    REPAIRED("repaired", 20, new int[] {200, 150, 100, 50, 25}),
    REFINED("refined", 15, new int[] {250, 150, 100, 50, 25});

    private final String itemName;
    public final int pingIntervalTicks;
    private final int[] ranges;

    GhostSeekItemType(String itemName, int pingIntervalSec, int[] ranges) {
        this.itemName = itemName;
        this.pingIntervalTicks = pingIntervalSec * 20;
        this.ranges = ranges;
    }

    public int getMaxRange() { return ranges[0]; }

    public InclusiveRange<@NotNull Integer> getPingRange(int pingLength) {
        int rangeIndex = Math.clamp(pingLength - 1, 0, ranges.length - 1);
        int minDistance = (rangeIndex < ranges.length - 1) ? ranges[rangeIndex + 1] : 0;
        int maxDistance = ranges[rangeIndex];

        return new InclusiveRange<>(minDistance, maxDistance);
    }

        /*
        public GhostSeekTracker.Measurement getPingMeasurement(Vec3 pos, int pingLength) {
            InclusiveRange<@NotNull Integer> pingRange = getPingRange(pingLength);

            double midDistance = (pingRange.maxInclusive() + pingRange.minInclusive()) / 2.0;
            double uncertainty = (pingRange.maxInclusive() - pingRange.minInclusive()) / 2.0;

            return new GhostSeekTracker.Measurement(pos, midDistance, uncertainty, pingLength);
        }

         */

    public static GhostSeekItemType fromItemStack(ItemStack stack) {
        Component itemName = stack.get(DataComponents.ITEM_NAME);
        if (itemName == null) {
            LOGGER.warn("Could not access the item name data component of the active ghost seek itemstack, returning makeshift for now");
            return MAKESHIFT;
        };

        String name = itemName.getString().toLowerCase();

        for (GhostSeekItemType type : GhostSeekItemType.values()) {
            if (name.contains(type.itemName)) {
                return type;
            }
        }

        LOGGER.warn("Could not match the active ghost seek itemstack to a GhostSeekType, returning makeshift for now");
        return MAKESHIFT;
    }
}