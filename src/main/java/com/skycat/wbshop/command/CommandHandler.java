package com.skycat.wbshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.gui.DonateGui;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class CommandHandler implements CommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        // Sorted code in a depth-first thing for the tree
        var root = dispatcher.getRoot();
        var wbshop = literal("wbshop")
                .build(); // TODO
        var econ = literal("econ")
                .build(); // TODO
        var econGet = literal("get")
                .build(); // TODO
        var players = argument("players", EntityArgumentType.players())
                .build();
        var econAdd = literal("add")
                .build(); // TODO
        var playersPoints = argument("players", EntityArgumentType.players())
                .build();
        var points = argument("points", LongArgumentType.longArg())
                .build();
        var econRemove = literal("remove")
                .build(); // TODO
        var econTotal = literal("total")
                .build(); // TODO
        var bal = literal("bal")
                .executes(this::bal)
                .build();
        var donate = literal("donate")
                .executes(this::donate)
                .build();
        var withdraw = literal("withdraw")
                .build(); // TODO
        var withdrawAll = literal("all")
                .build(); // TODO

        // Linking, depth-first
        root.addChild(wbshop);
            wbshop.addChild(econ);
                econ.addChild(econGet);
                    econGet.addChild(players);
                econ.addChild(econAdd);
                    econAdd.addChild(playersPoints);
                        playersPoints.addChild(points);
                econ.addChild(econRemove);
                    econRemove.addChild(playersPoints);
                econ.addChild(econTotal);
            // TODO: Admin commands
        root.addChild(bal);
        root.addChild(donate);
        root.addChild(withdraw);
            withdraw.addChild(points);
            withdraw.addChild(withdrawAll);

    }

    private int donate(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getEntity() instanceof ServerPlayerEntity player) {
            new DonateGui(player).open();
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendError(Text.of("This command must be executed by a player!"));
        return 0;
    }

    private int bal(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getEntity() instanceof ServerPlayerEntity player) {
            context.getSource().sendFeedback(()-> Text.of("You have " + WBShop.getEconomy().getOrCreateAccount(player).balance() + " points."), false);
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendError(Text.of("This command must be run by a player."));
        return 0;
    }


}
