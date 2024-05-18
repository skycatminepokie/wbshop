package com.skycat.wbshop.econ;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycat.wbshop.BadStateException;
import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.util.LogLevel;
import com.skycat.wbshop.util.Utils;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class Account implements EconomyAccount {
    public static final String POINTS_ACCOUNT = "points_account";
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
    public static final Identifier ID = Identifier.of(WBShop.MOD_ID, POINTS_ACCOUNT);
    private final UUID owner;
    private final HashMap<Item, Long> donatedItemCounts;
    /**
     * The balance of this account. Modify only via {@link Account#setBalance(long)}.
     */
    private long balance;
    private long totalItemsDonated;


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
        totalItemsDonated = 0;
        for (Long l : donatedItemCounts.values()) {
            totalItemsDonated += l;
        }
    }

    /**
     * Record items as donated and award points.
     *
     * @param items The stacks of items to donate.
     * @return The number of points awarded for donating.
     */
    public long donateItems(Collection<ItemStack> items) {
        long value = 0L;
        for (ItemStack stack : items) {
            value += donateItems(stack);
        }
        return value;
    }

    /**
     * Record items as donated and award points.
     *
     * @param stack The stack of items to donate.
     * @return The number of points awarded for donating.
     */
    public long donateItems(ItemStack stack) {
        if (stack.getItem() == Items.AIR) return 0;
        long current = donatedItemCounts.getOrDefault(stack.getItem(), 0L);
        long value;
        try {
            value = WBShop.getEconomy().pointValueOf(stack);
        } catch (BadStateException e) {
            throw new RuntimeException(e);
        }
        donatedItemCounts.put(stack.getItem(), current + stack.getCount());
        addBalance(value);
        totalItemsDonated += stack.getCount();
        return value;
    }

    public void addBalance(long value) {
        setBalance(balance + value);
    }

    public HashMap<Item, Long> getDonatedItemCounts() {
        return donatedItemCounts;
    }

    public long getTotalItemsDonated() {
        return totalItemsDonated;
    }

    //<editor-fold desc="Patbox's Economy API Handling">
    @Override
    public Text name() {
        return Text.of("Worldborder Shop Account");
    } // TODO: add player's name

    @Override
    public UUID owner() {
        return owner;
    }

    @Override
    public Identifier id() {
        return ID;
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
        } else {
            Utils.log("Something tried to set the value of account " + id() + " to " + value + ". Negatives are not allowed, defaulting to 0.", LogLevel.WARN);
            balance = 0;
        }
        try { // TODO: Fix state handling :sob:
            WBShop.getEconomy().markDirty();
            WBShop.updateBorder();
        } catch (BadStateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EconomyProvider provider() {
        try {
            return WBShop.getEconomy();
        } catch (BadStateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EconomyCurrency currency() {
        return Economy.CURRENCY;
    }

    public EconomyTransaction tryTransaction(long value) {
        if (balance + value >= 0) {
            long old = balance;
            addBalance(value);
            return new EconomyTransaction.Simple(true,
                    Text.of("Success!"),
                    balance,
                    old,
                    value,
                    this
            );
        }
        return new EconomyTransaction.Simple(false, Text.of("Transaction failed"), balance, balance, value, this);
    }
    //</editor-fold>


    /**
     * Used for withdrawing points to a voucher.
     *
     * @param amount The amount to take from the player's account and grant as a voucher.
     * @param player The player to grant the voucher to. This can be someone other than the account owner, but I don't see why you'd do that.
     * @return {@code false} if the account does not have enough points, {@code true} on success.
     */
    public boolean withdraw(long amount, ServerPlayerEntity player) {
        if (amount <= 0) return false;
        if (amount > balance) return false;
        ItemStack voucher = Economy.makeVoucher(amount);

        player.getInventory().offerOrDrop(voucher);
        removeBalance(amount);
        return true;
    }

    public void removeBalance(long value) {
        addBalance(-value);
    }

}
