import models.Crop;
import models.Disease;
import models.Fertilizer;
import service.FarmAssistService;
import service.FarmAssistService.CorpusSearchResult;
import service.FarmAssistService.DiseaseMatch;
import service.FarmAssistService.SymptomSearchResult;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        FarmAssistService service = new FarmAssistService(
                "data/crops.txt",
                "data/diseases.txt",
                "data/fertilizers.txt",
                "data/requirements.txt",
                "data/symptoms.txt"
        );

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {

            System.out.println("\n=== Farm Assist ===");
            System.out.println("1. Search Crop Information");
            System.out.println("2. Search Disease Information");
            System.out.println("3. Search Fertilizer Information");
            System.out.println("4. Crop Recommendation");
            System.out.println("5. Fertilizer Allocation");
            System.out.println("6. Search Disease by Multiple Symptoms");
            System.out.println("7. Search Agricultural Corpus (KMP + Rabin-Karp + Aho-Corasick)");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    runCropSearch(scanner, service);
                    break;

                case "2":
                    runDiseaseSearch(scanner, service);
                    break;

                case "3":
                    runFertilizerSearch(scanner, service);
                    break;

                case "4":
                    runCropRecommendation(scanner, service);
                    break;

                case "5":
                    runFertilizerAllocation(service);
                    break;

                case "6":
                    runSymptomSearch(scanner, service);
                    break;

                case "7":
                    runCorpusSearch(scanner, service);
                    break;

                case "8":
                    running = false;
                    System.out.println(
                            "Exiting Farm Assist. Goodbye!"
                    );
                    break;

                default:
                    System.out.println(
                            "Invalid option. Please try again."
                    );
            }
        }

        scanner.close();
    }

    // ==============================
    // CROP SEARCH — KMP (fixed, no algorithm prompt)
    // ==============================

    private static void runCropSearch(
            Scanner scanner,
            FarmAssistService service) {

        System.out.print("Search crops: ");
        String query = scanner.nextLine().trim();

        List<Crop> results = service.searchCrops(query);

        if (results.isEmpty()) {

            System.out.println(
                    "No matching crops found."
            );

        } else {

            System.out.println(
                    "--- Matching Crops (" +
                    results.size() +
                    ") ---"
            );

            results.forEach(System.out::println);
        }
    }

    // ==============================
    // DISEASE SEARCH — Rabin-Karp (fixed, no algorithm prompt)
    // ==============================

    private static void runDiseaseSearch(
            Scanner scanner,
            FarmAssistService service) {

        System.out.print("Search diseases: ");
        String query = scanner.nextLine().trim();

        List<Disease> results = service.searchDiseases(query);

        if (results.isEmpty()) {

            System.out.println(
                    "No matching diseases found."
            );

        } else {

            System.out.println(
                    "--- Matching Diseases (" +
                    results.size() +
                    ") ---"
            );

            results.forEach(System.out::println);
        }
    }

    // ==============================
    // FERTILIZER SEARCH — simple lookup (fixed, no algorithm prompt)
    // ==============================

    private static void runFertilizerSearch(
            Scanner scanner,
            FarmAssistService service) {

        System.out.print("Search fertilizers: ");
        String query = scanner.nextLine().trim();

        List<Fertilizer> results = service.searchFertilizers(query);

        if (results.isEmpty()) {

            System.out.println(
                    "No matching fertilizers found."
            );

        } else {

            System.out.println(
                    "--- Matching Fertilizers (" +
                    results.size() +
                    ") ---"
            );

            results.forEach(System.out::println);
        }
    }

    // ==============================
    // CROP RECOMMENDATION - DP
    // ==============================

    private static void runCropRecommendation(
            Scanner scanner,
            FarmAssistService service) {

        System.out.print(
                "Enter available land (acres): "
        );

        int land = Integer.parseInt(
                scanner.nextLine().trim()
        );

        System.out.print(
                "Enter available budget (Rs.): "
        );

        int budget = Integer.parseInt(
                scanner.nextLine().trim()
        );

        System.out.print(
                "Enter available water (units): "
        );

        int water = Integer.parseInt(
                scanner.nextLine().trim()
        );

        var result = service.recommendCrops(
                land,
                budget,
                water
        );

        if (result.selectedCrops.isEmpty()) {

            System.out.println(
                    "No crop combination fits within " +
                    "the given constraints."
            );

            return;
        }

        System.out.println("\nSelected Crops:");

        result.selectedCrops.forEach(
                crop -> System.out.println(
                        "- " + crop.getName()
                )
        );

        System.out.println(
                "Total Land: " +
                result.totalLand +
                " acre(s)"
        );

        System.out.println(
                "Total Budget: Rs." +
                result.totalBudget
        );

        System.out.println(
                "Total Water: " +
                result.totalWater +
                " unit(s)"
        );

        System.out.println(
                "Expected Profit: Rs." +
                result.totalProfit
        );
    }

    // ==============================
    // FERTILIZER ALLOCATION - EDMONDS-KARP
    // ==============================

    private static void runFertilizerAllocation(
            FarmAssistService service) {

        System.out.println(
                "\nAvailable Fertilizer Supply:"
        );

        service.getFertilizers().forEach(f ->
                System.out.println(
                        "- " +
                        f.getName() +
                        ": " +
                        f.getAvailableSupply() +
                        " unit(s)"
                )
        );

        System.out.println(
                "\nCrop Requirements:"
        );

        service.getRequirements().forEach(r ->
                System.out.println(
                        "- " + r
                )
        );

        var result =
                service.allocateFertilizers();

        System.out.println(
                "\nFertilizer Allocation:"
        );

        for (String[] allocation :
                result.allocations) {

            System.out.println(
                    allocation[0] +
                    " -> " +
                    allocation[1] +
                    ": " +
                    allocation[2]
            );
        }

        System.out.println(
                "\nTotal Allocated: " +
                result.maxFlow
        );

        int unmet =
                result.getUnmetDemand();

        if (unmet > 0) {

            System.out.println(
                    "Unmet Demand: " +
                    unmet +
                    " unit(s) could not be fulfilled " +
                    "due to limited supply."
            );

        } else {

            System.out.println(
                    "All crop fertilizer demands " +
                    "were fully satisfied."
            );
        }
    }

    // ==============================
    // MULTI-SYMPTOM SEARCH — AHO-CORASICK
    // ==============================

    private static void runSymptomSearch(
            Scanner scanner,
            FarmAssistService service) {

        System.out.print("Enter symptoms: ");
        String query = scanner.nextLine().trim();

        SymptomSearchResult result = service.searchBySymptoms(query);

        if (result.matchedSymptoms.isEmpty()) {
            System.out.println("No known symptoms matched your description.");
            return;
        }

        System.out.println("\nMatched Symptoms:");
        result.matchedSymptoms.forEach(s -> System.out.println("- " + s));

        if (result.rankedDiseases.isEmpty()) {
            System.out.println("\nNo diseases could be linked to the matched symptoms.");
            return;
        }

        System.out.println("\nPossible Diseases:");
        int rank = 1;
        for (DiseaseMatch dm : result.rankedDiseases) {
            String label = dm.matchCount == 1 ? "matching symptom" : "matching symptoms";
            System.out.println(rank + ". " + dm.diseaseName + " - " + dm.matchCount + " " + label);
            rank++;
        }
    }

    // ==============================
    // AGRICULTURAL CORPUS SEARCH — KMP + RABIN-KARP + AHO-CORASICK
    // ==============================
    //
    // Runs the same, unmodified KMP / Rabin-Karp / Aho-Corasick
    // implementations used elsewhere in the app, but over the larger
    // agricultural corpus (data/agricultural_corpus.txt, loaded once at
    // startup by io.CorpusLoader) instead of a single short record.

    private static void runCorpusSearch(
            Scanner scanner,
            FarmAssistService service) {

        System.out.print(
                "Enter one or more search terms, comma-separated " +
                "(e.g. nitrogen, drip irrigation, aphids): "
        );
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("No search term entered.");
            return;
        }

        List<String> terms = Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .toList();

        // KMP + Rabin-Karp: run on each term individually so their
        // results (which should always agree, since both are exact
        // substring searches) can be compared side by side.
        System.out.println("\n--- KMP / Rabin-Karp results (per term) ---");
        for (String term : terms) {
            CorpusSearchResult result = service.searchCorpus(term);
            System.out.println(
                    "\"" + term + "\": KMP found " + result.kmpPositions.size() +
                    " occurrence(s), Rabin-Karp found " + result.rabinKarpPositions.size() +
                    " occurrence(s) in the corpus."
            );
            if (!result.snippets.isEmpty()) {
                System.out.println("  Sample context:");
                result.snippets.forEach(s -> System.out.println("    " + s));
            }
        }

        // Aho-Corasick: search for every term SIMULTANEOUSLY in one pass
        // over the same corpus text, demonstrating the multi-pattern
        // case Aho-Corasick is designed for.
        List<String> foundTogether = service.searchCorpusMultiTerm(terms);
        System.out.println(
                "\n--- Aho-Corasick result (all terms in a single pass) ---"
        );
        if (foundTogether.isEmpty()) {
            System.out.println("None of the given terms were found in the corpus.");
        } else {
            System.out.println("Found in one pass: " + String.join(", ", foundTogether));
        }
    }
}
//javac -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
//javac -cp "lib\onnxruntime-1.26.0.jar;out" -d out (Get-ChildItem -Recurse -Filter *.java src-optional | ForEach-Object { $_.FullName })
//java -cp "out;lib\onnxruntime-1.26.0.jar" GuiMain