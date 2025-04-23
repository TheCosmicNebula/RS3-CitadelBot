package com.tcn.citadelbot.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.json.JSONArray;
import org.json.JSONObject;

public class Settings {

	public enum S {
		SERVER_ID(0, 0, "server_id", String.class),
		ADMIN_ROLE(1, 0, "admin_role_id", String.class),
		PING_ROLE(2, 0, "ping_role_id", String.class),
		BOT_MEMBER_ID(3, 0, "bot_member_id", String.class),
		
		COMMANDS_ID(4, 1, "commands_id", String.class),
		ANNOUNCE_ID(5, 1, "announce_id", String.class),
		
		PREFIX(6, 2, "command_prefix", String.class),
		
		CITADEL_RESET_DAY(7, 3, "citadel_reset_day", String.class),
		CITADEL_RESET_TIME(8, 3, "citadel_reset_time", String.class),
		
		ACTIVITY_TYPE(9, 4, "activity_type", Integer.class),
		ACTIVITY_DESC(10, 4, "activity_desc", String.class),
		
		TIMER_INTERVAL(11, 5, "timer_interval_mins", Integer.class),
		
		CLAN_WORLD(12, 6, "clan_world", Integer.class),
		
		WEEK_CAPPED(13, 7, "week_capped", Boolean.class),
		LOCKED(14, 7, "locked", Boolean.class),
		GOAL_COMPLETE(15, 7, "goal_complete", Boolean.class),
		CURRENT_GOAL(16, 7, "current_goal", Integer.class),
		CURRENT_CAPPERS(17, 7, "current_cappers", Integer.class),
		PREVIOUS_GOAL(18, 7, "previous_goal", Integer.class),
		PREVIOUS_CAPPERS(19, 7, "previous_cappers", Integer.class),
		OPEN(22, 7, "open", Boolean.class),
		INCENTIVE(23, 7, "incentive", String.class),
		
		RESOURCE_EMOJI(20, 8, "resource_emoji", Boolean.class),
		
		BOT_OWNER(21, 9, "bot_owner", String.class);
				
		int index;
		int id;
		String ident;
		Class<?> objectType;
		
		S(int indexIn, int idIn, String identIn, Class<?> objectTypeIn) {
			index = indexIn;
			id = idIn;
			ident = identIn;
			objectType = objectTypeIn;
		}
		
		public int getIndex() {
			return index;
		}
		
		public int getId() {
			return id;
		}
		
		public String getIdent() {
			return ident;
		}

		public Class<?> getClazz() {
			return objectType;
		}
	}

	public static String SERVER_ID = "";
	public static String ADMIN_ROLE = "";
	public static String PING_ROLE = "";
	public static String BOT_MEMBER_ID = "";
	
	public static String ANNOUNCE_ID = "";
	public static String COMMANDS_ID = "";
	
	public static String PREFIX = "";
	
	public static String CITADEL_RESET_DAY = "monday";
	public static String CITADEL_RESET_TIME = "00:00";

	public static int ACTIVITY_TYPE = 0;
	public static String ACTIVITY_DESC = "";
	
	public static int TIMER_INTERVAL = 5;
	
	public static int CLAN_WORLD = 0;
	
	public static boolean WEEK_CAPPED = false;
	public static boolean LOCKED = false;
	public static boolean GOAL_COMPLETE = false;
	public static int CURRENT_GOAL = 0;
	public static int CURRENT_CAPPERS = 0;
	public static int PREVIOUS_GOAL = 0;
	public static int PREVIOUS_CAPPERS = 0;
	public static boolean OPEN = false;
	public static String INCENTIVE = "";
	
	public static boolean RESOURCE_EMOJI = false;
	
	public static String BOT_OWNER = "";

	public static JSONArray ARRAY = new JSONArray();
	public static JSONArray RESOURCES = new JSONArray();
	
	public static void initiateSettings() {
		loadSettings();
		loadResources();

		SERVER_ID = (String) get(S.SERVER_ID);
		ADMIN_ROLE = (String) get(S.ADMIN_ROLE);
		PING_ROLE = (String) get(S.PING_ROLE);
		BOT_MEMBER_ID = (String) get(S.BOT_MEMBER_ID);
		
		COMMANDS_ID = (String) get(S.COMMANDS_ID);
		ANNOUNCE_ID = (String) get(S.ANNOUNCE_ID);
		
		PREFIX = (String) get(S.PREFIX);
		
		CITADEL_RESET_DAY = (String) get(S.CITADEL_RESET_DAY);
		CITADEL_RESET_TIME = (String) get(S.CITADEL_RESET_TIME);
		
		ACTIVITY_TYPE = (int) get(S.ACTIVITY_TYPE);
		ACTIVITY_DESC = (String) get(S.ACTIVITY_DESC);
		
		TIMER_INTERVAL = (int) get(S.TIMER_INTERVAL);
		
		CLAN_WORLD = (int) get(S.CLAN_WORLD);
		
		WEEK_CAPPED = (boolean) get(S.WEEK_CAPPED);
		LOCKED = (boolean) get(S.LOCKED);
		GOAL_COMPLETE = (boolean) get(S.GOAL_COMPLETE);
		CURRENT_GOAL = (int) get(S.CURRENT_GOAL);
		PREVIOUS_CAPPERS = (int) get(S.PREVIOUS_CAPPERS);
		OPEN = (boolean) get(S.OPEN);
		INCENTIVE = (String) get(S.INCENTIVE);
		
		RESOURCE_EMOJI = (Boolean) get(S.RESOURCE_EMOJI);
		
		BOT_OWNER = (String) get(S.BOT_OWNER);
	}

	public static Object get(S setting) {
		return Settings.ARRAY.getJSONObject(setting.getId()).get(setting.getIdent());
	}
	
	public static void set(S setting, Object object) {		
		if (object.getClass() == setting.getClazz()) {
			switch (setting.index) {
				case 0:
					SERVER_ID = (String) object;
					break;
				case 1:
					ADMIN_ROLE = (String) object;
					break;
					
				case 2:
					PING_ROLE = (String) object;
					break;
					
				
				case 4:
					COMMANDS_ID = (String) object;
					break;
					
				case 5:
					ANNOUNCE_ID = (String) object;
					break;
					
				case 6:
					PREFIX = (String) object;
					break;
					
				case 7:
					CITADEL_RESET_DAY = (String) object;
					break;
					
				case 8:
					CITADEL_RESET_TIME = (String) object;
					break;
					
			
				case 9:
					ACTIVITY_TYPE = (int) object;
					break;
					
				case 10:
					ACTIVITY_DESC = (String) object;
					break;
					
				
				case 11:
					TIMER_INTERVAL = (int) object;
					break;
					
				
				case 12:
					CLAN_WORLD = (int) object;
					break;
					
				
				case 13:
					WEEK_CAPPED = (Boolean) object;
					break;
					
				case 14:
					LOCKED = (Boolean) object;
					break;
					
				case 15:
					GOAL_COMPLETE = (Boolean) object;
					break;
					
				case 16:
					CURRENT_GOAL = (int) object;
					break;
					
				case 17:
					CURRENT_CAPPERS = (int) object;
					break;
					
				case 18:
					PREVIOUS_GOAL = (int) object;
					break;
					
				case 19:
					PREVIOUS_CAPPERS = (int) object;
					break;
					
					
				case 20:
					RESOURCE_EMOJI = (Boolean) object;
					break;
					
				case 22:
					OPEN = (Boolean) object;
					break;
					
				case 23:
					INCENTIVE = (String) object;
					break;
					
				default:
					break;
			}

			ARRAY.getJSONObject(setting.getId()).put(setting.getIdent(), object);
		}
		
		saveSettings();
	}
	
	public static void saveSettings() {
		File users = Reference.SETTINGS;
		JSONArray a = ARRAY;
		String jsonstr = a.toString(2);
		try {
			writeFile(jsonstr, users, StandardCharsets.UTF_8);
		} catch (IOException e) { }
	}

	public static void loadSettings() {
		File users = Reference.SETTINGS;
		String usersInfo = null;
		try {
			usersInfo = readFile(users, StandardCharsets.UTF_8);
		} catch (IOException e) { }
		
		ARRAY = new JSONArray();
		
		JSONArray a = new JSONArray(usersInfo);
		for (int i = 0; i < a.length(); i++) {
			JSONObject item = a.getJSONObject(i);

			ARRAY.put(item);
		}
	}

	public static void saveResources() {
		File users = Reference.RESOURCES;
		JSONArray a = RESOURCES;
		String jsonstr = a.toString(2);
		try {
			writeFile(jsonstr, users, StandardCharsets.UTF_8);
		} catch (IOException e) { }
	}

	public static void loadResources() {
		File users = Reference.RESOURCES;
		String usersInfo = null;
		try {
			usersInfo = readFile(users, StandardCharsets.UTF_8);
		} catch (IOException e) { }
		
		RESOURCES = new JSONArray();
		
		JSONArray a = new JSONArray(usersInfo);
		for (int i = 0; i < a.length(); i++) {
			JSONObject item = a.getJSONObject(i);

			RESOURCES.put(item);
		}
	}

	public static String readFile(File path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(path.toPath());
		return new String(encoded, encoding);
	}

	public static void writeFile(String content, File path, Charset encoding) throws IOException {
		Files.write(path.toPath(), content.getBytes(encoding));
	}

	public static String loadToken() throws IOException {
		return readFile(Reference.token, StandardCharsets.UTF_8);
	}
}