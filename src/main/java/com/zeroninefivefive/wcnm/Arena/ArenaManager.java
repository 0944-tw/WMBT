package com.zeroninefivefive.wcnm.Arena;

  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.zeroninefivefive.wcnm.Arena.GameMap.GameMap;
  import com.zeroninefivefive.wcnm.Arena.GameMap.Position;
  import com.zeroninefivefive.wcnm.Main;
  import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
  import org.mvplugins.multiverse.core.MultiverseCoreApi;
 import org.mvplugins.multiverse.core.utils.result.Attempt;
 import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
 import org.mvplugins.multiverse.core.world.WorldManager;
 import org.mvplugins.multiverse.core.world.options.CloneWorldOptions;
 import org.mvplugins.multiverse.core.world.reasons.CloneFailureReason;
 import org.mvplugins.multiverse.external.vavr.control.Option;


 import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
  import java.util.*;

public class ArenaManager {
    private final Main plugin;
    public Map<String, Arena> LobbyLists;
    public Map<String, String> WorldUUID_to_ArenaUUID;
    public enum GameState {
        IN_GAME,
        STAGE,
        COUNTDOWN
    }



    public ArenaManager(Main plugin) {
        this.plugin = plugin;
        this.LobbyLists = new HashMap<>();
        this.WorldUUID_to_ArenaUUID = new HashMap<>();
    }

    public Arena GetArenaFromArenaUUID(String UUID){

        return null;
    }

    public Arena GetArenaFromWorldUUID (String UUID) {
        return LobbyLists.get(UUID);
    }

    public String CreateGameLobby(@Nullable String Map) throws IOException {
        if (Map == null) {
            plugin.getLogger().warning("No Map Selected, Choosing Random Map Instead");
        }
        File file = new File(plugin.getDataFolder(), "eh_maps.yml");

        MultiverseCoreApi coreApi = MultiverseCoreApi.get();
        String gameLobbyId =  UUID.randomUUID().toString();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<GameMap> avaliableGameMaps;
        // Deb
        plugin.getLogger().info("Game Instance Created With ID: " + gameLobbyId);
        // Need to Jackson |
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> rawGameMaps = (List<Map<String, Object>>) config.getList("maps");
        List<GameMap> parsedStages = new ArrayList<>();

        for (Map<String, Object> map : rawGameMaps) {
            GameMap stage = mapper.convertValue(map, GameMap.class);
            parsedStages.add(stage);
        }
        // Jackson
        avaliableGameMaps = parsedStages;


        GameMap WMBT_Random_Map = avaliableGameMaps.get(new Random().nextInt(avaliableGameMaps.size()));
        Position SurvivorSpawn = WMBT_Random_Map.survivor_spawn;
        Option<LoadedMultiverseWorld> lw = coreApi.getWorldManager().getLoadedWorld((String) WMBT_Random_Map.folder_name);
        if (!lw.isDefined()) throw new IllegalStateException("World not found: " + WMBT_Random_Map.folder_name);
        Attempt<LoadedMultiverseWorld, CloneFailureReason> CloneWorldAttempt = coreApi.getWorldManager().cloneWorld(CloneWorldOptions.fromTo(lw.get(),"wmbt_" + gameLobbyId));
        if (CloneWorldAttempt.isSuccess()) {

            CloneWorldAttempt.get().setSpawnLocation(SurvivorSpawn.getBukkitLocation(null));
            Arena lobby = new Arena(plugin,gameLobbyId,CloneWorldAttempt.get().getBukkitWorld().get(),WMBT_Random_Map);
            lobby.lobbyName = "MyLobby";
            lobby.MAX_PLAYERS = 10;
            // lobby.Players = new HashMap<>();
            lobby.LobbyUUID = gameLobbyId;
            lobby.SpawnPosition = SurvivorSpawn;
            WorldUUID_to_ArenaUUID.put(CloneWorldAttempt.get().getUID().toString(),gameLobbyId);
            LobbyLists.put(gameLobbyId,lobby);
            lobby.PreStart();
        }
        return gameLobbyId;
    }

    public List<Arena> GetArenaLists(){
        return new ArrayList<Arena>(LobbyLists.values());
    }
    public void StopGame(String LobbyId) {
        Arena SArena = LobbyLists.get(LobbyId);
        if (SArena == null) {
           throw new NoSuchElementException("Lobby Not Exists");
        }
        SArena.Stop();
    }

    public void JoinGame(Player Player, String LobbyId) {
        MultiverseCoreApi coreApi = MultiverseCoreApi.get();
        WorldManager worldManager = coreApi.getWorldManager();
        Option<LoadedMultiverseWorld> LobbyWorld = worldManager.getLoadedWorld("wmbt_" + LobbyId);
        if (!LobbyWorld.isDefined()) {
            plugin.getLogger().warning("Game Lobby World Not Foujnd");
            return;
        }
        if (!LobbyLists.containsKey(LobbyId)) {
            plugin.getLogger().warning("Game Lobby Instance Not Foujnd");
            return;
        }
        Arena ArenaGrenade = LobbyLists.get(LobbyId);
        ArenaGrenade.AttemptToJoin(Player);
        plugin.getLogger().info("Teleporting");

       // Player.teleport(new Location(LobbyWorld.get().getBukkitWorld().get(), (Integer) SpawnPos.get("x"),(Integer) SpawnPos.get("y"), (Integer) SpawnPos.get("z")));
    }
}
