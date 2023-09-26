package com.skycat.wbshop;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WBShopPoints implements EconomyCurrency {
    private final EconomyProvider provider;
    public WBShopPoints(EconomyProvider provider) {
        this.provider = provider;
    }
    @Override
    public Text name() {
        return Text.of("Points");
    }

    @Override
    public Identifier id() {
        return Identifier.of(WBShop.MOD_ID, "points");
    }

    @Override
    public String formatValue(long value, boolean precise) {
        return String.valueOf(value);
    }

    @Override
    public long parseValue(String value) throws NumberFormatException {
        return Long.parseLong(value);
    }

    @Override
    public EconomyProvider provider() {
        return provider;
    }
}
