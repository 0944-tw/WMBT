package com.zeroninefivefive.wcnm.Arena.Stages;

import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;

public class Position {
    public int x, y, z;
    public Location getBukkitLocation(@Nullable World World){
        return new Location(World,x,y,z);
    }
}