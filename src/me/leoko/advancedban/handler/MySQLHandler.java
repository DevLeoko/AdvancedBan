package me.leoko.advancedban.handler;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.leoko.advancedban.Main;

import org.bukkit.Bukkit;

public class MySQLHandler {
	
	public static Connection myConnT;
	public static PreparedStatement myStmtT;
	public static ResultSet myRs;
	public static PreparedStatement myStmtPH;
	public static ResultSet myRsPH;
	
	private static MySQLHandler instance;
	public static MySQLHandler get(){
		if(instance == null) instance = new MySQLHandler();
		return instance;
	}
	
	@SuppressWarnings("deprecation")
	public void setup(){
		try{
			myConnT = DriverManager.getConnection("jdbc:mysql://" + Main.get().conf.getString("MySQL.IP") + ":" + Main.get().conf.getString("MySQL.Port") + "/" + Main.get().conf.getString("MySQL.DB-Name"), Main.get().conf.getString("MySQL.Username"), Main.get().conf.getString("MySQL.Password"));
			DatabaseMetaData md = myConnT.getMetaData();
			ResultSet rs = md.getTables(null, null, "AdvancedBans", null);
			ResultSet rs2 = md.getTables(null, null, "PlayerHistory", null);
			
			if(!rs.next()){
				Statement Stmt = myConnT.createStatement();
			      
				String sql = "CREATE TABLE `AdvancedBans` ("  +
							  "`name` TEXT NULL DEFAULT NULL,"  +
							  "`reason` TEXT NULL DEFAULT NULL,"+
							  "`until` TEXT NULL DEFAULT NULL)"; 
				Stmt.executeUpdate(sql);
			}
			if(!rs2.next()){
				Statement Stmt = myConnT.createStatement();
				String sql = "CREATE TABLE `PlayerHistory` ("  +
						  "`uuid` TEXT NULL DEFAULT NULL,"  +
						  "`name` TEXT NULL DEFAULT NULL,"  +
						  "`reason` TEXT NULL DEFAULT NULL,"+
						  "`by` TEXT NULL DEFAULT NULL,"+
						  "`start` TEXT NULL DEFAULT NULL,"+
						  "`end` TEXT NULL DEFAULT NULL)"; 
				Stmt.executeUpdate(sql);
			}
			myStmtT = myConnT.prepareStatement("select * from AdvancedBans");
			myRs = myStmtT.executeQuery();
			myStmtPH = myConnT.prepareStatement("select * from PlayerHistory");
			myRs = myStmtPH.executeQuery();
		}catch(Exception exc){
			Main.get().mysql = false;
		exc.printStackTrace();}
		
		try{
			Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.get(), new Runnable(){
				@Override
				public void run() {
					try {
						myConnT.close();
						myConnT = DriverManager.getConnection("jdbc:mysql://" + Main.get().conf.getString("MySQL.IP") + ":" + Main.get().conf.getString("MySQL.Port") + "/" + Main.get().conf.getString("MySQL.DB-Name"), Main.get().conf.getString("MySQL.Username"), Main.get().conf.getString("MySQL.Password"));
						myStmtT = myConnT.prepareStatement("select * from AdvancedBans");
						myStmtPH = myConnT.prepareStatement("select * from PlayerHistory");
						myRs = myStmtT.executeQuery();
						myRsPH = myStmtPH.executeQuery();
					} catch (SQLException e) {
						Main.get().mysql = false;
					e.printStackTrace();}
				}
			}, 20*60*60, 20*60*60);
		}catch(Exception exc){
		exc.printStackTrace();}
	}
}
