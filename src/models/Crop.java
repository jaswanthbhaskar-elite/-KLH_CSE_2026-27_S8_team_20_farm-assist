package models;

public class Crop {
    private final String name;
    private final String soilType;
    private final String waterRequirement;   // Low / Medium / High (used for search)
    private final String season;             // Kharif / Rabi / Zaid (used for search)
    private final int landRequired;          // acres (used for DP)
    private final int budgetRequired;        // rupees (used for DP)
    private final int waterUnits;            // irrigation units (used for DP)
    private final int expectedProfit;        // rupees (used for DP)

    public Crop(String name, String soilType, String waterRequirement, String season,
                int landRequired, int budgetRequired, int waterUnits, int expectedProfit) {
        this.name = name;
        this.soilType = soilType;
        this.waterRequirement = waterRequirement;
        this.season = season;
        this.landRequired = landRequired;
        this.budgetRequired = budgetRequired;
        this.waterUnits = waterUnits;
        this.expectedProfit = expectedProfit;
    }

    public String getName() { return name; }
    public String getSoilType() { return soilType; }
    public String getWaterRequirement() { return waterRequirement; }
    public String getSeason() { return season; }
    public int getLandRequired() { return landRequired; }
    public int getBudgetRequired() { return budgetRequired; }
    public int getWaterUnits() { return waterUnits; }
    public int getExpectedProfit() { return expectedProfit; }

    @Override
    public String toString() {
        return String.format(
                "%s | Soil: %s | Water: %s | Season: %s | Land: %d acre(s) | Budget: Rs.%d | Water Units: %d | Profit: Rs.%d",
                name, soilType, waterRequirement, season, landRequired, budgetRequired, waterUnits, expectedProfit);
    }
}