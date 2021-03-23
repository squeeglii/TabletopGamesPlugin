package me.cg360.games.tabletop.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import me.cg360.games.tabletop.TabletopGamesNukkit;
import me.cg360.games.tabletop.Util;
import me.cg360.games.tabletop.ngapimicro.MicroGameProfile;
import me.cg360.games.tabletop.ngapimicro.MicroGameRegistry;
import me.cg360.games.tabletop.ngapimicro.keychain.GamePropertyKeys;
import net.cg360.nsapi.commons.id.Identifier;

import java.util.Optional;

public class CommandTableGame extends PluginCommand<TabletopGamesNukkit> {

    public CommandTableGame() {
        super("tablegame", TabletopGamesNukkit.get());
        this.setDescription("Provides tools to manage the tabletop game registered in the Micro-Game registry.");
        this.setUsage("/game start <String: Micro-Game ID>\nOR /game list");

        this.commandParameters.clear();
        this.commandParameters.put("start", new CommandParameter[]{
                CommandParameter.newEnum("start", new CommandEnum("StartGame","start")),
                CommandParameter.newType("game_id", CommandParamType.STRING)
        });
        this.commandParameters.put("list", new CommandParameter[]{
                CommandParameter.newEnum("list", new CommandEnum("ListGames","list"))
        });
        this.commandParameters.put("detail", new CommandParameter[]{
                CommandParameter.newEnum("detail", new CommandEnum("DetailGame","detail")),
                CommandParameter.newType("game_id", CommandParamType.STRING)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        if(args.length < 1) {
            sender.sendMessage(Util.eMessage("This command requires at least 1 parameter. (sub-command)"));
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "start":
                if (!Util.permissionCheck(sender, Util.COMMAND_PERMISSION+".tablegame.start")) return true;

                break;



            case "list":
                if (!Util.permissionCheck(sender, Util.COMMAND_PERMISSION+".tablegame.list")) return true;

                sender.sendMessage(Util.fMessage("MICRO", TextFormat.DARK_AQUA, "The currently registered games are:"));

                for (MicroGameProfile<?> profile: MicroGameRegistry.get().getGameProfiles()) {
                    sender.sendMessage(String.format("%s - %s%s", TextFormat.DARK_GRAY, TextFormat.AQUA, profile.getIdentifier().getID()));
                } // Maybe this should be async? oh well.
                break;



            case "detail":
                if (!Util.permissionCheck(sender, Util.COMMAND_PERMISSION+".tablegame.detail")) return true;

                if(args.length < 2) {
                    sender.sendMessage(Util.eMessage("This sub-command requires 2 parameters. (Game ID)"));
                    return true;
                }
                Optional<MicroGameProfile<?>> p = MicroGameRegistry.get().getProfile(new Identifier(args[1]));

                if(p.isPresent()) {
                    MicroGameProfile<?> profile = p.get();
                    sender.sendMessage(Util.fMessage("MICRO", TextFormat.DARK_AQUA, String.format("Listing details for type %s'%s'", TextFormat.AQUA, args[1].toLowerCase())));

                    String name = profile.getProperties().getOrElse(GamePropertyKeys.DISPLAY_NAME, "???");
                    String description = profile.getProperties().getOrElse(GamePropertyKeys.DESCRIPTION, "???");
                    String authors = profile.getProperties().getOrElse(GamePropertyKeys.AUTHORS, "???");

                    sender.sendMessage(String.format("%s%s - Name: %s%s%s", TextFormat.GRAY, TextFormat.BOLD, TextFormat.RESET, TextFormat.AQUA, name));
                    sender.sendMessage(String.format("%s%s - Description: %s%s%s", TextFormat.GRAY, TextFormat.BOLD, TextFormat.RESET, TextFormat.AQUA, description));
                    sender.sendMessage(String.format("%s%s - Authors: %s%s%s", TextFormat.GRAY, TextFormat.BOLD, TextFormat.RESET, TextFormat.AQUA, authors));

                } else {
                    sender.sendMessage(Util.eMessage("This specified micro-game type does not exist."));
                }

                break;



            default:
                sender.sendMessage(Util.eMessage("Invalid command! (sub-command)"));
                sender.sendMessage(TextFormat.RED + getUsage());
                break;
        }

        return true;
    }
}
