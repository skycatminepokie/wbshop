package com.skycat.wbshop.gui;

import com.mojang.authlib.GameProfile;
import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.econ.Account;
import com.skycat.wbshop.econ.Economy;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.UserCache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class OverviewGui extends LayeredGui {
    private final ServerPlayerEntity player;
    GuiElement adminButton; // TODO
    GuiElement myAccountIcon;
    GuiElement borderInformationIcon;
    GuiElement prevButton; // TODO
    GuiElement nextButton; // TODO
    List<Account> playerList;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player the player to server this gui to
     */
    public OverviewGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.player = player;
        MinecraftServer server = player.getServer();
        Economy econ = WBShop.getEconomy(player);
        Account account = econ.getOrCreateAccount(player);
        addLayer(initMenuBarLayer(server, account, econ), 0, 0);
        ListLayer topPlayerLayer = initTopPlayerLayer(server, econ);
        addLayer(topPlayerLayer, 1, 2);
        prevButton = topPlayerLayer.prevButtonBuilder().build();
        setSlot(27, prevButton);
        nextButton = topPlayerLayer.nextButtonBuilder().build();
        setSlot(35, nextButton);
    }

    private ListLayer initTopPlayerLayer(MinecraftServer server, Economy econ) {
        playerList = econ.getAccountList();
        playerList.sort(Comparator.comparingLong(Account::balance).reversed());

        ArrayList<GuiElement> playerIconList = new ArrayList<>(playerList.size());
        for (Account account : playerList) {
            playerIconList.add(createTopPlayerIcon(account, server));
        }
        return new ListLayer(3, 7, playerIconList);
    }

    private Layer initMenuBarLayer(MinecraftServer server, Account account, Economy econ) {
        Layer menuBar = new Layer(1, 9);


        myAccountIcon = new GuiElementBuilder(Items.PLAYER_HEAD)
                // TODO .setCallback(this::onClickMyAccountButton)
                .setSkullOwner(player.getGameProfile(), server)
                .setName(player.getName().copy().setStyle(Style.EMPTY.withColor(Formatting.AQUA)).append("'s account"))
                .addLoreLine(Text.of("Balance: " + account.balance()))
                .addLoreLine(Text.of("Total items donated: " + account.getTotalItemsDonated()))
                .build();
        menuBar.setSlot(4, myAccountIcon);

        borderInformationIcon = new GuiElementBuilder(Items.FILLED_MAP)
                .setName(Text.of("Border size: " + ((int) player.method_48926().getWorldBorder().getSize())))
                .addLoreLine(Text.of("World's total points: " + econ.getTotalPoints()))
                // TODO: Add algorithm to lore
                .build();
        menuBar.setSlot(8, borderInformationIcon);
        return menuBar;
    }

    /**
     * Creates an icon showing a player and their account balance.
     *
     * @param account The account of the player to create an icon for
     * @return A new icon.
     */
    public GuiElement createTopPlayerIcon(Account account, MinecraftServer server) {
        GameProfile profile;
        UserCache userCache = server.getUserCache();
        if (userCache != null) {
            Optional<GameProfile> optProfile = userCache.getByUuid(account.owner());
            profile = optProfile.orElse(new GameProfile(account.owner(), null));
        } else {
            profile = new GameProfile(account.owner(), null); // This is repeating code but it looks best to me
        }

        return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setSkullOwner(profile, server)
                .setName(Text.of(profile.getName()))
                .addLoreLine(Text.of("Points: " + account.balance()))
                .build();
    }

    public void onClickMyAccountButton(int index, ClickType clickType, SlotActionType actionType, SlotGuiInterface gui) {
        gui.close();
        new AccountGui(player, false).open(); // TODO: open in admin mode if right click and has perms
    }
}
