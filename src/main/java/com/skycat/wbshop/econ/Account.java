package com.skycat.wbshop.econ;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.util.Utils;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.HashMap;
import java.util.UUID;

public class Account implements EconomyAccount {
    private final UUID owner;
    private long balance;
    private final HashMap<Item, Long> donatedItemCounts;
    public static final Codec<Account> CODEC = RecordCodecBuilder.create((account) -> account.group(
            Uuids.CODEC.fieldOf("owner").forGetter(Account::owner),
            Codec.LONG.fieldOf("balance").forGetter(Account::balance),
            Utils.hashMapCodec(
                    Registries.ITEM.getCodec(),
                    "item",
                    Codec.LONG,
                    "count"
            ).fieldOf("donatedItemCounts").forGetter(Account::getDonatedItemCounts)
    ).apply(account, Account::new));


    public HashMap<Item, Long> getDonatedItemCounts() {
        return donatedItemCounts;
    }

    public Account(UUID owner) {
        this(owner, 0);
    }

    public Account(UUID owner, long balance) {
        this(owner, balance, new HashMap<>());
    }

    public Account(UUID owner, long balance, HashMap<Item, Long> donatedItemCounts) {
        this.owner = owner;
        this.balance = balance;
        this.donatedItemCounts = donatedItemCounts;
    }

    //<editor-fold desc="Patbox's Economy API Handling">
    @Override
    public Text name() {
        return Text.of("Worldborder Shop Account");
    }

    @Override
    public UUID owner() {
        return owner;
    }

    @Override
    public Identifier id() { // I believe this is the type of account this is
        return defaultId();
    }

    @Override
    public long balance() {
        return balance;
    }

    @Override
    public EconomyTransaction canIncreaseBalance(long value) {
        return tryTransaction(value);
    }

    @Override
    public EconomyTransaction canDecreaseBalance(long value) {
        return tryTransaction(-value);
    }

    @Override
    public void setBalance(long value) throws IllegalArgumentException {
        if (value >= 0) {
            balance = value;
        }
        WBShop.LOGGER.error("Something tried to set the value of account " + id() + " to " + value + ". Negatives are not allowed, defaulting to 0.");
        balance = 0;
    }

    @Override
    public EconomyProvider provider() {
        return WBShop.ECONOMY;
    }

    @Override
    public EconomyCurrency currency() {
        return Economy.CURRENCY;
    }

    public EconomyTransaction tryTransaction(long value) {
        if (balance + value >= 0){
            long old = balance;
            balance += value;
            return new EconomyTransaction.Simple(true,
                    Text.of("Success!"),
                    balance,
                    old,
                    value,
                    this
            );
        }
        return new EconomyTransaction.Simple(false, Text.of("Transaction failed"), balance, balance,value, this);
    }
    public static Identifier defaultId() {
        return Identifier.of(WBShop.MOD_ID, "points_account");
    }
    //</editor-fold>

}
