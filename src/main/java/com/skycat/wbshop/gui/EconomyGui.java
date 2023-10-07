package com.skycat.wbshop.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class EconomyGui extends SimpleGui { // TODO
    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     */
    public EconomyGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        setLockPlayerInventory(true);
    }
}
