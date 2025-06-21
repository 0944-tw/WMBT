package com.zeroninefivefive.wcnm.Arena;

import com.zeroninefivefive.wcnm.Arena.Stages.*;
import com.zeroninefivefive.wcnm.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.*;

public class Arena {

    private final Main plugin;
    private final Map<UUID, Player> spectators = new HashMap<>();
    private final Map<UUID, Player> killers = new HashMap<>();
    private final Map<UUID, Player> survivors = new HashMap<>();
    private final Map<UUID, Player> deaths = new HashMap<>();
    private final Map<UUID,Player> players = new HashMap<>();

    private World ArenaWorld;

    public String lobbyName;
    public Integer MAX_PLAYERS;
    public Position SpawnPosition;
    public Boolean GameStarted;
    private final GameMap MapData;
    private final List<Stage> Stages;

    private Boolean IsOpeningDoor;
    private Integer CurrentStage;

    public String LobbyUUID;

    private final Sidebar Sidebar;


    public Arena(Main Plugin, String LobbyUUID, World World, GameMap MapData) {

        super();
        this.plugin = Plugin;
        this.LobbyUUID = LobbyUUID;
        this.ArenaWorld = World;
        this.MapData = MapData;
        // this.Stages = (List<Stage>) MapData.get("stages");
        this.CurrentStage = 0;
        this.GameStarted = false;
        this.Sidebar = plugin.Scoreboard.createSidebar();
        // ChatGPT Converted


        this.Stages = MapData.stages;
        this.IsOpeningDoor = false;

        Sidebar.title(Component.text("亡命奔逃"));
        Sidebar.line(0, Component.empty());
        Sidebar.line(1, Component.text("時間：0"));
        Sidebar.line(2, Component.text("你的身份：逃亡者"));
        Sidebar.line(3, Component.empty());
        Sidebar.line(4, Component.text("127.0.0.1:25565"));

    }

    public void PreStart() {
        new BukkitRunnable() {
            int time = 10;

            public void run() {
                if (time <= 0) {
                    if (ArenaWorld.getPlayers().size() < 2) {
                        time = 25;
                        broadcastMessageInArena(Component.text("人數不足！").color(TextColor.color(255, 0, 0)));
                        return;
                    }
                    cancel();
                    StartGame();
                    return;
                }
                Sidebar.line(1, Component.text("時間：").append(Component.text(time).color(NamedTextColor.YELLOW)));

                for (int i = 0; i < ArenaWorld.getPlayers().size(); i++) {
                    ArenaWorld.getPlayers().get(i).sendActionBar(Component.text(time + " 秒後開始"));
                }
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20 ticks = 1 second

    }

    public void StartGame() {
        plugin.getLogger().info("Game ID:" + LobbyUUID + " Is Starting");
        List<Player> Players = ArenaWorld.getPlayers();
        Player Killer = Players.get(new Random().nextInt(Players.size()));
        KillerSpawn Map_KillerSpawn = MapData.killer_spawn;

        for (Player LPlayer : Players) {
            LPlayer.getInventory().clear();
            LPlayer.setGameMode(GameMode.ADVENTURE);
            if (LPlayer.getUniqueId().equals(Killer.getUniqueId())) continue;
            survivors.put(LPlayer.getUniqueId(), LPlayer);
        }
        killers.put(Killer.getUniqueId(), Killer);
        for (Player SKiller : killers.values()) {
            ItemStack sword = new ItemStack(Material.IRON_SWORD);
            ItemMeta itemMeta = sword.getItemMeta();
            itemMeta.displayName(Component.text("西瓜刀").color(NamedTextColor.RED));
            ArrayList<Component> lore = new ArrayList<Component>();
            lore.add(Component.text("拼夕夕上面9.99包郵的西瓜刀"));
            lore.add(Component.text("看起來十分銳利，實際上它一點也不銳利"));
            lore.add(Component.text("美國製造 中國生產"));
            itemMeta.lore(lore);

            sword.addEnchantment(Enchantment.SHARPNESS, 3);
            SKiller.give(sword);
            SKiller.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 255));
            SKiller.teleport(Map_KillerSpawn.spawn.getBukkitLocation(ArenaWorld));
        }

        for (Player SelectedSurvivor : survivors.values()) {
            SelectedSurvivor.give(new ItemStack(Material.BOW));
            SelectedSurvivor.give(new ItemStack(Material.ARROW, 64));

        }
        final TextComponent textComponent = Component.text("亡命奔逃")
                .color(TextColor.color(0xFFFFFF)).decorate(TextDecoration.BOLD)
                .appendNewline()
                .append(Component.text("在時間結束和殺手殺死你們之前逃出去！"));
        broadcastMessageInArena(textComponent);
        this.GameStarted = true;
        // Killer Door
        new BukkitRunnable() {
            int time = 10;

            public void run() {
                if (time <= 0) {
                    fillBlocks(Map_KillerSpawn.door.start_pos.getBukkitLocation(ArenaWorld),Map_KillerSpawn.door.end_pos.getBukkitLocation(ArenaWorld),Material.AIR);
                    cancel();
                }
                broadcastMessageInArena(Component.text("殺手將在 " + time + " 秒後被釋出！"));
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20 ticks = 1 second
        // Game Start
        new BukkitRunnable() {
            int time = 60 * 30;

            public void run() {
                if (Players.isEmpty()) {
                    cancel();
                    return;
                }
                if (time <= 0) {
                    cancel();
                    Stop();
                    return;
                }
                if (Sidebar.closed()) {
                    cancel();
                    return;
                }
                Sidebar.line(1, Component.text("時間：").append(Component.text(time).color(NamedTextColor.YELLOW)));
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20 ticks = 1 second
    }

    // Utility
    private void fillBlocks(Location Location1, Location Location2, Material TargetMaterial) {
        for (int x =  Math.min(Location1.getBlockX(), Location2.getBlockX()); x <= Math.max(Location1.getBlockX(), Location2.getBlockX()); x++) {
            for (int y =   Math.min(Location1.getBlockY(),Location2.getBlockY()); y <= Math.max(Location1.getBlockY(),Location2.getBlockY()); y++) {
                for (int z =   Math.min(Location1.getBlockZ(),Location2.getBlockZ()); z <= Math.max(Location1.getBlockZ(),Location2.getBlockZ()); z++) {
                    if (TargetMaterial == Material.AIR) {
                        ArenaWorld.getBlockAt(x, y, z).breakNaturally(true);
                    } else {
                        ArenaWorld.getBlockAt(x, y, z).setType(TargetMaterial);
                    }
                }
            }
        }
    }
    // Broadcast
    public void broadcastMessageInArena(Component Content) {
        for (Player player : ArenaWorld.getPlayers()) {
            player.sendMessage(Content);
        }
    }

    public void broadcastActionbarInArena(Component Content) {
        for (Player player : ArenaWorld.getPlayers()) {
            player.sendActionBar(Content);
        }
    }

    // Broadcast
    public void ActivateDoor(PlayerInteractEvent event) {
        Stage StageData = Stages.get(this.CurrentStage);
        if (Stages.get(this.CurrentStage) == null) return;
        if (GameStarted == false) {
            event.getPlayer().sendMessage(Component.text("遊戲還沒開始！你不能這樣做！").color(NamedTextColor.RED));
            return;
        }
        if (IsOpeningDoor) {
            event.getPlayer().sendMessage(Component.text("666開了！").color(NamedTextColor.RED));
            return;
        }
        Position ActivatePosition = StageData.activate.pos;
        double Activate_X = ((Number) ActivatePosition.x).doubleValue();
        double Activate_Y = ((Number) ActivatePosition.y).doubleValue();
        double Activate_Z = ((Number) ActivatePosition.z).doubleValue();
        Location InteractBlockPosition = event.getClickedBlock().getLocation();
        this.IsOpeningDoor = true;
        if (Activate_X == InteractBlockPosition.x() & Activate_Y == InteractBlockPosition.y() & Activate_Z == InteractBlockPosition.z()) {
            new BukkitRunnable() {
                int time = StageData.time;

                public void run() {
                    if (time < 0) {
                        if (StageData.door != null) {
                            broadcastMessageInArena(Component.text("大門[" + CurrentStage + "] 成功開啟").color(NamedTextColor.GREEN));
                            Position StartDoorPos = StageData.door.start_pos;
                            Position EndDoorPos = StageData.door.end_pos;
                            fillBlocks(StartDoorPos.getBukkitLocation(ArenaWorld),EndDoorPos.getBukkitLocation(ArenaWorld),Material.AIR);
                        }
                        if (CurrentStage >= Stages.size()) {
                                broadcastMessageInArena(Component.text("逃生者獲勝！").color(NamedTextColor.GREEN));
                        }
                        IsOpeningDoor = false;
                        CurrentStage += 1;
                        cancel();
                    }
                    if (time % 5 == 0) {
                        broadcastMessageInArena(Component.text("大門[" + CurrentStage + "] ").color(NamedTextColor.YELLOW).append(Component.text(time).color(NamedTextColor.RED)).append(Component.text(" 秒後開啟")).color(NamedTextColor.YELLOW));
                    }

                    time--;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        } else {
            plugin.getLogger().warning(String.format("Position Mismatch, Activate Position: %s,%s,%s | Require Position: %s", Activate_X, Activate_Y, Activate_Z, InteractBlockPosition.toString()));
        }

    }

    public void MarkPlayerAsDead(Player Player) {
        Player Survivor = survivors.get(Player.getUniqueId());
        if (Survivor == null) return;
        Survivor.setGameMode(GameMode.SPECTATOR);
        deaths.put(Survivor.getUniqueId(), Survivor);
        spectators.put(Survivor.getUniqueId(), Survivor);
        survivors.remove(Survivor.getUniqueId());
        if (survivors.isEmpty()) {
            // Killer Wins
            broadcastActionbarInArena(Component.text("殺手獲勝！"));
            this.Stop();
        }
    }

    private void Cleanup() {
        plugin.arenaEvents.removeArenaFromCache(ArenaWorld.getUID().toString());
        survivors.clear();
        spectators.clear();
        deaths.clear();
        killers.clear();
        // Players = null;
        ArenaWorld = null;
    }

    public void UnexpectedStop(String Title, String Subtitle){
        for (Player player : ArenaWorld.getPlayers()) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendTitle(Title,Subtitle,20,100,20);
        }
    }
    public void Stop() {
        new BukkitRunnable() {
            int time = 10;

            public void run() {
                if (time <= 0) {
                    cancel();
                    for (Player player : ArenaWorld.getPlayers()) {
                        player.setGameMode(GameMode.ADVENTURE);
                        player.clearActivePotionEffects();
                        player.clearActiveItem();
                        player.teleport(new Location(Bukkit.getServer().getWorld("world"), 0, 0, 0));
                    }
                    Cleanup();

                    return;
                }
                broadcastActionbarInArena(Component.text("將在 " + time + " 後將你送回大廳"));
                time--;
            }

        }.runTaskTimer(plugin, 0L, 20L); // 20 ticks = 1 second

        Sidebar.close();

    }

    public void AttemptToJoin(Player Player) {
        if (GameStarted) {
            Player.setGameMode(GameMode.SPECTATOR);
        }
        Sidebar.addPlayer(Player);
        players.put(Player.getUniqueId(),Player);
        Player.teleport(SpawnPosition.getBukkitLocation(ArenaWorld));
    }

    public void AttemptToLeave(Player Player) {
        players.remove(Player.getUniqueId());
        Sidebar.removePlayer(Player);
        if (killers.get(Player.getUniqueId()) != null) {
            // Player Is Killer
            if (killers.size() - 1 <= 0) {
                UnexpectedStop(ChatColor.RED + "殺手人數不足","殺手人數不足導致遊戲無法正常進行，已強制結束");
                Stop();
            }
            killers.remove(Player.getUniqueId());
        }
        if (survivors.get(Player.getUniqueId()) != null) {
            // Player Is Survivors
            if (survivors.size() - 1 <= 0) {
                UnexpectedStop(ChatColor.RED + "逃亡者不足","逃亡者人數不足導致遊戲無法正常進行，已強制結束");

                Stop();
            }
            survivors.remove(Player.getUniqueId());
        }

    }
}
