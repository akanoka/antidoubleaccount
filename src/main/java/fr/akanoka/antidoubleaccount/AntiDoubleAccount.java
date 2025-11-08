package fr.akanoka.antidoubleaccount;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class AntiDoubleAccount extends JavaPlugin implements Listener {

    private Map<String, String> playerIPs = new HashMap<>();
    private Set<String> allowedIPs = new HashSet<>();
    private File logFile;
    private File messagesFile;
    private File allowedIPsFile;
    private FileConfiguration messages;
    private FileConfiguration allowedIPsConfig;

    @Override
    public void onEnable() {
        getLogger().info("AntiDoubleAccount activé !");
        getServer().getPluginManager().registerEvents(this, this);

        // Création du dossier du plugin
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        // messages.yml
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // allowed_ips.yml
        allowedIPsFile = new File(getDataFolder(), "allowed_ips.yml");
        if (!allowedIPsFile.exists()) {
            try {
                allowedIPsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        allowedIPsConfig = YamlConfiguration.loadConfiguration(allowedIPsFile);
        allowedIPs.addAll(allowedIPsConfig.getStringList("ips"));

        // connections.log
        logFile = new File(getDataFolder(), "connections.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        saveAllowedIPs();
        getLogger().info("AntiDoubleAccount désactivé !");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        String ip = player.getAddress().getAddress().getHostAddress();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // Log IP + date
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            String logLine = messages.getString("log_format", "{date} - {player} connecté depuis {ip}");
            logLine = logLine.replace("{date}", date)
                             .replace("{player}", player.getName())
                             .replace("{ip}", ip);
            writer.println(logLine);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Vérifie si une autre personne utilise la même IP
        for (Map.Entry<String, String> entry : playerIPs.entrySet()) {
            if (entry.getValue().equals(ip) && !entry.getKey().equals(player.getName()) && !allowedIPs.contains(ip)) {
                String kickMsg = messages.getString("kick_double_ip", "&cUn autre joueur est déjà connecté depuis cette IP !");
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMsg));
                return;
            }
        }

        playerIPs.put(player.getName(), ip);

        // Message de bienvenue
        String welcomeMsg = messages.getString("welcome", "&aBienvenue sur le serveur !");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', welcomeMsg));
    }

    private void saveAllowedIPs() {
        allowedIPsConfig.set("ips", new ArrayList<>(allowedIPs));
        try {
            allowedIPsConfig.save(allowedIPsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("antidouble")) return false;

        if (!sender.hasPermission("antidouble.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("no_permission", "&cVous n'avez pas la permission pour cette commande.")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("usage", "&cUsage: /antidouble <allow|remove|list> [ip]")));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "allow":
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("usage", "&cUsage: /antidouble <allow|remove|list> [ip]")));
                    return true;
                }
                String ipAllow = args[1];
                if (allowedIPs.contains(ipAllow)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("allow_ip_exists", "&eIP {ip} est déjà dans la liste.").replace("{ip}", ipAllow)));
                    return true;
                }
                allowedIPs.add(ipAllow);
                saveAllowedIPs();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("allow_ip_added", "&aIP {ip} ajoutée à la liste des IP autorisées.").replace("{ip}", ipAllow)));
                break;

            case "remove":
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("usage", "&cUsage: /antidouble <allow|remove|list> [ip]")));
                    return true;
                }
                String ipRemove = args[1];
                if (!allowedIPs.contains(ipRemove)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("allow_ip_not_found", "&eIP {ip} n'existe pas dans la liste.").replace("{ip}", ipRemove)));
                    return true;
                }
                allowedIPs.remove(ipRemove);
                saveAllowedIPs();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("allow_ip_removed", "&cIP {ip} retirée de la liste des IP autorisées.").replace("{ip}", ipRemove)));
                break;

            case "list":
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("list_ips", "&aIP autorisées: {ips}").replace("{ips}", String.join(", ", allowedIPs))));
                break;

            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("unknown_command", "&cCommande inconnue !")));
                break;
        }

        return true;
    }
}
