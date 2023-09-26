package com.skycat.wbshop;

import eu.pb4.common.economy.api.CommonEconomy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WBShop implements ModInitializer, ServerWorldEvents.Load {
    public static final String MOD_ID = "wbshop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static WBShopEconomy ECONOMY;

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register(this);
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {
        if (world.isClient) return;
        ECONOMY = server.getOverworld().getPersistentStateManager().getOrCreate(WBShopEconomy::readFromNbt, WBShopEconomy::new, WBShopEconomy.SAVE_ID);
        CommonEconomy.register(WBShopEconomy.ID.toString(), ECONOMY);
    }
}
