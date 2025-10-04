package org.alex_melan.svh2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class SVH2 extends JavaPlugin implements Listener {

    UUID targetUUID;
    List<UUID> gamePlayers = new ArrayList<>();
    private int newGameTime = 30;
    private int runTime = 90;
    boolean allowCompass = false;
    private int compassTime = 180;
    private int seconds = 0;

    @Override
    public void onEnable() {
        // Listeners
        getServer().getPluginManager().registerEvents(new Events(), this);
        getServer().getPluginManager().registerEvents(new InteractionManager(), this);
        getServer().getPluginManager().registerEvents(this, this);

        // Tracker Command
        Objects.requireNonNull(this.getCommand("tracker")).setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) {
                if (args.length > 0) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        targetUUID = target.getUniqueId();
                        player.sendMessage("Compass is now tracking " + target.getName());

                    } else {
                        player.sendMessage("Could not find player " + args[0]);
                    }
                } else {
                    player.sendMessage("Please specify a player to track.");
                }
            }
            return true;
        });

        // Track Target
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (targetUUID != null) {
                Player target = Bukkit.getPlayer(targetUUID);
                if (target != null && target.isOnline()) {
                    World world = target.getWorld();
                    if (!world.getName().equals("world_nether") && !world.getName().equals("world_end")) {
                        for (UUID playerUUID : gamePlayers) {
                            Player player = Bukkit.getPlayer(playerUUID);
                            if (player != null && !playerUUID.equals(targetUUID)) {
                                player.setCompassTarget(target.getLocation());
                            }
                        }
                    }
                }
            }
        }, 0L, 1L);

        // Timer
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID playerUUID : gamePlayers) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null) {
                    // Enable player's interaction
                    InteractionManager.setPlayerInteraction(player, true);

                    // Calculate minutes and seconds
                    int minutes = seconds / 60;
                    //int remainingSeconds = seconds % 60;

                    // Send the timer to the action bar
                    //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(String.format("Timer: %02d:%02d", minutes, remainingSeconds)));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(String.format("Timer: %02d:%02d", minutes, seconds)));

                    // Increment the number of seconds
                    seconds++;
                }
            }
        }, 0L, 20L);

        // Join Game Command
        Objects.requireNonNull(this.getCommand("join_game")).setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) {
                if (!gamePlayers.contains(player.getUniqueId())) {
                    gamePlayers.add(player.getUniqueId());
                    player.sendMessage(Component.text("You have joined the game!", NamedTextColor.GREEN));

                    // Send a message to all players
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(Component.text("Player " + player.getName() + " joined.", NamedTextColor.YELLOW));
                    }
                } else {
                    player.sendMessage(Component.text("You are already in the game!", NamedTextColor.RED));
                }
            }
            return true;
        });

        // New Game Command
        Objects.requireNonNull(this.getCommand("new_game")).setExecutor((sender, command, label, args) -> {

            if (sender instanceof Player) {
                gamePlayers.clear();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " {\"text\":\"A new game is starting! Click \",\"color\":\"yellow\",\"extra\":[{\"text\":\"[HERE]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/join_game\"}}]}");
                    player.sendMessage(Component.text("Or you can join the game by typing /join_game in the chat.", NamedTextColor.YELLOW));
                }
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    for (UUID playerUUID : gamePlayers) {
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null) {
                            // Teleport the player to the new world
                            player.teleport(player.getWorld().getSpawnLocation());

                            // Set the player's spawn point to the new world's spawn location
                            player.setBedSpawnLocation(player.getWorld().getSpawnLocation(), true);

                            // Очистить инвентарь
                            player.getInventory().clear();

                            // Восстановить здоровье
                            player.setHealth(20.0);

                            // Снять все эффекты
                            for (PotionEffect effect : player.getActivePotionEffects())
                                player.removePotionEffect(effect.getType());

                            // Восстановить сытость
                            player.setFoodLevel(20);

                            // Очистка достижений
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke " + player.getName() + " everything");

                            // Disable player's interaction
                            if (playerUUID != targetUUID) {
                                InteractionManager.setPlayerInteraction(player, false);
                            }

                            // Gamemode
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                    }
                    seconds = 0;
                },newGameTime * 20L);

                Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
                    for (UUID playerUUID : gamePlayers) {
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null) {
                            // Enable player's interaction
                            InteractionManager.setPlayerInteraction(player, true);
                        }
                        // Send a message to all players
                        player.sendMessage(Component.text("The hunters have unfrozen", NamedTextColor.RED));

                    }
                }, (newGameTime + runTime) * 20L);

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    for (UUID playerUUID : gamePlayers) {
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null && playerUUID != targetUUID) {
                            // Создание нового компаса
                            ItemStack compass = new ItemStack(Material.COMPASS);

                            // Добавление компаса в инвентарь игрока
                            player.getInventory().addItem(compass);

                            // Send a message to all players
                            player.sendMessage(Component.text("The hunters got a compass", NamedTextColor.RED));
                        }
                    }
                    allowCompass = true;
                }, (newGameTime + runTime + compassTime) * 20L);
            }
            return true;
        });

        // Команда /set_new_game_time
        Objects.requireNonNull(this.getCommand("set_new_game_time")).setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player && args.length > 0) {
                try {
                    newGameTime = Integer.parseInt(args[0]);
                    //sender.sendMessage("New game time set to " + newGameTime + " seconds.");
                    for (UUID playerUUID : gamePlayers) {
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null) {
                            player.sendMessage(Component.text("New game time set to " + newGameTime + " seconds.", NamedTextColor.YELLOW));
                        }
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid number format. Please enter a valid number of seconds.");
                }
            }
            return true;
        });

        // Команда /set_run_time
        Objects.requireNonNull(this.getCommand("set_run_time")).setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player && args.length > 0) {
                try {
                    runTime = Integer.parseInt(args[0]);
                    //sender.sendMessage("Run time set to " + runTime + " seconds.");
                    for (UUID playerUUID : gamePlayers) {
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null) {
                            player.sendMessage(Component.text("Run time set to " + runTime + " seconds.", NamedTextColor.YELLOW));
                        }
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid number format. Please enter a valid number of seconds.");
                }
            }
            return true;
        });

        // Команда /compass_time
        Objects.requireNonNull(this.getCommand("compass_time")).setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player && args.length > 0) {
                try {
                    compassTime = Integer.parseInt(args[0]);
                    //sender.sendMessage("Compass time set to " + compassTime + " seconds.");
                    for (UUID playerUUID : gamePlayers) {
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null) {
                            player.sendMessage(Component.text("Compass time set to " + compassTime + " seconds.", NamedTextColor.YELLOW));
                        }
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid number format. Please enter a valid number of seconds.");
                }
            }
            return true;
        });
    }

    // Spectator for gamers
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUniqueId();

        // Проверка, является ли умерший игрок целевым игроком
        if (playerUUID.equals(targetUUID)) {
            for (UUID gamePlayerUUID : gamePlayers) {
                Player gamePlayer = Bukkit.getPlayer(gamePlayerUUID);
                if (gamePlayer != null) {
                    // Перевод игрока в режим наблюдателя
                    gamePlayer.setGameMode(GameMode.SPECTATOR);
                }
            }
        }
    }

    // Compass For Hunters
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Проверка, является ли возродившийся игрок целевым игроком
        if (!playerUUID.equals(targetUUID) && gamePlayers.contains(playerUUID) && allowCompass) {
            // Создание нового компаса
            ItemStack compass = new ItemStack(Material.COMPASS);

            // Добавление компаса в инвентарь игрока
            player.getInventory().addItem(compass);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
