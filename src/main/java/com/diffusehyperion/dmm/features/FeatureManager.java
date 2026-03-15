package com.diffusehyperion.dmm.features;

import com.diffusehyperion.dmm.features.ghostseek.GhostSeekFeature;

import java.util.ArrayList;
import java.util.List;

public class FeatureManager {
    public static List<Feature> features = new ArrayList<>();

    public FeatureManager() {
        features.add(new GhostSeekFeature());
    }

    public void initializeFeatures() {
        for (Feature feature : features) {
            feature.initialize();
        }
    }

    public void closeFeatures() {
        for (Feature feature : features) {
            feature.close();
        }
    }

    public Feature getFeature(Class<? extends Feature> featureClass) {
        return features.stream().filter(feature -> feature.getClass().equals(featureClass)).findFirst().orElse(null);
    }
}
