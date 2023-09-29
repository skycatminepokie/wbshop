package com.skycat.wbshop.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DonateGui extends SimpleGui {
    /**
     * Constructs a new donation container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     */
    public DonateGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        setLockPlayerInventory(false);
        setTitle(Text.of("Donate"));
        setSlot(0, new CurrentPointsIcon(player));
    }

}
