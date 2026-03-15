package com.diffusehyperion.dmm.features.ghostseek;

import com.diffusehyperion.dmm.features.Feature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;

import static com.diffusehyperion.dmm.DMM.config;

public class GhostSeekFeature extends Feature {
    public final GhostSeekTracker ghostSeekTracker = new GhostSeekTracker();
    public final GhostSeekRenderer ghostSeekRenderer = new GhostSeekRenderer(ghostSeekTracker);

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

    @Override
    public void close() {
        ghostSeekRenderer.close();
    }
}
