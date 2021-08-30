package me.noobedidoob.minigames.lasertag.methods;

import me.noobedidoob.minigames.lasertag.Lasertag.LasertagColor;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum Weapon {

	
	LASERGUN(Material.DIAMOND_HOE, LasertagColor.Blue,"§bLasergun", true),
	DAGGER(Material.DIAMOND_SWORD, LasertagColor.Green, "§aDagger", false),
	SHOTGUN(Material.DIAMOND_SHOVEL, LasertagColor.Yellow, "§eShotgun", true),
	SNIPER(Material.DIAMOND_PICKAXE, LasertagColor.Purple, "§dSniper", true),
	GRENADE(Material.TURTLE_EGG, LasertagColor.White, "§fGrenade", true);

	private final Material material;
	private final ItemStack item;
	public final boolean inTestSet;

	Weapon(Material material, LasertagColor defaultColor, String displayName, boolean inTestSet){
		this.material = material;
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setUnbreakable(true);
		meta.setCustomModelData(defaultColor.ordinal()+1);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		item.setItemMeta(meta);
		this.item = item;
		this.inTestSet = inTestSet;
	}

	public Material getType() {
		return material;
	}
	public ItemStack getItem() {
		return item;
	}
	public ItemStack getItem(String displayName){
		ItemStack item = this.item.clone();
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		item.setItemMeta(meta);
		return item;
	}
	public ItemStack getTestItem() {
		return getTestItem(1);
	}
	public ItemStack getTestItem(int amount) {
		ItemStack newItem = item.clone();
		item.setAmount(amount);
		if(this == SNIPER) newItem.setAmount(Mod.SNIPER_AMMO.getOgInt());
		ItemMeta meta = newItem.getItemMeta();
		meta.setDisplayName(meta.getDisplayName()+" Test");
		newItem.setItemMeta(meta);
		return newItem;
	}
	public ItemStack getColoredItem(LasertagColor color) {
		ItemStack item = this.item.clone();
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(color.getChatColor()+meta.getDisplayName().substring(2));
		meta.setCustomModelData(color.ordinal()+1);
		item.setItemMeta(meta);
		return item;
	}
	public ItemStack getColoredItem(LasertagColor color, String displayName){
		ItemStack item = this.item.clone();
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setCustomModelData(color.ordinal()+1);
		item.setItemMeta(meta);
		return item;
	}
	public ItemStack getColoredItem(LasertagColor color, int amount) {
		ItemStack item = this.item.clone();
		item.setAmount(amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(color.getChatColor()+meta.getDisplayName().substring(2));
		meta.setCustomModelData(color.ordinal()+1);
		item.setItemMeta(meta);
		return item;
	}

	public static Weapon getWeaponFromItem(ItemStack item) {
		Material type = item.getType();
		String name = item.getItemMeta().getDisplayName();
		for(Weapon w : values()){
			if(w.getType() == type && name.toUpperCase().contains(w.name().substring(2))) return w;
		}
		return null;
	}

	public boolean isWeapon(ItemStack item){
		if(item == null) return false;
		return item.getType() == material && item.getItemMeta().getDisplayName().contains(this.item.getItemMeta().getDisplayName().substring(3));
	}

	public String getName(){
		return this.name().charAt(0)+this.name().toLowerCase().substring(1);
	}

	//TODO: Automate
	public boolean hasCooldown(Player p) {
		return p.hasCooldown(material);
	}
	public void setCooldown(Player p) {
		Session s = Session.getPlayerSession(p);
		int cooldown = Mod.LASERGUN_NORMAL_COOLDOWN_TICKS.getOgInt();
		if(s != null){
			switch (this) {
			case LASERGUN:
				cooldown = (s.withMultiweapons())? s.getIntMod(Mod.LASERGUN_MULTIWEAPONS_COOLDOWN_TICKS) : s.getIntMod(Mod.LASERGUN_NORMAL_COOLDOWN_TICKS);
				break;
			case SNIPER:
				cooldown = s.getIntMod(Mod.SNIPER_COOLDOWN_TICKS);
				Utils.runLater(() -> p.getInventory().getItem(p.getInventory().first(SNIPER.material)).setAmount(s.getIntMod(Mod.SNIPER_AMMO)), s.getIntMod(Mod.SNIPER_COOLDOWN_TICKS));
				break;
			case SHOTGUN:
				cooldown = s.getIntMod(Mod.SHOTGUN_COOLDOWN_TICKS);
				break;
			case GRENADE:
				cooldown = s.getIntMod(Mod.GRENADE_COOLDOWN_TICKS);
				break;
			default:
				break;
			}
		} else {
			switch (this) {
			case SHOTGUN:
				cooldown = Mod.SHOTGUN_COOLDOWN_TICKS.getOgInt();
				break;
			case SNIPER:
				cooldown = Mod.SNIPER_COOLDOWN_TICKS.getOgInt();
				Utils.runLater(() -> p.getInventory().getItem(p.getInventory().first(SNIPER.material)).setAmount(Mod.SNIPER_AMMO.getOgInt()), Mod.SNIPER_COOLDOWN_TICKS.getOgInt());
				break;
			case GRENADE:
				cooldown = Mod.GRENADE_COOLDOWN_TICKS.getOgInt()/10;
				break;
			default:
				break;
			}
		}
		p.setCooldown(material, cooldown);
	}

	public static void setTestInventory(Player p){
		if(p == null) return;
 		p.getInventory().clear();
		int i = 0;
		for (Weapon w : values()) {
			if(!w.inTestSet) continue;
			p.getInventory().setItem(i++,w.getTestItem());
		}
	}
}
	
