package com.diffusehyperion.dmm.features.ghostseek;

import com.diffusehyperion.dmm.DMM;
import com.diffusehyperion.dmm.features.Feature;
import com.diffusehyperion.dmm.features.ghostseek.legacy.GhostSeekRenderer;
import com.diffusehyperion.dmm.features.ghostseek.legacy.GhostSeekTracker;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.diffusehyperion.dmm.DMM.config;

public class GhostSeekFeature extends Feature {
    public final GhostSeekTracker ghostSeekTracker = new GhostSeekTracker();
    public final GhostSeekRenderer ghostSeekRenderer = new GhostSeekRenderer(ghostSeekTracker);

    public final GhostSeekActiveItemManager ghostSeekActiveItemManager = new GhostSeekActiveItemManager();
    public final GhostSeekPingManager ghostSeekPingManager = new GhostSeekPingManager();

    @Override
    public void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(ghostSeekTracker::tick);
        WorldRenderEvents.END_MAIN.register(ghostSeekRenderer::render);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() || !(entity instanceof Interaction interaction)) return InteractionResult.PASS;
            if (!config.clearBreadcrumbsOnFind) return InteractionResult.PASS;

            for (Entity entityNear : world.getEntities(player, interaction.getBoundingBox().inflate(1.5))) {
                if (entityNear instanceof Display.ItemDisplay itemDisplay && GhostSeekTracker.isPrayingSkeleton(itemDisplay)) {
                    ghostSeekTracker.clearMeasurements();
                    break;
                }
            }

            return InteractionResult.PASS;
        });
    }

    public Component onGhostSeekTriggered(Component message) {
        // preconditions
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return message;

        Pair<GhostSeekItemType, ItemStack> ghostSeekPair = ghostSeekActiveItemManager.getGhostSeek();
        if (ghostSeekPair == null) return message;

        // get ping distance range
        GhostSeekItemType ghostSeekItemType = ghostSeekPair.getFirst();

        String messageText = message.getString().trim().toLowerCase();
        int pingLength = switch (messageText) {
            case String s when s.startsWith("du du du du dum") -> 5;
            case String s when s.startsWith("du du du dum") -> 4;
            case String s when s.startsWith("du du dum") -> 3;
            case String s when s.startsWith("du dum") -> 2;
            case String s when s.startsWith("dum") -> 1;
            default -> 0;
        };

        // awaitingPingTicks = type.pingIntervalTicks; for gui
        InclusiveRange<@NotNull Integer> pingRange = ghostSeekItemType.getPingRange(pingLength);

        // create ping
        GhostSeekPing ghostSeekPing = new GhostSeekPing(player.position(), pingRange.minInclusive(), pingRange.maxInclusive());
        ghostSeekPingManager.addPing(ghostSeekPing);

        // handle modification
        String range = "%d-%d blocks".formatted(pingRange.minInclusive(), pingRange.maxInclusive());
        DMM.LOGGER.info("Ghost seek ping: {}, range: {}", pingLength, range);

        if (!DMM.config.ghostSeekDistanceHint && !DMM.config.pingColorMatchesBreadcrumb) return message;

        MutableComponent editedMessage = message.copy();
        if (DMM.config.ghostSeekDistanceHint) {
            editedMessage.append(" (" + range + ")");
        }

        /*
        if (DMM.config.pingColorMatchesBreadcrumb) {
            editedMessage.setStyle(Style.EMPTY.withColor(measurement.getColor(type.getMaxRange())));
        }
         */

        return editedMessage;
    }

    @Override
    public void close() {
        ghostSeekRenderer.close();
    }
}
