package io;

import models.Crop;
import models.Disease;
import models.Fertilizer;
import models.Requirement;
import models.Symptom;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

public static List<Crop> loadCrops(String filePath) {
        List<Crop> crops = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|");
                if (parts.length != 8) continue; // skip malformed rows
                crops.add(new Crop(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        Integer.parseInt(parts[4].trim()),
                        Integer.parseInt(parts[5].trim()),
                        Integer.parseInt(parts[6].trim()),
                        Integer.parseInt(parts[7].trim())
                ));
            }
        } catch (IOException e) {
            System.out.println("Error loading crops file: " + e.getMessage());
        }
        return crops;
    }

public static List<Requirement> loadRequirements(String filePath) {
        List<Requirement> requirements = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|");
                if (parts.length != 3) continue;
                requirements.add(new Requirement(
                        parts[0].trim(),
                        parts[1].trim(),
                        Integer.parseInt(parts[2].trim())
                ));
            }
        } catch (IOException e) {
            System.out.println("Error loading requirements file: " + e.getMessage());
        }
        return requirements;
    }

    public static List<Disease> loadDiseases(String filePath) {
        List<Disease> diseases = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|");
                if (parts.length != 3) continue;
                diseases.add(new Disease(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim()
                ));
            }
        } catch (IOException e) {
            System.out.println("Error loading diseases file: " + e.getMessage());
        }
        return diseases;
    }

    public static List<Symptom> loadSymptoms(String filePath) {
        List<Symptom> symptoms = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|");
                if (parts.length != 2) continue; // skip malformed rows
                symptoms.add(new Symptom(
                        parts[0].trim(),
                        parts[1].trim()
                ));
            }
        } catch (IOException e) {
            System.out.println("Error loading symptoms file: " + e.getMessage());
        }
        return symptoms;
    }

    public static List<Fertilizer> loadFertilizers(String filePath) {
        List<Fertilizer> fertilizers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|");
                if (parts.length != 3) continue;
                fertilizers.add(new Fertilizer(
                        parts[0].trim(),
                        parts[1].trim(),
                        Integer.parseInt(parts[2].trim())
                ));
            }
        } catch (IOException e) {
            System.out.println("Error loading fertilizers file: " + e.getMessage());
        }
        return fertilizers;
    }
}