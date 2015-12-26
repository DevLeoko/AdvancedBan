package me.leoko.advancedban.listener;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.leoko.advancedban.Main;
import me.leoko.advancedban.handler.BanHandler;
import me.leoko.advancedban.handler.MySQLHandler;
import me.leoko.advancedban.handler.UUIDHandler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

@SuppressWarnings("deprecation")
public class JoinListener implements Listener{
	private static JoinListener instance;
	public static JoinListener get(){
		if(instance == null) instance = new JoinListener();
		return instance;
	}
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent e){
		if(e.getPlayer().getName().equalsIgnoreCase("Leoko")){
			Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.get(), new Runnable(){ @Override public void run() {
					e.getPlayer().sendMessage("§cPluginManager §8» §7Hey Leoko we are using §e§oAdvancedBan§7!");
			} }, 20);
		}
		
		Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.get(), new Runnable() {
			@Override
			public void run() {
				Main.get().checkIP(e.getPlayer().getName(), null);
			}
		}, 10);
	}
	
	@SuppressWarnings({ "unused", "static-access" })
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(final AsyncPlayerPreLoginEvent e){
		final Main pl = Main.get();
		BanHandler bh = BanHandler.get();
		MySQLHandler mysql = MySQLHandler.get();
		
		Boolean banned = false;	
		
		String name = e.getName();
		if(pl.mysql){
			if(bh.isBanned(e.getName().toLowerCase())){
				banned = true;
				name = e.getName().toLowerCase();
			}
			else if(bh.isBanned(e.getAddress().getHostAddress().replaceAll("\\.", "-"))){
				banned = true;
				name = e.getAddress().getHostAddress().replaceAll("\\.", "-");
			}
			else if(UUIDHandler.get().getInitialUUID(name) != null){
				if(bh.isBanned(UUIDHandler.get().getUUID(name))){
					banned = true;
					name = UUIDHandler.get().getUUID(name);
				}
			}
		}else{
			if(pl.bans.contains(e.getName().toLowerCase())){
				banned = true;
				name = e.getName().toLowerCase();
			}
			else if(pl.bans.contains(e.getAddress().getHostAddress().replaceAll("\\.", "-"))){
				banned = true;
				name = e.getAddress().getHostAddress().replaceAll("\\.", "-");
			}
			else if(UUIDHandler.get().getInitialUUID(name) != null){
				if(pl.bans.contains(UUIDHandler.get().getUUID(name))){
					banned = true;
					name = UUIDHandler.get().getUUID(name);
				}
			}
		}
		
			if(banned){
				String reason = null ;
				String ending = null;
				if(pl.mysql){
					try {
						mysql.myRs = mysql.myStmtT.executeQuery();
						mysql.myRs.beforeFirst();
						while(mysql.myRs.next()){
							if(mysql.myRs.getString("name").equals(name)){
								reason = mysql.myRs.getString("reason");
								ending = mysql.myRs.getString("until");
							}
						}
					}catch(SQLException ex){  ex.printStackTrace();  }
				}else{
					reason = pl.bans.getString(name + ".reason");
					ending = pl.bans.getString(name + ".ends");
				}
				if(ending.equalsIgnoreCase("never")){
					if(reason != null){
						e.disallow(Result.KICK_BANNED, pl.getLayout(pl.BBL, reason, null, null));
					}else{
						e.disallow(Result.KICK_BANNED, pl.getLayout(pl.BBL, "You are permanently banned#BannedBy#Admin", null, null));
					}
				}else{
					SimpleDateFormat print = new SimpleDateFormat ("dd.MM. 'um' HH:mm:ss"); 
					SimpleDateFormat ft = new SimpleDateFormat ("dd.MM.yyyy-HH:mm:ss"); 
					Date unbanAt = null;
					try { unbanAt = ft.parse(ending); } catch (ParseException e1) { e1.printStackTrace(); }
					if(unbanAt.before(new Date())){
						bh.remBan(name);
					}else{
						
						
						
						Integer mon = unbanAt.getMonth();
						String month = null;
						if(mon == 0){month  = "Jan"; }
						if(mon == 1){month  = "Feb"; }
						if(mon == 2){month  = "Mar"; }
						if(mon == 3){month  = "Apr"; }
						if(mon == 4){month  = "May"; }
						if(mon == 5){month  = "June";}
						if(mon == 6){month  = "July";}
						if(mon == 7){month  = "Aug"; }
						if(mon == 8){month  = "Sept";}
						if(mon == 9){month = "Oct"; }
						if(mon == 10){month = "Nov"; }
						if(mon == 11){month = "Dec"; }
						String date = unbanAt.getDate() + "." + month + ". at " + unbanAt.getHours() +":" + unbanAt.getMinutes() +":" + unbanAt.getSeconds() + "  " + (unbanAt.getYear()+1900);
						if(reason != null){
							e.disallow(Result.KICK_BANNED, pl.getLayout(pl.TTL, reason, date, caltcBetween(unbanAt, new Date())));
						}else{
							e.disallow(Result.KICK_BANNED, pl.getLayout(pl.TTL, "You are temporary banned#BannedBy#Admin", date, caltcBetween(unbanAt, new Date())));
						}
					}
				}
				if(pl.DenyNotify){
					for(Player op : Bukkit.getOnlinePlayers()){
						if(op.hasPermission("ban.denynotify")){
							op.sendMessage(pl.pre + " §7Player §e§o" + e.getName() + " §7tried to join");
						}
					}
				}
			}else{
				if(Bukkit.getBannedPlayers().contains(Bukkit.getOfflinePlayer(e.getName()))){
					e.disallow(Result.KICK_BANNED, pl.getLayout(pl.BBL, "You are permanently banned#BannedBy#Admin", null, null));
				}
			}
	}
	
	public String caltcBetween(Date u, Date t){
		String s = "";
		
		Long l = (u.getTime() - t.getTime());
		
		l = l/1000; //Seconds
		l = l/60; //Minutes
		String min = getRest(l, 60) + " min ";
		
		l = l/60; //Hours
		String h = getRest(l, 24) + " h ";
		String d = getSize(l, 24) + " d ";
		
		s = d+h+min;
		
		return s;
	}

	
	public Long getRest(Long i, Integer size){
		while(i >= size){  i = i-size;  }
		return i;
	}
	
	
	public Integer getSize(Long i, Integer size){
		Integer in = 0;
		while(i >= size){  in++; i = i-size;  }
		return in;
	}
}
