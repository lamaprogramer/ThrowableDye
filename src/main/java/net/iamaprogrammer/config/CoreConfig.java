package net.iamaprogrammer.config;

import java.util.Map;

public class CoreConfig {
    private Map<String, String> outliers;

    public CoreConfig(){}

    public void setOutliers(Map<String, String> outliers) {
        this.outliers = outliers;
    }

    public Map<String, String> getOutliers() {
        return this.outliers;
    }
}
