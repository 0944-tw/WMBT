package com.zeroninefivefive.wcnm.Arena.GameMap;

import com.zeroninefivefive.wcnm.Arena.GameMap.Stage.KillerSpawn;
import com.zeroninefivefive.wcnm.Arena.GameMap.Stage.Stage;

import java.util.List;

public class GameMap {
    public String name;
    public String folder_name;
    public Position survivor_spawn;
    public KillerSpawn killer_spawn;
    public List<Stage> stages;
}

