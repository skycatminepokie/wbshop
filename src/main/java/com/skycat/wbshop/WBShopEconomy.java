package com.skycat.wbshop;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WBShopEconomy extends PersistentState implements EconomyProvider {
    public final Codec<WBShopAccount> ACCOUNT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Uuids.CODEC.fieldOf("owner").forGetter(WBShopAccount::owner),
                Codec.LONG.fieldOf("balance").forGetter(WBShopAccount::balance)
            ).apply(instance, this::newAccount));
    public static final String SAVE_ID = "wbshopEconomy"; // Save it in wbshopEconomy.dat
    public static final Identifier ID = new Identifier(WBShop.MOD_ID, "economy");
    public final WBShopPoints CURRENCY = new WBShopPoints(this);
    private HashMap<UUID, WBShopAccount> accounts = new HashMap<>();

    /**
     * Gets a player's account by uuid, or creates a new account for the player.
     * @param playerUUID The player.
     * @return The player's account.
     */
    public WBShopAccount getOrCreateAccount(UUID playerUUID) {
        return getOrCreateAccount(playerUUID, 0);
    }

    /**
     * Gets a player's account by uuid, or creates a new account for the player. <p>
     *     Searching does not match balance.
     * @param playerUUID The player.
     * @param defaultBalance Balance for new accounts.
     * @return The player's account.
     */
    public WBShopAccount getOrCreateAccount(UUID playerUUID, long defaultBalance) {
        if (accounts.containsKey(playerUUID)) {
            return accounts.get(playerUUID);
        }
        return accounts.put(playerUUID, newAccount(playerUUID, defaultBalance));
    }

    /**
     * Makes a new account, but does not register it.
     * @param owner The owner of the account.
     * @param balance The balance the account should have.
     * @return A new account, not registered.
     */
    private WBShopAccount newAccount(UUID owner, long balance) {
        return new WBShopAccount(owner, balance, this, CURRENCY);
    }

    @Override
    public Text name() {
        return Text.of("Worldborder Shop Economy");
    }

    @Override
    public @Nullable EconomyAccount getAccount(MinecraftServer server, GameProfile profile, String accountId) {
        return getOrCreateAccount(profile.getId());
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer server, GameProfile profile) {
        return List.of(getOrCreateAccount(profile.getId()));
    }

    @Override
    public @Nullable EconomyCurrency getCurrency(MinecraftServer server, String currencyId) {
        if (Identifier.tryParse(currencyId) == CURRENCY.id()) { // Assuming (identifier.toString().equals(string) && Identifier.tryParse(string) == identifier)
            return CURRENCY;
        }
        return null;
    }

    @Override
    public Collection<EconomyCurrency> getCurrencies(MinecraftServer server) {
        return List.of(CURRENCY);
    }

    @Override
    public @Nullable String defaultAccount(MinecraftServer server, GameProfile profile, EconomyCurrency currency) {
        if (currency == CURRENCY) {
            return WBShopAccount.defaultId().toString();
        }
        return null;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return null; // TODO
    }

    public static WBShopEconomy readFromNbt(NbtCompound nbt) {
        return null; // TODO
    }
}
