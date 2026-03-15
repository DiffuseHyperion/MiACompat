package com.diffusehyperion.dmm.mods.modmenu;

import com.diffusehyperion.dmm.DMM;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class SupportModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DMM.config::createScreen;
    }
}
