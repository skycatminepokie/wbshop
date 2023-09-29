package com.skycat.wbshop.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

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
        var players = literal("players")
                .build(); // TODO, also should be arg
        var econAdd = literal("add")
                .build(); // TODO
        var playersPoints = literal("players")
                .build(); // TODO, also should be arg
        var points = literal("points")
                .build(); // TODO, also should be arg
        var econRemove = literal("remove")
                .build(); // TODO
        var econTotal = literal("total")
                .build(); // TODO
        var bal = literal("bal")
                .build(); // TODO
        var donate = literal("donate")
                .build(); // TODO
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
}
