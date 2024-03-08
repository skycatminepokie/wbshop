package com.skycat.wbshop.econ;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.util.LogLevel;
import com.skycat.wbshop.util.Utils;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import lombok.NonNull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents and handles the points economy.
 *
 * @author skycatminepokie
 */
public class Economy extends PersistentState implements EconomyProvider { // TODO: Cleanup, especially not having defaults set outside of constructor
    /**
     * The file prefix for the save location: {@code world/data/SAVE_ID.dat}
     */
    public static final String SAVE_ID = WBShop.MOD_ID + "_economy";
    public static final String PROVIDER_ID = "wbshop";
    public static final Points CURRENCY = new Points();
    public static final Codec<Economy> CODEC = RecordCodecBuilder.create(economy -> economy.group(
            Codec.INT.fieldOf("configVersion").forGetter(Economy::getConfigVersion),
            Account.CODEC.listOf().fieldOf("accounts").forGetter(Economy::getAccountList),
            Codec.STRING.optionalFieldOf("borderFunctionString", "sqrt(points)").forGetter(Economy::getBorderFunctionString)
    ).apply(economy, Economy::new));
    private final HashMap<UUID, Account> accounts = new HashMap<>(); // Keep it in a HashMap for fast lookup.
    private String borderFunctionString = "sqrt(points)";
    private Expression borderFunction = new ExpressionBuilder(borderFunctionString)
            .variable("points")
            .build();
    private int configVersion = 0;

    public Economy() {
    }

    private Economy(int configVersion, List<Account> accountList, String borderFunctionString) {
        this.configVersion = configVersion;
        for (Account account : accountList) {
            accounts.put(account.owner(), account);
        }
        this.borderFunctionString = borderFunctionString;
        borderFunction = new ExpressionBuilder(borderFunctionString)
                .variable("points")
                .build();
    }

    public static Economy readFromNbt(NbtCompound nbt) {
        var result = CODEC.decode(NbtOps.INSTANCE, nbt.get("economy")).result();
        if (result.isEmpty()) {
            Utils.log("WBShop couldn't load the economy. This is normal when you start a new world.");
            return null;
        }
        return result.get().getFirst();
    }

    @NotNull
    public static ItemStack makeVoucher(long amount) {
        if (amount <= 0) {
            Utils.log("Making voucher for " + amount + " points, which is <= 0. That's strange, but we'll do it anyway...", LogLevel.WARN);
        }
        ItemStack voucher = new ItemStack(Items.PAPER, 1);

        NbtCompound nbt = new NbtCompound();
        // NbtString#of apparently needs the JSON format of a Text in the form of a string
        nbt.put("wbpoints", NbtLong.of(amount));
        voucher.setNbt(nbt);

        NbtList lore = new NbtList();
        lore.add(NbtString.of(Text.Serializer.toJson(Text.of(amount + " point" + (amount == 1 ? "" : "s")))));

        voucher.getOrCreateSubNbt("display").put("Lore", lore);

        voucher.setCustomName(Text.of("Point Voucher"));
        return voucher;
    }

    public String getBorderFunctionString() {
        return borderFunctionString;
    }

    /**
     * Try to set the border function.
     *
     * @param newExpression The string expression to parse.
     * @param server The server to set the border on
     * @return True if the function is valid, false if the function is not valid.
     */
    public boolean setBorderFunction(String newExpression, @NonNull MinecraftServer server) {
        Expression newFunction = new ExpressionBuilder(newExpression).variable("points").build();
        newFunction.setVariable("points", getTotalPoints());
        if (newFunction.validate().isValid()) {
            borderFunction = newFunction;
            borderFunctionString = newExpression;
            markDirty();
            WBShop.updateBorder(server);
            return true;
        }
        return false;
    }

    public long getTotalPoints() {
        long total = 0;
        for (Account account : accounts.values()) {
            total += account.balance();
        }
        return total;
    }

    /**
     * Calculates how big the border should be.
     *
     * @param points The total points in the economy.
     * @return The width of the border - minimum of 5.
     */
    public Double evaluateBorderSize(Long points) throws ArithmeticException {
        try {
            return Math.ceil(Math.max(borderFunction.setVariable("points", points).evaluate(), 5));
        } catch (Exception e) {
            Utils.log("Could not evaluate border expression with " + points + " points. Is it written properly?");
        }
        return 5.0;
    }

    /**
     * Get a list of all accounts.
     * For read-only access.
     *
     * @return A new list containing all accounts.
     */
    public ArrayList<Account> getAccountList() {
        return new ArrayList<>(accounts.values());
    }

    public int getConfigVersion() {
        return this.configVersion;
    }

    public Account getOrCreateAccount(GameProfile profile) {
        return getOrCreateAccount(profile.getId());
    }

    public Account getOrCreateAccount(UUID uuid) {
        if (accounts.containsKey(uuid)) {
            return accounts.get(uuid);
        }
        Account newAccount = new Account(uuid);
        accounts.put(uuid, newAccount);
        return newAccount;
    }

    public Account getOrCreateAccount(ServerPlayerEntity player) {
        return getOrCreateAccount(player.getUuid());
    }

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

    public long pointValueOf(Collection<ItemStack> stacks) {
        long sum = 0;
        for (ItemStack stack : stacks) {
            sum += pointValueOf(stack);
        }
        return sum;
    }

    public long pointValueOf(ItemStack stack) {
        if (stack.hasNbt()) {
            NbtCompound nbt = stack.getNbt();
            if (nbt == null) {
                Utils.log("ItemStack had custom nbt, but the nbt was null. I don't think that should happen.");
            } else {
                NbtLong points = (NbtLong) nbt.get("wbpoints");
                if (points != null) {
                    return points.longValue() * stack.getCount();
                }
            }
        }
        return stack.getCount() * pointValueOf(stack.getItem());
    }

    public long pointValueOf(Item itemType) {
        return WBShop.globalConfig.getItemValue(itemType);
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
}
