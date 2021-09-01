package me.noobedidoob.minigames.lasertag.commands;

import java.util.ArrayList;
import java.util.List;

import me.noobedidoob.minigames.Commands;
import me.noobedidoob.minigames.utils.Map;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.lasertag.methods.Inventories;
import me.noobedidoob.minigames.lasertag.session.SessionTeam;
import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.utils.Utils.TimeFormat;

public class SessionCommands implements CommandExecutor, TabCompleter{
	
	
	private final Minigames minigames;
	public SessionCommands(Minigames minigames) {
		this.minigames = minigames;
	}

	String commands = "\n§7———————— §bSession Commands§7 —————————\n"
			+ "§6 new §7— Create new session\n"
			+ "§6 new §7<§6solo§7 | §6teams§7> — Create new session\n"
			+ "§6 join §7<§6owner§7> — Join a players session\n"
			+ "§6 start §7— Start session\n"
			+ "§6 invite §7<§6player§7> §7— Invite player to session\n"
			+ "§6 addAdmin §7— Promote player to admin\n"
			+ "§6 removeAdmin §7— Demote player from admin\n"
			+ "§6 setSolo §7— Change to solo\n"
			+ "§6 setTeams §7— Change to teams\n"
			+ "§6 setTeamsAmount §7— Set amount of teams\n"
			+ "§6 setWithMultiWeapons §7<§6true§7|§6false§7> — Set amount of teams\n"
			+ "§6 setWithCaptureTheFlag §7<§6true§7|§6false§7> — Set amount of teams\n"
			+ "§6 stop §7— Stop session\n"
			+ "§6 close §7— Close session\n"
			+ "§6 setTime §7<§6time§7> <§6format§7> §7— Start session\n"
			+ "§6 kick §7<§6player§7> — Kick player\n"
			+ "§6 leave §7— Leave session\n  "
			+ "\n§a Use §6/session §7<§6command§7> §ato perform a command!\n"
			+ "§7————————————————————————————\n  ";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(args.length == 0){
			sender.sendMessage(commands);
			return true;
		}

		if(sender instanceof Player){
			Player p = (Player) sender;
			Session s = Session.getPlayerSession(p);
			if (args.length == 1) {
				if(s == null) {
					if (args[0].equalsIgnoreCase("new")) {
						Inventories.openNewSessionInv(p);
						return true;
					}
					if (args[0].equalsIgnoreCase("quickNew")) {
						Session.sendMessage(p, "§aRegistered new Session!");
						Session session = new Session(minigames, p, 0, false);
						session.setTime(5, TimeFormat.MINUTES, false);
						session.setMap(Map.MAPS.get(0));
						return true;
					}
				} else {
					if (args[0].equalsIgnoreCase("start")) {
						if (s.isAdmin(p)) {
							if(s.getPlayers().length > 0) {
								boolean enoughTeams = true;
								if(s.isTeams()) {
									int teamsWithPlayers = 0;
									for(SessionTeam team : s.getTeams()) {
										if(team.getPlayers().length > 0) teamsWithPlayers++;
									}
									if(teamsWithPlayers < 2) enoughTeams = false;
								}
								if(enoughTeams) s.start(false);
								else Session.sendMessage(p, "§cThere must be at least 2 teams with at least 1 player in it!");
							} else Session.sendMessage(p, "§cNot enough players!");
						} else Session.sendMessage(p, "§aYou need to be an admin of this session to perform this command!");
						return true;
					} 
					
					else if (args[0].equalsIgnoreCase("stop")) {
						if (s.isAdmin(p)) {
							s.stop(true, false);
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
						return true;
					} 
					
					else if(args[0].equalsIgnoreCase("close")) {
						if(s.getOwner() == p) {
							if(s.tagging()) s.stop(true, true);
							else s.close();
						} else Session.sendMessage(p, "§cYou have to be the owner of this session to perform this command");
						return true;
					}
					
					else if(args[0].equalsIgnoreCase("leave")) {
						s.removePlayer(p);
						return true;
					} 
					
					else if(args[0].equalsIgnoreCase("setTime")) {
						if(s.isAdmin(p)) {
							Inventories.openTimeInv(p);
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
						return true;
					} 
					
					else if(args[0].equalsIgnoreCase("addAdmin") | args[0].equalsIgnoreCase("setAdmin")) {
						if(s.isAdmin(p)) {
							if (!s.tagging()) {
								Inventories.openInviteInv(p);
							} else Session.sendMessage(p, "§cYou can't promote players while the game is running!");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
						return true;
					}
					
					else if(args[0].equalsIgnoreCase("setSolo")) {
						if (s.isAdmin(p)) {
							if (s.waiting()) {
								s.setTeamsAmount(0);
							} else Session.sendMessage(p, "§cYou can't perform this command in a running round!");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
						return true;
					}
					else if(args[0].equalsIgnoreCase("setTeams")) {
						if (s.isAdmin(p)) {
							if (s.waiting()) {
								s.setTeamsAmount(2);
							} else Session.sendMessage(p, "§cYou can't perform this command in a running round!");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
						return true;
					}
					else if(args[0].equalsIgnoreCase("setTeamAmount") | args[0].equalsIgnoreCase("setTeamsAmount")) {
						if (s.isAdmin(p)) {
							if (s.waiting()) {
								Inventories.openTeamsInv(p);
							} else Session.sendMessage(p, "§cYou can't perform this command in a running round!");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
						return true;
					}
				}
				
			} else if(args.length == 2) {
				if(args[0].equalsIgnoreCase("new")) {
					if(args[1].equalsIgnoreCase("solo") | args[1].equalsIgnoreCase("teams")) {
						if(Session.isPlayerInSession(p)) {
							Session.sendMessage(p, "§cPlease leave this session first!");
							return true;
						}
						Session.sendMessage(p, "§aRegistered new Session!");
						new Session(minigames, p, (args[1].equalsIgnoreCase("teams") ? 2 : 1), true);
						return true;
					}
				} 
				
				else if(args[0].equalsIgnoreCase("join")) {
					if(s != null) {
						Session.sendMessage(p, "§cPlease leave this session first!");
						return true;
					}
					Session invS = Session.getSessionFromName(args[1]);
					if(invS != null) {
						if(!invS.isPlayerBanned(p)) {
							if (!invS.tagging()) {
								invS.addPlayer(p);
							} else Session.sendMessage(p, "§cThe plasyers are currently in-game. Please wait!");
						} else Session.sendMessage(p, "§cYou are banned from this session!");
					} else Session.sendMessage(p, "§cThis invitation expired!");
					return true;
				} 
				
				else if(args[0].equalsIgnoreCase("kick") | args[0].equalsIgnoreCase("ban")) {
					if(s != null) {
						if (s.isAdmin(p)) {
							Player kp = Bukkit.getPlayer(args[1]);
							if (kp != null) {
								if(s.isInSession(kp)) {
									if(kp != s.getOwner()) s.banPlayer(kp, p);
									else Session.sendMessage(p, "§cYou can't kick the owner!");
								} else Session.sendMessage(p, "§b"+args[1]+" §cis not in this session!");
							} else Session.sendMessage(p, "§cPlayer §b"+args[1]+" §cnot found!");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
					} else Session.sendMessage(p, "§cYou're not in a session!");
					return true;
				} 
				
				else if(args[0].equalsIgnoreCase("addAdmin") | args[0].equalsIgnoreCase("promoteAdmin") | args[0].equalsIgnoreCase("setAdmin")) {
					if(s != null) {
						if(s.isAdmin(p)) {
							Player ap = Bukkit.getPlayer(args[1]);
							if(ap != null) {
								if (s.isInSession(ap)) {
									if (!s.isAdmin(ap)) {
										s.addAdmin(ap);
										Session.sendMessage(p, "§aPromoted §b"+ap.getName()+" §ato Admin!");
									} else Session.sendMessage(p, "§b"+args[1]+" §cis already an admin of this session!");
								} else Session.sendMessage(p, "§b"+args[1]+" §cis not in this session!");
							} else Session.sendMessage(p, "§cPlayer §b"+args[1]+" §cnot found!");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
					} else Session.sendMessage(p, "§cYou're not in a session!");
					return true;
				}
				
				else if(args[0].equalsIgnoreCase("removeAdmin") | args[0].equalsIgnoreCase("demoteAdmin")) {
					if(s != null) {
						if(s.isAdmin(p)) {
							Player dp = Bukkit.getPlayer(args[1]);
							if(dp != null) {
								if (s.isInSession(dp)) {
									if (s.isAdmin(dp)) {
										if(dp != s.getOwner()) {
											s.removeAdmin(dp);
											Session.sendMessage(p, "§aDemoted §b"+dp.getName()+" §afrom Admin!");
										} else Session.sendMessage(p, "§cYou can't do this to the owner!");
									} else Session.sendMessage(p, "§b"+args[1]+" §cis not an admin of this session!");
								} else Session.sendMessage(p, "§b"+args[1]+" §cis not in this session!");
							} else Session.sendMessage(p, "§cPlayer §b"+args[1]+" §cnot found!");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
					} else Session.sendMessage(p, "§cYou're not in a session!");
					return true;
				}
				
				else if(args[0].equalsIgnoreCase("invite")) {
					if(s != null) {
						if(s.isAdmin(p)) {
							Player ip = Bukkit.getPlayer(args[1]);
							if(ip != null) {
								if (!s.isInSession(ip)) {
									if(!s.invitedPlayers.contains(ip)){
										s.sendInvitation(ip);
										Session.sendMessage(p, "§aInvitation sent!");
									} else Session.sendMessage(p, "§cYou have to wait some seconds before you can invite §b"+ip.getName()+" §cagain!");
								} else Session.sendMessage(p, "§b"+args[1]+" §cis already in a session!");
							} else Session.sendMessage(p, "§cPlayer §b"+args[1]+" §cnot found!");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
					} else Session.sendMessage(p, "§cYou're not in a session!");
					return true;
				}
				else if(args[0].equalsIgnoreCase("forceInvite")) {
					if(s != null) {
						if(p.isOp()) {
							if(s.isAdmin(p)) {
								Player ip = Bukkit.getPlayer(args[1]);
								if(ip != null) {
									if (Session.getPlayerSession(ip) == null) {
										s.addPlayer(ip);
									} else Session.sendMessage(p, "§b"+args[1]+" §cis already in a session!");
								} else Session.sendMessage(p, "§cPlayer §b"+args[1]+" §cnot found!");
							} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
						} else Session.sendMessage(p, "§cYou have to be a server operator to perform this command!");
					} else Session.sendMessage(p, "§cYou're not in a session!");
					return true;
				}
				
				else if(args[0].equalsIgnoreCase("setTeams")) {
					if(s != null) {
						if(s.isAdmin(p)) {
							if (!s.tagging()) {
								try {
									int amount = Integer.parseInt(args[1]);
									if(amount > 1) {
										s.setTeamsAmount(amount);
									} else {
										Session.sendMessage(p, "§cYou have to give a valid number (minimum of 2)!");
									}
								} catch (NumberFormatException e) {
									Session.sendMessage(p, "§cYou have to give a valid number!");
								}
							} else Session.sendMessage(p, "§cYou cant change the amount of teams while the game is running");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
					} else Session.sendMessage(p, "§cYou're not in a session!");
					return true;
				}
				
				else if(args[0].equalsIgnoreCase("setTeamAmount") | args[0].equalsIgnoreCase("setTeamsAmount")) {
					if(s != null) {
						if(s.isAdmin(p)) {
							if (s.waiting()) {
								try {
									int amount = Integer.parseInt(args[1]);
									s.setTeamsAmount(amount);
								} catch (NumberFormatException e) {
									Session.sendMessage(p, "§cYou have to give a valid number!");
								}
							} else Session.sendMessage(p, "§cYou can't perform this command in a running game!");
						} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
					} else Session.sendMessage(p, "§cYou're not in a session!");
					return true;
				}

				else if(args[0].equalsIgnoreCase("setWithMultiWeapons")) {
					if(s != null) {
						if (s.isAdmin(p)) {
							if (s.waiting()) {
								s.setWithMultiWeapons(Boolean.parseBoolean(args[1]));
							} else Session.sendMessage(p, "§cYou can't perform this command in a running game!");
						} else Session.sendMessage(p,"§cYou have to be an admin of this session to perform this command!");
					} else Session.sendMessage(p, "§cYou're not in a session!");
					return true;
				}

				else if(args[0].equalsIgnoreCase("setWithCaptureTheFlag")) {
					if(s != null) {
						if (s.isAdmin(p)) {
							if (s.waiting()) {
								s.setWithCaptureTheFlag(Boolean.parseBoolean(args[1]));
							} else Session.sendMessage(p, "§cYou can't perform this command in a running game!");
						} else Session.sendMessage(p,"§cYou have to be an admin of this session to perform this command!");
					} else Session.sendMessage(p, "§cYou're not in a session!");
					return true;
				}
			}
			if(args[0].equalsIgnoreCase("setTime")) {
				if(s != null) {
					if(s.isAdmin(p)) {
						try {
							int time = Integer.parseInt(args[1]);
							TimeFormat format = TimeFormat.MINUTES;
							if(args.length == 3) format = TimeFormat.getFromString(args[2]);
							s.setTime(time, format, true);
						} catch (NumberFormatException e) {
							sender.sendMessage("§cThe given argument §e"+StringUtils.replace(e.getMessage(), "For input string: ","")+" §cis not a Number!");
							return true;
						} catch (Exception e) {
							sender.sendMessage("§cSyntax error: "+e.getMessage());
							return true;
						}
					} else Session.sendMessage(p, "§cYou have to be an admin of this session to perform this command!");
				} else Session.sendMessage(p, "§cYou're not in a session!");
				return true;
			}
		} else sender.sendMessage("You may only perform this command as a player!");
		
		
		
		sender.sendMessage("§cSyntax error! Please use §e/sessions §cto see all commands and their arguments");
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		List<String> list = new ArrayList<>();
		if(!(sender instanceof Player)) return list;

		Player p = (Player) sender;
		Session s = Session.getPlayerSession(p);
		if(args.length == 1) {
			if(s == null) {
				list.add("new");
				list.add("quickNew");
				list.add("join");
			} else {
				if(s.isAdmin(p)) {
					if(!s.tagging()) {
						list.add("start");
						list.add("invite");
						if(p.isOp()) list.add("forceInvite");
						list.add("addAdmin");
						list.add("removeAdmin");
						list.add("setSolo");
						list.add("setTeams");
						list.add("setTeamsAmount");
						list.add("setWithMultiWeapons");
						list.add("setWithCaptureTheFlag");
					}
					list.add("stop");
					list.add("close");
					list.add("setTime");
					list.add("kick");
					list.add("leave");
				}
			}
		} else if(args.length == 2) {
			if (s == null) {
				if (args[0].equalsIgnoreCase("new")) {
					list.add("solo");
					list.add("teams");
				} else if(args[0].equalsIgnoreCase("join")) {
					for(Player op : Bukkit.getOnlinePlayers()) {
						Session ops = Session.getPlayerSession(op);
						if(ops != null) {
							if(!list.contains(ops.getOwner().getName()) && ops.getOwner().getName().toLowerCase().contains(args[1].toLowerCase())) list.add(ops.getOwner().getName());
						}
					}
				}
			} else if(s.isAdmin(p)){
				if((args[0].equalsIgnoreCase("addAdmin") | args[0].equalsIgnoreCase("setAdmin")) && !s.tagging()) {
					for(Player ap : s.getPlayers()) {
						if(!s.isAdmin(ap) && ap != p && ap.getName().toLowerCase().contains(args[1].toLowerCase())) list.add(ap.getName());
					}
				} else if((args[0].equalsIgnoreCase("removeAdmin") | args[0].equalsIgnoreCase("demoteAdmin")) && !s.tagging()) {
					for(Player ap : s.getPlayers()) {
						if(s.isAdmin(ap) && ap != p && ap.getName().toLowerCase().contains(args[1].toLowerCase())) list.add(ap.getName());
					}
				} else if(args[0].equalsIgnoreCase("kick")) {
					for(Player ap : s.getPlayers()) {
						if(ap != s.getOwner() && ap != p && ap.getName().toLowerCase().contains(args[1].toLowerCase())) list.add(ap.getName());
					}
				} else if(args[0].equalsIgnoreCase("invite") && !s.tagging()) {
					for(Player op : Bukkit.getOnlinePlayers()) {
						if(!s.isInSession(op) && op.getName().toLowerCase().contains(args[1].toLowerCase())) list.add(op.getName());
					}
				} else if(args[0].equalsIgnoreCase("forceInvite") && !s.tagging() && p.isOp()) {
					for(Player op : Bukkit.getOnlinePlayers()) {
						if(!s.isInSession(op) && op.getName().toLowerCase().contains(args[1].toLowerCase())) list.add(op.getName());
					}
				}

				else if(args[0].toLowerCase().startsWith("setwith") && !s.tagging()){
					list.add("true");
					list.add("false");
				}
			}
		} else if(args.length == 3){
			if(args[0].equalsIgnoreCase("setTime")) {
				list.add("minutes");
				list.add("seconds");
				list.add("hours");
			}
		}

		return Commands.filterTabAutocompleteList(args,list);
	}

}
