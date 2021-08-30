package me.noobedidoob.minigames.lasertag.commands;

import me.noobedidoob.minigames.Commands;
import me.noobedidoob.minigames.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import me.noobedidoob.minigames.lasertag.methods.Mod;
import me.noobedidoob.minigames.lasertag.session.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModifierCommands implements CommandExecutor, TabCompleter {

    String commands = "\n§7————————— §bModifier Commands§7 ——————————\n"
            + "§6 get §7— Get current values\n"
            + "§6 getTypes §7— Get mod value types\n"
            + "§6 set §7<§6mod§7> <§6value§7> — Set value\n  "
            + "§6 reset §7<§6mod§7> — Reset value\n  "
            + "§6 resetAll — Reset all values\n  "
            + "\n§a Use §6/mods §7<§6command§7> §ato perform a command!\n"
            + "§7——————————————————————————————\n  ";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if(args.length == 0){
            sender.sendMessage(commands);
            return true;
        } else {
            if(args[0].equalsIgnoreCase("get")){
                sendModifiers(sender);
                return true;
            } else if(args[0].equalsIgnoreCase("getTypes") | args[0].equalsIgnoreCase("types")) {
                if(sender instanceof Player){
                    sender.sendMessage("\n§7———————§b§lModifier Types§r§7———————");
                    for(Mod m : Mod.values()) {
                        sender.sendMessage("§7"+m.name()+" <§a"+m.getValueType().getName()+"§7>");
                    }
                    sender.sendMessage("§7———————————————————\n");
                } else {
                    sender.sendMessage("\n§7------------§b§lModifier Types§r§7------------");
                    for(Mod m : Mod.values()) {
                        sender.sendMessage("§7"+m.name()+" <§a"+m.getValueType().getName()+"§7>");
                    }
                    sender.sendMessage("§7--------------------------------------\n");
                }
                return true;
            }

            if((sender instanceof Player) && Session.getPlayerSession((Player) sender) != null) {
                Player p = (Player) sender;
                Session session = Session.getPlayerSession(p);
                if(!session.isAdmin(p)){
                    Session.sendMessage(p,"§cYou have to be an Admin");
                    return true;
                }

                if(args[0].equalsIgnoreCase("set") && args.length == 3) {
                    Mod m = Mod.getMod(StringUtils.replace(args[1].toUpperCase(), "-", "_"));
                    String valString = args[2];
                    //noinspection UnusedAssignment
                    Object value = valString;

                    try {
                        value = Integer.parseInt(valString);
                    } catch (NumberFormatException e1) {
                        try {
                            value = Double.parseDouble(valString+"d");
                        } catch (NumberFormatException e2) {
                            if(valString.equalsIgnoreCase("true") | valString.equalsIgnoreCase("false")) value = Boolean.parseBoolean(valString);
                            else {
                                Session.sendMessage(p, "§cThe given value is invalid! Please use a §evalid number §cor §etrue§c/§efalse!");
                                return true;
                            }
                        }
                    }
                    if(m != null) {
                        if(value.getClass() == m.getOg().getClass()) {
                            session.setMod(m, value);
    //						Session.sendMessage(p, "§aSuccessfully set the value of the modifier §b"+m.name().toLowerCase()+" §a to §e"+value.toString());
                            return true;
                        } else if(value.getClass() == Integer.class && m.getOg().getClass() == Double.class) {
                            value = Double.parseDouble(valString+"d");
                            session.setMod(m, value);
    //						Session.sendMessage(p, "§aSuccessfully set the value of the modifier §b"+m.name().toLowerCase()+" §a to §e"+value.toString());
                            return true;
                        } else {
                            Session.sendMessage(p, "§cThe given type of value doesnt match with the modifiers value type! Please use §e"+m.getValueType().getName());
                            return true;
                        }
                    } else {
                        Session.sendMessage(p, "§cThe modifier §b"+args[1]+" §cdoesn't exist! Use §e/lt setmodifier §cto get all available modifiers");
                        return true;
                    }
                } else if(args[0].equalsIgnoreCase("reset") && args.length == 2){
                    Mod m = Mod.getMod(StringUtils.replace(args[1].toUpperCase(), "-", "_"));
                    session.resetMod(m);
                    return true;
                } else if(args[0].equalsIgnoreCase("resetAll") && args.length == 1){
                    for(Mod m : Mod.values()){
                        session.resetMod(m);
                    }
                }
            }

//            Mod mod = Mod.getMod(StringUtils.replace(args[0].toUpperCase(), "-", "_"));
//            if(mod != null){
//                if(args.length == 1){
//                    if(!(sender instanceof Player)){
//                        sender.sendMessage("[Lasertag] "+mod.get);
//                    }
//                }
//            }
        }


        sender.sendMessage("§cSyntax ERROR! Please use §e/mods §cto see all commands and their arguments");
        return true;
    }

    public void sendModifiers(CommandSender sender){
        if(sender instanceof Player){
            if(Session.getPlayerSession((Player) sender) != null){
                sender.sendMessage("\n§7—————————§b§lModifiers§r§7—————————");
                Session s = Session.getPlayerSession((Player) sender);
                for (Mod m : Mod.values()) {
                    sender.sendMessage("§7> " + m.getCommand() + ": §a" + s.getModValue(m).toString());
                    if(m.spaceAfter)sender.sendMessage(" ");
                }
                sender.sendMessage("§7———————————————————————————\n");
            } else {
                sender.sendMessage("\n§7———————§b§lStanderd Modifiers§r§7———————");
                for (Mod m : Mod.values()) {
                    sender.sendMessage("§7> " + m.getCommand()+ ": §a" + m.getOg().toString());
                    if(m.spaceAfter)sender.sendMessage("§7|");
                }
                sender.sendMessage("§7————————————————————————\n");
            }
        } else {
            sender.sendMessage("\n§7-----------§b§lStanderd Modifiers§r§7-----------");
            for (Mod m : Mod.values()) {
                sender.sendMessage("§7> " + m.getCommand()+ ": §a" + m.getOg().toString());
                if(m.spaceAfter)sender.sendMessage("§7|");
            }
            sender.sendMessage("§7----------------------------------------\n");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 1) {
           list.add("get");
           list.add("getTypes");
            if(sender instanceof Player && Session.getPlayerSession((Player) sender) != null && Session.getPlayerSession((Player) sender).waiting() && Session.getPlayerSession((Player) sender).isAdmin((Player) sender)) {
               list.add("set");
            }
        } else if(sender instanceof Player && args.length >= 2 && args[0].equalsIgnoreCase("set") && Session.getPlayerSession((Player) sender).isAdmin((Player) sender)) {
            if(args.length == 2) {
                for(Mod m : Mod.values()) {
                    if(m.getCommand().toLowerCase().contains(args[1].toLowerCase())) list.add(m.getCommand());
                }
            } else if(args.length == 3 && Mod.getMod(StringUtils.replace(args[1].toUpperCase(), "-", "_")) != null) {
                if(Mod.getMod(StringUtils.replace(args[1].toUpperCase(), "-", "_")).getValueType().getName().equals("true/false")) {
                   list.add("true");
                   list.add("false");
                }
            }
        }

        if(args.length == 3) {
            Mod m = Mod.getMod(args[2]);
            if (m != null) {
                if (m.getValueType() == Utils.ValueType.BOOLEAN) {
                    if (sender.isOp()) {
                       list.add("true");
                       list.add("false");
                    }
                }
            }
        }
        return Commands.filterTabAutocompleteList(args,list);
    }
}