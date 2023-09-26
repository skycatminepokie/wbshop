package com.skycat.wbshop;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class WBShopEconomy extends PersistentState implements EconomyProvider {
    public static final String SAVE_ID = "wbshopEconomy"; // Save it in wbshopEconomy.dat
    public static final Identifier ID = new Identifier(WBShop.MOD_ID, "economy");
    public final WBShopPoints CURRENCY = new WBShopPoints(this);


    @Override
    public Text name() {
        return Text.of("Worldborder Shop Economy");
    }

    @Override
    public @Nullable EconomyAccount getAccount(MinecraftServer server, GameProfile profile, String accountId) {
        return null; // TODO
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer server, GameProfile profile) {
        return null; // TODO
    }

    @Override
    public @Nullable EconomyCurrency getCurrency(MinecraftServer server, String currencyId) {
        if (Identifier.tryParse(currencyId) == CURRENCY.id()) { // TODO: ??? (identifier.toString().equals(string) && Identifier.tryParse(string) == identifier)? or two IDs?
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
        return null;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return null;
    }

    public static WBShopEconomy readFromNbt(NbtCompound nbt) {

    }
}
