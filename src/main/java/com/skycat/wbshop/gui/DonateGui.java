package com.skycat.wbshop.gui;

import com.skycat.wbshop.WBShop;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonateGui extends SimpleGui {
    public CurrentPointsIcon currentPointsIcon;
    public GuiElement donationWorthIcon;

    /**
     * Constructs a new donation container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     */
    public DonateGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        setLockPlayerInventory(false);
        setTitle(Text.of("Donate"));
        currentPointsIcon = new CurrentPointsIcon(player);
        donationWorthIcon = new GuiElement(new ItemStack(Items.PAPER, 1), GuiElementInterface.EMPTY_CALLBACK); // TODO


        setSlot(0, currentPointsIcon);
        setSlot(1, donationWorthIcon);
        setupTopDonationIcons(2, 5);
    }

    private void setupTopDonationIcons(int startingIndex, int numberOfIcons) { // TODO: Optimize sort
        if (startingIndex + numberOfIcons > this.getSize()) {
            throw new IllegalArgumentException("startingIndex + numberOfIcons may not exceed number of slots");
        }
        HashMap<Item, Long> donatedItems = WBShop.ECONOMY.getOrCreateAccount(player).getDonatedItemCounts();
        ArrayList<Map.Entry<Item, Long>> itemList = new ArrayList<>(donatedItems.entrySet());
        itemList.sort(Map.Entry.comparingByValue());
        for (int i = 0; i < numberOfIcons; i++) {
            int index = itemList.size() - i - 1;
            if (index < 0) {
                setSlot(i + startingIndex, new GuiElementBuilder()
                        .setItem(Items.BARRIER)
                        .setName(Text.of("Donate more items!"))
                        .setLore(List.of(Text.of("Your most donated items are shown here."))));
                continue;
            }
            Map.Entry<Item, Long> entry = itemList.get(index); // Get the ith most donated item
            Item item = entry.getKey();
            Text name = item.getName().copy().append(" donated: " + entry.getValue());
            setSlot(i + startingIndex, new GuiElementBuilder()
                    .setItem(item)
                    .setName(name)
            );
        }
    }

}
