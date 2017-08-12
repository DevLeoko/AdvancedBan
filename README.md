# AdvancedBan


Bukkit- & BungeeCord-Plugin at once <br>
Check out our [Spigot-Page](https://www.spigotmc.org/resources/advancedban.8695/) for more  information!

![Minecraft Version 1.7-1.12](https://img.shields.io/badge/supports%20minecraft%20versions-1.7--1.12-brightgreen.svg)
![license GLP-3.0](https://img.shields.io/badge/license-GLP--3.0-lightgrey.svg)
![latest build](https://img.shields.io/jenkins/s/http/v.skamps.eu/jenkins/job/AdvancedBanV2.svg)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a84ecbeefd4d4eca864152d72adfde9c)](https://www.codacy.com/app/DevLeoko/AdvancedBan?utm_source=github.com&utm_medium=referral&utm_content=DevLeoko/AdvancedBan&utm_campaign=badger)

_Coded by Leoko_ 

## Description
AdvancedBan is an All-In-One Punishment-System with warns, tempwarns, mutes, tempmutes, bans, tempbans, ipbans and kicks.
There is also a PlayerHistory so you can see the players past punishments and 
the plugin has configurable Time & Message-Layouts which automatically calculate and increase the Punishment-Time for certain reasons.
AdvancedBan provides also a full Message-File so you can change and translate all messages & a detailed config-file with a lot of useful settings.
This is a BungeeCord & Bukkit/Spigot-Plugin in one and it supports MySQL and Local-File-Storage.

## API
To use the API just add the AdvancedBan.jar to your BuildPath and as a dependency in your Plugin.yml
You can use this API in Bukkit/Spigot-Plugins but also in Bungee-Plugins.
The API-Methods can be accessed trough PunishmentManager.get()
Here are the currently available methods:

``` Java
//To get a PlayerUUID use:
UUIDManager.get().getUUID("NAME");

PunishmentManager.get().getPunishments(checkExpired);
// Returns a list of active punishments
// checkExpired - If it should check for expired punishments & remove them | should be on true

PunishmentManager.get().getPunishments(uuid, type, current);
// Returns a list of Punishments
// uuid - Players uuid | null = all players
// type - Type of the punishments PunishmentType-Enum | null = all types
// current - If only currently active punishments should be displayed | true = currently active | false = all
// Example:
PunishmentManager.get().getPunishments(UUIDManager.get().getUUID("Leoko"), PunishmentType.WARNING, true);
// Would return all active Warnings for the player Leoko

PunishmentManager.get().getBan(uuid);
// Returns a Punishment of the type Ban, IPBan or Tempban | returns null if player is not banned

PunishmentManager.get().getMute(uuid);
// Returns a Punishment of the type Mute or Tempmute | returns null if player is not muted

PunishmentManager.get().getMute(id);
// Returns a Punishment of the type Warn or Tempwarn | returns null if there is no warn with that id

PunishmentManager.get().isBanned/isMuted(uuid);
// Returns whether a player is banned or muted

PunishmentManager.get().getCurrentWarns(uuid);
// Returns the count of a players current warnings

//Create a new Punishment:
new Punishment(name, uuid, reason, operator, type, start, end, calculation, id).create();
// name - The users name
// uuid - The users uuid
// reason - The punishment reason | Can be null or a layout like "#LayoutName" or a basic reason "Hacking in FFA"
// operator - The one who banned | You can just use "CONSOLE"
// type - The Type of the Punishment from the PunishmentType-Enum
// start - The current time | use TimeManager.getTime()
// end - The end of the punishment | -1 for perma or TimeManager.getTime() + millisecs for temp
// calculation - TimeLayout | Can just be set to null
// id - has to be set to -1
```
