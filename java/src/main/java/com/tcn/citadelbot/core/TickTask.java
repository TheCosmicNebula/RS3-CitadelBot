package com.tcn.citadelbot.core;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.json.JSONObject;

import com.tcn.citadelbot.core.Settings.S;
import com.tcn.citadelbot.helper.JavaHelper;
import com.tcn.citadelbot.helper.JavaHelper.LVL;

class TickTask extends TimerTask {
	
	private String day;
	private int hour;
	private int minute;
	private Server server;
	
	public TickTask(String dayIn, int hourIn, int minuteIn, Server serverIn) {
		this.day = dayIn;
		this.hour = hourIn;
		this.minute = minuteIn;
		
		this.server = serverIn;
	}

	@Override
	public void run() {
		JavaHelper.sendSystemMessage(LVL.DEBUG, "<citadel-tick> tick");
		
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		String currentDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime()).toLowerCase();
		
		int currentHour = Integer.parseInt(JavaHelper.getTime(1, 0, false));
		int currentMinute = Integer.parseInt(JavaHelper.getTime(2, 0, false));

		if (currentMinute >= 30 - Settings.TIMER_INTERVAL && currentMinute <= 30 + Settings.TIMER_INTERVAL) {
			JavaHelper.sendSystemMessage(LVL.INFO, "Half hour tick notification");
		}
		
		if (!Settings.GOAL_COMPLETE) {
			if (Settings.CURRENT_GOAL > 0) {
				if (Settings.CURRENT_CAPPERS >= Settings.CURRENT_GOAL) {
					CitadelBot.setGoalState(true);
					announceGoal(server);
				}
			}
		}
		
		if (currentDay.equals(this.day.toLowerCase())) {
			if (!Settings.WEEK_CAPPED) {
				if (currentHour == this.hour) {
					if (currentMinute >= this.minute && currentMinute <= this.minute + (Settings.TIMER_INTERVAL * 2)) {
						CitadelBot.resetTickState(server);
						announceTick(server);
					}
				}
			}
		} else {
			if (Settings.WEEK_CAPPED) {
				Settings.set(S.WEEK_CAPPED, false);
			}
		}
	}
	
	public static void announceGoal(Server serverIn) {
		ServerTextChannel announcements = serverIn.getChannelById(Settings.ANNOUNCE_ID).get().asServerTextChannel().get();

		MessageBuilder message = new MessageBuilder();
		
		EmbedBuilder embed = new EmbedBuilder();
		
		embed.setAuthor(serverIn.getMemberById(Settings.BOT_MEMBER_ID).get());
		embed.setColor(Color.GREEN);
		embed.setTitle("Cap Goal Reached :tada:");
		embed.setDescription("Excellent work, the capping goal for the week has been reached!");
		
		embed.addField(":goal: ・ Goal", "> The goal this week was `" + Settings.CURRENT_GOAL + "` players.");
		
		embed.setThumbnail(CitadelBot.ICON);
		embed.setFooter(Reference.MSG.EMBED_FOOTER);
		embed.setTimestampToNow();
		
		message.setContent(serverIn.getRoleById(Settings.PING_ROLE).get().getMentionTag() + " - Goal Reached!");
		message.setEmbed(embed);
		message.send(announcements);
		JavaHelper.sendSystemMessage(LVL.INFO, "Goal met announcement sent");
	}
	
	public static void announceTick(Server serverIn) {
		ServerTextChannel announcements = serverIn.getChannelById(Settings.ANNOUNCE_ID).get().asServerTextChannel().get();
		
		try {
			announcements.deleteMessages(announcements.getMessages(1000).get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		MessageBuilder message = new MessageBuilder();
		
		EmbedBuilder embed = new EmbedBuilder();
		
		String readable = Settings.CITADEL_RESET_DAY;
		String built = readable.substring(0, 1).toUpperCase() + readable.substring(1);
		
		embed.setAuthor(serverIn.getMemberById(Settings.BOT_MEMBER_ID).get());
		embed.setUrl("https://runescape.wiki/w/Clan_Citadel");
		embed.setColor(Color.WHITE);
		embed.setTitle("The Clan Citadel has reset!");
		embed.setDescription("The Citadel has been reset for the week, so please come and cap!");
		
		embed.addField(":earth_americas: ・ Clan World:", "> Don't forget, the current clan world is `" + Settings.CLAN_WORLD + "`.");
		embed.addField(":coin: ・ Benefits:", "> Capping at the citadel has many benefits, including **`Bonus XP`**, a **`6% XP Boost`** and **`XP from your Clan Cloak's fealty reward`**. As a Clan we are also trying to achieve a `Tier 7` Clan, so everyone that caps aids in that goal!");
		embed.addField(":date: ・ Current Schedule:", "> Currently the Citadel resets every **`" + built + "`** at **`" + Settings.CITADEL_RESET_TIME + "`**.");
		
		if (Settings.CURRENT_GOAL > 0) {
			embed.addField(":goal: ・ Capping Goal:", "> The goal for this week is `" + Settings.CURRENT_GOAL + "` accounts.");
		}
		
		String resourcesAvailable = "";
		
		for (int i = 0; i < Settings.RESOURCES.length(); i++) {
			JSONObject resource = Settings.RESOURCES.getJSONObject(i);
			
			String resourceName = resource.getString("name").substring(0, 1).toUpperCase() + resource.getString("name").substring(1);			
			String resourceEmoji = resource.getString("emoji_id");
			int resourceAmount = resource.getInt("max_cap");
			boolean available = resource.getBoolean("active");
			
			if (available) {
				resourcesAvailable += " > " + (Settings.RESOURCE_EMOJI ? "<:" + resourceName.toLowerCase() + ":" + resourceEmoji + ">" : "") + " ・ `" + resourceName + " (" + resourceAmount + ")`\n";
			}
		}
		
		embed.addField(":books: ・ Resources Available:", resourcesAvailable);
		
		if (Settings.PREVIOUS_CAPPERS > 0) {
			embed.addField(":arrow_backward: ・ Previous Performance:", "> Last week `" + Settings.PREVIOUS_CAPPERS + "` accounts capped, thank you!");
		}
		
		if (!Settings.INCENTIVE.isBlank()){
			embed.addField(":dollar: ・ Incentive:", "> There is an active incentive! \n > `" + Settings.INCENTIVE + "`.");
		}
		
		embed.addField(":gear: ・ Commands:", "> Please visit the <#" + Settings.COMMANDS_ID + "> channel to register and inform the bot that you have capped.");
		
		embed.setThumbnail(CitadelBot.ICON);
		embed.setFooter(Reference.MSG.EMBED_FOOTER);
		embed.setTimestampToNow();
		
		message.setContent(serverIn.getRoleById(Settings.PING_ROLE).get().getMentionTag() + " - The Clan Citadel has reset for the week! Come and cap!");
		message.setEmbed(embed);
		message.send(announcements);
		JavaHelper.sendSystemMessage(LVL.INFO, "Citadel Reset Announcement sent.");
	}
}