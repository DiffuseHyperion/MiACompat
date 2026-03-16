package com.diffusehyperion.dmm.features.ghostseek;
//
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GhostSeekPingSequence {
    private final List<GhostSeekPing> ghostSeekPings = new ArrayList<>();
    public final Color colour = Color.getHSBColor((float) Math.random(), 1, 1);

    public List<GhostSeekPing> getGhostSeekPings() {
        return ghostSeekPings;
    }

    public void addPing(GhostSeekPing ghostSeekPing) {
        ghostSeekPings.add(ghostSeekPing);
    }
}
