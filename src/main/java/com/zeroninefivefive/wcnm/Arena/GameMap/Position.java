package com.zeroninefivefive.wcnm.Arena.GameMap;

import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;

public class Position {
    public double x, y, z;
    public Location getBukkitLocation(@Nullable World World){
        return new Location(World,x,y,z);
    }
}