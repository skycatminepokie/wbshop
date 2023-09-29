package com.skycat.wbshop;

import com.skycat.wbshop.command.CommandHandler;
import com.skycat.wbshop.econ.WBShopEconomy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WBShop implements ModInitializer, ServerWorldEvents.Load, ServerWorldEvents.Unload {
    public static final String MOD_ID = "wbshop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /**
     * Null when world is remote or not loaded.
     */
    public static WBShopEconomy ECONOMY = null;
    private static final CommandHandler COMMAND_HANDLER = new CommandHandler();

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register(this);
        CommandRegistrationCallback.EVENT.register(COMMAND_HANDLER);
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {
        if (world.isClient) {
            ECONOMY = null; // Potentially unnecessary? Not gonna mess with it though.
            return;
        }
        ECONOMY = server.getOverworld().getPersistentStateManager().getOrCreate(WBShopEconomy::readFromNbt, WBShopEconomy::new, WBShopEconomy.SAVE_ID);
    }


    @Override
    public void onWorldUnload(MinecraftServer server, ServerWorld world) {
        ECONOMY = null;
    }
}
