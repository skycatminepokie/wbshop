package com.skycat.wbshop;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycat.wbshop.util.Utils;
import lombok.Getter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.UUID;

/**
 * Represents and handles the points economy.
 * @author skycatminepokie
 */
public class WBShopEconomy extends PersistentState {
    /**
     * The file prefix for the save location: {@code world/data/$SAVE_ID.dat}
     */
    public static final String SAVE_ID = WBShop.MOD_ID + "_economy"; // Save in SAVE_ID.dat
    private static final Codec<HashMap<UUID, Long>> ACCOUNTS_CODEC = Utils.hashMapCodec(Uuids.CODEC,
            "uuid",
            Codec.LONG,
            "balance");
    public static final Codec<WBShopEconomy> CODEC = RecordCodecBuilder.create(economy -> economy.group(
            Codec.INT.fieldOf("configVersion").forGetter(WBShopEconomy::getConfigVersion),
            ACCOUNTS_CODEC.fieldOf("accounts").forGetter(WBShopEconomy::getAccounts)
    ).apply(economy, WBShopEconomy::new));
    @Getter
    private HashMap<UUID, Long> accounts = new HashMap<>();
    @Getter
    private int configVersion = 0;

    public WBShopEconomy() {}

    public WBShopEconomy(HashMap<UUID, Long> accounts) {
        this.accounts = accounts;
    }

    public WBShopEconomy(int configVersion, HashMap<UUID, Long> accounts) {
        this.accounts = accounts;
        this.configVersion = configVersion;
    }

    /**
     * Gets the balance of a player.
     * @param player The player to get the balance of.
     */
    public long getBalance(UUID player) {
        return accounts.get(player);
    }

    /**
     * Gets the balance of a player.
     * @param player The player to get the balance of.
     * @implNote Overload of {@link WBShopEconomy#getBalance(UUID)}.
     */
    public long getBalance(ServerPlayerEntity player) {
        return getBalance(player.getUuid());
    }

    /**
     * Gets the balance of a player.
     * @param player The player to get the balance of.
     * @implNote Overload of {@link WBShopEconomy#getBalance(UUID)}.
     */
    public long getBalance(GameProfile player) {
        return getBalance(player.getId());
    }

    /**
     * Sets the balance of a player.
     * @param player The player to set the balance of.
     * @param balance The balance to give the player.
     */
    private void setBalance(UUID player, long balance) {
        accounts.put(player, balance);
        this.markDirty();
    }

    /**
     * Sets the balance of a player.
     * @param player The player to set the balance of.
     * @param balance The balance to give the player.
     * @implNote Overload of {@link WBShopEconomy#setBalance(UUID, long)}.
     */
    private void setBalance(GameProfile player, long balance) {
        setBalance(player.getId(), balance);
    }

    /**
     * Sets the balance of a player.
     * @param player The player to set the balance of.
     * @param balance The balance to give the player.
     * @implNote Overload of {@link WBShopEconomy#setBalance(UUID, long)}.
     */
    private void setBalance(ServerPlayerEntity player, long balance) {
        setBalance(player.getUuid(), balance);
    }

    /**
     * Adds to a player's balance.
     * @param player The player to add points to.
     * @param balance The number of points to add.
     * @return The player's new balance.
     */
    private long addBalance(UUID player, long balance) {
        long newBalance = getBalance(player) + balance;
        setBalance(player, newBalance);
        return newBalance;
    }

    /**
     * Adds to a player's balance.
     * @param player The player to add points to.
     * @param balance The number of points to add.
     * @return The player's new balance.
     * @implNote Overload of {@link WBShopEconomy#addBalance(UUID, long)}.
     */
    private long addBalance(GameProfile player, long balance) {
        return addBalance(player.getId(), balance);
    }

    /**
     * Adds to a player's balance.
     * @param player The player to add points to.
     * @param balance The number of points to add.
     * @return The player's new balance.
     * @implNote Overload of {@link WBShopEconomy#addBalance(UUID, long)}.
     */
    private long addBalance(ServerPlayerEntity player, long balance) {
        return addBalance(player.getUuid(), balance);
    }

    /**
     * Removes from a player's balance.
     * @param player The player to remove points from.
     * @param balance The number of points to remove.
     * @return The player's new balance.
     * @implNote Overload of {@link WBShopEconomy#addBalance(UUID, long)}.
     */
    private long removeBalance(UUID player, long balance) {
        return addBalance(player, -balance);
    }

    /**
     * Removes from a player's balance.
     * @param player The player to remove points from.
     * @param balance The number of points to remove.
     * @return The player's new balance.
     * @implNote Overload of {@link WBShopEconomy#removeBalance(UUID, long)}.
     */
    private long removeBalance(GameProfile player, long balance) {
        return removeBalance(player.getId(), balance);
    }

    /**
     * Removes from a player's balance.
     * @param player The player to remove points from.
     * @param balance The number of points to remove.
     * @return The player's new balance.
     * @implNote Overload of {@link WBShopEconomy#removeBalance(UUID, long)}.
     */
    private long removeBalance(ServerPlayerEntity player, long balance) {
        return removeBalance(player.getUuid(), balance);
    }

    public static WBShopEconomy readFromNbt(NbtCompound nbt) {
        var result = CODEC.decode(NbtOps.INSTANCE, nbt.get("economy")).result();
        if (result.isEmpty()) {
            Utils.log("WBShop couldn't load the economy. This is normal when you start a new world.");
            return null;
        }
        return result.get().getFirst();
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
