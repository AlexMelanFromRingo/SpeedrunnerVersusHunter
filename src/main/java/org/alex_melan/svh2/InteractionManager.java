package org.alex_melan.svh2;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.UUID;

public class InteractionManager implements Listener {

    private static final HashMap<UUID, Boolean> playerInteractionMap = new HashMap<>();

    public static void setPlayerInteraction(Player player, boolean canInteract) {
        playerInteractionMap.put(player.getUniqueId(), canInteract);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!playerInteractionMap.getOrDefault(event.getPlayer().getUniqueId(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!playerInteractionMap.getOrDefault(event.getPlayer().getUniqueId(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!playerInteractionMap.getOrDefault(player.getUniqueId(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!playerInteractionMap.getOrDefault(player.getUniqueId(), true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!playerInteractionMap.getOrDefault(event.getPlayer().getUniqueId(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!playerInteractionMap.getOrDefault(event.getPlayer().getUniqueId(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!playerInteractionMap.getOrDefault(event.getPlayer().getUniqueId(), true)) {
            String message = event.getMessage();
            if (!message.startsWith("/register") && !message.startsWith("/reg") && !message.startsWith("/login") && !message.startsWith("/l")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("Please log in!");
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (!playerInteractionMap.getOrDefault(player.getUniqueId(), true)) {
                event.setCancelled(true);
            }
        }
    }
}
