<img src="https://cdn.modrinth.com/data/cached_images/01d4b3f0a8d469b8d7b36030f2039007500b00f4.png" align="right" height="64">

# Multiworld ![](http://cf.way2muchnoise.eu/multiworld-mod.svg) ![](http://cf.way2muchnoise.eu/versions/multiworld-mod.svg)

The Multiworld Mod - Adds support for creating & teleporting to multiple worlds. 

<a href="https://modrinth.com/mod/multiworld/versions?l=fabric"><img src="https://cdn.modrinth.com/data/cached_images/1b54a3f3b03745c57beaa1ab11d9d86b9222a41a.png" width="160"></a>
<a href="https://modrinth.com/mod/multiworld/versions?l=neoforge"><img src="https://cdn.modrinth.com/data/cached_images/a073c4dc33587010c5b7f0386d3df9e1b0eee3ed.png" width="160"></a>
 
## Command Usage:
| Command       | About                                      | Example |
|---------------|--------------------------------------------|---------|
| /mw           | View Help                                  |
| /mw list      | List all Worlds                            | 
| /mw tp        | Teleport to a World                        | /mw tp minecraft:overworld |
| /mw spawn     | Teleport to the Spawn of the current World | 
| /mw setspawn  | Sets the Spawn of the current World        |  
| /mw create    | Creates a new World                        | /mw create myLovelyWorld NORMAL -g=FLAT -s=1234 |
| /mw delete    | Delete a World (Console Only)              | /mw delete myWorld |

#### Gamerules & Difficulty
| Command        | About                                      | Example                            |
|----------------|--------------------------------------------|------------------------------------|
| /mw gamerule   | Gamerule Support for Custom Worlds         | /mw gamerule doDaylightCycle false |
| /mw difficulty | Set the Difficulty for the current World   | /mw difficulty EASY                |

## Portals <img src="https://static.wikia.nocookie.net/minecraft_gamepedia/images/0/03/Nether_portal_%28animated%29.png/revision/latest?cb=20191114182303" width="128" float="right" align="right">
The latest version of Multiworld introduces Portals. 
Portals lead to a Destination, which can be either a World *(`myWorld`)*, another Portal *(`p:myOtherPortal`)*, or exact cords *(`w:myWorld:0,0,0`)*.

To make a Portal use the Portal Wand, given by *`/mw portal wand`*. While holding the wand item, like in WorldEdit, Left & Right click the blocks to select the corners of the portal frame. The selected region will be used upon usage of the portal create command.

### Portal Commands
TBD
| Command           | About                                      | Example                                         |
|-------------------|--------------------------------------------|-------------------------------------------------|
| /mw portal        | View Help                                  |
| /mw portal create | Create a new Portal from the Wand Area     | /mw create myPortal myWorld                     |
| /mw portal wand   | Get a Portal Wand, to make a portal area   | Select the Obsidian Corners of the Portal Frame |


## Permissions

Multiworld supports either LuckPerms or CyberPerms.
The permission `multiworld.admin` or being `/op`-ed grants access to every command.

| Command |     |
|------|-----|
| /mw  | multiworld.cmd |
| /mw tp | multiworld.tp |
| /mw spawn | multiworld.spawn |
| /mw setspawn | multiworld.setspawn |
| /mw create <id> <dim> [-g=GENERATOR -s=SEED] | multiworld.create |
| /mw gamerule | multiworld.gamerule |
etc..
 
## Coming Soon

- World delete command
- Custom generator suppport
- Custom Portal support

## License & Credits

Multiworld is licensed under the terms of the [LGPL v3](LICENSE).

Note: Multiworld makes use of the Fantasy library by NucleoidMC for creation of runtime worlds, (also LGPLv3).

For the Forge version, a tiny exerpt from [Fabric Dimensions v1](https://github.com/FabricMC/fabric/blob/1.18/fabric-dimensions-v1/src/main/java/net/fabricmc/fabric/impl/dimension/FabricDimensionInternals.java#L45) is used & is licensed under Apache License v2.0
