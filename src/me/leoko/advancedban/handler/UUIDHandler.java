package me.leoko.advancedban.handler;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import me.leoko.advancedban.Main;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class UUIDHandler {
	private Map<String, String> activeUUIDs = new HashMap<String, String>();
	
	private static UUIDHandler instance;
	public static UUIDHandler get(){
		if(instance == null) instance = new UUIDHandler();
		return instance;
	}
	
	public String getInitialUUID(String name){
		if(!Main.get().useUUID) return name;
		try{
	    URL url = new URL("https://api.mojang.com/users/profiles/minecraft/"+name+"?at="+new Date().getTime());
	    HttpURLConnection request = (HttpURLConnection) url.openConnection();
	    request.connect();
	    /*
	    JsonParser jp = new JsonParser(); 
	    JsonElement je = jp.parse(new InputStreamReader((InputStream) request.getContent()));
	    JsonObject json = je.getAsJsonObject(); 
	    return json.get("id").toString().substring(1, json.get("id").toString().length()-1);*/
	    
	    JSONParser jp = new JSONParser(); 
	    JSONObject json = (JSONObject) jp.parse(new InputStreamReader(request.getInputStream()));
	    String uuid = json.get("id").toString();
	    if(uuid != null){
	    	if(activeUUIDs.containsKey(name)) activeUUIDs.remove(name);
		    activeUUIDs.put(name, json.get("id").toString());
	    }
	    return uuid;
	    
		}catch(Exception exc){
			System.out.println("\n \n[AdvancedBan] Failed while getting player's UUID \n Either the Mojang-Servers ran offline or this player does not exist\n \n ");
			return null;
		}
	}
	
	public String getUUID(String name){
		if(!Main.get().useUUID) return name;
		if(activeUUIDs.containsKey(name)) return activeUUIDs.get(name);
		return getInitialUUID(name);
	}
	
	@SuppressWarnings("resource")
	public String getNameFromUUID(String uuid){
		if(!Main.get().useUUID) return uuid;
		try {
			String s = new Scanner(new URL("https://api.mojang.com/user/profiles/"+uuid+"/names").openStream(), "UTF-8").useDelimiter("\\A").next();
			s=s.substring(s.lastIndexOf('{'), s.lastIndexOf('}')+1);
			return ((JSONObject) new JSONParser().parse(s)).get("name").toString();
		} catch (Exception exc) {
			// exc.printStackTrace();
			return null;
		}
	}
}
