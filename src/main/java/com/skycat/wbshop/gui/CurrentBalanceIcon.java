package com.skycat.wbshop.gui;

import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.util.Utils;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CurrentBalanceIcon extends GuiElement {
    /**
     * Makes an icon with no callback.
     * @param player Player that owns the points
     */
    public CurrentBalanceIcon(ServerPlayerEntity player) {
        this(player, GuiElementInterface.EMPTY_CALLBACK);
    }

    public CurrentBalanceIcon(ServerPlayerEntity player, ClickCallback callback) {
        super(makeItemStack(player), callback);
    }

    private static ItemStack makeItemStack(ServerPlayerEntity player) {
        ItemStack stack = new ItemStack(Items.PAPER, 1); // TODO: Make this player skull
        if (WBShop.ECONOMY != null) {
            stack.setCustomName(Text.of("You have " + WBShop.ECONOMY.getOrCreateAccount(player).balance() + " points."));
        } else {
            stack.setCustomName(Text.of("Error! Couldn't find how many points you have."));
            Utils.log("Couldn't create points stack for player " + player.getUuid() + ": Economy was null");
        }
        return stack;
    }
}
