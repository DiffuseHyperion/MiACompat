package com.diffusehyperion.dmm.features.ghostseek;

import com.diffusehyperion.dmm.DMM;
import com.diffusehyperion.dmm.features.Feature;
import com.diffusehyperion.dmm.features.ghostseek.gui.GhostSeekCooldownElement;
import com.diffusehyperion.dmm.gui.Hud;
import com.jcraft.jorbis.Block;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.coordinates.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static com.diffusehyperion.dmm.DMM.config;

public class GhostSeekFeature extends Feature {
    public final GhostSeekCooldownElement ghostSeekCooldownElement = new GhostSeekCooldownElement();

    public final GhostSeekActiveItemManager ghostSeekActiveItemManager = new GhostSeekActiveItemManager();
    public final GhostSeekPingManager ghostSeekPingManager = new GhostSeekPingManager();

    @Override
    public void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        AttackEntityCallback.EVENT.register(this::onEntityAttacked);

        Hud.hudElements.add(this.ghostSeekCooldownElement);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("debug_ghost")
                        .then(ClientCommandManager.argument("innerRadius", IntegerArgumentType.integer())
                                .then(ClientCommandManager.argument("outerRadius", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int innerRadius = IntegerArgumentType.getInteger(context, "innerRadius");
                                            int outerRadius = IntegerArgumentType.getInteger(context, "outerRadius");

                                            GhostSeekPing ghostSeekPing = new GhostSeekPing(Minecraft.getInstance().player.blockPosition(), innerRadius, outerRadius);
                                            GhostSeekPingSequence ghostSeekPingSequence = ghostSeekPingManager.addPing(ghostSeekPing);

                                            context.getSource().sendFeedback(Component.literal("Created a new ping."));
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }

    private void tick(Minecraft minecraft) {
        ghostSeekCooldownElement.tick();
    }

    public Component onGhostSeekTriggered(Component message) {
        // preconditions
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return message;

        GhostSeekItemType ghostSeekItemType = ghostSeekActiveItemManager.getGhostSeek();
        if (ghostSeekItemType == null) return message;

        String messageText = message.getString().trim().toLowerCase();
        int pingLength = switch (messageText) {
            case String s when s.startsWith("du du du du dum") -> 5;
            case String s when s.startsWith("du du du dum") -> 4;
            case String s when s.startsWith("du du dum") -> 3;
            case String s when s.startsWith("du dum") -> 2;
            case String s when s.startsWith("dum") -> 1;
            default -> 0;
        };

        InclusiveRange<@NotNull Integer> pingRange = ghostSeekItemType.getPingRange(pingLength);

        // create ping
        GhostSeekPing ghostSeekPing = new GhostSeekPing(player.blockPosition(), pingRange.minInclusive(), pingRange.maxInclusive());
        GhostSeekPingSequence ghostSeekPingSequence = ghostSeekPingManager.addPing(ghostSeekPing);

        ghostSeekCooldownElement.onGhostSeekCooldownStart(ghostSeekItemType, ghostSeekPingSequence.colour);

        // handle modification
        String range = "%d-%d blocks".formatted(pingRange.minInclusive(), pingRange.maxInclusive());
        DMM.LOGGER.info("Ghost seek ping: {}, range: {}", pingLength, range);

        if (!DMM.config.ghostSeekDistanceHint && !DMM.config.pingColorMatchesBreadcrumb) return message;

        MutableComponent editedMessage = message.copy();
        if (DMM.config.ghostSeekDistanceHint) {
            editedMessage.append(" (" + range + ")");
        }

        if (DMM.config.pingColorMatchesBreadcrumb) {
            editedMessage.setStyle(Style.EMPTY.withColor(ghostSeekPingSequence.colour.getRGB()));
        }

        return editedMessage;
    }

    // makes sense to always return same - don't want to cancel any attacks, especially praying skeletons lol
    @SuppressWarnings("SameReturnValue")
    private InteractionResult onEntityAttacked(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        if (!world.isClientSide() || !(entity instanceof Interaction interaction)) return InteractionResult.PASS;
        if (!config.clearBreadcrumbsOnFind) return InteractionResult.PASS;

        for (Entity entityNear : world.getEntities(player, interaction.getBoundingBox().inflate(1.5))) {
            // checks if the entity is a praying skeleton
            if (!(entityNear instanceof Display.ItemDisplay itemDisplayEntity)) {
                continue;
            }

            ItemStack stack = itemDisplayEntity.getItemStack();
            Identifier modelName = stack.get(DataComponents.ITEM_MODEL);
            if (modelName == null || !modelName.getPath().startsWith("praying_skeleton")) {
                continue;
            }

            onPrayingSkeletonAttacked(itemDisplayEntity);
            break;
        }

        return InteractionResult.PASS;
    }

    public void onPrayingSkeletonAttacked(Display.ItemDisplay prayingSkeletonEntity) {
    }

    @Override
    public void close() {

    }
}
