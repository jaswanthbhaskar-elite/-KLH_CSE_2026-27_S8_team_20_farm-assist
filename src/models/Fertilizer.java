package models;

public class Fertilizer {
    private final String name;
    private final String usedFor;   // e.g. crop or nutrient deficiency it addresses
    private final int availableSupply; // used later by max-flow module

    public Fertilizer(String name, String usedFor, int availableSupply) {
        this.name = name;
        this.usedFor = usedFor;
        this.availableSupply = availableSupply;
    }

    public String getName() { return name; }
    public String getUsedFor() { return usedFor; }
    public int getAvailableSupply() { return availableSupply; }

    @Override
    public String toString() {
        return String.format("%s | Used for: %s | Supply: %d", name, usedFor, availableSupply);
    }
}