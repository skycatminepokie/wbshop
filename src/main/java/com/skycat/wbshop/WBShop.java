package com.skycat.wbshop;

import com.skycat.wbshop.command.CommandHandler;
import com.skycat.wbshop.econ.Account;
import com.skycat.wbshop.econ.Economy;
import lombok.NonNull;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
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

import java.util.Objects;

public class WBShop implements ModInitializer, ServerWorldEvents.Load, ServerWorldEvents.Unload, ServerLivingEntityEvents.AfterDeath {
    public static final String MOD_ID = "wbshop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static @Nullable MinecraftServer server = null;
    private static final CommandHandler COMMAND_HANDLER = new CommandHandler();

    /**
     * Fails when world is remote or not loaded.
     */
    public static Economy getEconomy() throws BadStateException { // This shouldn't cause problems with saving/loading, since Minecraft seems to cache it.
        if (server == null) {
            throw new BadStateException("Failed to get economy, server cache was null.");
        }
        return server.getOverworld().getPersistentStateManager().getOrCreate(Economy::readFromNbt, Economy::new, Economy.SAVE_ID);
    }

    /**
     * Counterpart to {@link WBShop#getEconomy()}, requiring a {@link MinecraftServer} but shouldn't fail. <p>
     * Use this instead of {@link WBShop#getEconomy()} when it's not inconvenient.
     * @param server The server to grab the economy from.
     * @return The economy.
     */
    public static @NonNull Economy getEconomy(@NonNull MinecraftServer server) {
        return Objects.requireNonNull(server.getOverworld().getPersistentStateManager().getOrCreate(Economy::readFromNbt, Economy::new, Economy.SAVE_ID)); // I'm pretty confident in that requireNonNull.
    }

    /**
     * Null when world is remote or not loaded.
     */
    public static @Nullable MinecraftServer getServer() {
        return server;
    }

    public static @NonNull Economy getEconomy(@NonNull ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        assert server != null;
        return getEconomy(server);
    }

    public static void updateBorder() throws BadStateException {
        Economy economy = getEconomy();
        MinecraftServer server = getServer();
        if (server == null) {
            throw new BadStateException("Could not update border, since the server was null.");
        }
        getServer().getOverworld().getWorldBorder().setSize(economy.evaluateBorderSize(economy.getTotalPoints()));
    }

    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity instanceof ServerPlayerEntity player) {
            Economy econ = getEconomy(player);
            Account account = econ.getOrCreateAccount(player);
            long pointsLost = (long) Math.ceil(account.balance() * 0.1);
            account.removeBalance(pointsLost);
            player.sendMessage(Text.of("You died and lost " + pointsLost + " points."));
        }
    }

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register(this);
        CommandRegistrationCallback.EVENT.register(COMMAND_HANDLER);
        ServerLivingEntityEvents.AFTER_DEATH.register(this);
        // CommonEconomy.register(EconomyProvider) // Hmm. How is this gonna work?
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {
        if (world.isClient) {
            return;
        }
        WBShop.server = server;
        try {
            updateBorder();
        } catch (BadStateException e) {
            throw new RuntimeException(e); // State management shouldn't allow this. Probably.
        }
    }


    @Override
    public void onWorldUnload(MinecraftServer server, ServerWorld world) {
    }
}
