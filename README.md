# AntiDoubleAccount Plugin

**Version:** 1.0.0  
**Platform:** Paper / Bukkit / Spigot  

## Description
AntiDoubleAccount is a Minecraft server plugin designed to prevent multiple accounts from connecting simultaneously from the same IP address. It helps server administrators reduce account sharing, manage whitelisted IPs, and keep detailed connection logs.

## Features
- **Prevent multiple connections per IP**: Automatically kicks players if another player is already connected from the same IP.
- **Customizable messages**: All kick messages, logs, and notifications can be modified in `messages.yml`.
- **Commands**:
  - `/antidouble allow <ip>` → Add an IP to the whitelist.
  - `/antidouble remove <ip>` → Remove an IP from the whitelist.
  - `/antidouble list` → Show all whitelisted IPs.
- **Connection logs**: Logs every connection with date, player, and IP address.
- **Magic kick messages**: Supports Minecraft formatting codes (`&k`, `&c`, `&r`, etc.) for stylish kick messages.

## Configuration
- **messages.yml**: Customize all plugin messages.
- **logs/connections.log**: Stores all connection attempts and IPs.

## Permissions
- `antidouble.allow` → Allows use of `/antidouble allow`
- `antidouble.remove` → Allows use of `/antidouble remove`
- `antidouble.list` → Allows use of `/antidouble list`

## Installation
1. Place the `AntiDoubleAccount.jar` file in the `plugins` folder.
2. Start the server to generate the default configuration files.
3. Edit `messages.yml` to customize messages.
4. Restart the server.

## Notes
- Works on Paper, Spigot, and Bukkit servers.
- Fully compatible with Minecraft 1.19+ (adjust API version in build if needed).
