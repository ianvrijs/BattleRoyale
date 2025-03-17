# Battle Royale Plugin

A Battle Royale plugin for Bukkit/Spigot servers that features team-based gameplay, gulag system, and dynamic storm mechanics.

## Features

- Team-based Battle Royale gameplay
- Storm system with shrinking border
- Sumo redemption system
- Custom death messages
- Team management with friendly-fire protection
- Tab list with game state indicators
- Configurable game settings

## Commands

### Game Control
- `/br start` - Starts the Battle Royale match
- `/br stop` - Stops the current match

### Team Management
- `/br team create <player1> <player2>` - Creates a new team
- `/br team add <player> <teamID>` - Adds a player to a team
- `/br team leave` - Leaves current team
- `/br team invite <player>` - Invites a player to your team
- `/br team accept <team>` - Accepts a team invitation
- `/br team deny <team>` - Denies a team invitation
- `/br team list <page>` - Lists all teams
- `/br team remove <player>` - Removes a player from your team

### Setup
- `/br setup setlobby` - Sets the lobby spawn location
- `/br setup setmapradius <radius>` - Sets the radius of the map
- `/br setup setstormspeed <blocks per second>` - Sets the speed of the storm
- `/br setup setgulag <1|2>` - Sets the gulag arena spawn points
- `/br setup setgulagheight <radius>` - Sets the height players should fall below to get eliminated
- `/br setup setgracetime <seconds>` - Sets the grace period before the storm starts

## Installation

1. Clone the repository and build the project using ```mvn clean install```
2. Place the `.jar` file in your server's `plugins` folder
3. Start/restart the server
4. Configure the plugin settings

