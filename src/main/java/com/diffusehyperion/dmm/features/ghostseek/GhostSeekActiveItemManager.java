package com.diffusehyperion.dmm.features.ghostseek;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

import static com.diffusehyperion.dmm.DMM.LOGGER;

public class GhostSeekActiveItemManager {
    private static final String GHOST_SEEK_ITEM_NAME = "ghost seek";

    private GhostSeekItemType cachedGhostSeekItemType;
    private ItemStack cachedGhostSeek;
    private long cacheExpireTime;

    public @Nullable Pair<GhostSeekItemType, ItemStack> getGhostSeek() {
        long currentTime = Instant.now().toEpochMilli();

        // Use cache to avoid repeated inventory checks
        if (currentTime < cacheExpireTime) return new Pair<>(cachedGhostSeekItemType, cachedGhostSeek);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            LOGGER.warn("Player was null when trying to refresh active ghost seek item cache");
            return null;
        }

        Inventory inventory = player.getInventory();

        final int[] PASSIVE_SLOTS = {9, 10};

        for (int slotIndex : PASSIVE_SLOTS) {
            ItemStack stack = inventory.getItem(slotIndex);
            if (isItemGhostSeek(stack)) {
                cachedGhostSeekItemType = GhostSeekItemType.fromItemStack(stack);
                cachedGhostSeek = stack;
                cacheExpireTime = currentTime + 20;
                return new Pair<>(cachedGhostSeekItemType, cachedGhostSeek);
            }
        }

        // no ghost seek was in the passive slots
        return null;
    }

    private static boolean isItemGhostSeek(ItemStack stack) {
        Component itemName = stack.get(DataComponents.ITEM_NAME);
        if (itemName == null) return false;

        return itemName.getString().toLowerCase().contains(GHOST_SEEK_ITEM_NAME);
    }
}
