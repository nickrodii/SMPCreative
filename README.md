# <div align="center">SMPCreative</div>

[![Minecraft](https://img.shields.io/badge/Minecraft-PaperMC-4FC08D?style=for-the-badge)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge)](https://www.oracle.com/java/technologies/downloads/)
[![Build](https://img.shields.io/badge/Build-Maven-blue?style=for-the-badge)](https://maven.apache.org/)
[![Website](https://img.shields.io/badge/website-nickrodi.com-lightgrey?style=for-the-badge)](https://nickrodi.com)

**SMPCreative** is a Paper Minecraft plugin designed for small survival multiplayer servers. 
It allows players to teleport to a dedicated **Creative world** to test builds, redstone, or designs **without affecting their survival gameplay at all**.

### [<div align="center">Download on Modrinth</div>](https://modrinth.com/project/smpcreative)

This plugin is designed to be **plug and play**, essentially being a tool that any beginner server admin can drag and drop into their plugins folder to play with.
Permissions are preset to only give players creative mode and other operator permissions in the creative world.

---

## Key Features

### Automatic World Generation
- Automatically generates a dedicated flat world
- Sets safe gamerules for build testing like disallowing natural mob spawning and no daylight/weather cycle

##

### Full Inventory & Player State Separation
Survival and Creative player states are completely separated, including:
- Inventory contents
- Ender chest
- Health, hunger, experience
- Active potion effects

##

### Advancement Revocation
- Automatically detects advancements earned in the Creative world
- Instantly revokes them upon re-entering survival to ensure creative environment doesn't give cheated progress

##

### Drag & Drop WorldEdit Integration
If **WorldEdit** is installed:
- Grants `worldedit.*` permissions **only while in the Creative world**
- Non-OP players will have their WorldEdit permissions instantly revoked upon re-entering survival. No permission plugin needed!
- Enforces a configurable **block change limit** for non-OP players to prevent server lag / crashing (default: `50,000`)

##

### Custom Teleportation & Utility Commands
Includes Creative-only travel systems:
- **/rtp**: teleports the player to a random coordinate to give a blank slate to build on
- **Teleport Requests**: Players can request to tp to others *only in creative mode* using the `/tp` command
- **Personal Warps**:
  - Set personal warps within the creative world to keep track of multiple builds in different locations
  - view all warps created (with a clickable GUI in chat to warp to each one)
  - Instantly teleport to any warp created

---

## Installation

1. Ensure you have a working [installation of PaperMC](https://papermc.io/downloads/paper) on your Minecraft server
2. Download the latest `.jar` release from [Modrinth](https://modrinth.com/project/smpcreative)
3. Place it into your server’s `plugins` folder
4. *(Optional)* Install **WorldEdit** into the `plugins` folder as well to be able to utilize WorldEdit features in the creative world
5. Restart your server

The plugin will automatically generate the creative world (`world_creative`) on first load!

---

## Commands & Permissions

| Command | Permission | Description |
|--------|------------|-------------|
| `/creative` | `smpcreative.creative` | Teleport to the Creative world |
| `/survival` | `smpcreative.survival` | Return to the Survival world |
| `/spawn` | `smpcreative.spawn` | Teleport to the Creative world spawn *(Creative only)* |
| `/rtp` | `smpcreative.rtp` | Teleport to random coordinates in the Creative world *(Creative only)* |
| `/warp <name>` | `smpcreative.warp` | Teleport to a personal warp already added *(Creative only)* |
| `/warp add <name>` | `smpcreative.warp` | Create a personal warp at your current location *(Creative only)* |
| `/warp list` | `smpcreative.warp` | List your personal warps |
| `/tp <player>` | `smpcreative.tp` | Send a teleport request *(Creative only)* |
| `/tpaccept` | `smpcreative.tp.accept` | Accept a pending teleport request *(Creative only)* |
| `/tpdecline` | `smpcreative.tp.accept` | Decline a pending teleport request *(Creative only)* |

**Default permissions:**  
All permissions are set to `true` only while in the Creative world, and are set to `false` otherwise.

---

## Configuration

Customize behavior in `config.yml`:

```yaml
#
#     SMPCreative CONFIGURATION
#
#        Created by nickrodi!
#

# Name of the creative world that players are sent to upon entering /creative.
# This usually shouldn't be changed, only mess with this if you know what you're doing.
creative_world_name: world_creative

# WorldEdit block change limit applied to non-op players inside the creative world.
# This only matters if WorldEdit is installed alongside this plugin.
# Be careful changing this as making it too high can cause instability and may crash the server!
worldedit_block_limit: 50000
```
