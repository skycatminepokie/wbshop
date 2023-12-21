package com.skycat.wbshop;

import com.skycat.wbshop.command.CommandHandler;
import com.skycat.wbshop.econ.Account;
import com.skycat.wbshop.econ.Economy;
import com.skycat.wbshop.util.Utils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WBShop implements ModInitializer, ServerWorldEvents.Load, ServerWorldEvents.Unload, ServerLivingEntityEvents.AfterDeath, ServerLifecycleEvents.ServerStopping {
    public static final String MOD_ID = "wbshop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static @Nullable MinecraftServer server = null;
    private static Economy economy = null;
    private static final CommandHandler COMMAND_HANDLER = new CommandHandler();
    public static GlobalConfig globalConfig = GlobalConfig.load();

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

    public static void updateBorder() {
        if (getServer() != null && getEconomy() != null && getServer().getOverworld() != null) { // WARN something can easily be null here
            getServer().getOverworld().getWorldBorder().setSize(getEconomy().evaluateBorderSize(economy.getTotalPoints()));
        } else {
            Utils.log("Attempted to update border while something was null - this shouldn't happen. Hopefully nothing goes wrong, but please report this.");
        }
    }

    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity instanceof ServerPlayerEntity player) {
            Economy econ = getEconomy();
            if (econ != null) {
                Account account = econ.getOrCreateAccount(player);
                long pointsLost = (long) Math.ceil(account.balance() * 0.1);
                account.removeBalance(pointsLost);
                player.sendMessage(Text.of("You died and lost " + pointsLost + " points."));
            }
        }
    }

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register(this);
        CommandRegistrationCallback.EVENT.register(COMMAND_HANDLER);
        ServerLivingEntityEvents.AFTER_DEATH.register(this);
        ServerLifecycleEvents.SERVER_STOPPING.register(this);
        updateBorder();
        // CommonEconomy.register(EconomyProvider) // Hmm. How is this gonna work?
    }

    @Override
    public void onServerStopping(MinecraftServer server) {
        globalConfig.save();
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {
        if (world.isClient) {
            economy = null; // Potentially unnecessary? Not gonna mess with it though.
            return;
        }
        WBShop.server = server;
        economy = server.getOverworld().getPersistentStateManager().getOrCreate(Economy::readFromNbt, Economy::new, Economy.SAVE_ID);
    }


    @Override
    public void onWorldUnload(MinecraftServer server, ServerWorld world) {
        economy = null;
    }
}
