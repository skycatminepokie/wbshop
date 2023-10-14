package com.skycat.wbshop.gui;

import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.util.Utils;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonateGui extends SimpleGui { // Slight problem: When force closed (portal, maybe death) items are taken but not donated
    public GuiElement currentBalanceIcon;
    public GuiElement donationWorthIcon;
    public GuiElement itemsDonatedIcon;
    public GuiElement autoDonateIcon;
    private final SimpleInventory donationInventory;

    /**
     * Constructs a new donation container gui for the supplied player.
     *
     * @param player                The player to send this gui to
     */
    public DonateGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        setLockPlayerInventory(false);
        setTitle(Text.of("Donate"));
        currentBalanceIcon = makeCurrentBalanceIcon(player);
        donationWorthIcon = new GuiElement(new ItemStack(Items.PAPER, 1), GuiElementInterface.EMPTY_CALLBACK); // TODO
        itemsDonatedIcon = new GuiElementBuilder(Items.CHEST)
                .setName(Text.of("Items donated: "+ WBShop.getEconomy().getOrCreateAccount(player).getTotalItemsDonated()))
                .build();
        autoDonateIcon = new GuiElementBuilder(Items.BARRIER)
                .setName(Text.of("Coming eventually!"))
                .build();

        setSlot(getFirstEmptySlot(), currentBalanceIcon);
        // setSlot(getFirstEmptySlot(), donationWorthIcon);
        setSlot(getFirstEmptySlot(), itemsDonatedIcon);
        setupTopDonationIcons(getFirstEmptySlot(), 6);
        setSlot(8, autoDonateIcon);
        donationInventory = new SimpleInventory(45); // 9 slots x 5 rows
        int donationSlotNum = 0;
        int emptySlot = getFirstEmptySlot();
        while (emptySlot != -1) {
            setSlotRedirect(getFirstEmptySlot(), new Slot(donationInventory, donationSlotNum, donationSlotNum % 9, donationSlotNum / 9));
            donationSlotNum++;
            emptySlot = getFirstEmptySlot();
        }
    }

    private void setupTopDonationIcons(int startingIndex, int numberOfIcons) { // TODO: Optimize sort
        if (startingIndex + numberOfIcons > this.getSize()) {
            throw new IllegalArgumentException("startingIndex + numberOfIcons may not exceed number of slots");
        }
        HashMap<Item, Long> donatedItems = WBShop.getEconomy().getOrCreateAccount(player).getDonatedItemCounts();
        ArrayList<Map.Entry<Item, Long>> itemList = new ArrayList<>(donatedItems.entrySet());
        itemList.sort(Map.Entry.comparingByValue());
        for (int i = 0; i < numberOfIcons; i++) {
            int index = itemList.size() - i - 1;
            if (index < 0) {
                setSlot(getFirstEmptySlot(), new GuiElementBuilder()
                        .setItem(Items.BARRIER)
                        .setName(Text.of("Donate more items!"))
                        .setLore(List.of(Text.of("Your most donated items are shown here.")))
                        .build());
                continue;
            }
            Map.Entry<Item, Long> entry = itemList.get(index); // Get the ith most donated item
            Item item = entry.getKey();
            Text name = item.getName().copy().append(" donated: " + entry.getValue());
            setSlot(getFirstEmptySlot(), new GuiElementBuilder()
                    .setItem(item)
                    .setName(name)
                    .build()
            );
        }
    }

    public GuiElement makeCurrentBalanceIcon(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        GuiElementBuilder builder = new GuiElementBuilder(Items.PLAYER_HEAD);
        if (server != null) {
            builder.setSkullOwner(player.getGameProfile(), server);
        }
        if (WBShop.getEconomy() != null) {
            long balance = WBShop.getEconomy().getOrCreateAccount(player).balance();
            builder.setName(Text.of("You have " + balance + " points."));
        } else {
            builder.setName(Text.of("Error loading points."));
            Utils.log("Couldn't create points stack for player " + player.getUuid() + ": Economy was null");
        }
        return builder.build();
    }

    @Override
    public void onClose() {
        super.onClose();
        WBShop.getEconomy().getOrCreateAccount(player).donateItems(donationInventory.stacks);
    }
}
