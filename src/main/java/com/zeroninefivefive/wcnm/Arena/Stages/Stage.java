package com.zeroninefivefive.wcnm.Arena.Stages;

import com.zeroninefivefive.wcnm.Arena.Stages.Activate;
import com.zeroninefivefive.wcnm.Arena.Stages.Door;

import javax.annotation.Nullable;

public class Stage {
    public String name;
    public int time;
    public Activate activate;

    @Nullable
    public Door door;
}
