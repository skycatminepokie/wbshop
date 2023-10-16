package com.skycat.wbshop.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

// TODO: Display all donated items
public class AccountGui extends SimpleGui { // TODO
    public final ServerPlayerEntity player;
    public final boolean adminMode;
    public final UUID accountHolder;
    private GuiElement prevPage;
    private GuiElement withdrawOrRemove; // Withdraw if own, remove if admin mode, blank if other
    private GuiElement accountIcon;
    private GuiElement donateOrAdd;
    private GuiElement nextPage;
    private GuiElement[] donatedItemIcons;
    private int page;


    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player The player to serve this gui to
     * @param adminMode  If the gui is to be opened in admin mode
     * @param accountHolder  The account to look at.
     * @param page The page of donated item icons to open to. Zero-indexed.
     */
    public AccountGui(ServerPlayerEntity player, boolean adminMode, UUID accountHolder, int page) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.player = player;
        this.adminMode = adminMode;
        this.accountHolder = accountHolder;
        setLockPlayerInventory(true);
        this.page = page;
    }

    /**
     * Constructs an account GUI for the supplied player.
     * @param player The player who is opening their account
     * @param adminMode  If the gui is to be opened in admin mode
     */
    public AccountGui(ServerPlayerEntity player, boolean adminMode) {
        this(player, adminMode, player.getUuid(), 0);
    }

}
