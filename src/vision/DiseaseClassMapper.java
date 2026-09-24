package vision;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps a model's output class index to a disease name that exists in
 * data/diseases.txt, via two small config files instead of hardcoded
 * values:
 *
 *   labelsFile  : one raw model class label per line, in the EXACT
 *                 order the model's output vector uses (index 0 =
 *                 first line, index 1 = second line, ...). This order
 *                 must come from the model's own metadata (its
 *                 id2label mapping) — see tools/extract_labels.py.
 *
 *   mappingFile : "RawModelLabel|DiseaseName" per line, mapping each
 *                 raw label onto a name that exists in diseases.txt.
 *                 Lines starting with '#' and blank lines are ignored.
 *
 * This keeps diseases.txt itself completely untouched, and keeps the
 * mapping editable without recompiling any code.
 */
public class DiseaseClassMapper {

    private final List<String> indexToRawLabel = new ArrayList<>();
    private final Map<String, String> rawLabelToDisease = new HashMap<>();

    public DiseaseClassMapper(String labelsFile, String mappingFile) throws IOException {
        loadLabels(labelsFile);
        loadMapping(mappingFile);
    }

    private void loadLabels(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                indexToRawLabel.add(trimmed);
            }
        }
    }

    private void loadMapping(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                int sep = trimmed.indexOf('|');
                if (sep < 0) continue;
                String rawLabel = trimmed.substring(0, sep).trim();
                String diseaseName = trimmed.substring(sep + 1).trim();
                rawLabelToDisease.put(rawLabel, diseaseName);
            }
        }
    }

    public int getClassCount() {
        return indexToRawLabel.size();
    }

    /** Raw model label for a given output index, or null if index is out of range. */
    public String getRawLabel(int index) {
        if (index < 0 || index >= indexToRawLabel.size()) return null;
        return indexToRawLabel.get(index);
    }

    /** Disease name mapped to this raw label, or null if no mapping entry exists for it. */
    public String getDiseaseName(String rawLabel) {
        return rawLabelToDisease.get(rawLabel);
    }
}