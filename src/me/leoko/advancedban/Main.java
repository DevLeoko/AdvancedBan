package me.leoko.advancedban;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.leoko.advancedban.handler.BanHandler;
import me.leoko.advancedban.handler.CommandHandler;
import me.leoko.advancedban.handler.MySQLHandler;
import me.leoko.advancedban.handler.UUIDHandler;
import me.leoko.advancedban.listener.JoinListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;


public class Main extends JavaPlugin implements Listener, PluginMessageListener{
	public Map<String, String> iPs = new HashMap<>();
	
	//Files
	public File confFile = new File(getDataFolder().getPath(), "config.yml");
	public FileConfiguration conf;
	public File banFile = new File(getDataFolder().getPath(), "bans.yml");
	public YamlConfiguration bans = YamlConfiguration.loadConfiguration(banFile);
	public File playerHistoryFile = new File(getDataFolder().getPath(), "playerHistory.yml");
	public YamlConfiguration ph = YamlConfiguration.loadConfiguration(playerHistoryFile);
	
	//Config-Vars
	public String pre = "§cAdvancedBan";
	public boolean mysql = false;
	public boolean DenyNotify = false;
	public boolean loading = false;
	public boolean useUUID = true;
	
	//Layouts - KKL=KickLayout | BBL=BanLayout | TTL=TempbanLayout
	public List<String> KKL = new ArrayList<String>();
	public List<String> BBL = new ArrayList<String>();
	public List<String> TTL = new ArrayList<String>();
	
	private static Main instance;
	public static Main get(){
		return instance;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		instance = this;
		
		//Bungee-Support
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	    this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
		
		//Registering Plugin activity | MCStats
		try {
	        Metrics metrics = new Metrics(this);
	        metrics.start();
	        System.out.println("[AdvancedBan] MC-Stats >> Connected");
	    } catch (IOException e) {
	        System.out.println("[AdvancedBan] Failed to send Stats!\n Contact: Leoko4433@gmail.com \n Error Code: AB344");
	    }
		
		System.setProperty("file.encoding","UTF-8");
		
		if(!confFile.exists())saveResource("config.yml", false);
		conf = YamlConfiguration.loadConfiguration(confFile);
		
		mysql = conf.getBoolean("MySQL.enabled");
		useUUID = conf.getBoolean("UseUUID");
		DenyNotify = conf.getBoolean("DenyNotify");
		loading = conf.getBoolean("LoadingMessage");
		pre = conf.getString("Prefix").replace('&', '§');
		
		KKL = conf.getStringList("KickLayout");
		BBL = conf.getStringList("BanLayout");
		TTL = conf.getStringList("TempbanLayout");
		
		//Connecting tp MySQL with JDBC
		if(mysql) MySQLHandler.get().setup();
		
		//Register Events
		this.getServer().getPluginManager().registerEvents(this              , this);
		this.getServer().getPluginManager().registerEvents(JoinListener.get(), this);
		
		//Creating Ban-File
		if(!banFile.exists()){
			try { bans.save(banFile); } catch (IOException e) { e.printStackTrace(); }
		}
		
		//Creating PlayerHistory-File
		if(!playerHistoryFile.exists()){
			try { ph.save(playerHistoryFile); } catch (IOException e) { e.printStackTrace(); }
		}
		
		//Reload fix | IP-Ban
		Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.get(), new Runnable() {
			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers())checkIP(p.getName(), null);
			}
		}, 10);
		
		System.out.println("\n[=]---------------------------[=]"+
						   "\n-= AdvancedBan =-"+
						   "\nDev: Leoko"+
						   "\nStatus: Enabled"+
						   "\nLicense: Public"+
						   "\nLink: http://dev.bukkit.org/bukkit-plugins/advancedban/"+
						   "\nSupport [Skype/Mail]: Leoko33 / Leoko4433@gmail.com"+
						   "\nVersion: "+this.getDescription().getVersion()+
						   "\n[=]---------------------------[=]"); }
	
	@Override
	public void onDisable() {
		System.out.println("\n[=]---------------------------[=]"+
						   "\n-= AdvancedBan =-"+
						   "\nDev: Leoko"+
						   "\nStatus: Disabled"+
						   "\nLicense: Public"+
						   "\nLink: http://dev.bukkit.org/bukkit-plugins/advancedban/"+
						   "\nSupport [Skype/Mail]: Leoko33 / Leoko4433@gmail.com"+
						   "\nVersion: "+this.getDescription().getVersion()+
						   "\n[=]---------------------------[=]"); }
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, Command cmd, String cmdlable, String[] args){
		Object p = sender;
		//Console DEBUG
		if(cmd.getName().equalsIgnoreCase("doNothing")){
			return true;
		}
		
		//Plugin information
		if(cmd.getName().equalsIgnoreCase("advancedban")){
			sendMessage(p,"§7§l§m-=====§r §3§lBanSystem §7§l§m=====-§r ");
			sendMessage(p,"  §8§lDev §8• §7Leoko");
			sendMessage(p,"  §8§lStatus §8• §a§oStabel");
			sendMessage(p,"  §8§lVersion §8• §7" + this.getDescription().getVersion());
			sendMessage(p,"  §8§lLicense §8• §7Public");
			sendMessage(p,"  §8§lMySQL §8• §7" + Boolean.toString(mysql));
			sendMessage(p,"  §8§lPrefix §8• §7" + pre);
			sendMessage(p,"§7§l§m-===================-§r ");
		return true;}
		
		//Enabeling TAB-Completion
		if(cmd.getName().equalsIgnoreCase("tempban") ||
		   cmd.getName().equalsIgnoreCase("ban") ||
		   cmd.getName().equalsIgnoreCase("ban-ip") ||
		   cmd.getName().equalsIgnoreCase("kick") ||
		   cmd.getName().equalsIgnoreCase("banip") ||
		   cmd.getName().equalsIgnoreCase("check") ||
		   cmd.getName().equalsIgnoreCase("history") ||
		   cmd.getName().equalsIgnoreCase("unban") ||
		   cmd.getName().equalsIgnoreCase("banlist")){
			String sArgs = "";
			for(String s : args) sArgs = sArgs+" "+s;
			final String sCmd = "/"+cmd.getName()+sArgs;
			Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					CommandHandler.get().runCommand(sCmd, sender);
				}
			});
			return true;
		}
		
	return false;}
	
	//
	//Command handerler
	//
	
	// Player commands
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommandPlayer(final PlayerCommandPreprocessEvent ev){
		
		if(ev.getMessage().startsWith("/pardon") ){
			sendMessage(ev.getPlayer(), pre+"§cThe /pardon command is only for VanillaBans use /unban for AdvancedBans");
			return;
		}
		
		final String[] args = ev.getMessage().split(" ");
		
		if(args[0].equalsIgnoreCase("/ban") ||
		   args[0].equalsIgnoreCase("/tempban") ||
		   args[0].equalsIgnoreCase("/ban-ip") ||
		   args[0].equalsIgnoreCase("/banip") ||
		   args[0].equalsIgnoreCase("/kick") ||
		   args[0].equalsIgnoreCase("/check") ||
		   args[0].equalsIgnoreCase("/history") ||
		   args[0].equalsIgnoreCase("/unban") ||
		   args[0].equalsIgnoreCase("/banlist")){
			ev.setCancelled(true);
			if(loading)sendMessage(ev.getPlayer(), getMSG("Loading", true));
		}
		Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				CommandHandler.get().runCommand(ev.getMessage(), ev.getPlayer());
			}
		});
	}
	
	//Console commands
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onCommandConsole(final ServerCommandEvent ev){
		if(ev.getCommand().startsWith("pardon") ){
			sendMessage(ev.getSender(), pre+"§cThe /pardon command is only for VanillaBans use /unban for AdvancedBans");
			return;
		}
		
		final String cmd = ev.getCommand();
		Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				CommandHandler.get().runCommand("/"+cmd, ev.getSender());
			}
		});
		
		final String[] args = ev.getCommand().split(" ");
		
		if(args[0].equalsIgnoreCase("ban") ||
				   args[0].equalsIgnoreCase("tempban") ||
				   args[0].equalsIgnoreCase("ban-ip") ||
				   args[0].equalsIgnoreCase("banip") ||
				   args[0].equalsIgnoreCase("kick") ||
				   args[0].equalsIgnoreCase("check") ||
				   args[0].equalsIgnoreCase("history") ||
				   args[0].equalsIgnoreCase("unban") ||
				   args[0].equalsIgnoreCase("banlist")){
			ev.setCommand("doNothing :)");
			if(loading)sendMessage(ev.getSender(), getMSG("Loading", true));
		}
	}
	
	
	
	//PlayerHistory
	@SuppressWarnings("static-access")
	public void addHistoryEntry(String name, String reason, String by, Object ending){
		
		String uuid = UUIDHandler.get().getUUID(name);
		
		if(reason == null)reason = "none";
		
		SimpleDateFormat ft = new SimpleDateFormat ("dd.MM.yyyy-HH:mm:ss"); 
		String now = ft.format(new Date());
		
		if(ending instanceof Date) ending = ft.format(ending);
		
		if(mysql){
			reason = reason.replace('\'', '"').replace(')', ']').replace('(', '[');
			try {
				String sql = " INSERT INTO `PlayerHistory` "
						   +"(`uuid`, `name`, `reason`, `by`, `start`, `end`) VALUES "
						   +"('" + uuid + "','" + name + "','" + reason + "','" + by + "','" + now + "','" + ending + "')";
				MySQLHandler.get().myStmtPH.executeUpdate(sql);
			}catch(SQLException e){  e.printStackTrace();  }
		}else{
			List<String> current = new ArrayList<String>();
			if(ph.contains(uuid)){
				current = ph.getStringList(uuid);
			}
			current.add(uuid+"#SPLIT#"+name+"#SPLIT#"+reason+"#SPLIT#"+by+"#SPLIT#"+now+"#SPLIT#"+ending);
			ph.set(uuid, current);
			try { ph.save(playerHistoryFile); } catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	@SuppressWarnings("unused")
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	//Getting Layout based on REASON, DATE, PREFIX, OPERATOR and REMAINING-TIME
	public String getLayout(List<String> ls, String reason, String date, String betw){
		String s = "";
		String by = null;
		
		if(reason.split("#BannedBy#").length > 1){
			by = reason.split("#BannedBy#")[1];
		}else{
			by = "an Admin";
		}
		
		if(reason.startsWith("NoReason")){
			reason = conf.getString("DefaultReason", "Connection lost");
		}else{
			if(reason.split("#BannedBy#").length > 1){
				reason = reason.split("#BannedBy#")[0];
			}
		}
		
		
		for(String str : ls){
			s = s + str.replaceAll("%OPERATOR%", by).replaceAll("%REMAINING%", betw).replaceAll("%REASON%", reason).replaceAll("%DATE%", date).replaceAll("%PREFIX%", pre).replace('&', '§') + "\n";
		}
		
		return s;
	}
	
	//Check if String is IP
	public Boolean isIP(String ip){
		if(!(ip.split("\\.").length == 4)){
			return false;
		}
		
		if(!isNumeric(ip.split("\\.")[0])){ return false; }
		if(!isNumeric(ip.split("\\.")[1])){ return false; }
		if(!isNumeric(ip.split("\\.")[2])){ return false; }
		if(!isNumeric(ip.split("\\.")[3])){ return false; }
		
		return true;
	}
	
	//Check if String is a changed IP | I have to change the IP's to save tem in the config file
	//eg: 1.1.1.1 would be not saveable so I change it to 1-1-1-1
	public Boolean isChangedIP(String ip){
		if(!(ip.split("-").length == 4)){
			return false;
		}
		
		if(!isNumeric(ip.split("-")[0])){ return false; }
		if(!isNumeric(ip.split("-")[1])){ return false; }
		if(!isNumeric(ip.split("-")[2])){ return false; }
		if(!isNumeric(ip.split("-")[3])){ return false; }
		
		return true;
	}
	
	//Registering Plugin activity | My Stats-System
	public static boolean ping(String url, int timeout) {
	    url = url.replaceFirst("^https", "http");

	    try {
	        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	        connection.setConnectTimeout(timeout);
	        connection.setReadTimeout(timeout);
	        connection.setRequestMethod("HEAD");
	        int responseCode = connection.getResponseCode();
	        return (200 <= responseCode && responseCode <= 399);
	    } catch (IOException exception) {
	        return false;
	    }
	}
	
	public String getMSG(String path, boolean pre){
		
		String s = null;
		path = "ChatMessages."+path;
		try{ s = conf.getString(path); }catch(Exception exc){ exc.printStackTrace(); }
		
		if(s == null){
			System.out.println("\n \n--===[AdvancdedBan-ERROR]===--" +
							   "\nErrorType: Config-Error"+
							   "\nError: Could not find the message under the path "+path.replace('.', '>')+
							   "\nReportIt?: No"+
							   "\nHELP Skype: Leoko33"+
							   "\n--============================--\n ");
			return "§4ERROR! See Console for ErrorLog";
		}else{
			if(pre) s = this.pre+" "+s;
			return s.replace('&', '§');
		}
	}
	
	public void sendMessage(Object target,String msg){
		if(target instanceof Player){
			((Player) target).sendMessage(msg);
		}else{
			System.out.println(ChatColor.stripColor(msg));
		}
	}

	
	@SuppressWarnings("deprecation")
	public void checkIP(final String name, String prefIP){
		if(prefIP == null) prefIP = Bukkit.getPlayer(name).getAddress().toString().split("/")[(Bukkit.getPlayer(name).getAddress().toString().split("/").length)-1].split(":")[0].replaceAll("\\.", "-");
		System.out.println("Registering IP of "+name);
		if(iPs.containsKey(name.toLowerCase())) iPs.remove(name.toLowerCase());
		iPs.put(name.toLowerCase(), prefIP);
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("IP");
		Bukkit.getPlayer(name).sendPluginMessage(Main.get(), "BungeeCord", out.toByteArray());
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) return;
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		if (subchannel.equals("IP")) {
			String ip = in.readUTF();
			
			if(iPs.containsKey(player.getName().toLowerCase())) iPs.remove(player.getName().toLowerCase());
			iPs.put(player.getName().toLowerCase(), ip);
			
			boolean banned = false;
			
			if(mysql){ if(BanHandler.get().isBanned(ip.replaceAll("\\.", "-"))) banned = true; }
			else{ 	   if(bans.contains(            ip.replaceAll("\\.", "-"))) banned = true; }
			
			
			if(banned){
				String reason = null;
				if(mysql){
					try {
						MySQLHandler.get().myRs = MySQLHandler.get().myStmtT.executeQuery();
						MySQLHandler.get().myRs.beforeFirst();
						while(MySQLHandler.get().myRs.next()){
							if(MySQLHandler.get().myRs.getString("name").equals(ip.replaceAll("\\.", "-"))){
								reason = MySQLHandler.get().myRs.getString("reason");
							}
						}
					}catch(SQLException ex){  ex.printStackTrace();  }
				}else reason = bans.getString(ip.replaceAll("\\.", "-") + ".reason");
				
				player.kickPlayer(getLayout(BBL, reason, null, null));
			}
		}
	}
}
