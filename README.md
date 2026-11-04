# AdvancedBan


Bukkit- & BungeeCord-Plugin at once <br>
Check out our [Spigot-Page](https://www.spigotmc.org/resources/advancedban.8695/) for more  information!

![Minecraft Version 1.7.10-26.3](https://img.shields.io/badge/supports%20minecraft%20versions-1.7.10--26.3-brightgreen.svg)
![license GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-lightgrey.svg)
[![Unit tests](https://github.com/DevLeoko/AdvancedBan/actions/workflows/test.yml/badge.svg)](https://github.com/DevLeoko/AdvancedBan/actions/workflows/test.yml)

_Coded by Leoko_ 

## Description
AdvancedBan is an All-In-One Punishment-System with warns, tempwarns, mutes, tempmutes, bans, tempbans, ipbans and kicks.
There is also a PlayerHistory so you can see the players past punishments and 
the plugin has configurable Time & Message-Layouts which automatically calculate and increase the Punishment-Time for certain reasons.
AdvancedBan provides also a full Message-File so you can change and translate all messages & a detailed config-file with a lot of useful settings.
This is a BungeeCord & Bukkit/Spigot-Plugin in one and it supports MySQL and Local-File-Storage.

Requires Java 8 or newer.

## New in v3
### Alt detection
AdvancedBan remembers which accounts have been using which IP. Logins are forgotten after 90 days by default.
- `/alts [Name/IP]` lists the accounts sharing an IP and whether they are banned or muted (permission: `ab.alts`)
- Players with the permission `ab.alts.notify` get alerted if someone joins who has been using the same IP as a banned account
- `AltDetection.AutoBan` in the config.yml also bans accounts joining from an IP used by a banned account. It's off by default, as people sharing an internet connection would get banned as well. It only works on servers in online mode, as names can't be trusted otherwise

### Punishment ladders
`/punish (-s) [Name] [Offence] <Details>` (permission: `ab.punish`) picks the punishment from the steps of an offence, configured in the Layouts.yml.
Every further punishment for the same offence uses the next step. Revoking a punishment does not move a player back down.
```yaml
Ladder:
  Hacking:
    Reason: "Using a hacked client"
    Steps:
    - 'tempban 7d'
    - 'tempban 30d'
    - 'ban'
```

### PlaceholderAPI
These placeholders are available on Bukkit servers if [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) is installed:

| Placeholder | Value |
|---|---|
| `%advancedban_is_banned%`, `%advancedban_is_muted%` | yes or no, as configured in PlaceholderAPI |
| `%advancedban_warns%`, `%advancedban_notes%` | The amount of active warnings or notes |
| `%advancedban_ban_reason%`, `%advancedban_mute_reason%` | The reason |
| `%advancedban_ban_operator%`, `%advancedban_mute_operator%` | Who issued it |
| `%advancedban_ban_remaining%`, `%advancedban_mute_remaining%` | The remaining time |
| `%advancedban_ban_end%`, `%advancedban_mute_end%` | The date it ends |
| `%advancedban_ban_date%`, `%advancedban_mute_date%` | The date it was issued |
| `%advancedban_ban_id%`, `%advancedban_mute_id%` | The ID |

Online players are always up to date. Values of offline players get loaded in the background and can be up to 30 seconds old, so the very first request for an offline player is answered as if there was no punishment.

## Building
Run `mvn package` with JDK 17 or newer. The plugin can be found at `bundle/target/AdvancedBan-Bundle-*.jar`.

## API
To use the API you need to add AdvancedBan to your project and declare it as a dependency in the plugin.yml.

Add AdvancedBan to you project by adding the AdvancedBan.jar to your build-path or as a:
#### Maven dependency in your pom.xml

Example Usage from Jitpack:
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
...
<dependency>
  <groupId>com.github.DevLeoko</groupId>
  <artifactId>AdvancedBan</artifactId>
  <version>v2.3.0</version>
</dependency>
```
Note: Jitpack also supports dependencies for gradle!

[AdvancedBan on Jitpack](https://jitpack.io/#DevLeoko/AdvancedBan)


You can use this API for both Spigot and Bungeecord plugins.
Check out the [Java Docs](https://devleoko.github.io/AdvancedBan/) to get started.
