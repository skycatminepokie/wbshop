package com.skycat.wbshop.gui;

import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.util.Utils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CurrentPointsIcon extends GuiElement {
    public CurrentPointsIcon(ServerPlayerEntity player) {
        super(makeItemStack(player), CurrentPointsIcon::onClicked);
    }

    private static ItemStack makeItemStack(ServerPlayerEntity player) {
        ItemStack stack = new ItemStack(Items.PAPER, 1);
        if (WBShop.ECONOMY != null) {
            stack.setCustomName(Text.of("You have " + WBShop.ECONOMY.getBalance(player) + " points."));
        } else {
            stack.setCustomName(Text.of("Error! Couldn't find how many points you have."));
            Utils.log("Couldn't create points stack for player " + player.getUuid() + ": Economy was null");
        }
        return stack;
    }

    static private void onClicked(int index, ClickType type, SlotActionType action, SlotGuiInterface gui) {

    }
}
