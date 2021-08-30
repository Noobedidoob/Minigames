package me.noobedidoob.minigames.lasertag.listeners;

import me.noobedidoob.minigames.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.PluginManager;

import me.noobedidoob.minigames.lasertag.Lasertag;
import me.noobedidoob.minigames.lasertag.listeners.DeathListener.HitType;
import me.noobedidoob.minigames.lasertag.methods.Mod;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.utils.BaseSphere;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class HitListener implements Listener {
	
	public HitListener(Minigames minigames) {
		PluginManager pluginManeger = Bukkit.getPluginManager();
		pluginManeger.registerEvents(this, minigames);
	}
	
	
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player victim = (Player) e.getEntity();
			Session session = Session.getPlayerSession(victim);
			if(session == null) return;
			Player damager = (Player) e.getDamager();
			
			if(session.tagging()) {
				if (session.isInSession(victim) && session.isInSession(damager)) {
					if (!session.inSameTeam(victim, damager)) {
						if (Lasertag.isPlayerProtected(victim)) {
							damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+""+ChatColor.BOLD+"Player has spawnprotection"));
							BaseSphere.drawPlayerProtectionSphere(victim);
							e.setCancelled(true);
						} else {
							if (!session.withMultiweapons()) {
								double damage = e.getDamage();
								if (victim.getInventory().getItemInMainHand().getType() != Material.AIR) {
									if(victim.getInventory().getItemInMainHand().getItemMeta().getDisplayName().toUpperCase().contains("LASERGUN")) damage = session.getIntMod(Mod.LASERGUN_PVP_DAMAGE);
								}
								if(damage < victim.getHealth()-1) {
									e.setDamage(1);
									damage--;
								} else e.setCancelled(true);
								DeathListener.hit(HitType.PVP, damager, victim, damage, false, false, false);
							} else {
								if (damager.getInventory().getItemInMainHand().getItemMeta().getDisplayName().toUpperCase().contains("DAGGER")) {
									if(Utils.isPlayerBehindOtherPlayer(damager, victim)) {
										DeathListener.hit(HitType.PVP, damager, victim, 100, false, false, true);
										e.setCancelled(true);
									}
									else{
										double damage = session.getIntMod(Mod.DAGGER_DAMAGE);
										if(damage < victim.getHealth()-1) {
											e.setDamage(1);
											damage--;
										} else e.setCancelled(true);
										DeathListener.hit(HitType.PVP, damager, victim, damage, false, false, false);
									}
								}
							}
						}
					} else {
						e.setCancelled(true);
					} 
				} else {
					e.setCancelled(true);
				}
			} else {
				if(Utils.isPlayerBehindOtherPlayer(damager, victim)) {
					victim.setGlowing(true);
					Utils.runLater(() -> victim.setGlowing(false), 5);
				}
			}
		}
	}
}
