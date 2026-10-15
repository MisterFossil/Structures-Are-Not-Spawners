package com.mr.structures_are_not_spawners;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SANS.MOD_ID)
public final class SANS {
    public static final String MOD_ID = "structures_are_not_spawners";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SANS(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(Config::onConfigLoad);
        modEventBus.addListener(Config::onConfigReload);
    }
}
