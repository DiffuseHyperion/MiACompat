package com.diffusehyperion.dmm;

import com.diffusehyperion.dmm.features.FeatureManager;
import com.diffusehyperion.dmm.mods.yacl.Config;
import com.diffusehyperion.dmm.gui.Hud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class DMM implements ClientModInitializer {
	public static final String MOD_ID = "dmm";
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Config config = new Config();
    private final Hud hud = new Hud();
    public static final FeatureManager featureManager = new FeatureManager();

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    public static boolean isMiAServer() {
        ServerData serverInfo = Minecraft.getInstance().getCurrentServer();
        return serverInfo != null && serverInfo.ip.contains("mineinabyss");
    }

	@Override
	public void onInitializeClient() {
        config.loadFromFile();

        HudElementRegistry.attachElementBefore(VanillaHudElements.HELD_ITEM_TOOLTIP, id("dmm_hud"), hud);

        featureManager.initializeFeatures();
	}

    public static void close() {
        featureManager.closeFeatures();
    }
}