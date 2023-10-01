package com.skycat.wbshop.econ;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.util.Utils;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Represents and handles the points economy.
 *
 * @author skycatminepokie
 */
public class Economy extends PersistentState implements EconomyProvider {
    /**
     * The file prefix for the save location: {@code world/data/$SAVE_ID.dat}
     */
    public static final String SAVE_ID = WBShop.MOD_ID + "_economy";
    public static final Points CURRENCY = new Points();
    public static final Codec<Economy> CODEC = RecordCodecBuilder.create(economy -> economy.group(
            Codec.INT.fieldOf("configVersion").forGetter(Economy::getConfigVersion),
            Account.CODEC.listOf().fieldOf("accounts").forGetter(Economy::getAccountList)
    ).apply(economy, Economy::new));
    private final HashMap<UUID, Account> accounts = new HashMap<>(); // Keep it in a HashMap for fast lookup.
    private int configVersion = 0;

    public Economy() {
    }

    private Economy(int configVersion, List<Account> accountList) {
        this.configVersion = configVersion;
        for (Account account : accountList) {
            accounts.put(account.owner(), account);
        }
    }

    public Account getOrCreateAccount(UUID uuid) {
        if (accounts.containsKey(uuid)) {
            return accounts.get(uuid);
        }
        Account newAccount = new Account(uuid);
        accounts.put(uuid, newAccount);
        return newAccount;
    }

    public Account getOrCreateAccount(GameProfile profile) {
        return getOrCreateAccount(profile.getId());
    }

    public Account getOrCreateAccount(ServerPlayerEntity player) {
        return getOrCreateAccount(player.getUuid());
    }

    /**
     * Get a list of all accounts.
     * Utility method for serialization.
     * @return A new list containing all accounts.
     */
    private List<Account> getAccountList() {
        return accounts.values().stream().toList();
    }

    public int getConfigVersion() {
        return this.configVersion;
    }

    public static Economy readFromNbt(NbtCompound nbt) {
        var result = CODEC.decode(NbtOps.INSTANCE, nbt.get("economy")).result();
        if (result.isEmpty()) {
            Utils.log("WBShop couldn't load the economy. This is normal when you start a new world.");
            return null;
        }
        return result.get().getFirst();
    }

    public long pointValueOf(Collection<ItemStack> stacks) {
        long sum = 0;
        for (ItemStack stack : stacks) {
            sum += pointValueOf(stack);
        }
        return sum;
    }

    public long pointValueOf(ItemStack stack) {
        return stack.getCount() * pointValueOf(stack.getItem());
    }

    public long pointValueOf(Item itemType) {
        return 1; // TODO: Configurable
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var encodedResult = CODEC.encode(this, NbtOps.INSTANCE, NbtOps.INSTANCE.empty()).result();
        if (encodedResult.isEmpty()) {
            Utils.log("Well crud. WBShop couldn't save the economy.");
            throw new RuntimeException("WBShop couldn't save the economy - Codec returned empty result.");
        }
        nbt.put("economy", encodedResult.get());
        return nbt;
    }

    //<editor-fold desc="Patbox's Common Economy API handling">
    @Override
    public Text name() {
        return Text.of("WBShop Economy");
    }

    @Override
    public @Nullable EconomyAccount getAccount(MinecraftServer server, GameProfile profile, String accountId) {
        if (accountId.equals(Account.defaultId().toString())) {
            return accounts.get(profile.getId());
        }
        return null;
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer server, GameProfile profile) {
        return List.of(accounts.get(profile.getId()));
    }

    @Override
    public @Nullable EconomyCurrency getCurrency(MinecraftServer server, String currencyId) {
        if (CURRENCY.id().toString().equals(currencyId)) {
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
            return Account.defaultId().toString();
        }
        return null;
    }
    //</editor-fold>
}
