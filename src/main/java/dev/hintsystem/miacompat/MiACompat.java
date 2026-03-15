package dev.hintsystem.miacompat;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.hintsystem.miacompat.client.GhostSeekRenderer;
import dev.hintsystem.miacompat.client.GhostSeekTracker;
import dev.hintsystem.miacompat.config.Config;
import dev.hintsystem.miacompat.gui.Hud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MiACompat implements ClientModInitializer {
	public static final String MOD_ID = "miacompat";
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Config config = new Config();

    public static final GhostSeekTracker ghostSeekTracker = new GhostSeekTracker();
    private static final GhostSeekRenderer ghostSeekRenderer = new GhostSeekRenderer(ghostSeekTracker);

    private final Hud hud = new Hud();

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    public static boolean isMiAServer() {
        ServerData serverInfo = Minecraft.getInstance().getCurrentServer();
        return serverInfo != null && serverInfo.ip.contains("mineinabyss");
    }

	@Override
	public void onInitializeClient() {
        config.loadFromFile();

        Minecraft client = Minecraft.getInstance();

        // hud.tick(); tick method empty for now
        ClientTickEvents.END_CLIENT_TICK.register(ghostSeekTracker::tick);

        HudElementRegistry.attachElementBefore(VanillaHudElements.HELD_ITEM_TOOLTIP, id("miacompat_hud"), hud);

        WorldRenderEvents.END_MAIN.register(this::onRenderWorld);

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide()) return InteractionResult.PASS;

            return InteractionResult.PASS;
        });

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

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("miacompat")
                        .then(ClientCommandManager.literal("config")
                                .executes(context -> {
                                    client.schedule(() -> client.setScreen(config.createScreen(null)));
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("breadcrumbs")
                                .then(ClientCommandManager.literal("add")
                                        .then(ClientCommandManager.argument("pingLength", IntegerArgumentType.integer(0, 5))
                                                .executes(context -> {
                                                    if (client.player == null) return 0;

                                                    int pingLength = IntegerArgumentType.getInteger(context, "pingLength");

                                                    GhostSeekTracker.GhostSeekType ghostSeekType = GhostSeekTracker.GhostSeekType.REFINED;
                                                    ghostSeekTracker.awaitingPingTicks = ghostSeekType.pingIntervalTicks;
                                                    ghostSeekTracker.addMeasurement(
                                                            ghostSeekType.getPingMeasurement(client.player.position(), pingLength)
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                                .then(ClientCommandManager.literal("clear")
                                        .executes(context -> {
                                            ghostSeekTracker.clearMeasurements();
                                            return 1;
                                        })
                                )
                        )
                ));
	}

    private void onRenderWorld(WorldRenderContext context) {
        ghostSeekRenderer.render(context);
    }

    public static void close() {
        ghostSeekRenderer.close();
    }
}