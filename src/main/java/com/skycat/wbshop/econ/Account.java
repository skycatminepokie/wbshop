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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class Account implements EconomyAccount {
    private final UUID owner;
    /**
     * The balance of this account. Modify only via {@link Account#setBalance(long)}.
     */
    private long balance;
    private final HashMap<Item, Long> donatedItemCounts;
    private long totalItemsDonated;

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
    /**
     * Record items as donated and award points.
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
     * @param stack The stack of items to donate.
     * @return The number of points awarded for donating.
     */
    public long donateItems(ItemStack stack) {
        if (stack.getItem() == Items.AIR) return 0;
        long current = donatedItemCounts.getOrDefault(stack.getItem(), 0L);
        long value = WBShop.getEconomy().pointValueOf(stack);
        donatedItemCounts.put(stack.getItem(), current + stack.getCount());
        addBalance(value);
        totalItemsDonated += stack.getCount(); // TODO: Better way - make a wrapper on hashmap
        return value;
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
        totalItemsDonated = 0;
        for (Long l : donatedItemCounts.values()) {
            totalItemsDonated += l;
        }
    }

    public void addBalance(long value) {
        setBalance(balance + value);
    }

    public long getTotalItemsDonated() {
        return totalItemsDonated;
    }

    public void removeBalance(long value) {
        addBalance(-value);
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
        } else {
            WBShop.LOGGER.error("Something tried to set the value of account " + id() + " to " + value + ". Negatives are not allowed, defaulting to 0.");
            balance = 0;
        }
        WBShop.getEconomy().markDirty();
        WBShop.updateBorder();
    }

    @Override
    public EconomyProvider provider() {
        return WBShop.getEconomy();
    }

    @Override
    public EconomyCurrency currency() {
        return Economy.CURRENCY;
    }

    public EconomyTransaction tryTransaction(long value) {
        if (balance + value >= 0){
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
        return new EconomyTransaction.Simple(false, Text.of("Transaction failed"), balance, balance,value, this);
    }
    public static Identifier defaultId() {
        return Identifier.of(WBShop.MOD_ID, "points_account");
    }
    //</editor-fold>

}
