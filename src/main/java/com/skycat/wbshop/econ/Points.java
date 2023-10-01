package com.skycat.wbshop.econ;

import com.skycat.wbshop.WBShop;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Points implements EconomyCurrency {
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
        return WBShop.ECONOMY;
    }
}
