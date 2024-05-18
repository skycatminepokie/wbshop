package com.skycat.wbshop.econ;

import com.skycat.wbshop.BadStateException;
import com.skycat.wbshop.WBShop;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Points implements EconomyCurrency {
    public static final Identifier ID = Identifier.of(WBShop.MOD_ID, "points");
    public static final String STRING_ID = "points";
    @Override
    public Text name() {
        return Text.of("Points");
    }

    @Override
    public Identifier id() {
        return ID;
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
        try {
            return WBShop.getEconomy();
        } catch (BadStateException e) {
            throw new RuntimeException(e);
        }
    }
}
