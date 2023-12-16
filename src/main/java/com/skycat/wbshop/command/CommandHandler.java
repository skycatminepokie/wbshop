package com.skycat.wbshop.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.skycat.wbshop.WBShop;
import com.skycat.wbshop.econ.Account;
import com.skycat.wbshop.econ.Economy;
import com.skycat.wbshop.gui.DonateGui;
import com.skycat.wbshop.gui.OverviewGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.GameProfileArgumentType;
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
                .executes(this::wbshop)
                .build();
        var econ = literal("econ")
                .requires(Permissions.require("wbshop.econ", 4))
                .build();
        var econGet = literal("get")
                .build();
        var econGetPlayers = argument("players", GameProfileArgumentType.gameProfile())
                .executes(this::econGet)
                .build();
        var econAdd = literal("add")
                .build();
        var econAddPlayers = argument("players", GameProfileArgumentType.gameProfile())
                .build();
        var econAddPlayersPoints = argument("points", LongArgumentType.longArg(1))
                .executes(this::econAdd)
                .build();
        var econRemove = literal("remove")
                .build();
        var econRemovePlayers = argument("players", GameProfileArgumentType.gameProfile())
                .build();
        var econRemovePlayersPoints = argument("points", LongArgumentType.longArg(1))
                .executes(this::econRemove)
                .build();
        var econTotal = literal("total")
                .executes(this::econTotal)
                .build();
        var econBorderFunction = literal("borderFunction")
                .build();
        var econBorderFunctionFunction = argument("function", StringArgumentType.string())
                .executes(this::setBorderFunction)
                .build();
        var bal = literal("bal")
                .executes(this::bal)
                .build();
        var donate = literal("donate")
                .executes(this::donate)
                .build();
        var withdraw = literal("withdraw")
                .build();
        var withdrawPoints = argument("points", LongArgumentType.longArg(1))
                .executes(this::withdraw)
                .build();
        var withdrawAll = literal("all")
                .executes(this::withdrawAll)
                .build();
        var pay = literal("pay")
                .build();

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
                econ.addChild(econBorderFunction);
                    econBorderFunction.addChild(econBorderFunctionFunction);
        root.addChild(bal);
        root.addChild(donate);
        root.addChild(withdraw);
            withdraw.addChild(withdrawPoints);
            withdraw.addChild(withdrawAll);

    }

    private int econTotal(CommandContext<ServerCommandSource> context) {
        long total = WBShop.getEconomy(context.getSource().getServer()).getTotalPoints();
        context.getSource().sendFeedback(()-> Text.of("There are a total of " + total + " points in player's accounts."), false);
        return Command.SINGLE_SUCCESS;
    }

    private int setBorderFunction(CommandContext<ServerCommandSource> context) {
        String functionString = StringArgumentType.getString(context, "function");
        if (WBShop.getEconomy(context.getSource().getServer()).setBorderFunction(functionString)) {
            context.getSource().sendFeedback(() -> Text.of("Successfully updated border function!"), true);
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendError(Text.of("Failed to update border function."));
        return -1;
    }

    private int wbshop(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getEntity() instanceof ServerPlayerEntity player) {
            new OverviewGui(player).open();
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendError(Text.of("This command must be executed by a player!"));
        return 0;
    }

    private int econGet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<GameProfile> players = GameProfileArgumentType.getProfileArgument(context, "players");
        Economy economy = WBShop.getEconomy(context.getSource().getServer());
        for (GameProfile player : players) {
            long bal = economy.getOrCreateAccount(player).balance();
            context.getSource().sendFeedback(() -> Text.of(player.getName() + " has " + bal + " points."), false);
        }
        return players.size();

    }

    private int econRemove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        long points = LongArgumentType.getLong(context, "points");
        Collection<GameProfile> players = GameProfileArgumentType.getProfileArgument(context, "players");
        Economy economy = WBShop.getEconomy(context.getSource().getServer());
        for (GameProfile player : players) {
            economy.getOrCreateAccount(player).removeBalance(points);
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
            context.getSource().sendFeedback(()-> Text.of("You have " + WBShop.getEconomy(player).getOrCreateAccount(player).balance() + " points."), false);
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
        Account account = WBShop.getEconomy(player).getOrCreateAccount(player);

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
        Account account = WBShop.getEconomy(player).getOrCreateAccount(player);

        return account.withdraw(account.balance(), player) ? Command.SINGLE_SUCCESS : (0 /* Should not happen unless there's some weird desync */);
    }

    private int econAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        long points = LongArgumentType.getLong(context, "points");
        Collection<GameProfile> players = GameProfileArgumentType.getProfileArgument(context, "players");
        Economy economy = WBShop.getEconomy(context.getSource().getServer());
        for (GameProfile player : players) {
            economy.getOrCreateAccount(player).addBalance(points);
            context.getSource().sendFeedback(() -> Text.literal("Gave " + points + " points to ").append(player.getName()), false);
        }
        return players.size();
    }


}
