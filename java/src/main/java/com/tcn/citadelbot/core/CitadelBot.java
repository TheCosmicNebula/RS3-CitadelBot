package com.tcn.citadelbot.core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONArray;
import org.json.JSONObject;

import com.tcn.citadelbot.core.Settings.S;
import com.tcn.citadelbot.helper.DiscordHelper;
import com.tcn.citadelbot.helper.JavaHelper;
import com.tcn.citadelbot.helper.JavaHelper.LVL;

@SuppressWarnings("unused")
public class CitadelBot {

	static final String USER_DISCORD_NAME_JSON = "discord_name";
	static final String USER_DISCORD_ID_JSON = "user_id";
	static final String USER_ACCOUNTS_JSON = "accounts";
	
	static final String USER_CAPPED_TIMES_JSON = "times_capped";
	static final String USER_CAPPED_WEEK_JSON = "capped_week";
	static final String USER_WEEKS_SINCE_LAST_CAPPED_JSON = "weeks_since_last_capped";

	static final String ACCOUNT_NAME_JSON = "account_name";
	static final String ACCOUNT_CAPPED_JSON = "capped";
	static final String ACCOUNT_TIMES_CAPPED_JSON = "times_capped";

	static JSONArray USER_ARRAY = new JSONArray();
	
	static ArrayList<Timer> timers = new ArrayList<Timer>();
	
	static File ICON = new File("");
	
	public static void main(String[] args) throws IOException {
		JavaHelper.setupLogger("CitadelBot");
		
		ICON = new File("icon.png");
		
		loadSystem();
		Settings.initiateSettings();
		
		JavaHelper.sendSystemMessage(LVL.INFO, "Beginning System Load");
		JavaHelper.sendSystemMessage(LVL.INFO, "System version [" + Reference.VERSION + ", " + Reference.CHANNEL + "]");
		JavaHelper.sendSystemMessage(LVL.INFO, "Attempting Discord API Connection");
		
		DiscordApi api = new DiscordApiBuilder().setToken(Settings.loadToken()).setAllNonPrivilegedIntentsAnd(Intent.MESSAGE_CONTENT, Intent.GUILD_MEMBERS).login().join();
		JavaHelper.sendSystemMessage(LVL.INFO, "Discord API Connection Successful");
		
		JavaHelper.sendSystemMessage(LVL.DEBUG, "Begin load system settings");
		for (int i = 0; i < Settings.ARRAY.length(); i++) {
			JavaHelper.sendSystemMessage(LVL.DEBUG, "--> " + Settings.ARRAY.get(i));
		}
		JavaHelper.sendSystemMessage(LVL.INFO, "Finish load system settings");

		JavaHelper.sendSystemMessage(LVL.DEBUG, "Begin load resources");
		for (int i = 0; i < Settings.RESOURCES.length(); i++) {
			JavaHelper.sendSystemMessage(LVL.DEBUG, "--> " + Settings.RESOURCES.get(i));
		}
		JavaHelper.sendSystemMessage(LVL.INFO, "Finish load resources");
		
		ActivityType activityType = ActivityType.getActivityTypeById(Settings.ACTIVITY_TYPE);
		JavaHelper.sendSystemMessage(LVL.INFO, "Setting activity to: { " + activityType + " } [ " + Settings.ACTIVITY_DESC + " ]");
		api.updateActivity(activityType, Settings.ACTIVITY_DESC);
		
		ArrayList<Server> servers = new ArrayList<Server>();
		
		api.getServers().stream().forEach((server) -> {
			if (server.getIdAsString().equals(Settings.SERVER_ID)) {
				servers.add(server);
			}
		});

		if (servers.size() == 0) {
			JavaHelper.sendSystemMessage(LVL.CRITICAL, "ERROR! Defined server not present! Please add the bot to your server, and check the provided server ID!");
			shutdown(api);
		}
		
		Server specifiedServer = servers.get(0);
		JavaHelper.sendSystemMessage(LVL.INFO, "Verified Server: [ name: '" + specifiedServer.getName() + "', id: '" + specifiedServer.getIdAsString() + "' ]");
		
		startCitadelTickTask(api, specifiedServer);
		
		api.addListener(new MessageCreateListener() {
			@Override
			public void onMessageCreate(MessageCreateEvent eventIn) {
				Message message = eventIn.getMessage();
				User messageUser = message.getUserAuthor().get();
				String messageContent = message.getContent();

				if (message.isPrivateMessage()) {
					Role adminRole = specifiedServer.getRoleById(Settings.ADMIN_ROLE).get();
					
					if (!message.getUserAuthor().get().isBot()) {
						if (messageUser.getIdAsString().equals(Settings.BOT_OWNER)) {
							if (messageContent.contains("!ip")) {
								if (messageContent.contains(" ")) {
									String[] messageSplit = messageContent.split(" ");
									
									try {
										int site = Integer.parseInt(messageSplit[1]);
										
										switch (site) {
											case 0:
												JavaHelper.getIPAddress(message, "http://checkip.amazonaws.com/");
												break;
											case 1:
												JavaHelper.getIPAddress(message, "https://ipv4.icanhazip.com/");
												break;
											case 2:
												JavaHelper.getIPAddress(message, "http://myexternalip.com/raw");
												break;
											case 3:
												JavaHelper.getIPAddress(message, "http://ipecho.net/plain");
												break;
											case 4:
												JavaHelper.getIPAddress(message, "http://www.trackip.net/ip");
												break;
											default:
												JavaHelper.getIPAddress(message, "http://checkip.amazonaws.com/");
												break;
										}
									} catch (NumberFormatException e) {
										message.reply("`Check site must be a number.``");
									}
								} else {
									JavaHelper.getIPAddress(message, "http://checkip.amazonaws.com/");
								}
							}

							else if (message.getContent().equals(Settings.PREFIX + "shutdown")) {
								message.reply("`Shutting down`");
								
								JavaHelper.sleep(5);
								shutdown(api);
							}
						}
						
						else {
							message.reply("`Sorry, but this bot does not accept private messages.`");
						}
						
						JavaHelper.sendSystemMessage(LVL.INFO, "PM recieved from user " + DiscordHelper.getUserInfo(null, messageUser, "content: " + messageContent));
					}
					return;
				}
				
				Server currentServer = eventIn.getServer().get();
				Channel preChannel = eventIn.getChannel();

				ServerChannel serverChannel = preChannel.asServerChannel().get();
				
				if (!message.isPrivateMessage()) {
					ServerTextChannel announcements = currentServer.getChannelById(Settings.ANNOUNCE_ID).get().asServerTextChannel().get();
					
					ServerChannel currentChannel = serverChannel.asServerChannel().get();
					String channelName = serverChannel.getName();
					String channelID = serverChannel.getIdAsString();
					
					Role adminRole = currentServer.getRoleById(Settings.ADMIN_ROLE).get();
					
					String messageUserName = messageUser.getName();
					String messageUserNick = messageUser.getNickname(currentServer).isPresent() ? messageUser.getNickname(currentServer).get() : messageUserName;
					String messageUserId = messageUser.getIdAsString();
					
					if (!messageUser.isBot()) {
						if (messageContent.contains(Settings.PREFIX + "purge")) {
							if (DiscordHelper.isMessageUserRole(message, adminRole, currentServer)) {
								purgeChannel(message, currentChannel.asServerTextChannel().get());
							}
						}
						
						if (channelID.equals(Settings.COMMANDS_ID)) {
							if (DiscordHelper.isMessageUserRole(message, adminRole, currentServer)) {
								if (messageContent.contains(Settings.PREFIX + "tick")) {
									String[] contentSplit = messageContent.split(" ");
									
									if (contentSplit.length >= 3) {
										String day = contentSplit[1];
										String time = contentSplit[2];
										
										if (time.contains(":")) {
											Settings.set(S.CITADEL_RESET_DAY, day);
											Settings.set(S.CITADEL_RESET_TIME, time);
											
											startCitadelTickTask(api, currentServer);
											
											JavaHelper.sendSystemMessage(LVL.INFO, messageUserName + " has reset the tick to: " + day + ", " + time);
		
											message.reply("Citadel reset tick set to: `" + day.substring(0, 1).toUpperCase() + day.substring(1) + " at " + time + "`");
											saveSystem();
										} else {
											message.reply("Input **must** match the following: ```" + Settings.PREFIX + "tick {day}(monday/tuesday etc) {time}(HH:MM)```");
										}
									} else {
										message.reply("Input **must** match the following: ```" + Settings.PREFIX + "tick {day}(monday/tuesday etc) {time}(HH:MM)```");
									}
								}
								
								else if (messageContent.contains(Settings.PREFIX + "announce")) {
									TickTask.announceTick(currentServer);
									resetTickState(currentServer);
									saveSystem();
									
									message.reply("Announcement has been sent.");
								}
								
								else if (messageContent.contains(Settings.PREFIX + "prefix")) {
									String[] contentSplit = messageContent.split(" ");
									
									if (contentSplit.length >= 2) {
										Settings.set(S.PREFIX, contentSplit[1]);
										
										message.reply("Command prefix has been set to: `[ " + contentSplit[1] + " ]`");
									}
								}
								
								else if (message.getContent().equals(Settings.PREFIX + "shutdown")) {
									message.reply("`Shutting down`");
									
									JavaHelper.sleep(5);
									shutdown(api);
								}
								
								else if (messageContent.contains(Settings.PREFIX + "interval")) {
									String[] contentSplit = messageContent.split(" ");
								
									if (contentSplit.length >= 2) {
										int interval = Integer.parseInt(contentSplit[1]);
										Settings.set(S.TIMER_INTERVAL, interval);
										
										startCitadelTickTask(api, currentServer);
										
										message.reply("Citadel interval timer set to: `" + interval + (interval == 1 ? " minute" : " minutes") + "`");
										JavaHelper.sendSystemMessage(LVL.INFO, "Current number of timers running: [ " + timers.size()  + " ]");
										saveSystem();
									}
								}
								
								else if (messageContent.equals(Settings.PREFIX + "reset")) {
									Settings.set(S.WEEK_CAPPED, false);
									Settings.set(S.CURRENT_CAPPERS, 0);
									Settings.set(S.GOAL_COMPLETE, false);
									Settings.set(S.LOCKED, false);

									resetAllUserCaps(currentServer, message, messageUser);
									resetAllResourceGoals(currentServer, message, messageUser);
								}
								
								else if (messageContent.equals(Settings.PREFIX + "cap-reset")) {
									resetAllUserCaps(currentServer, message, messageUser);
								}
								
								else if (messageContent.contains(Settings.PREFIX + "world")) {
									String[] contentSplit = messageContent.split(" ");
									
									if (DiscordHelper.isMessageUserRole(message, adminRole, currentServer)) {
										if (contentSplit.length >= 2) {
											String world = contentSplit[1];
											
											try {
												Integer worldInt = Integer.parseInt(world);
												Settings.set(S.CLAN_WORLD, worldInt);
												
												message.reply("Clan World set to: `" + world + "`");
											} catch (NumberFormatException e) {
												message.reply("`Clan World must be set to a number.`");
											}
										}
									}
								}
								
								else if (messageContent.contains(Settings.PREFIX + "goal")) {
									if (messageContent.contains(" ")) {
										if (messageContent.contains("account")) {
											String[] contentSplit = messageContent.split(" ");
											String firstSplit = contentSplit[2];
											
											try {
												int setGoal = Integer.parseInt(firstSplit);
												
												if (Settings.CURRENT_GOAL != setGoal) {
													Settings.set(S.GOAL_COMPLETE, false);
													Settings.set(S.CURRENT_GOAL, setGoal);
													
													MessageBuilder messageBuilder = new MessageBuilder();
													EmbedBuilder embed = new EmbedBuilder();
													
													embed.setAuthor(currentServer.getMemberById(Settings.BOT_MEMBER_ID).get());
													embed.setColor(Color.ORANGE);
													embed.setTitle("New Cap Goal :goal:");
													embed.setDescription("A new cap goal for the week has been set.");
													embed.addField("Goal", "The goal this week is for at least `" + Settings.CURRENT_GOAL + "` players to cap at the Citadel.");
													
													embed.setThumbnail(CitadelBot.ICON);
													embed.setFooter(Reference.MSG.EMBED_FOOTER);
													embed.setTimestampToNow();
													
													messageBuilder.setContent(currentServer.getRoleById(Settings.PING_ROLE).get().getMentionTag() + " - New goal set!");
													messageBuilder.setEmbed(embed);
													messageBuilder.send(announcements);
													
													message.reply("`Goal set announcement sent.`");
													JavaHelper.sendSystemMessage(LVL.INFO, "<!goal> Goal set announcement sent by user: " + messageUserName);
												} else {
													message.reply("`Goal specified is the same as the current goal.`");
												}
											} catch (NumberFormatException e) {
												message.reply("`Goal must be a number.`");
											}
										}
										
										else if (messageContent.contains("resource")) {
											String[] messageSplit = messageContent.split(" ");
											
											boolean resourcePresent = false;
											String resourceName = "";
											boolean resourceActive = false;
											int resourceGoal = 0;
											
											for (int j = 0; j < Settings.RESOURCES.length(); j++) {
												JSONObject testResource = Settings.RESOURCES.getJSONObject(j);
												
												if (messageContent.contains(testResource.getString(Reference.JSON.R.NAME))) {
													resourcePresent = true;
	
													String resourceNameRaw = testResource.getString(Reference.JSON.R.NAME);
													resourceName = resourceNameRaw.substring(0, 1).toUpperCase() + resourceNameRaw.substring(1);
													resourceActive = testResource.getBoolean(Reference.JSON.R.ACTIVE);
													resourceGoal = testResource.getInt(Reference.JSON.R.GOAL);
													
													if (resourcePresent) {
														if (resourceActive) {
															int messageGoal = 0;
															
															if (messageSplit.length >= 4) {
																String number = messageSplit[3];
																
																try {
																	messageGoal = Integer.parseInt(number);
																} catch (NumberFormatException e) { 
																	message.reply("`Goal must be a number!`");
																}
																
																testResource.put(Reference.JSON.R.GOAL, messageGoal);
																
																message.reply("Goal for: `" + resourceName + "` set to: `" + testResource.getInt(Reference.JSON.R.GOAL) + "`");
																saveSystem();
															} else {
																message.reply("`Not enough arguments specified.`");
															}
														}
													} else {
														message.reply("`Im sorry, that resource was not found.`");
													}
												}
											}
										} 
										
										else if (messageContent.contains("announce")) {
											TickTask.announceGoal(currentServer);
											//resetTickState(currentServer);
											//saveSystem();
											
											message.reply("Announcement has been sent.");
										}
									}
								}

								else if (messageContent.contains(Settings.PREFIX + "incentive")) {								
									if (messageContent.contains(" ")) {
										if (messageContent.contains("set")) {
											if (messageContent.contains("\"")) {
												String[] contentSplit = messageContent.split("\"");
												
												if (contentSplit.length > 1 && contentSplit.length < 3) {
													String firstSplit = contentSplit[1];
													
													Settings.set(S.INCENTIVE, firstSplit);
													
													message.reply("Incentive set to: `" + firstSplit + "`.");
												} else {
													message.reply("You must specify the Incentive.");
												}
											} else {
												message.reply("You must enclose the Incentive with **\"quotation marks\"**.");
											}
										}
										
										else if (messageContent.contains("remove")) {
											Settings.set(S.INCENTIVE, "");
											
											message.reply("Incentive removed.");
										}
									}
								}
								
								else if (messageContent.equals(Settings.PREFIX + "save")) {
									saveSystem();
									
									message.reply("System saved.");
								}
								
								else if (messageContent.equals(Settings.PREFIX + "reminder")) {
									MessageBuilder messageBuilder = new MessageBuilder();
									EmbedBuilder embed = new EmbedBuilder();

									embed.setAuthor(currentServer.getMemberById(Settings.BOT_MEMBER_ID).get());
									embed.setColor(Color.ORANGE);
									embed.setTitle("Capping Reminder! :clock12:");
									embed.setDescription("This is a reminder to visit the Clan Citadel and cap!");
									embed.setThumbnail(CitadelBot.ICON);
									
									String userMentions = "";
									
									for (int i = 0; i < USER_ARRAY.length(); i++) {
										JSONObject testUser = USER_ARRAY.getJSONObject(i);
										
										if (!testUser.getBoolean(USER_CAPPED_WEEK_JSON)) {
											String userId = testUser.getString(USER_DISCORD_ID_JSON);
											
											Optional<User> optionalUser = currentServer.getMemberById(userId);
											
											if (optionalUser.isPresent()) {
												User discordUser = optionalUser.get();
												
												userMentions += " " + discordUser.getMentionTag();
											} else {
												System.out.println("User not detected.");
											}
										}
									}

									embed.setFooter(Reference.MSG.EMBED_FOOTER);
									embed.setTimestampToNow();
									
									if (userMentions.isBlank()) {
										message.reply("Everyone has capped this week, so no reminder needed!");
										messageBuilder.addEmbed(embed);
										messageBuilder.send(announcements);
										return;
									} else {
										messageBuilder.setContent("Capping Reminder:" + userMentions);
										
										message.reply("Capping Reminder Announcement sent.");
										messageBuilder.addEmbed(embed);
										messageBuilder.send(announcements);
									}
								}
								
								else if (messageContent.contains(Settings.PREFIX + "resource") && !(messageContent.contains(Settings.PREFIX + "resources"))) {
									if (messageContent.contains(" ")) {
										String[] messageSplit = messageContent.split(" ");
										
										if (messageContent.contains("update")) {
											if (messageSplit.length > 3) { 
												String resource = messageSplit[2];
												String number = messageSplit[3];
												
												try {
													int numberActual = Integer.parseInt(number);
	
													for (int i = 0; i < Settings.RESOURCES.length(); i++) {
														JSONObject testResource = Settings.RESOURCES.getJSONObject(i);
														
														if (resource.equals(testResource.getString(Reference.JSON.R.NAME))) {
															testResource.put(Reference.JSON.R.CAP, numberActual);
															saveSystem();
															
															message.reply("Resource Cap for resource: `" + resource + "` has been updated to: `" + numberActual + "`.");
														}
													}
												} catch (NumberFormatException e) {
													message.reply("Resource amount `" + number + "` must be a number!");
												}
											}
										}
										
										if (messageContent.contains("active")) {
											boolean resourcePresent = false;
											String resourceName = "";
											boolean resourceActive = false;
											
											for (int j = 0; j < Settings.RESOURCES.length(); j++) {
												JSONObject testResource = Settings.RESOURCES.getJSONObject(j);
												
												if (messageContent.contains(testResource.getString(Reference.JSON.R.NAME))) {
													resourcePresent = true;

													String resourceNameRaw = testResource.getString(Reference.JSON.R.NAME);
													resourceName = resourceNameRaw.substring(0, 1).toUpperCase() + resourceNameRaw.substring(1);
													resourceActive = messageContent.contains("true") ? true : messageContent.contains("false") ? false : testResource.getBoolean(Reference.JSON.R.ACTIVE);
													
													testResource.put(Reference.JSON.R.ACTIVE, resourceActive);
												}
											}
											
											if (resourcePresent) {
												message.reply("`" + resourceName + "` has been `" + (resourceActive ? "opened" : "closed") + "` for capping." + (resourceActive ? " :white_check_mark:" : " :red_square:"));
											} else {
												message.reply("`Im sorry, that resource was not found.`");
											}
											
											Settings.saveSettings();
										}
									} else {
										message.reply("`Not enough arguments specified.`");
									}
								}
								
								else if (messageContent.equals(Settings.PREFIX + "check-all")) {
									MessageBuilder builder = new MessageBuilder();
									EmbedBuilder embed = new EmbedBuilder();
									
									builder.setContent("As requested, below is a summary of all currently registered users:");
									
									embed.setAuthor(currentServer.getMemberById(Settings.BOT_MEMBER_ID).get());
									embed.setColor(Color.CYAN);
									embed.setTitle("User Capping Status:");
									embed.setDescription("Below is a current summary of all users cap status.");
									
									for (int i = 0; i < USER_ARRAY.length(); i++) {
										JSONObject testObject = USER_ARRAY.getJSONObject(i);
										
										Optional<User> optionalUser = currentServer.getMemberById(testObject.getString(USER_DISCORD_ID_JSON));
										
										if (optionalUser.isPresent()) {
											User user = optionalUser.get();
											
											String userName = user.getDisplayName(currentServer);
											boolean capped = testObject.getBoolean(USER_CAPPED_WEEK_JSON);
											int timesCapped = testObject.getInt(ACCOUNT_TIMES_CAPPED_JSON);
											int weeksSinceLastCap = testObject.getInt(USER_WEEKS_SINCE_LAST_CAPPED_JSON);
											
											embed.addField((capped ? ":white_check_mark:" : ":red_square:") + " - " + userName, "> This user has `" + (capped ? "capped" : "not capped") + "` this week."
												+ "\n > This user has capped `" + timesCapped + (timesCapped == 1 ? "`time" : "`times") + "."
												+ "\n > It has been `" + weeksSinceLastCap + (weeksSinceLastCap == 1 ? "`week" : "`weeks") + " since this user last capped."
											);
										}
									}

									embed.setFooter(Reference.MSG.EMBED_FOOTER);
									embed.setTimestampToNow();
									
									message.reply("`Please check your DMs.`");
									
									builder.setEmbed(embed);
									builder.send(messageUser);
								}
							}
							
							//Normie commands
							if (messageContent.equals(Settings.PREFIX + "resources")) {
								EmbedBuilder embed = new EmbedBuilder();
								
								embed.setAuthor(currentServer.getMemberById(Settings.BOT_MEMBER_ID).get());
								embed.setTitle("Current resource list :wood:");
								embed.setDescription("A list of the currently available resources at the Citadel");
								embed.setThumbnail(CitadelBot.ICON);
								
								for (int i = 0; i < Settings.RESOURCES.length(); i++) {
									JSONObject testResource = Settings.RESOURCES.getJSONObject(i);
									
									String resourceNameRaw = testResource.getString(Reference.JSON.R.NAME);
									String resourceName = resourceNameRaw.substring(0, 1).toUpperCase() + resourceNameRaw.substring(1).replace("_", " ");
									String resourceEmoji = testResource.getString(Reference.JSON.R.EMOJI_ID);
									boolean resourceActive = testResource.getBoolean(Reference.JSON.R.ACTIVE);
									int resourceCap = testResource.getInt(Reference.JSON.R.CAP);
									int resourceGoal = testResource.getInt(Reference.JSON.R.GOAL);
																			
									embed.addField(
										(i + 1) + " - " + resourceName + (Settings.RESOURCE_EMOJI ? "  <:" + resourceNameRaw + ":" + resourceEmoji + ">" : "") + 
										(resourceActive ? "  :white_check_mark:" : "  :red_square:"),
										(resourceActive ? "\n> `Unit cap: " + resourceCap + " units.`" : "") +
										(resourceActive && resourceGoal > 0 ? "\n> `Weekly goal: " + resourceGoal + " units`" : "")
									);
								}
								
								embed.setFooter(Reference.MSG.EMBED_FOOTER);
								embed.setTimestampToNow();
								
								message.reply(embed);
							}
							
							
							if (messageContent.contains(Settings.PREFIX + "locked")) {								
								if (messageContent.contains(" ")) {
									String[] contentSplit = messageContent.split(" ");
									String firstSplit = contentSplit[1];

									try {
										int peopleRequired = Integer.parseInt(firstSplit);
										
										if (!Settings.OPEN) {
											if (!Settings.LOCKED) {
												MessageBuilder messageBuilder = new MessageBuilder();
												EmbedBuilder embed = new EmbedBuilder();
												
												embed.setAuthor(currentServer.getMemberById(Settings.BOT_MEMBER_ID).get());
												embed.setColor(Color.RED);
												embed.setTitle("Citadel is locked! :lock:");
												embed.setDescription("The Citadel is currently locked!");
												embed.addField(":1234: ・ Required Member visits:", "> The Citadel requires at least: `" + peopleRequired + "` players to visit the Citadel to unlock.");
												
												embed.setThumbnail(CitadelBot.ICON);
												embed.setFooter(Reference.MSG.EMBED_FOOTER);
												embed.setTimestampToNow();
												
												messageBuilder.setContent(currentServer.getRoleById(Settings.PING_ROLE).get().getMentionTag() + " - Citadel locked!");
												messageBuilder.setEmbed(embed);
												messageBuilder.send(announcements);
												
												message.reply("`Citadel locked announcement sent.`");
												JavaHelper.sendSystemMessage(LVL.INFO, "<!locked> Citadel locked announcement sent by user: " + messageUserName);
												Settings.set(S.LOCKED, true);
												saveSystem();
											} else {
												message.reply("`The Citadel has already been marked as locked.`");
											}
										}
									} catch (NumberFormatException e) {
										message.reply("`People requried to unlock the Citadel must be a number.`");
									}
								} else {
									message.reply("You must specify the number of member visits required to unlock the Citadel.");
								}
							}
							
							if (messageContent.contains(Settings.PREFIX + "unlocked")) {
								if (!Settings.OPEN) {
									if (Settings.LOCKED) {
										MessageBuilder messageBuilder = new MessageBuilder();
										EmbedBuilder embed = new EmbedBuilder();
										
										embed.setAuthor(currentServer.getMemberById(Settings.BOT_MEMBER_ID).get());
										embed.setColor(Color.GREEN);
										embed.setTitle("Citadel Unlocked! :unlock:");
										embed.setDescription("The Citadel has been unlocked!");
										embed.addField(":white_check_mark: ・ Thank you!", "> Thanks " + messageUser.getMentionTag() + " for unlocking the citadel!");
										
										embed.setThumbnail(CitadelBot.ICON);
										embed.setFooter(Reference.MSG.EMBED_FOOTER);
										embed.setTimestampToNow();
										
										messageBuilder.setContent(currentServer.getRoleById(Settings.PING_ROLE).get().getMentionTag() + " - Citadel unlocked!");
										messageBuilder.setEmbed(embed);
										messageBuilder.send(announcements);
										
										message.reply("`Citadel unlocked announcement sent.`");
										JavaHelper.sendSystemMessage(LVL.INFO, "<!unlocked> Citadel unlocked announcement sent by user: " + messageUserName);
										Settings.set(S.LOCKED, false);
										Settings.set(S.OPEN, true);
									} else {
										message.reply("`The Citadel is already unlocked.`");
									}
								}
							}
							
							if (messageContent.contains(Settings.PREFIX + "role")) {
								Role pingRole = currentServer.getRoleById(Settings.PING_ROLE).get();
								
								if (messageUser.getRoles(currentServer).contains(pingRole)) {
									messageUser.removeRole(pingRole);
									
									message.reply("`Role removed @" + pingRole.getName() + "`");
								} else {
									messageUser.addRole(pingRole);
									message.reply("`Role added @" + pingRole.getName() + "`");
								}
							}
							
							if (messageContent.contains(Settings.PREFIX + "cap")) {
								if (messageContent.contains(" ")) {
									String[] contentSplit = messageContent.split(" ");
																		
									if (contentSplit.length >= 2) {
										String rsName = contentSplit[1];
										
										boolean resourcePresent = false;
										boolean resourceActive = false;
										String resourceName = null;
										String resourceEmoji = "";
										Integer resourceAmount = 0;
										
										for (int j = 0; j < Settings.RESOURCES.length(); j++) {
											JSONObject testResource = Settings.RESOURCES.getJSONObject(j);
											
											if (messageContent.contains(testResource.getString(Reference.JSON.R.NAME))) {
												resourcePresent = true;
												
												resourceName = testResource.getString(Reference.JSON.R.NAME);
												resourceEmoji = testResource.getString(Reference.JSON.R.EMOJI_ID);
												resourceAmount = testResource.getInt(Reference.JSON.R.CAP);
												
												resourceActive = testResource.getBoolean(Reference.JSON.R.ACTIVE);
											}
										}
										
										if (rsName.equals(resourceName)) {
											capUser(currentServer, message, messageUser, resourceName, resourceEmoji, resourceAmount, true);
											return;
										}
										
										if (resourcePresent && !resourceActive) {
											message.reply("`Im sorry, that resource is not currently active.`");
											return;
										}
										
										for (int i = 2; i < contentSplit.length - (resourcePresent ? 1 : 0); i++) {
											rsName = rsName + " " + contentSplit[i];
										}
										
										if (checkUserNameIsValid(rsName)) {
											capUser(currentServer, message, messageUser, rsName, resourceName, resourceEmoji, resourceAmount, true);
											return;
										} else {
											message.reply("`That is not a valid RS3 Username. Please try again.`");
										}
									}
								} else {
									capUser(currentServer, message, messageUser, null, "", 0, true);
								}
							}
							
							if (messageContent.equals(Settings.PREFIX + "check")) {
								getUserAccountsCapStatus(currentServer, message, messageUser);
							}
							
							if (messageContent.equals(Settings.PREFIX + "register")) {
								registerUser(currentServer, message, messageUser);
							}
							
							if (messageContent.equals(Settings.PREFIX + "deregister")) {
								deregisterUser(currentServer, message, messageUser);
							}
	
							if (messageContent.contains(Settings.PREFIX + "add")) {
								String[] contentSplit = messageContent.split(" ");
	
								if (contentSplit.length >= 2) {
									String rsName = contentSplit[1];
									
									for (int i = 2; i < contentSplit.length; i++) {
										rsName = rsName + " " + contentSplit[i];
									}
									
									if (checkUserNameIsValid(rsName)) {
										addAccountToUser(message, messageUser, rsName);
									} else {
										message.reply("`That is not a valid RS3 Username. Please try again.`");
									}
								}
							}
							
							if (messageContent.contains(Settings.PREFIX + "remove")) {
								String[] contentSplit = messageContent.split(" ");
	
								if (contentSplit.length >= 2) {
									String rsName = contentSplit[1];

									for (int i = 2; i < contentSplit.length; i++) {
										rsName = rsName + " " + contentSplit[i];
									}
									
									if (checkUserNameIsValid(rsName)) {
										removeAccountFromUser(message, messageUser, rsName);
									} else {
										message.reply("`That is not a valid RS3 Username. Please try again.`");
									}
								}
							}
							
							if (message.getContent().equals(Settings.PREFIX + "commands")) {
								String p = Settings.PREFIX;
								
								MessageBuilder builder = new MessageBuilder();
								
								builder.setContent(
									"__**Current Commands:**__\n```"
									+ "Key:\n"
									+ "`[]` - Required\n"
									+ "`{}` - Optional``` \n"
											
									+ "```01 ・ " + p + "role\n ---- Grants the Citadel Bot Capping Role to you. \n"
									+ "02 ・ " + p + "register\n ---- Registers you for use with the Bot. \n"
									+ "03 ・ " + p + "deregister\n ---- De-registers you for use with the Bot. WARNING: This will remove all of your capping data! \n"
									+ "04 ・ " + p + "add [account name]\n ---- Adds the RS3 Username to your registration. \n"
									+ "05 ・ " + p + "remove [account name]\n ---- Removes the specified RS3 Username from your registration. \n"
									+ "06 ・ " + p + "cap [account name] {resource}\n ---- Updates the system that you have capped on that account. You can also attach a screenshot of the cap message in-game. \n"
									+ "07 ・ " + p + "locked [number required to unlock]\n ---- Sends a notification that the Citadel is locked and requires [number] of people to visit to unlock. \n"
									+ "08 ・ " + p + "unlocked\n ---- Sends a notification that the Citadel has been unlocked. \n"
									+ "09 ・ " + p + "check\n ---- Provides you with a list of RS3 Usernames you have registered, whether they have capped this week, and more misc info. \n"
									+ "10 ・ " + p + "resources\n ---- Shows a list of currently available resources, their cap amounts and/or current goal. \n"
									+ "```"
								);
								builder.send(messageUser);
								
								JavaHelper.sleep(5);
								
								if (DiscordHelper.isMessageUserRole(message, adminRole, currentServer)) {
									MessageBuilder builderAdmin = new MessageBuilder();
									
									builderAdmin.setContent(
										"__**Administrator Commands:**__\n```"
										+ "11 ・ " + p + "tick [day] [HH:MM]\n ---- Updates day and time of the weekly reset. \n"
										+ "12 ・ " + p + "interval [mins]\n ---- Updates how frequently the bot checks whether it is time to announce a reset (technical). \n"
										+ "13 ・ " + p + "cap-reset\n ---- Resets all users capping status for the week. \n"
										+ "14 ・ " + p + "announce\n ---- Force sends an announcement of a reset. \n"
										+ "15 ・ " + p + "reminder\n ---- Sends a reminder to all users who have not yet capped for the week. \n"
										+ "16 ・ " + p + "prefix [prefix]\n ---- Changes the command prefix to [prefix]. \n"
										+ "17 ・ " + p + "world [world]\n ---- Changes the current clan world shown on announcements. \n"
										+ "18 ・ " + p + "goal [account|resource] [number|resource name] [number]\n ---- Sets the weekly goal, either with accounts: '" + p + "goal account 5' or with resources: '" + p + "resource timber 1200'. \n"
										+ "19 ・ " + p + "incentive \"[incentive]\"\n ---- Sets the current incentive. You must enclose your incentive with \" quotation marks \". \n"
										+ "20 ・ " + p + "resource [active] [resource name] [true|false]\n ---- Updates the resource to be available or not available 'true or false'. \n"
										+ "21 ・ " + p + "prefix [prefix]\n ---- Updates the resource to be available or not available 'true or false'. \n"
										+ "22 ・ " + p + "reset\n ---- Reset all stored values about current cappers, goals etc. \n"
										+ "23 ・ " + p + "shutdown\n ---- Shuts down the bot, you will have to call Cass to bring it online! \n"
										+ "```"
									);
									builderAdmin.send(messageUser);
								}
							}
						}
					}
				}
			}
		});
	}
	
	public static void purgeChannel(Message message, ServerTextChannel channel) {
		String content = message.getContent();
		
		String[] contentSplit = content.split(" ");
		int number = 1000;
		
		if (contentSplit.length >= 2) {
			number = Integer.parseInt(contentSplit[1]);
		}
		
		try {
			channel.asServerTextChannel().get().deleteMessages(channel.asServerTextChannel().get().getMessages(number).get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean checkUserNameIsValid(String rsName) {
		if (rsName.length() > 1 && rsName.length() <= 12) {
			Pattern specialChar = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~]");

			Matcher specialMatch = specialChar.matcher(rsName);
			
			JavaHelper.sendSystemMessage(LVL.INFO, "<rs3 name check> Username: [ '" + rsName + "' ] is: " + (!specialMatch.find() ? "VALID" : "NOT_VALID"));
			
			return !specialMatch.find();
		}
		
		return false;
	}
	
	public static void startCitadelTickTask(DiscordApi api, Server server) {
		if (timers.size() == 0) {
			startTimer(api, server);
		} else {
			timers.forEach((timer) -> {
				timer.purge();
				timer.cancel();
			});
			
			timers.clear();
			
			startTimer(api, server);
		}
	}
	
	public static void startTimer(DiscordApi api, Server server) {
		Timer timer = new Timer();
		int resetTimeHH = Integer.parseInt(((String) Settings.get(S.CITADEL_RESET_TIME)).split(":")[0]);
		int resetTimeMM = Integer.parseInt(((String) Settings.get(S.CITADEL_RESET_TIME)).split(":")[1]);
		
		TimerTask task = new TickTask((String) Settings.get(S.CITADEL_RESET_DAY), resetTimeHH, resetTimeMM, server);
		
		timer.scheduleAtFixedRate(task, new Date(), Duration.ofMinutes((int) Settings.get(S.TIMER_INTERVAL)).toMillis());
		
		timers.add(timer);
	}
	
	public static void registerUser(Server serverIn, Message message, User user) {
		boolean replaced = false;

		String discordName = user.getName();
		JSONObject arrayUser = new JSONObject();
		arrayUser.put(USER_DISCORD_NAME_JSON, discordName);
		arrayUser.put(USER_DISCORD_ID_JSON, user.getIdAsString());
		arrayUser.put(USER_CAPPED_TIMES_JSON, 0);
		arrayUser.put(USER_CAPPED_WEEK_JSON, false);
		arrayUser.put(USER_WEEKS_SINCE_LAST_CAPPED_JSON, 0);

		JSONArray accounts = new JSONArray();
		arrayUser.put(USER_ACCOUNTS_JSON, accounts);
		
		if (checkUserIsReged(user)) {
			for (int i = 0; i < USER_ARRAY.length(); i++) {
				JSONObject test = USER_ARRAY.getJSONObject(i);

				if (test.getString(USER_DISCORD_ID_JSON).equals(user.getIdAsString())) {
					message.reply("`You are already registered!`");
					saveSystem();
					replaced = true;
					break;
				}
			}
		}

		if (!replaced) {
			USER_ARRAY.put(arrayUser);
			JavaHelper.sendSystemMessage(LVL.INFO, "Registered new user: " + DiscordHelper.getUserInfo(serverIn, user, ""));
			message.reply("`Successfully registered.`");
			saveSystem();
		}
	}
	
	public static void deregisterUser(Server serverIn, Message message, User user) {
		if (checkUserIsReged(user)) {
			for (int i = 0; i < USER_ARRAY.length(); i++) {
				JSONObject test = USER_ARRAY.getJSONObject(i);

				if (test.getString(USER_DISCORD_ID_JSON).equals(user.getIdAsString())) {
					USER_ARRAY.remove(i);
					
					message.reply(Reference.MSG.DE_REGISTERED);
				}
			}
		} else {
			message.reply(Reference.MSG.NOT_REGISTERED);
		}
	}

	public static void addAccountToUser(Message message, User user, String rsName) {
		JSONObject rsAccount = new JSONObject();
		rsAccount.put(ACCOUNT_NAME_JSON, rsName);
		rsAccount.put(ACCOUNT_CAPPED_JSON, false);
		rsAccount.put(ACCOUNT_TIMES_CAPPED_JSON, 0);
		
		boolean toAdd = true;

		JSONObject jsonUser = new JSONObject();
		
		if (checkUserIsReged(user)) {
			for (int i = 0; i < USER_ARRAY.length(); i++) {
				jsonUser = USER_ARRAY.getJSONObject(i);

				if (jsonUser.getString(USER_DISCORD_ID_JSON).equals(user.getIdAsString())) {
					JSONArray testArray = jsonUser.getJSONArray(USER_ACCOUNTS_JSON);
					
					for (int j = 0; j < testArray.length(); j++) {
						JSONObject testObject = testArray.getJSONObject(j);
						String testName = testObject.getString(ACCOUNT_NAME_JSON);
						
						if (testName.equals(rsName)) {
							toAdd = false;
						}
					}
					
					if (toAdd) {
						testArray.put(rsAccount);
						message.reply("Account: **`" + rsName + "`** added to user: " + user.getMentionTag() + "");
					} else {
						message.reply("`You have already added that account.`");
					}
					
					saveSystem();
				}
			}
		} else {
			message.reply("`You are not yet registered.`");
			saveSystem();
		}
	}

	public static void removeAccountFromUser(Message message, User user, String rsName) {		
		boolean toRemove = false;
		int indexRemoved = 0;
		
		if (checkUserIsReged(user)) {
			for (int i = 0; i < USER_ARRAY.length(); i++) {
				JSONObject jsonUser = USER_ARRAY.getJSONObject(i);

				if (jsonUser.getString(USER_DISCORD_ID_JSON).equals(user.getIdAsString())) {
					JSONArray testArray = jsonUser.getJSONArray(USER_ACCOUNTS_JSON);
					
					for (int j = 0; j < testArray.length(); j++) {
						JSONObject testObject = testArray.getJSONObject(j);
						String testName = testObject.getString(ACCOUNT_NAME_JSON);
						
						if (testName.equals(rsName)) {
							toRemove = true;
							indexRemoved = j;
							break;
						}
					}
					
					if (toRemove) {
						testArray.remove(indexRemoved);
						message.reply("Account: **`" + rsName + "`** removed from user: " + user.getMentionTag() + "");
					} else {
						message.reply("`You have already removed that account.`");
					}
					
					saveSystem();
				}
			}
		} else {
			message.reply("`You are not yet registered.`");
			saveSystem();
		}
	}
	
	public static boolean checkUserHasAccount(User user, String rsName) {		
		boolean toRemove = false;
		
		if (checkUserIsReged(user)) {
			for (int i = 0; i < USER_ARRAY.length(); i++) {
				JSONObject testUser = USER_ARRAY.getJSONObject(i);

				if (testUser.getString(USER_DISCORD_ID_JSON).equals(user.getIdAsString())) {
					JSONArray testArray = testUser.getJSONArray(USER_ACCOUNTS_JSON);
					
					for (int j = 0; j < testArray.length(); j++) {
						JSONObject testObject = testArray.getJSONObject(j);
						String testName = testObject.getString(ACCOUNT_NAME_JSON);
						
						if (testName.equals(rsName)) {
							toRemove = true;
						}
					}
				}
				
				if (toRemove) {
					return true;
				}
			}
		} else {
			return false;
		}
		
		return false;
	}
	
	public static ArrayList<JSONObject> getUserAccounts() {
		return null;
	}
	
	public static void capUser(Server server, Message message, User user, String resourceNameIn, String resourceEmojiIn, int resourceAmountIn, boolean capped) {
		if (checkUserIsReged(user)) {
			if ((boolean) Settings.get(S.LOCKED)) {
				message.reply("`You cannot cap at the moment, as the Citadel is locked.`");
				return;
			}
			
			for (int i = 0; i < USER_ARRAY.length(); i++) {
				JSONObject testUser = USER_ARRAY.getJSONObject(i);

				if (testUser.getString(USER_DISCORD_ID_JSON).equals(user.getIdAsString())) {
					JSONArray testArray = testUser.getJSONArray(USER_ACCOUNTS_JSON);
					
					if (testArray.length() == 1) {
						JSONObject accountObject = testArray.getJSONObject(0);
						String rsName = accountObject.getString(ACCOUNT_NAME_JSON);
						
						if (!accountObject.getBoolean(ACCOUNT_CAPPED_JSON)) {
							if (capped) {
								testUser.put(USER_CAPPED_TIMES_JSON, testUser.getInt(USER_CAPPED_TIMES_JSON) + 1);
								testUser.put(USER_CAPPED_WEEK_JSON, capped);
								testUser.put(USER_WEEKS_SINCE_LAST_CAPPED_JSON, 0);
								
								accountObject.put(ACCOUNT_CAPPED_JSON, capped);
								accountObject.put(ACCOUNT_TIMES_CAPPED_JSON, accountObject.getInt(ACCOUNT_TIMES_CAPPED_JSON) + 1);
								
								Settings.set(S.CURRENT_CAPPERS, Settings.CURRENT_CAPPERS + 1);
																	
								ServerTextChannel announcements = server.getChannelById((String) Settings.get(S.ANNOUNCE_ID)).get().asServerTextChannel().get();
								
								MessageBuilder messageBuilder = new MessageBuilder();
								
								EmbedBuilder embed = new EmbedBuilder();
								
								embed.setAuthor(server.getMemberById(Settings.BOT_MEMBER_ID).get());
								embed.setColor(Color.decode("#03fce7"));
								embed.setTitle(user.getDisplayName(server) + " has capped. :billed_cap:");
								embed.addField(Math.random() >= 0.5 ? "Thanks a Million!" : "Awesome work!", "> `" + rsName + "` has capped for the week, why not join in?");
								embed.setThumbnail(user.getAvatar());
								embed.setFooter(Reference.MSG.EMBED_FOOTER);
								embed.setTimestampToNow();

								if (resourceNameIn != null) {
									embed.addField("Gathered Resource:", "> " + (Settings.RESOURCE_EMOJI ? "  <:" + resourceNameIn.toLowerCase() + ":" + resourceEmojiIn + "> ・ " : " ・ ") + "`" + (resourceNameIn.substring(0, 1).toUpperCase() + resourceNameIn.substring(1)) + " (" + resourceAmountIn + ")`");
								}
								
								if (message.getAttachments().size() > 0) {
									MessageAttachment attachment = message.getAttachments().get(0);
									
									if (attachment.isImage()) {
										BufferedImage image;
																				
										try {
											image = attachment.asImage().get();
											embed.addField("Proof image:", "");
											embed.setImage(image);
										} catch (InterruptedException e) {
											e.printStackTrace();
										} catch (ExecutionException e) {
											e.printStackTrace();
										}
									}
								}
								
								messageBuilder.setContent(user.getMentionTag());
								messageBuilder.addEmbed(embed);
								
								messageBuilder.send(announcements);
								JavaHelper.sendSystemMessage(LVL.INFO, "Cap announcement sent for user: " + DiscordHelper.getUserInfo(server, user, "account: " + rsName));
								message.reply("Successfully updated your capped status for account: `" + rsName + "`");
								
								saveSystem();
							} else {
								message.reply("You have already capped this week on account: `" + rsName + "`");
							}
						} else {
							message.reply("You have already capped this week on account: `" + rsName + "`");
						}
						
					} else {
						message.reply("You have multiple accounts registered, please specify which account you would like to cap on.");
					}
				}
			}
		}
	}

	public static void capUser(Server server, Message message, User user, String rsName, String resourceNameIn, String resourceEmojiIn, int resourceAmountIn, boolean capped) {		
		if (checkUserIsReged(user)) {
			if ((boolean) Settings.get(S.LOCKED)) {
				message.reply("`You cannot cap at the moment, as the Citadel is locked.`");
				return;
			}
			
			for (int i = 0; i < USER_ARRAY.length(); i++) {
				JSONObject testUser = USER_ARRAY.getJSONObject(i);

				if (testUser.getString(USER_DISCORD_ID_JSON).equals(user.getIdAsString())) {
					JSONArray testArray = testUser.getJSONArray(USER_ACCOUNTS_JSON);
					
					for (int j = 0; j < testArray.length(); j++) {
						JSONObject accountObject = testArray.getJSONObject(j);
						String testName = accountObject.getString(ACCOUNT_NAME_JSON);
						
						if (testName.equals(rsName)) {
							if (!accountObject.getBoolean(ACCOUNT_CAPPED_JSON)) {
								if (capped) {
									testUser.put(USER_CAPPED_TIMES_JSON, testUser.getInt(USER_CAPPED_TIMES_JSON) + 1);
									testUser.put(USER_CAPPED_WEEK_JSON, capped);
									testUser.put(USER_WEEKS_SINCE_LAST_CAPPED_JSON, 0);
									
									accountObject.put(ACCOUNT_CAPPED_JSON, capped);
									accountObject.put(ACCOUNT_TIMES_CAPPED_JSON, accountObject.getInt(ACCOUNT_TIMES_CAPPED_JSON) + 1);
									
									Settings.set(S.CURRENT_CAPPERS, Settings.CURRENT_CAPPERS + 1);
																		
									ServerTextChannel announcements = server.getChannelById((String) Settings.get(S.ANNOUNCE_ID)).get().asServerTextChannel().get();
									
									MessageBuilder messageBuilder = new MessageBuilder();
									
									EmbedBuilder embed = new EmbedBuilder();
									
									embed.setAuthor(server.getMemberById(Settings.BOT_MEMBER_ID).get());
									embed.setColor(Color.decode("#03fce7"));
									embed.setTitle(user.getDisplayName(server) + " has capped. :billed_cap:");
									embed.addField(Math.random() >= 0.5 ? "Thanks a Million!" : "Awesome work!", "> `" + rsName + "` has capped for the week, why not join in?");
									embed.setThumbnail(user.getAvatar());
									embed.setFooter(Reference.MSG.EMBED_FOOTER);
									embed.setTimestampToNow();
	
									if (resourceNameIn != null) {
										embed.addField("Gathered Resource:", "> " + (Settings.RESOURCE_EMOJI ? "  <:" + resourceNameIn.toLowerCase() + ":" + resourceEmojiIn + "> ・ " : " ・ ") + "`" + (resourceNameIn.substring(0, 1).toUpperCase() + resourceNameIn.substring(1)) + " (" + resourceAmountIn + ")`");
									}
									
									if (message.getAttachments().size() > 0) {
										MessageAttachment attachment = message.getAttachments().get(0);
										
										if (attachment.isImage()) {
											BufferedImage image;
																					
											try {
												image = attachment.asImage().get();
												embed.addField("Proof image:", "");
												embed.setImage(image);
											} catch (InterruptedException e) {
												e.printStackTrace();
											} catch (ExecutionException e) {
												e.printStackTrace();
											}
										}
									}
									
									messageBuilder.setContent(user.getMentionTag());
									messageBuilder.addEmbed(embed);
									
									messageBuilder.send(announcements);
									JavaHelper.sendSystemMessage(LVL.INFO, "Cap announcement sent for user: " + DiscordHelper.getUserInfo(server, user, "account: " + rsName));
									message.reply("Successfully updated your capped status for account: `" + rsName + "`");
									
									saveSystem();
								} else {
									message.reply("You have already capped this week on account: `" + rsName + "`");
								}
							} else {
								message.reply("You have already capped this week on account: `" + rsName + "`");
							}
						}
					}
				}
			}
		} else {
			message.reply(Reference.MSG.NOT_REGISTERED);
		}
		
		saveSystem();
	}
	
	public static void getUserAccountsCapStatus(Server serverIn, Message message, User user) {
		EmbedBuilder embed = new EmbedBuilder();

		if (checkUserIsReged(user)) {
			//embed.setAuthor(serverIn.getMemberById(Settings.BOT_MEMBER_ID).get());
			embed.setColor(Color.CYAN);
			embed.setTitle("Registration Check for: `" + user.getDisplayName(serverIn) + "`");
			embed.setDescription("Below is the capping status of all your registered accounts.");
			
			for (int i = 0; i < USER_ARRAY.length(); i++) {
				JSONObject testUser = USER_ARRAY.getJSONObject(i);
				
				if (testUser.getString(USER_DISCORD_ID_JSON).equals(user.getIdAsString())) {
					JSONArray testArray = testUser.getJSONArray(USER_ACCOUNTS_JSON);
					
					int times_capped = testUser.getInt(USER_CAPPED_TIMES_JSON);
					embed.addField("Total Caps:", "> You have capped `" + times_capped + "` times.");
					
					int weeks_since_last_capped = testUser.getInt(USER_WEEKS_SINCE_LAST_CAPPED_JSON);
					embed.addField("Last Recorded Cap:", "> It has been: `" + weeks_since_last_capped + (weeks_since_last_capped == 1 ? "` week" : "` weeks") + " since you last capped.");
					
					boolean capped_this_week = testUser.getBoolean(USER_CAPPED_WEEK_JSON);
					embed.addField("This Week:", "> You `" + (capped_this_week ? "have" : "have not") + "` capped this week.");
					
					for (int j = 0; j < testArray.length(); j++) {
						JSONObject testObject = testArray.getJSONObject(j);
						String testName = testObject.getString(ACCOUNT_NAME_JSON);
						boolean capped = testObject.getBoolean(ACCOUNT_CAPPED_JSON);
						Integer times = testObject.getInt(ACCOUNT_TIMES_CAPPED_JSON);
						
						embed.addField((capped ? ":white_check_mark:" : ":red_square:") + " ・ " + testName, "> " + (capped ? Reference.MSG.CAPPED : Reference.MSG.NOT_CAPPED) + "\n > You have capped: `" + times + (times == 1 ? "` time" : "` times") + " on this account.");
					}
				}
			}
	
			embed.setThumbnail(user.getAvatar());
			embed.setFooter(Reference.MSG.EMBED_FOOTER);
			embed.setTimestampToNow();

			message.reply(embed);
			JavaHelper.sendSystemMessage(LVL.INFO, "<!check> Successful check for user: " + DiscordHelper.getUserInfo(serverIn, user, ""));
		} else {
			message.reply(Reference.MSG.NOT_REGISTERED);
			JavaHelper.sendSystemMessage(LVL.INFO, "<!check> User: " + DiscordHelper.getUserInfo(serverIn, user, "") + " is not registered");
		}
	}
	
	public static void resetAllResourceGoals(Server serverIn, Message message, User user) {
		for (int i = 0; i < Settings.RESOURCES.length(); i++) {
			JSONObject testResource = Settings.RESOURCES.getJSONObject(i);
			
			testResource.put(Reference.JSON.R.GOAL, 0);
		}
		
		if (message != null) {
			message.reply("`All resource goals have been set to 0.`");
			JavaHelper.sendSystemMessage(LVL.INFO, "<!reset> Resource goals have been reset by" + (user != null ? " by user: " + DiscordHelper.getUserInfo(serverIn, user, "") : "."));
		}
		saveSystem();
	}
	
	public static void resetAllUserCaps(Server serverIn, Message message, User user) {
		for (int i = 0; i < USER_ARRAY.length(); i++) {
			JSONObject testUser = USER_ARRAY.getJSONObject(i);

			JSONArray testArray = testUser.getJSONArray(USER_ACCOUNTS_JSON);
			
			for (int j = 0; j < testArray.length(); j++) {
				JSONObject testObject = testArray.getJSONObject(j);
				
				testObject.put(ACCOUNT_CAPPED_JSON, false);
			}

			if (!testUser.getBoolean(USER_CAPPED_WEEK_JSON)) {
				testUser.put(USER_WEEKS_SINCE_LAST_CAPPED_JSON, testUser.getInt(USER_WEEKS_SINCE_LAST_CAPPED_JSON) + 1);
			}
			
			testUser.put(USER_CAPPED_WEEK_JSON, false);
		}
		if (message != null) {
			message.reply("`All users + account cap status has been set to false`");
			JavaHelper.sendSystemMessage(LVL.INFO, "<!reset> Citadel cap status has been reset" + (user != null ? " by user: " + DiscordHelper.getUserInfo(serverIn, user, "") : "."));
		}
		saveSystem();
	}
	
	public static void resetTickState(Server serverIn) {
		Settings.set(S.PREVIOUS_CAPPERS, Settings.CURRENT_CAPPERS);
		resetAllUserCaps(serverIn, null, null);
		resetAllResourceGoals(serverIn, null, null);
		Settings.set(S.WEEK_CAPPED, true);
		Settings.set(S.GOAL_COMPLETE, false);
		Settings.set(S.CURRENT_CAPPERS, 0);
		Settings.set(S.OPEN, false);
	}
	
	public static void setGoalState(boolean state) {
		Settings.set(S.GOAL_COMPLETE, true);
	}
	
	public static boolean checkUserIsReged(User messageUser) {
		boolean registered = false;

		for (int i = 0; i < USER_ARRAY.length(); i++) {
			JSONObject test = USER_ARRAY.getJSONObject(i);

			if (test.getString(USER_DISCORD_ID_JSON).equals(messageUser.getIdAsString())) {
				registered = true;
			}
		}
		
		return registered;
	}

	public static void saveUsers() throws IOException {
		File users = Reference.USERS;
		JSONArray a = USER_ARRAY;
		String jsonstr = a.toString(2);
		Settings.writeFile(jsonstr, users, StandardCharsets.UTF_8);
	}

	public static void loadUsers() throws IOException {
		File users = Reference.USERS;
		String usersInfo = Settings.readFile(users, StandardCharsets.UTF_8);
		USER_ARRAY = new JSONArray();

		JSONArray a = new JSONArray(usersInfo);
		for (int i = 0; i < a.length(); i++) {
			JSONObject item = a.getJSONObject(i);

			USER_ARRAY.put(item);
		}
	}
	
	public static void saveSystem() {
		try {
			saveUsers();
		} catch (IOException e) { }

		Settings.saveSettings();
		Settings.saveResources();
	}

	public static void loadSystem() {
		try {
			loadUsers();
		} catch (IOException e) { }
	}

	public static void shutdown(DiscordApi api) {
		timers.clear();
		saveSystem();
		JavaHelper.sleep(5);
		api.disconnect();
		JavaHelper.sleep(10);
		System.exit(0);
	}
}