package me.noobedidoob.minigames;

import me.noobedidoob.minigames.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Test implements CommandExecutor, Listener {


    public Test(Minigames minigames) {
        Bukkit.getPluginManager().registerEvents(this, minigames);
    }


    public void test(Player p) {
        BossBar bar = Bukkit.createBossBar(ChatColor.GREEN+"Test§bwdw", BarColor.GREEN, BarStyle.SOLID);
        bar.addPlayer(p);
        int time = 20*10;
        new BukkitRunnable(){
            int curr = time;
            @Override
            public void run() {
                if(curr > 0){
                    curr--;
                    bar.setProgress((float)curr/time);
                } else {
                    cancel();
                    bar.removeAll();
                }
            }
        }.runTaskTimer(Minigames.INSTANCE, 1, 1);
    }
    public void test2(Player p){
        p.sendMessage(p.getInventory().contains(Material.DIAMOND)+"");
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        try {
            if(e.getAction() == Action.RIGHT_CLICK_AIR | e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                if(e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("Test")) test(e.getPlayer());
            } else if(e.getAction() == Action.LEFT_CLICK_AIR | e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
                if(e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("Test")) test2(e.getPlayer());
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (sender instanceof Player && sender.isOp()) {
            Player p = (Player) sender;
            if (args.length == 0) {
                p.getInventory().addItem(Utils.getItemStack(Material.STICK,"Test"));
            }
        }
        return true;
    }
}
