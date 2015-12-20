package me.leoko.advancedban.handler;

import java.io.IOException;
import java.sql.SQLException;

import me.leoko.advancedban.Main;

import org.bukkit.Bukkit;

public class BanHandler {
	private static BanHandler instance;
	public static BanHandler get(){
		if(instance == null) instance = new BanHandler();
		return instance;
	}
	//Removing bans
		@SuppressWarnings({ "deprecation", "static-access" })
		public Boolean remBan(String name){
			Main pl = Main.get();
			name = name.toLowerCase();
			
			if(Bukkit.getOfflinePlayer(name).isBanned()) Bukkit.getOfflinePlayer(name).setBanned(false);
			
			if(!pl.isChangedIP(name)){
				if(UUIDHandler.get().getUUID(name) != null){
					name = UUIDHandler.get().getUUID(name);
				}
			}
			
			if(pl.mysql){
				if(BanHandler.get().isBanned(name)){
					try{
						String sql = "delete from AdvancedBans "
								   + "where name="
								   + "'" + name + "'";
						MySQLHandler.get().myStmtT.executeUpdate(sql);
					}catch(SQLException e){  e.printStackTrace();  }
				}else{
					return false;
				}
			}else{
				if(pl.bans.contains(name)){
					pl.bans.set(name, null);
					try { pl.bans.save(pl.banFile); } catch (IOException e) { e.printStackTrace(); }
				}else{
					return false;
				}
			}
			return true;
		}
		
		//Adding bans
		@SuppressWarnings("static-access")
		public Boolean addBan(String reason, String ending, String name, String by){
			String rlName = name;
			Boolean b = true;
			Main pl = Main.get();
			MySQLHandler mysql = MySQLHandler.get();
			
			name = name.toLowerCase();
			
			if(!pl.isChangedIP(rlName)){
				if(ending.equalsIgnoreCase("never")) execCommands("Ban", rlName);
				else execCommands("Tempban", rlName);
			}
			
			if(UUIDHandler.get().getUUID(name) != null){
				name = UUIDHandler.get().getUUID(name);
			}
			
			if(reason == null){
				reason = "NoReason";
			}
			
			reason = reason + "#BannedBy#"+ by;
			
			if(pl.mysql){
				reason = reason.replace('\'', '"').replace(')', ']').replace('(', '[');
				try {
					//Ban
					mysql.myStmtT = mysql.myConnT.prepareStatement("select * from AdvancedBans");
					mysql.myRs = mysql.myStmtT.executeQuery();
					mysql.myRs.beforeFirst();
					while(mysql.myRs.next()){
						if(mysql.myRs.getString("name").equals(name)){
							return false;
						}
					}
					String sql = "insert into AdvancedBans "
							   + " (name, reason, until)"
							   + " values ('" + name + "','" + reason + "','" + ending + "')";
					
					mysql.myStmtT.executeUpdate(sql);
				}catch(SQLException e){  e.printStackTrace();  }
			}else{
				if(!pl.bans.contains(name)){
					pl.bans.createSection(name);
					pl.bans.set(name + ".reason", reason);
					pl.bans.set(name + ".ends", ending);
					if(pl.useUUID) pl.bans.set(name + ".name", rlName);
					
					try { pl.bans.save(pl.banFile); } catch (IOException e) { e.printStackTrace(); }
				}else{
					b = false;
				}
			}
			return b;
		}
	
		
		public void execCommands(String type, String player){
			if(Main.get().conf.contains("CommandsOn."+type)) for(String s : Main.get().conf.getStringList("CommandsOn."+type)){
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), s.replace("%PLAYER%", player));
			}
		}
		
	public Boolean canTempban(Object sender, Integer dur, String type){
		Integer h = 0;
		
		if(CommandHandler.get().hasPermission(sender, "ban.admin")) return true;
		
		if(type.equals("s")) return true;
		if(type.equals("m")) return true;
		if(type.equals("h")) h = dur;
		if(type.equals("d")) h = dur*24;
		if(type.equals("w")) h = dur*24*7;
		if(type.equals("mo")) h = dur*24*30;
		if(type.equals("y")) h = dur*24*300;
		
		Integer max = 720;
		
		while(max != 0){
			if(CommandHandler.get().hasPermission(sender, "ban.tempban.maxHours."+max)) break;
			max--;
		}
		
		if(max == 0) return true;
		
		if(h > max) return false;
		else return true;
	}
	
	//Checking MySQL Bans
		@SuppressWarnings("static-access")
		public Boolean isBanned(String name){
			name = name.toLowerCase();
			MySQLHandler mysql = MySQLHandler.get();
			try {
				mysql.myStmtT = mysql.myConnT.prepareStatement("select * from AdvancedBans");
				mysql.myRs = mysql.myStmtT.executeQuery();
				mysql.myRs.beforeFirst();
				while(mysql.myRs.next()){
					if(mysql.myRs.getString("name").equals(name)){
						return true;
					}
				}
			}catch(SQLException e){  e.printStackTrace();  }
			return false;
		}
}
