package me.noobedidoob.minigames.lasertag.methods;

import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Random;

public class PointEvents {
    //TODO: Enable PointsEvents

    Session session;

    ArrayList<Mod> mods = new ArrayList<>();
    public PointEvents(Session session){
        this.session = session;
        for(Mod m : Mod.values()){
            if(m.inPointEvents) {
                if(session.getBooleanMod(Mod.POINT_EVENT_ONLY_POINTS) && !m.name().contains("POINTS")) continue;
                if(!session.withMultiweapons() && (m.name().contains("MULTIWEAPONS") | m.name().contains("SNIPER") | m.name().contains("SHOTGUN") | m.name().contains("DAGGER")) ) continue;
                if(!session.withGrenades() && m.name().contains("GRENADE")) continue;
                if(!session.withCaptureTheFlag() && m.name().contains("FLAG")) continue;
                mods.add(m);
            }
        }


    }


    boolean running;

    private BukkitTask currentTimer;
    private BossBar bar;
    public void startEvents(){
        System.out.println("starting point events");
        if(!running){
            running = true;
            bar = Bukkit.createBossBar("Events", BarColor.RED, BarStyle.SOLID);
            bar.setVisible(false);
            for(Player p : session.getPlayers()) bar.addPlayer(p);
            bar.setVisible(false);
            new BukkitRunnable(){
                @Override
                public void run() {
                    prepareNewEvent(session.getIntMod(Mod.POINT_EVENT_FIRST_EVENT_DELAY_TICKS)/2);
                }
            }.runTaskLater(session.minigames, session.getIntMod(Mod.POINT_EVENT_FIRST_EVENT_DELAY_TICKS)/2);
        }
    }

    private Event event;
    public void prepareNewEvent(int ticks){
        event = new Event(session, mods);
        bar.setTitle("§aNext Event: §r§b"+event.name.substring(0, event.name.length()-2)+" §7(§c"+event.backupValue.toString()+" §7-> §a" + event.value.toString()+"§7)");
        bar.setVisible(true);
        bar.setColor(BarColor.RED);
        currentTimer = new BukkitRunnable(){
            int curr = ticks;
            @Override
            public void run() {
                if (curr > 0) {
                    curr--;
                    bar.setProgress((float) curr / ticks);
                } else {
                    cancel();
                    startEvent();
                }
            }
        }.runTaskTimer(session.minigames, 1, 1);
    }

    private void startEvent(){
        event.start();
        bar.setColor(BarColor.GREEN);
        bar.setProgress(1);
        bar.setTitle("§2Current Event: §b" + event.name + event.value.toString());
        currentTimer = new BukkitRunnable(){
            int curr = session.getIntMod(Mod.POINT_EVENT_DEFAULT_LENGTH_TICKS);
            @Override
            public void run() {
                if(curr > 0){
                    curr--;
                    bar.setProgress((float) curr / session.getIntMod(Mod.POINT_EVENT_DEFAULT_LENGTH_TICKS));
                } else {
                    cancel();
                    event.reset();
                    prepareNewEvent(session.getIntMod(Mod.POINT_EVENT_NEXT_EVENT_DELAY_TICKS));
                }
            }
        }.runTaskTimer(session.minigames, 1 , 1);
    }

    public void stopEvents(){
        running = false;
        if(currentTimer != null & !currentTimer.isCancelled()) currentTimer.cancel();
        if(event != null) event.resetSilent();
        if(bar != null) bar.removeAll();
    }

}

class Event{

    private final Session session;
    public final Mod mod;
    public final String name;
    public final int ticks;
    public final Object value;
    public final Object backupValue;

    public Event(Session session, ArrayList<Mod> mods){
        this.session = session;
        Mod randomMod = mods.get(Utils.randomInt(0, mods.size()-1));
        System.out.println("new event: "+randomMod.getCommand());
        this.mod = randomMod;
        this.name = mod.eventName;
        this.backupValue = session.getModValue(mod);

        int max = (session.getTime(Utils.TimeFormat.SECONDS) < 60)?session.getTime(Utils.TimeFormat.SECONDS)*20:20*60;
        ticks = Utils.randomInt(Math.round(max/2f), max);
        Object value = mod.getOg();
        while (value == mod.getOg()){
            switch (mod.getValueType()){
                case INTEGER:
                    value = Utils.randomInt((Integer) mod.eventChangeRange.get1(), (Integer) mod.eventChangeRange.get2());
                    if(mod.name().contains("TICK")) value = ((int) value )/ 20;
                    break;
                case DOUBLE:
                    try {
                        value = Utils.randomDouble((Double) mod.eventChangeRange.get1(),(Double) mod.eventChangeRange.get2());
                    } catch (Exception exception) {
                        try {
                            value = Utils.randomInt((Integer) mod.eventChangeRange.get1(), (Integer) mod.eventChangeRange.get2());
                        } catch (Exception e) {
                            value = mod.getOg();
                            Minigames.severe("ERROR occured while parsing");
                            e.printStackTrace();
                        }
                    }
                    break;
                case BOOLEAN:
                    value = new Random().nextBoolean();
                    break;
                default:
                    break;
            }
        }
        this.value = value;
    }

    public void start(){
        session.setModSilent(mod, value);
        for (Player p : session.getPlayers()) {
            p.sendTitle("", "§b"+name+" §c"+backupValue.toString()+" §7-> §a" + value.toString(), 10, 40, 10);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 1);
            Utils.runLater(()->p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 1), 5);
        }
    }

    public void reset(){
        session.setModSilent(mod, backupValue);
        for (Player p : session.getPlayers()) {
            p.sendTitle("", "§b"+name.substring(0, name.length()-2)+ " §awas resetset to §e" + backupValue.toString(), 10, 3*20, 10);
        }
    }
    public void resetSilent(){
        session.setModSilent(mod, backupValue);
    }
}
