package com.skycat.wbshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.econ.Account;
import com.skycat.wbshop.gui.DonateGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

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
                .requires(Permissions.require("wbshop.econ", 4))
                .build(); // TODO
        var econGet = literal("get")
                .build(); // TODO
        var econGetPlayers = argument("players", EntityArgumentType.players())
                .executes(this::econGet)
                .build(); // TODO
        var econAdd = literal("add")
                .build(); // TODO
        var econAddPlayers = argument("players", EntityArgumentType.players())
                .build(); // TODO
        var econAddPlayersPoints = argument("points", LongArgumentType.longArg(1))
                .executes(this::econAdd)
                .build();
        var econRemove = literal("remove")
                .build(); // TODO
        var econRemovePlayers = argument("players", EntityArgumentType.players())
                .build(); // TODO
        var econRemovePlayersPoints = argument("points", LongArgumentType.longArg(1))
                .executes(this::econRemove)
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
        var withdrawPoints = argument("points", LongArgumentType.longArg(1))
                .executes(this::withdraw)
                .build();
        var withdrawAll = literal("all")
                .executes(this::withdrawAll)
                .build();
        var pay = literal("pay")
                .build(); // TODO

        // Building tree
        root.addChild(wbshop);
            wbshop.addChild(econ);
                econ.addChild(econGet);
                    econGet.addChild(econGetPlayers);
                econ.addChild(econAdd);
                    econAdd.addChild(econAddPlayers);
                        econAddPlayers.addChild(econAddPlayersPoints);
                econ.addChild(econRemove);
                    econRemove.addChild(econRemovePlayers);
                        econRemovePlayers.addChild(econRemovePlayersPoints);
                econ.addChild(econTotal);
        root.addChild(bal);
        root.addChild(donate);
        root.addChild(withdraw);
            withdraw.addChild(withdrawPoints);
            withdraw.addChild(withdrawAll);

    }

    private int econGet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        for (ServerPlayerEntity player : players) {
            long bal = WBShop.getEconomy().getOrCreateAccount(player).balance();
            context.getSource().sendFeedback(() -> player.getName().copy().append(" has " + bal + " points."), false);
        }
        return players.size();

    }

    private int econRemove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        long points = LongArgumentType.getLong(context, "points");
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        for (ServerPlayerEntity player : players) {
            WBShop.getEconomy().getOrCreateAccount(player).removeBalance(points);
            context.getSource().sendFeedback(() -> Text.literal("Removed " + points + " points from ").append(player.getName()), false);
        }
        return players.size();
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

    private int withdraw(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.of("This command must be executed by a player!"));
            return -1;
        }
        Account account = WBShop.getEconomy().getOrCreateAccount(player);

        long points;
        try {
            points = LongArgumentType.getLong(context, "points");
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(Text.of("You have to specify how many points you want to withdraw."));
            return -2;
        }

        if (account.withdraw(points, player)) { // Intended side effect: Voucher is given to player if they have enough points.
                return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.of("You don't have enough points!"));
            return 0;
        }
    }

    private int withdrawAll(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.of("This command must be executed by a player!"));
            return -1;
        }
        Account account = WBShop.getEconomy().getOrCreateAccount(player);

        return account.withdraw(account.balance(), player) ? Command.SINGLE_SUCCESS : (0 /* Should not happen unless there's some weird desync */);
    }

    private int econAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        long points = LongArgumentType.getLong(context, "points");
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        for (ServerPlayerEntity player : players) {
            WBShop.getEconomy().getOrCreateAccount(player).addBalance(points);
            context.getSource().sendFeedback(() -> Text.literal("Gave " + points + " points to ").append(player.getName()), false);
        }
        return players.size();
    }


}
