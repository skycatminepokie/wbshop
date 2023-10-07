package com.skycat.wbshop.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AdminGui extends SimpleGui {

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     */
    public AdminGui(ServerPlayerEntity player, boolean admin) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        setLockPlayerInventory(true);
        setTitle(Text.of("Worldborder Shop"));

    }
}
