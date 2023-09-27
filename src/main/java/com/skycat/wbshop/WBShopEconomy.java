package com.skycat.wbshop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.UUID;

public class WBShopEconomy extends PersistentState {
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
