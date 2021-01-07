<img src="https://raw.githubusercontent.com/jeremynoesen/CourierNew/main/cnlogo.png" align="right"/>

# CourierNew

## About
CourierNew is a physical mail system for Spigot Minecraft servers that allows users to send letters in the form of books and receive them through couriers. Admins can use this to send letters to all players at once as a way of sending a message that people will be sure to see.

## Purpose
CourierNew is a built-from-scratch partial remake of an abandoned Bukkit plugin called [Courier](https://dev.bukkit.org/projects/courier). This was written as a means to bring similar functionality of the original plugin to more modern versions of Minecraft, as well as improve on the logic behind it.

## Usage

### Commands
- `/letter <message>` - Compose a new letter with the specified message. Minecraft color codes and \n linebreaks are allowed in the message. If holding a not-sent letter written by you, this command will append the specified message to the letter.
- `/post <player>` - Send a letter to a specified player. You can list multiple players by separating their usernames with a comma. Use the command with only `*` to send to all online players, or `**` to send to all players who have ever joined the server.
- `/unread` - Retrieve unread mail, if any.
- `/shred` - Delete the letter in your hand.
- `/shredall` - Delete all letters in your inventory.
- `/cnhelp` - Show the help message.
- `/cnreload` - Reload all configuration files.

### Permissions
- `couriernew.letter` - Allows players to write/edit letters
- `couriernew.post.one` - Allows players to send letters to one player at a time 
- `couriernew.post.multiple` - Allows players to send letters to multiple players at a time
- `couriernew.post.allonline` - Allows players to send letters to all online players
- `couriernew.post.all` - Allows players to send letters to all players who ever joined the server
- `couriernew.help` - Allows players to use the help command
- `couriernew.shred` - Allows players to shred a letter
- `couriernew.shredall` - Allows players to shred all in their inventory
- `couriernew.unread` - Allows players to retrieve unread mail
- `couriernew.reload` - Allows for reloading of configs

## Requirements
- Spigot 1.13.0 - 1.16.4
- Java 8

## Installation
To install the plugin, download the latest release, put it in your server plugins folder, and start or restart your server. This will generate the necessary files for configuration of the plugin, located in `plugins/CourierNew`.

## Configuration
After running for the first time, the default configs will be generated. The main configuration will look like this:
```yaml
receive-delay: 100
resend-delay: 2400
remove-delay: 200
spawn-distance: 5
protected-courier: true
courier-entity-type: VILLAGER
blocked-gamemodes: []
blocked-worlds: []
```
- `receive-delay` - This is the delay, in ticks, for when a letter should be received. This is used on join, after sending, and after leaving a blocked world or gamemode. 
- `resend-delay` - How long to wait, in ticks, before trying to resend a letter when the mail was not taken
- `remove-delay` - How long to wait, in ticks, after the courier spawns before removing it
- `spawn-distance` - How far away to spawn the courier from the player in blocks
- `protected-courier` - This will determine whether other players can grab their mail from a courier that isn't theirs.
- `blocked-gamemodes` - Gamemodes that receiving mail isn't allowed in. (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)
- `blocked-worlds` - Names of worlds that receiving is blocked in

For the message configuration, you can use color codes. You can also use the placeholder `$PLAYER$` in messages that have it by default to replace it with player name(s). The messages and their names should explain what they are used for.

The third configuration file is actually used to store outgoing mail. Don't modify this file unless you know exactly what you are doing!

## Demonstration
![Demonstration](demo.gif)

## Building
If you wish to build from source, a `build.gradle` is included to create the jar, as well as get dependencies if you import the project into your IDE. 

## Troubleshooting
Courier not spawning? 
- In the WorldGuard config, set `block-plugin-spawning` to false.
- Set `allow-npcs` to true in `server.properties`.
- If you are using EssentialsProtect, make sure to not block villager spawning. 
- Also check to make sure that no other plugins block mob spawning.

## Notice
This project is no longer in development.