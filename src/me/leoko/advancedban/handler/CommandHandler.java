package me.leoko.advancedban.handler;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.leoko.advancedban.Main;
import me.leoko.advancedban.listener.JoinListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class CommandHandler {
	private static CommandHandler instance;
	public static CommandHandler get(){
		if(instance == null) instance = new CommandHandler();
		return instance;
	}
	
	private String getMSG(String s, boolean b){
		return Main.get().getMSG(s, b);
	}
	
	@SuppressWarnings({ "deprecation", "static-access" })
	public boolean runCommand(String command, final Object sender){
		Boolean cancle = false;
		final String[] args = command.split(" ");
		
			if(!(args.length == 0)){
				final Main pl = Main.get();
				final MySQLHandler msh = MySQLHandler.get();
				final BanHandler bh = BanHandler.get();
				
				//
				// TODO COMMAND: Unban
				//
				if(args[0].equalsIgnoreCase("/unban")){
					cancle = true;
					if(hasPermission(sender,"ban.unban")){
					if(args.length == 2){
						String name = args[1];
						if(pl.isIP(name)){
							name = name.replaceAll("\\.", "-");
						}
						if(bh.remBan(name)){
							sendMessage(sender,getMSG("Unbanned", true).replaceAll("%PLAYER%", args[1]));
						}else{
							sendMessage(sender,getMSG("NotBanned", true).replaceAll("%PLAYER%", args[1]));
						}
					}else{
						sendMessage(sender,getMSG("UsageUnban", true));
					}
					}else{
						sendMessage(sender,getMSG("NoPerms", true));
					}
				}
				
				//
				// TODO COMMAND: Kick
				//
				if(args[0].equalsIgnoreCase("/kick")){
					cancle = true;
					if(hasPermission(sender,"ban.kick")){
						if(args.length > 1){
							if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
								if(!Bukkit.getPlayer(args[1]).hasPermission("kick.exempt")){
									String grund = null;
									if(args.length > 2){
										Integer i = 3;
										grund = args[2];
										while(i != (args.length)){
											grund = grund + " " + args[i];
											i++;
										}
									}
									sendMessage(sender, getMSG("Kicked", true).replaceAll("%PLAYER%", args[1]));
									if(grund != null){
										sendMessage(sender, getMSG("Reason", false) + grund);
									}
									for(Player op : Bukkit.getOnlinePlayers()){
										if(op.hasPermission("kick.notify") && !op.equals(sender)){
												op.sendMessage(getMSG("Kicked", true).replaceAll("%PLAYER%", args[1]));
											if(grund != null){
												op.sendMessage(getMSG("Reason", false) + grund);
											}
										}
									}
									final String nGrund = grund;
									Bukkit.getScheduler().runTask(pl, new Runnable() {
										@Override
										public void run() {
											BanHandler.get().execCommands("Kick", Bukkit.getPlayer(args[1]).getName());
											
											ByteArrayDataOutput out = ByteStreams.newDataOutput();
											out.writeUTF("KickPlayer");
											out.writeUTF(Bukkit.getPlayer(args[1]).getName());
											out.writeUTF(pl.getLayout(pl.KKL, (nGrund == null ? "You got kicked" : nGrund) + "#BannedBy#"+ getName(sender), null, null));
											Bukkit.getPlayer(args[1]).sendPluginMessage(pl, "BungeeCord", out.toByteArray());
											
											Bukkit.getPlayer(args[1]).kickPlayer(pl.getLayout(pl.KKL, (nGrund == null ? "You got kicked" : nGrund) + "#BannedBy#"+ getName(sender), null, null));
										}
									});
									pl.addHistoryEntry(args[1], grund, getName(sender) , "KICK");
								}else{
									sendMessage(sender,getMSG("KickExempt", true));
								}
							}else{
								sendMessage(sender,getMSG("NotOnline", true));
							}
						}else{
							sendMessage(sender,getMSG("UsageKick", true));
						}
					}else{
						sendMessage(sender,getMSG("NoPerms", true));
					}
				}
				
				//
				// TODO COMMAND: Tempban
				//
				if(args[0].equalsIgnoreCase("/tempban")){
					cancle = true;
					if(hasPermission(sender,"ban.tempban")){
					if(args.length > 3){
						//Exempt
						if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
							if(Bukkit.getPlayer(args[1]).hasPermission("ban.exempt")){
								sendMessage(sender,getMSG("BanExempt", true));
								return true;
							}
						}
						if(pl.isNumeric(args[2])){
							if(args[3].equalsIgnoreCase("S") || args[3].equalsIgnoreCase("M") || args[3].equalsIgnoreCase("H") || args[3].equalsIgnoreCase("D") || args[3].equalsIgnoreCase("W") || args[3].equalsIgnoreCase("Mo") || args[3].equalsIgnoreCase("Y")){
								
								if(!bh.canTempban(sender, Integer.parseInt(args[2]), args[3].toLowerCase())){
									sendMessage(sender,getMSG("NoPerms", true));
									return true;
								}
								
								final Date unbanAt = new Date();
								if(args[3].equalsIgnoreCase("S")){
									unbanAt.setSeconds(unbanAt.getSeconds() + Integer.parseInt(args[2]));
								}
								if(args[3].equalsIgnoreCase("H")){
									unbanAt.setHours(unbanAt.getHours() + Integer.parseInt(args[2]));
								}
								if(args[3].equalsIgnoreCase("M")){
									unbanAt.setMinutes(unbanAt.getMinutes() + Integer.parseInt(args[2]));
								}
								if(args[3].equalsIgnoreCase("D")){
									unbanAt.setDate(unbanAt.getDate() + Integer.parseInt(args[2]));
								}
								if(args[3].equalsIgnoreCase("W")){
									unbanAt.setDate(unbanAt.getDate() + Integer.parseInt(args[2])*7);
								}
								if(args[3].equalsIgnoreCase("Mo")){
									unbanAt.setDate(unbanAt.getDate() + Integer.parseInt(args[2])*30);
								}
								if(args[3].equalsIgnoreCase("Y")){
									unbanAt.setYear(unbanAt.getYear() + Integer.parseInt(args[2]));
								}
								
								String grund = null;
								if(args.length > 4){
									Integer i = 5;
									grund = args[4];
									while(i != (args.length)){
										grund = grund + " " + args[i];
										i++;
									}
								}
								
								SimpleDateFormat ft = new SimpleDateFormat ("dd.MM.yyyy-HH:mm:ss"); 
								
								if(bh.addBan(grund, ft.format(unbanAt), args[1], getName(sender))){
									sendMessage(sender,getMSG("Banned", true).replaceAll("%PLAYER%", args[1]));
									if(grund != null){
										sendMessage(sender,getMSG("Reason", false) + grund);
									}
									Integer mon = unbanAt.getMonth();
									String month = null;
									if(mon == 0){month = "Jan";}
									if(mon == 1){month = "Feb";}
									if(mon == 2){month = "Mar";}
									if(mon == 3){month = "Apr";}
									if(mon == 4){month = "May";}
									if(mon == 5){month = "June";}
									if(mon == 6){month = "July";}
									if(mon == 7){month = "Aug";}
									if(mon == 8){month = "Sept";}
									if(mon == 9){month = "Oct";}
									if(mon == 10){month = "Nov";}
									if(mon == 11){month = "Dec";}
									
									sendMessage(sender,getMSG("Until", false)+" §7" + unbanAt.getDate() + " " + month + ". at " + unbanAt.getHours() +":" + unbanAt.getMinutes() +":" + unbanAt.getSeconds());
									
									for(Player op : Bukkit.getOnlinePlayers()){
										if(op.hasPermission("ban.notify") && !op.equals(sender)){
											if(op != sender){
												op.sendMessage(getMSG("Banned", true).replaceAll("%PLAYER%", args[1]));
												if(grund != null){
													op.sendMessage(getMSG("Reason", false) + grund);
												}
												op.sendMessage(getMSG("Until", false) + unbanAt.getDate() + " " + month + ". at " + unbanAt.getHours() +":" + unbanAt.getMinutes() +":" + unbanAt.getSeconds());
											}
										}
									}
									if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
										final String date = unbanAt.getDate() + "." + month + ". at " + unbanAt.getHours() +":" + unbanAt.getMinutes() +":" + unbanAt.getSeconds();
										final String nGrund = grund;
										Bukkit.getScheduler().runTask(pl, new Runnable() {
											@Override
											public void run() {
												if(nGrund != null){
													Bukkit.getPlayer(args[1]).kickPlayer(pl.getLayout(pl.TTL, nGrund+"#BannedBy#"+ getName(sender), date, JoinListener.get().caltcBetween(unbanAt, new Date())));
												}else{
													Bukkit.getPlayer(args[1]).kickPlayer(pl.getLayout(pl.TTL, "NoReason#BannedBy#"+ getName(sender), date, JoinListener.get().caltcBetween(unbanAt, new Date())));
												}
											}
										});
									}
									pl.addHistoryEntry(args[1], grund, getName(sender) , unbanAt);
								}else{
									sendMessage(sender,getMSG("AlreadyBanned", true).replaceAll("%PLAYER%", args[1]));
								}
								
							}else{
								sendMessage(sender,getMSG("UsageTempBan", true));
							}
						}else{
							sendMessage(sender,getMSG("UsageTempBan", true));
						}
					}else{
						sendMessage(sender,getMSG("UsageTempBan", true));
					}
					}else{
						sendMessage(sender,getMSG("NoPerms", true));
					}
				}
				
				//
				// TODO COMMAND: Ban
				//
				if(args[0].equalsIgnoreCase("/ban")){
					cancle = true;
					if(hasPermission(sender,"ban.ban")){
					if(args.length > 1){
						//Exempt
						if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
							if(Bukkit.getPlayer(args[1]).hasPermission("ban.exempt")){
								sendMessage(sender,getMSG("BanExempt", true));
								return true;
							}
						}
							String grund = null;
							if(args.length > 2){
								Integer i = 3;
								grund = args[2];
								while(i != (args.length)){
									grund = grund + " " + args[i];
									i++;
								}
							}
							if(bh.addBan(grund, "never", args[1], getName(sender))){
								sendMessage(sender,getMSG("Banned", true).replaceAll("%PLAYER%", args[1]));
								if(grund != null){
									sendMessage(sender,getMSG("Reason", false) + grund);
								}
								
								for(Player op : Bukkit.getOnlinePlayers()){
									if(op.hasPermission("ban.notify") && !op.equals(sender)){
										if(op != sender){
											op.sendMessage(getMSG("Banned", true).replaceAll("%PLAYER%", args[1]));
											if(grund != null){
												op.sendMessage(getMSG("Reason", false) + grund);
											}
										}
									}
								}
								
								if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
									final String nGrund = grund;
									Bukkit.getScheduler().runTask(pl, new Runnable() {
										@Override
										public void run() {
											if(nGrund != null){
												Bukkit.getPlayer(args[1]).kickPlayer(pl.getLayout(pl.BBL, nGrund + "#BannedBy#"+ getName(sender), null, null));
											}else{
												Bukkit.getPlayer(args[1]).kickPlayer(pl.getLayout(pl.BBL, "NoReason#BannedBy#"+ getName(sender), null, null));
											}
										}
									});
								}
								pl.addHistoryEntry(args[1], grund, getName(sender) , "BAN");
							}else{
								sendMessage(sender,getMSG("AlreadyBanned", true).replaceAll("%PLAYER%", args[1]));
							}
					}else{
						sendMessage(sender,getMSG("UsageBan", true));
					}
				}else{
					sendMessage(sender,getMSG("NoPerms", true));
				}
				}
				
				//
				// TODO COMMAND: Banlist
				//
				if(args[0].equalsIgnoreCase("/banlist")){
					cancle = true;
					if(hasPermission(sender,"ban.banlist")){
						List<String> bl = new ArrayList<String>();
						List<String> toRem = new ArrayList<String>();
						
						if(pl.mysql){
							try {
								msh.myStmtT = msh.myConnT.prepareStatement("select * from AdvancedBans");
								msh.myRs = msh.myStmtT.executeQuery();
								msh.myRs.beforeFirst();
								while(msh.myRs.next()){
									if(!msh.myRs.getString("until").equalsIgnoreCase("never")){
										//SimpleDateFormat print = new SimpleDateFormat ("dd.MM. 'um' HH:mm:ss"); 
										SimpleDateFormat ft = new SimpleDateFormat ("dd.MM.yyyy-HH:mm:ss"); 
										Date unbanAt = null;
										try { unbanAt = ft.parse(msh.myRs.getString("until")); } catch (ParseException e1) { e1.printStackTrace(); }
										if(unbanAt.before(new Date())){
											toRem.add(msh.myRs.getString("name"));
										}else{
											bl.add(msh.myRs.getString("name")+"-=#=-"+msh.myRs.getString("reason")+"-=#=-"+msh.myRs.getString("until"));
										}
									}else{
										bl.add(msh.myRs.getString("name")+"-=#=-"+msh.myRs.getString("reason")+"-=#=-"+msh.myRs.getString("until"));
									}
								}
							}catch(SQLException e){  e.printStackTrace();  }
						}else{
							for(String s : pl.bans.getKeys(false)){
								if(!pl.bans.getString(s+".ends").equalsIgnoreCase("never")){
									//SimpleDateFormat print = new SimpleDateFormat ("dd.MM. 'um' HH:mm:ss"); 
									SimpleDateFormat ft = new SimpleDateFormat ("dd.MM.yyyy-HH:mm:ss"); 
									Date unbanAt = null;
									try { unbanAt = ft.parse(pl.bans.getString(s+".ends")); } catch (ParseException e1) { e1.printStackTrace(); }
									if(unbanAt.before(new Date())){
										toRem.add(s);
									}else{
										bl.add(s+"-=#=-"+pl.bans.getString(s+".reason")+"-=#=-"+pl.bans.getString(s+".ends"));
									}
								}else{
									bl.add(s+"-=#=-"+pl.bans.getString(s+".reason")+"-=#=-"+pl.bans.getString(s+".ends"));
								}
							}
						}
						
						for(String s : toRem){
							bh.remBan(s);
						}
						toRem.clear();
						
						for(String s : pl.conf.getStringList("BanListLayout.Header")) sendMessage(sender, s.replace('&', '§'));
						for(String s : bl){
							try{
								String name = "";
								if(pl.isChangedIP(s.split("-=#=-")[0])){
									name = s.split("-=#=-")[0].replaceAll("-", "\\.");
								}else{
									name = s.split("-=#=-")[0];
									if(UUIDHandler.get().getNameFromUUID(name) != null) name = UUIDHandler.get().getNameFromUUID(name);
								}
								for(String str : pl.conf.getStringList("BanListLayout.Entry")){
									sendMessage(sender, str.replaceAll("%NAME%", name)
														   .replaceAll("%UNTIL%", s.split("-=#=-")[2])
														   .replaceAll("%BY%", s.split("-=#=-")[1].split("#BannedBy#")[1])
														   .replaceAll("%REASON%", s.split("-=#=-")[1].split("#BannedBy#")[0])
														   .replace('&', '§'));
								}
							}catch(IndexOutOfBoundsException exc){}
						}
						sendMessage(sender, pl.conf.getString("BanListLayout.Footer").replaceAll("%NO%", bl.size()+"").replace('&', '§'));
					}else{
						sendMessage(sender,getMSG("NoPerms", true));
					}
				}
				
				//
				// TODO COMMAND: ban-ip
				//
				if(args[0].equalsIgnoreCase("/ban-ip") || args[0].equalsIgnoreCase("/banip")){
					cancle = true;
					if(hasPermission(sender,"ban.ipban")){
					if(args.length > 1){
						//Exempt
						if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
							if(Bukkit.getPlayer(args[1]).hasPermission("ban.exempt")){
								sendMessage(sender,getMSG("BanExempt", true));
								return true;
							}
						}
							String grund = null;
							if(args.length > 2){
								Integer i = 3;
								grund = args[2];
								while(i != (args.length)){
									grund = grund + " " + args[i];
									i++;
								}
							}
							
							if(!pl.isIP(args[1]) && !pl.iPs.containsKey(args[1].toLowerCase())){
								sendMessage(sender,getMSG("NotOnline", true));
								return true;
							}
							
							String player = args[1];
							
							if(pl.iPs.containsKey(args[1].toLowerCase())){
								player = pl.iPs.get(player.toLowerCase()).replaceAll("\\.", "-");;
							}else if(pl.isIP(args[1])){
								player = player.replaceAll("\\.", "-");
							}else{
								player = "IP_NOT_FOUND";
							}
							
							if(bh.addBan(grund, "never", player, getName(sender))){
								sendMessage(sender,getMSG("Banned", true).replaceAll("%PLAYER%", args[1]));
								if(grund != null){
									sendMessage(sender,getMSG("Reason", false) + grund);
								}
								
								for(Player op : Bukkit.getOnlinePlayers()){
									if(op.hasPermission("ban.notify")){
										if(op != sender){
											op.sendMessage(getMSG("Banned", true).replaceAll("%PLAYER%", args[1]));
											if(grund != null){
												op.sendMessage(getMSG("Reason", false) + grund);
											}
										}
									}
								}
								if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
									final String nGrund = grund;
									Bukkit.getScheduler().runTask(pl, new Runnable() {
										@Override
										public void run() {
											if(nGrund != null){
												Bukkit.getPlayer(args[1]).kickPlayer(pl.getLayout(pl.BBL, nGrund + "#BannedBy#"+ getName(sender), null, null));
											}else{
												Bukkit.getPlayer(args[1]).kickPlayer(pl.getLayout(pl.BBL, "NoReason#BannedBy#"+ getName(sender), null, null));
											}
										}
									});
								}
							}else{
								sendMessage(sender,getMSG("AlreadyBanned", true).replaceAll("%PLAYER%", args[1]));
							}
					}else{
						sendMessage(sender,getMSG("UsageIpBan", true));
					}
				}else{
					sendMessage(sender,getMSG("NoPerms", true));
				}
				}
				
				//
				// TODO COMMAND: history
				//
				if(args[0].equalsIgnoreCase("/history")){
					cancle = true;
					if(args.length == 1 || args.length == 2){
						sendMessage(sender, getMSG("UsagePlayerHistory", true));
					}else if(args[1].equalsIgnoreCase("get")){
						if(hasPermission(sender, "ban.history.get")){
							List<String> curr = new ArrayList<String>();
							String uuid =UUIDHandler.get().getUUID(args[2]);
							if(pl.mysql){
								try {
									msh.myRsPH = msh.myConnT.prepareStatement("select * from PlayerHistory").executeQuery();
									msh.myRsPH.beforeFirst();
									while(msh.myRsPH.next()){
										if(msh.myRsPH.getString("uuid").equalsIgnoreCase(uuid)){
											curr.add(uuid+" #SPLIT# "+msh.myRsPH.getString("name")
														+" #SPLIT# "+msh.myRsPH.getString("reason")
														+" #SPLIT# "+msh.myRsPH.getString("by")
														+" #SPLIT# "+msh.myRsPH.getString("start")
														+" #SPLIT# "+msh.myRsPH.getString("end"));
										}
									}
								} catch (SQLException e) { e.printStackTrace(); }
							}else{
								if(pl.ph.contains(uuid)){
									curr = pl.ph.getStringList(uuid);
								}
							}
							
							if(curr.isEmpty()){
								sendMessage(sender, getMSG("NoPlayerHistory", true));
							}else{
								sendMessage(sender, "§8");
								sendMessage(sender, "§8");
								sendMessage(sender, getMSG("PlayerHistory", true));
								for(String s : curr){
									String[] sl = s.split("#SPLIT#");
									String type = "";
									try{
										SimpleDateFormat ft = new SimpleDateFormat ("dd.MM.yyyy-HH:mm:ss"); 
										final Long l = (ft.parse(sl[5]).getTime()) - (ft.parse(sl[4]).getTime());
										if(TimeUnit.MILLISECONDS.toHours(l) > 0) type = "TEMP-BAN ["+(TimeUnit.MILLISECONDS.toHours(l))+" h]";
										else type = "TEMP-BAN ["+(TimeUnit.MILLISECONDS.toMinutes(l))+" min]";
									}catch(Exception exc){type = sl[5];}
									sendMessage(sender, "§7");
									for(String str : pl.conf.getStringList("HistoryLayout")){
										sendMessage(sender, str.replaceAll("%NAME%", sl[1])
																.replaceAll("%DATE%", sl[4])
																.replaceAll("%BY%", sl[3])
																.replaceAll("%TYPE%", type)
																.replaceAll("%REASON%", sl[2]).replace('&', '§'));
									}
									sendMessage(sender, "§7");
								}
								sendMessage(sender, "§8");
							}
						}else{
							sendMessage(sender, getMSG("NoPerms", true));
						}
					}else if(args[1].equalsIgnoreCase("clear")){
						if(hasPermission(sender, "ban.history.clear")){
							String uuid = UUIDHandler.get().getUUID(args[2]);
							if(pl.mysql){
								try { msh.myConnT.prepareStatement("DELETE FROM PlayerHistory WHERE uuid = '"+uuid+"'").execute();
								} catch (SQLException e) { e.printStackTrace(); }
							}else{
								pl.ph.set(uuid, null);
								try { pl.ph.save(pl.playerHistoryFile); } catch (IOException e) { e.printStackTrace(); }
							}
							sendMessage(sender, getMSG("RemovedPlayerHistory", true).replaceAll("%PLAYER%", args[2]));
						}else{
							sendMessage(sender, getMSG("NoPerms", true));
						}
					}else{
						sendMessage(sender, getMSG("UsagePlayerHistory", true));
					}
				}
				
				//
				// TODO COMMAND: check
				//
				if(args[0].equalsIgnoreCase("/check")){
					cancle = true;
					if(hasPermission(sender,"ban.check")){
						if(args.length == 1){
							sendMessage(sender,getMSG("UsageCheck", true));
						}else{
							Boolean banned = false;	
							String name = args[1];
							
							if(pl.mysql){
								if(bh.isBanned(args[1].toLowerCase())){
									banned = true;
									name = args[1].toLowerCase();
								}
								else if(UUIDHandler.get().getUUID(name) != null){
									if(bh.isBanned(UUIDHandler.get().getUUID(name))){
										banned = true;
										name = UUIDHandler.get().getUUID(name);
									}
								}
							}else{
								if(pl.bans.contains(args[1].toLowerCase())){
									banned = true;
									name = args[1].toLowerCase();
								}
								else if(UUIDHandler.get().getUUID(name) != null){
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
										msh.myRs = msh.myStmtT.executeQuery();
										msh.myRs.beforeFirst();
										while(msh.myRs.next()){
											if(msh.myRs.getString("name").equals(name)){
												reason = msh.myRs.getString("reason");
												ending = msh.myRs.getString("until");
											}
										}
									}catch(SQLException ex){  ex.printStackTrace();  }
								}else{
									reason = pl.bans.getString(name + ".reason");
									ending = pl.bans.getString(name + ".ends");
								}
								if(ending.equalsIgnoreCase("never")){
									if(!reason.split("#BannedBy#")[0].equalsIgnoreCase("NoReason")){
										sendMessage(sender,getMSG("IsBanned", true).replaceAll("%PLAYER%", args[1]));
										sendMessage(sender,getMSG("EndingIn", false)+"never");
										sendMessage(sender,getMSG("Reason", false)+reason.split("#BannedBy#")[0]);
										sendMessage(sender,getMSG("BannedBy", false)+reason.split("#BannedBy#")[1]);
									}else{
										sendMessage(sender,getMSG("IsBanned", true).replaceAll("%PLAYER%", args[1]));
										sendMessage(sender,getMSG("EndingIn", false)+"never");
										sendMessage(sender,getMSG("Reason", false)+"none");
										sendMessage(sender,getMSG("BannedBy", false)+reason.split("#BannedBy#")[1]);
									}
								}else{
									SimpleDateFormat ft = new SimpleDateFormat ("dd.MM.yyyy-HH:mm:ss"); 
									Date unbanAt = null;
									try { unbanAt = ft.parse(ending); } catch (ParseException e1) { e1.printStackTrace(); }
									if(unbanAt.before(new Date())){
										bh.remBan(name);
										sendMessage(sender,"§aPlayer "+args[1]+" is not banned");
									}else{
										if(!reason.split("#BannedBy#")[0].equalsIgnoreCase("NoReason")){
											sendMessage(sender,getMSG("IsBanned", true).replaceAll("%PLAYER%", args[1]));
											sendMessage(sender,getMSG("EndingIn", false)+JoinListener.get().caltcBetween(unbanAt, new Date()));
											sendMessage(sender,getMSG("Reason", false)+reason.split("#BannedBy#")[0]);
											sendMessage(sender,getMSG("BannedBy", false)+reason.split("#BannedBy#")[1]);
										}else{
											sendMessage(sender,getMSG("IsBanned", true).replaceAll("%PLAYER%", args[1]));
											sendMessage(sender,getMSG("EndingIn", false)+JoinListener.get().caltcBetween(unbanAt, new Date()));
											sendMessage(sender,getMSG("Reason", false)+"none");
											sendMessage(sender,getMSG("BannedBy", false)+reason.split("#BannedBy#")[1]);
										}
									}
								}
							}else{
								sendMessage(sender,getMSG("NotBanned", true).replaceAll("%PLAYER%", args[1]));
							}
						}
					}else{
						sendMessage(sender,getMSG("NoPerms", true));
					}
				}
				
			}
			return cancle;
	}
	
	public void sendMessage(Object target,String msg){
		if(target instanceof Player){
			((Player) target).sendMessage(msg);
		}else{
			System.out.println(ChatColor.stripColor(msg));
		}
	}
	
	public String getName(Object target){
		if(target instanceof Player){
			return ((Player) target).getName();
		}else{
			return "Console";
		}
	}
	
	public boolean hasPermission(Object target,String perm){
		if(target instanceof Player){
			if(((Player) target).hasPermission(perm)){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
}
