package me.noobedidoob.minigames.lasertag.methods;

import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.lasertag.Lasertag;
import me.noobedidoob.minigames.lasertag.listeners.DeathListener;
import me.noobedidoob.minigames.lasertag.listeners.DeathListener.HitType;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.lasertag.session.SessionModifiers;
import me.noobedidoob.minigames.utils.BaseSphere;
import me.noobedidoob.minigames.utils.Grenade;
import me.noobedidoob.minigames.utils.HitBox;
import me.noobedidoob.minigames.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LaserShooter{



	public static void fire(Player p, Weapon w) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return;
		SessionModifiers modifiers = session.modifiers;
		
		List<Player> alreadyKilledPlayers = new ArrayList<>();
		List<Player> killedPlayers = new ArrayList<>();
		
		switch (w) {
			case LASERGUN:
				if(!w.hasCooldown(p)) {
					if(session.getMap().checkLocPlayerShootingFrom(p)) return;
					p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 6);

					Location l1 = p.getLocation();
					l1.setY(l1.getY()+p.getHeight()-0.225);
					Vector direction = l1.getDirection();
					direction.multiply(0.1);
					if(session.withMultiweapons()) {
						Vector newDirection = direction;
						direction = direction.setX(newDirection.getX()+ThreadLocalRandom.current().nextDouble(-0.0001,0.0001));
						direction = direction.setZ(newDirection.getZ()+ThreadLocalRandom.current().nextDouble(-0.0001,0.0001));
						direction = direction.setY(newDirection.getY()+ThreadLocalRandom.current().nextDouble(-0.0001,0.0001));
					}

					w.setCooldown(p);

					int range = 100;
					for(double d = 0; d<range; d += 0.1) {
						Location loc = l1.add(direction);
						if(session.getMap().checkPlayerLaserLoc(loc,p)) break;

						spawnProjectile(p, loc);
						checkGrenades(loc, p);

						for(Player hitP : session.getPlayers()) {
							if(hitP != p && !alreadyKilledPlayers.contains(hitP)) {
								if(isLaserInsideEntity(hitP, loc)) {
									if(Lasertag.isPlayerProtected(hitP)) {
										p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+""+ChatColor.BOLD+"Player has spawnprotection"));
										BaseSphere.drawPlayerProtectionSphere(hitP);
										break;
									}
									if(!session.inSameTeam(p,hitP)){
										alreadyKilledPlayers.add(hitP);
										if(DeathListener.hit(HitType.SHOT, p, hitP, (session.withMultiweapons())?session.getIntMod(Mod.LASERGUN_MULTIWEAPONS_DAMAGE): session.getIntMod(Mod.LASERGUN_NORMAL_DAMAGE), (loc.getY() < hitP.getEyeLocation().getY()+0.25 && loc.getY() > hitP.getEyeLocation().getY()-0.25), (d > modifiers.getInt(Mod.MINIMAL_SNIPE_DISTANCE)), false)){
											killedPlayers.add(hitP);
										}
									}
								}
							}
						}
						if(isInBlock(session, loc)) break;
					}
					checkMultikill(p,killedPlayers);
				}
				break;


			case SHOTGUN:
				if(!w.hasCooldown(p)) {
					if(session.getMap().checkLocPlayerShootingFrom(p)) return;

					p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 6);

					Location startLoc = p.getLocation();
					startLoc.setY(startLoc.getY()+p.getEyeHeight()-0.1);

					Location[] startLocs = new Location[9];
					float dis = 16;
					int n = 0;
					for(float pitch = dis; pitch > ((dis)*2)*(-1); pitch -= dis) {
						for(float yaw = dis*(-1); yaw < dis*2; yaw += dis) {
							startLocs[n] = startLoc.clone();
							startLocs[n].setPitch(startLocs[n].getPitch()+pitch);
							startLocs[n].setYaw(startLocs[n].getYaw()+yaw);
							n++;
						}
					}

					Vector[] dirs = new Vector[9];
					Location[] locs = new Location[9];
					for(int i = 0; i < 9; i++) {
						dirs[i] = startLocs[i].getDirection().multiply(0.1);
						locs[i] = startLocs[i].add(dirs[i]);
					}


					w.setCooldown(p);
					for(double d = 0; d<6; d += 0.1) {
						for(int i = 0; i < 9; i++) {
							if(locs[i] == null) continue;
							if(session.getMap().checkPlayerLaserLoc(locs[i],p)) locs[i] = null;
							locs[i] = startLocs[i].add(dirs[i]);
							spawnProjectile(p, locs[i]);
							checkGrenades(locs[i], p);
						}
						for(Player hitP : session.getPlayers()) {
							if(hitP != p && !alreadyKilledPlayers.contains(hitP)) {
								for(Location loc : locs) {
									if(loc == null) continue;
									if(isLaserInsideEntity(hitP, loc)) {
										if(Lasertag.isPlayerProtected(hitP)) {
											p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+""+ChatColor.BOLD+"Player has spawnprotection"));
											BaseSphere.drawPlayerProtectionSphere(hitP);
											return;
										}
										if(!session.inSameTeam(p,hitP)) {
											alreadyKilledPlayers.add(hitP);
											if (DeathListener.hit(HitType.SHOT, p, hitP, modifiers.getInt(Mod.SHOTGUN_DAMAGE), false, false, false)) {
												killedPlayers.add(hitP);
											}
										}
									}
								}
							}
						}
						for (int i = 0; i < locs.length; i++) {
							if(locs[i] != null && isInBlock(session, locs[i])) locs[i] = null;
						}
					}
					checkMultikill(p,killedPlayers);
				}
				break;
			case SNIPER:
				if(!w.hasCooldown(p)) {
					if(session.getMap().checkLocPlayerShootingFrom(p)) return;

					p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, 10, 0);

					Location l1 = p.getLocation();
					l1.setY(l1.getY()+p.getHeight()-0.225);
					Vector direction = l1.getDirection();
					direction.multiply(1);

					for(double d = 0; d<100; d += 1) {
						if(d==0) {
							if(p.getInventory().getItem(2).getAmount() > 1) {
								p.getInventory().getItem(2).setAmount(p.getInventory().getItem(2).getAmount()-1);
							} else {
								w.setCooldown(p);
							}
						}
						Location loc = l1.add(direction);
						if(session.getMap().checkPlayerLaserLoc(loc,p)) break;

						spawnProjectile(p, loc);
						checkGrenades(loc, p);

						for(Player hitP : session.getPlayers()) {
							if(hitP != p && !alreadyKilledPlayers.contains(hitP)) {
								if(isLaserInsideEntity(hitP, loc)) {
									if(Lasertag.isPlayerProtected(hitP)) {
										p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+""+ChatColor.BOLD+"Player has spawnprotection"));
										BaseSphere.drawPlayerProtectionSphere(hitP);
										break;
									}
									if (!session.inSameTeam(p,hitP)) {
										alreadyKilledPlayers.add(hitP);
										if(DeathListener.hit(HitType.SHOT, p, hitP, modifiers.getInt(Mod.SNIPER_DAMAGE), (loc.getY() < hitP.getEyeLocation().getY()+0.25 && loc.getY() > hitP.getEyeLocation().getY()-0.25), (d > modifiers.getInt(Mod.MINIMAL_SNIPE_DISTANCE)), false)) {
											killedPlayers.add(hitP);
										}
									}
								}
							}
						}
						if(isInBlock(session, loc)) break;
					}
					checkMultikill(p, killedPlayers);
				}
				break;

			case GRENADE:
				if(session.getMap().checkLocPlayerShootingFrom(p)) return;
				if(!p.hasCooldown(Weapon.GRENADE.getType())) {
					Weapon.GRENADE.setCooldown(p);
					new Grenade(p,20*p.getInventory().getItem(p.getInventory().first(Weapon.GRENADE.getType())).getAmount(),p.isSneaking()?1:0.5, session.getMap().getArea(), Minigames.INSTANCE);
				}
				break;
			default:
				break;
		}
	}

	private static  void checkMultikill(Player p, List<Player> players){
		Session session = Session.getPlayerSession(p);
		if(session == null) return;
		if(players.size() > 1) {
			Utils.runLater(()->{
				StringBuilder killedPlayersNames = new StringBuilder();
				int i = 0;
				for(Player kp : players) {
					if(i == 0) killedPlayersNames.append(session.getPlayerColor(kp).getChatColor()).append(kp.getName());
					else killedPlayersNames.append(", ").append(session.getPlayerColor(kp).getChatColor()).append(kp.getName());
					i++;
				}
				int points = session.getIntMod(Mod.MULTIKILLS_EXTRA_POINTS)*players.size();
				session.addPoints(p, points, "§e—— "+session.getPlayerColor(p).getChatColor()+p.getName()+" §dkilled "+killedPlayersNames+" §dwith one shot! §7(§a+"+points+" extra Point"+((points > 1)?"s":"")+"§7) §e——");
			}, 10);
		}
	}

	public static void colorPlayerHitBox(Player p) {
		HitBox hb = new HitBox(p);
		for(double x = hb.getMinX(); x <= hb.getMaxX(); x += 0.1) {
			for(double y = hb.getMinY(); y <= hb.getMaxY(); y += 0.1) {
				for(double z = hb.getMinZ(); z <= hb.getMaxZ(); z += 0.1) {
					spawnTestProjectile(new Location(p.getWorld(), x, y, z), Color.RED);
				}
			}
		}
	}
	
	
	public static boolean isLaserInsideEntity(Entity e, Location loc) {
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		
		double width = e.getWidth();
		double height = e.getHeight()+0.15;

		if (e instanceof Player && Session.getPlayerSession((Player)e) != null) {
			width += Session.getPlayerSession((Player) e).modifiers.getDouble(Mod.WIDTH_ADDON);
			height += Session.getPlayerSession((Player) e).modifiers.getDouble(Mod.HEIGHT_ADDON);
		}

		double minX = e.getLocation().getX()-(width/2);
		double minY = e.getLocation().getY();
		double minZ = e.getLocation().getZ()-(width/2);
		double maxX = (e.getLocation().getX()-(width/2))+width;
		double maxY = e.getLocation().getY()+height;
		double maxZ = (e.getLocation().getZ()-(width/2))+width;
		
		if(minX <= x && x <= maxX) {
			if(minY <= y && y <= maxY) {
				return minZ <= z && z <= maxZ;
			}
		}
		return false;
	}

	public static void checkGrenades(Location laserLoc, Player shooter){
		for(Entity e : laserLoc.getWorld().getNearbyEntities(laserLoc, 1,1,1)){
			if(e instanceof Snowball){
				Grenade.explodeGrenade((Snowball) e, shooter);
			}
		}
	}
	
	public static boolean isInBlock(Session s, Location loc) {
		Block b = loc.getBlock();
		Material fm = b.getType();
		if(fm.name().contains("STAINED")) {
			b.breakNaturally();
			loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.1f, 1);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Minigames.INSTANCE, () -> b.setType(fm), 20*5);
			return true;
		}
		boolean shootThroughEnabled = false;
		if(s != null) shootThroughEnabled= s.getBooleanMod(Mod.SHOOT_THROUGH_BLOCKS);
		if(!shootThroughEnabled) {
			Material m = Minigames.INSTANCE.world.getBlockAt(loc).getType();
			if(!m.isAir() && !m.name().contains("Fence")) {
				if(m.isSolid()) {
					if(Tag.SLABS.isTagged(m)) {
						if(b.getBlockData() instanceof Slab) {
							Slab slab = (Slab) b.getBlockData();
							if(slab.getType() == Type.BOTTOM) {
								return loc.getY() < b.getY() + 0.5;
							} else {
								return loc.getY() > b.getY() + 0.5;
							}
						}
					} else if(Tag.TRAPDOORS.isTagged(m)) {
						return !((TrapDoor) b.getBlockData()).isOpen();
					} else if(Tag.DOORS.isTagged(m)) {
						return !((Door) b.getBlockData()).isOpen();
					} else return true;
				}
			}
		}
		return false;
	}
	
	public static void spawnProjectile(Player p, Location loc) {
		Session session = Session.getPlayerSession(p);
		if(session.getPlayerColor(p).getColor() != Color.BLUE) p.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(session.getPlayerColor(p).getColor(), 0.8f));
		else p.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(Color.fromRGB(0, 183, 255), 0.8f));
	}
	
	
	
	
	public static void fireTest(Player p, Weapon w) throws NullPointerException{
		if(w == null) throw new NullPointerException("Weapon is null");
		switch (w) {
			case LASERGUN:
				if(!p.hasCooldown(Weapon.LASERGUN.getType())) {
					p.setCooldown(Weapon.LASERGUN.getType(), Mod.LASERGUN_NORMAL_COOLDOWN_TICKS.getOgInt());
					Location startLoc = p.getLocation();
					startLoc.setY(startLoc.getY()+p.getEyeHeight());
					p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 5);

					Vector direction = startLoc.getDirection();
					direction.multiply(0.1);

					for(double d = 0; d<100; d += 0.1) {
						Location loc = startLoc.add(direction);
						spawnTestProjectile(loc, Color.fromRGB(0, 170, 255));
						checkGrenades(loc, p);

						if(!checkloc(p, loc)) return;
					}
				}
				break;






			case SHOTGUN:
				if(!p.hasCooldown(Weapon.SHOTGUN.getType())) {
					p.setCooldown(Weapon.SHOTGUN.getType(), Mod.SHOTGUN_COOLDOWN_TICKS.getOgInt());
					Location startLoc = p.getLocation();
					startLoc.setY(startLoc.getY()+p.getEyeHeight());
					p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 5);
					Location[] startLocs = new Location[9];
					float dis = 15;
					int n = 0;
					for(float pitch = dis; pitch > ((dis)*2)*(-1); pitch -= dis) {
						for(float yaw = dis*(-1); yaw < dis*2; yaw += dis) {
							startLocs[n] = startLoc.clone();
							startLocs[n].setPitch(startLocs[n].getPitch()+pitch);
							startLocs[n].setYaw(startLocs[n].getYaw()+yaw);
							n++;
						}
					}

					Vector[] dirs = new Vector[9];
					Location[] locs = new Location[9];
					for(int i = 0; i < 9; i++) {
						dirs[i] = startLocs[i].getDirection().multiply(0.1);
						locs[i] = startLocs[i].add(dirs[i]);
					}

					for(double d = 0; d<3.6; d += 0.1) {
						for(int i = 0; i < 9; i++) {
							if(locs[i] == null) continue;
							locs[i] = startLocs[i].add(dirs[i]);
							spawnTestProjectile(locs[i], Color.YELLOW);
							checkGrenades(locs[i], p);
						}
						for (int i = 0; i < 9; i++) {
							if(locs[i]!= null && !checkloc(p, locs[i])) locs[i] = null;
						}
					}
				}
				break;






			case SNIPER:
				if(!p.hasCooldown(Weapon.SNIPER.getType())) {
					Location startLoc = p.getLocation();
					startLoc.setY(startLoc.getY()+p.getEyeHeight());
					p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 5);
					Vector direction1 = startLoc.getDirection();
					direction1.multiply(1);

					for(double d = 0; d<100; d += 1) {
						try {
							if(d==0) {
								int s = p.getInventory().getItem(2).getAmount();
								if(s > 1) {
									p.getInventory().getItem(2).setAmount(s-1);
								} else {
									p.setCooldown(Weapon.SNIPER.getType(), Mod.SNIPER_COOLDOWN_TICKS.getOgInt());
									p.getInventory().getItem(2).setAmount(1);
									new BukkitRunnable() {
										@Override
										public void run() {
											p.getInventory().getItem(2).setAmount(Mod.SNIPER_AMMO.getOgInt());
										}
									}.runTaskLater(Minigames.INSTANCE, Mod.SNIPER_COOLDOWN_TICKS.getOgInt());
								}

							}

							Location loc1 = startLoc.add(direction1);
							spawnTestProjectile(loc1, Color.PURPLE);
							checkGrenades(loc1, p);

							if(!checkloc(p, loc1)) return;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
			case GRENADE:
				if(!p.hasCooldown(Weapon.GRENADE.getType())) {
					Weapon.GRENADE.setCooldown(p);
					new Grenade(p,20*p.getInventory().getItem(p.getInventory().first(Weapon.GRENADE.getType())).getAmount(), (p.isSneaking()?1:0.5), Lasertag.getTestAera(), Minigames.INSTANCE);

//					int amount = p.getInventory().getItem(p.getInventory().first(Weapon.GRENADE.getType())).getAmount();
//					System.out.println("1: "+amount);
//					Utils.runLater(()->{
//						System.out.println("2: "+amount);
//						p.getInventory().setItem(3, Weapon.GRENADE.getTestItem(amount));
//					},20);
				}
				break;
		default:
			
			break;
		}
			
	}
	
	private static boolean checkloc(Player p, Location loc) {
		if(!Lasertag.getTestAera().isInside(loc)) return false;
		if(isInBlock(null, loc)) return false;
		for(Entity entity : loc.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
			if (entity != p) {
				if(isLaserInsideEntity(entity, loc)) {
					p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0);
				}
			}
		}
		return true;
	}

	public static void spawnTestProjectile(Location loc, Color c) {
		loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(c, 0.5f));
	}
	
}
