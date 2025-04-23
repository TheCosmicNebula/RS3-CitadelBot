package com.tcn.citadelbot.core;

import java.io.File;

public class Reference {
	
	public static final File token = new File("token.txt");
	public static final File SETTINGS = new File("settings.json");
	public static final File RESOURCES = new File("resources.json");
	public static final File USERS = new File("users.json");
	
	public static final String VERSION = "6.8";
	public static final String CHANNEL = "release";

	public class MSG {
		public static final String CAPPED = "You have capped this week on this account.";
		public static final String NOT_CAPPED = "You have not capped this week on this account.";
		
		public static final String NOT_REGISTERED = "`You have not yet registered to use the Citadel Service.`";
		public static final String REGISTERED = "`You have now registered to use the Citadel Service.";
		public static final String DE_REGISTERED = "`You have successfully de-registered from the Citadel Service.`";
		
		public static final String EMBED_FOOTER = "This message brought to you by the RS3 CitadelBot (v" + VERSION + "), made by TheCosmicNebula_";
	}
	
	public class JSON {
		public class R {
			public static final String ID = "id";
			public static final String NAME = "name";
			public static final String ACTIVE = "active";
			public static final String EMOJI_ID = "emoji_id";
			public static final String CAP = "max_cap";
			
			public static final String GOAL = "goal";
		}
	}
}
