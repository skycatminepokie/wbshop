package com.skycat.wbshop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.common.economy.api.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;

import java.util.UUID;

public class WBShopAccount implements EconomyAccount {

    private final UUID owner;
    private long balance;
    private final EconomyProvider provider;
    private final EconomyCurrency pointsCurrency;

    public WBShopAccount(UUID owner, long balance, EconomyProvider provider, EconomyCurrency pointsCurrency) {
        this.owner = owner;
        this.balance = balance;
        this.provider = provider;
        this.pointsCurrency = pointsCurrency;
    }

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
        WBShop.LOGGER.error("Someone tried to set the value of account " + id() + " to " + value + ". Negatives are not allowed, defaulting to 0.");
        balance = 0;
    }

    @Override
    public EconomyProvider provider() {
        return provider;
    }

    @Override
    public EconomyCurrency currency() {
        return pointsCurrency;
    }

    private EconomyTransaction tryTransaction(long value) {
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
        return new EconomyTransaction.Simple(false,
                Text.of("Too rich! (would have negative balance)"),
                balance,
                balance,
                value, // Hopefully this is right and it's not supposed to be 0
                this
        );
    }
    public static Identifier defaultId() {
        return Identifier.of(WBShop.MOD_ID, "points_account");
    }

}
