package models;

public class Disease {
    private final String name;
    private final String symptoms;
    private final String treatment;

    public Disease(String name, String symptoms, String treatment) {
        this.name = name;
        this.symptoms = symptoms;
        this.treatment = treatment;
    }

    public String getName() { return name; }
    public String getSymptoms() { return symptoms; }
    public String getTreatment() { return treatment; }

    @Override
    public String toString() {
        return String.format("%s | Symptoms: %s | Treatment: %s", name, symptoms, treatment);
    }
}