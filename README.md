![Version](https://img.shields.io/badge/VERSION-3.0-informational?style=for-the-badge) ![LICENSE](https://img.shields.io/badge/LICENSE-Custom-red?style=for-the-badge) ![Repo Size](https://img.shields.io/github/repo-size/TheCosmicNebula/RS3-CitadelBot?label=REPO%20SIZE&style=for-the-badge) ![Open Issues](https://img.shields.io/github/issues/TheCosmicNebula/RS3-CitadelBot?style=for-the-badge) ![Closed Issues](https://img.shields.io/github/issues-closed/TheCosmicNebula/RS3-CitadelBot?color=green&style=for-the-badge)

# RS3-CitadelBot
A Discord bot to track RuneScape 3 Citadel reset ticks, capping and resources.

This Bot is run locally on your machine, or a server. You can also use a hosting service.

This Bot may be available without a local running in the future, depentant on popularity.

## Setup
- Head to the [Discord Developer Portal](https://discord.com/developers/applications)
- Follow the instructions to [Setup a Bot Application](https://discord.com/developers/docs/getting-started)

Once you have a Discord Bot Application setup follow the below instructions to setup the Bot for your server.
### Bot Setup
 - Enter the token generated by the Discord Application into the "token.txt" file, **do not** add any other characters.
 - Open the "settings.txt" file, and fill in the below information:
   - Input the Server ID inside the "".
   - Input the Admin Role ID inside the "". This will be used for users who can control system settings of the Bot.
   - Input the Ping Role ID inside the "". This role will be used to ping users who wish to receive pings when Citadel Events occur.
   - Input the Commands Channel ID inside the "". This will be the channel used to input commands to the Bot.
   - Input the Announcements Channel ID inside the "". It is recommended to restrict sending messages to Admin Roles.
   - Input the Command Prefix of your choosing. This is defauled to !.
   - Ignore the Citadel reset day and time, as this can be changed via the Commands Channel with !tick.
   - Change the Activity type (default: 3, WATCHING) if you so wish, and Input the Activity description (Usually the name of your clan) inside of the "".
   - Ignore the Timer Interval, as this can be changed via the commands channel with !interval. (Warning: anything more than a 10 minute interval may lead to the Bot not announcing Citadel Reset Ticks.
   - Input your chosen Clan World, and your Clan name.

Once you have added the Bot to your server, you can input !commands into the chosen commands channel to see a list of the available commands.

### Data Security
Ensure that you keep data generated by this Bot safe:
- Ensure that you do not host this Bot on public storage.
- Ensure that you do not share files such as the "token.txt", "settings.txt" and "users.txt" with anyone you do not explicitly trust, as these can be used to gain access to your server, or user information. (Most of the data stored by this Bot is publicly available, however data such as RS3 Usernames and your Token are not public information, and should be protected)

## Licence
This repo does not have a licence. This means that by default, it is All Rights Reserved. However, due to having complete control over what rights that includes, here is a list of what you can and cannot do:

## You can:
 - Modify the code for your own personal private use, so long as the modified code is significantly dissimilar from the original code.
 - Use the pull request feature.
 - Create a Fork of this repository if you satisfy the following conditions:
   - You do not change the package base name. (com.tcn or com.tcn.citadelbot)
   - You do not remove or change any references to the original Bot Author.
   - You are fixing a bug within the code.
   - Or you intend to commit to any of the actions listed below under the "You Cannot" section.
 - Use or copy small portions of code (such as a single method) for personal private use.
 - Copy any classes that have the #OPEN tag in the comment after the Imports.

## You Cannot:
 - Copy large chunks of code, entire Class files or any assets contained within the Bot.
 - Redistribute the Bot, unless the link you provide is the original. (GitHub)
 - Redistribute the Bot under a different name or Author.

## Development Team:
- TheCosmicNebula (Main Author)

### Development Team Applications:
 - Please feel free to email: thecosmicnebula_@outlook.com

| 2023 @ TheCosmicNebula | TCN | All Rights Reserved (Except Where Specified) |
