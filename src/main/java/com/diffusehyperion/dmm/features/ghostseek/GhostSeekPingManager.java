package com.diffusehyperion.dmm.features.ghostseek;

import java.util.ArrayList;
import java.util.List;

public class GhostSeekPingManager {
    public final List<GhostSeekPingSequence> ghostSeekPingSequences = new ArrayList<>();

    public GhostSeekPingSequence addPing(GhostSeekPing ghostSeekPing) {
        for (GhostSeekPingSequence sequence : ghostSeekPingSequences) {
            for (GhostSeekPing ping : sequence.getGhostSeekPings()) {
                if (ping.origin().distanceToSqr(ghostSeekPing.origin()) > Math.pow(ping.maxDistance() + ghostSeekPing.maxDistance(), 2)) {
                    sequence.addPing(ghostSeekPing);
                    return sequence;
                }
            }
        }

        GhostSeekPingSequence newSequence = new GhostSeekPingSequence();
        newSequence.getGhostSeekPings().add(ghostSeekPing);
        ghostSeekPingSequences.add(newSequence);
        return newSequence;
    }
}
