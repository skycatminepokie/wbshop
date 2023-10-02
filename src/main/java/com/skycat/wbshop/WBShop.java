package com.skycat.wbshop;

import com.skycat.wbshop.command.CommandHandler;
import com.skycat.wbshop.econ.Economy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WBShop implements ModInitializer, ServerWorldEvents.Load, ServerWorldEvents.Unload {
    public static final String MOD_ID = "wbshop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static @Nullable MinecraftServer server = null;
    private static Economy economy = null;
    private static final CommandHandler COMMAND_HANDLER = new CommandHandler();

    /**
     * Null when world is remote or not loaded.
     */
    public static Economy getEconomy() {
        return economy;
    }

    /**
     * Null when world is remote or not loaded.
     */
    public static @Nullable MinecraftServer getServer() {
        return server;
    }

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register(this);
        CommandRegistrationCallback.EVENT.register(COMMAND_HANDLER);
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {
        if (world.isClient) {
            economy = null; // Potentially unnecessary? Not gonna mess with it though.
            return;
        }
        economy = server.getOverworld().getPersistentStateManager().getOrCreate(Economy::readFromNbt, Economy::new, Economy.SAVE_ID);
    }


    @Override
    public void onWorldUnload(MinecraftServer server, ServerWorld world) {
        economy = null;
    }
}
