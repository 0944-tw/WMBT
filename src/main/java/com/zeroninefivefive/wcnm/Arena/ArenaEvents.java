package com.zeroninefivefive.wcnm.Arena;

import com.zeroninefivefive.wcnm.Main;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class ArenaEvents implements Listener {
    private final Main plugin;

    private final Map<String,Arena> UUID_ArenaCache;

    public ArenaEvents(Main plugin) {
        super();
        this.plugin = plugin;
        this.UUID_ArenaCache = new WeakHashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private Arena getArenaFromCache(String World_UUID) {
        Arena CacheArena = UUID_ArenaCache.get(World_UUID);
        if (CacheArena == null) {
            if (plugin.arenaManager.GetArenaFromWorldUUID(World_UUID) == null) {
                plugin.getLogger().info("Arena from World UUID is null");
                return null;
            };
            Arena AM_Arena = plugin.arenaManager.GetArenaFromWorldUUID(World_UUID);
            UUID_ArenaCache.put(World_UUID,AM_Arena);
            return AM_Arena;
        } else {
            plugin.getLogger().info("CACHE OK");
        }
        return CacheArena;
    }

    public void removeArenaFromCache(String World_UUID){
        if (UUID_ArenaCache.get(World_UUID) != null) {
            UUID_ArenaCache.remove(World_UUID);
        }
    }

    @EventHandler
    public void onBowShot(EntityShootBowEvent event) {
        plugin.getLogger().info("ohio");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID WorldUUID = event.getPlayer().getWorld().getUID();
        if (getArenaFromCache(WorldUUID.toString()) != null) {
            event.setCancelled(true);
            getArenaFromCache(WorldUUID.toString()).MarkPlayerAsDead(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        UUID WorldUUID = event.getPlayer().getWorld().getUID();
        plugin.getLogger().info("PlayerInteractEvent");
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            plugin.getLogger().info("RIGHT_CLICK_BLOCK");
            if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.STONE_BUTTON) {
                plugin.getLogger().info("STONE BUTTON!!!");
                if (getArenaFromCache(WorldUUID.toString()) != null) {
                    getArenaFromCache(WorldUUID.toString()).ActivateDoor(event);
                } else {
                    plugin.getLogger().info("Arena not found");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
         Arena Arena = getArenaFromCache(event.getFrom().getWorld().getUID().toString());
         if (Arena != null & event.getFrom().getWorld() != event.getTo().getWorld()) {
             // Attempt to leave the arena
             Arena.AttemptToLeave(event.getPlayer());
         }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        UUID WorldUUID = event.getPlayer().getWorld().getUID();
        Arena Arena = getArenaFromCache(WorldUUID.toString());
        if (Arena != null) {
            Arena.AttemptToLeave(event.getPlayer());
        }
    }


}
