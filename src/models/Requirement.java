package models;

public class Requirement {
    private final String cropName;
    private final String fertilizerName;
    private final int demand;

    public Requirement(String cropName, String fertilizerName, int demand) {
        this.cropName = cropName;
        this.fertilizerName = fertilizerName;
        this.demand = demand;
    }

    public String getCropName() {
        return cropName;
    }

    public String getFertilizerName() {
        return fertilizerName;
    }

    public int getDemand() {
        return demand;
    }

    @Override
    public String toString() {
        return String.format(
            "%s needs %d unit(s) of %s",
            cropName, demand, fertilizerName
        );
    }
}