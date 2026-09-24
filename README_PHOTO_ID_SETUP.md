# Photo-Based Disease Identification — Setup Guide

This feature is **implemented but not runnable in the sandbox this project
was built in**, because that environment cannot reach Maven Central
(`repo1.maven.org`), where ONNX Runtime's Java library is distributed.
On a normal Windows machine with internet access, the steps below will
make it fully functional. No prediction has been faked anywhere in this
codebase — until you complete these steps, the app honestly reports
"Feature unavailable" with an accurate reason.

## What's already done vs. what's left

**Already done (pure Java, no setup needed, part of the default build):**
- `src/vision/DiseaseImageClassifier.java` — the interface
- `src/vision/PredictionResult.java`, `ClassificationException.java`, `ClassifierUnavailableException.java`
- `src/vision/UnavailableDiseaseClassifier.java` — honest fallback
- `src/vision/DiseaseClassMapper.java` — loads label/mapping config files
- `src/gui/PhotoDiseasePanel.java` — full UI: image picker, preview, async
  inference (SwingWorker, no UI freeze), confidence display, error handling
- `src/GuiMain.java` — reflectively loads the real classifier if present,
  else falls back with an accurate, environment-specific reason
- `src-optional/vision/OnnxDiseaseClassifier.java` — the **real** classifier
  implementation, written against the actual ONNX Runtime Java API and the
  actual documented preprocessing spec of the chosen model (see below).
  This compiles independently and was verified to compile correctly
  against faithful stubs of the real `ai.onnxruntime` API surface — it
  has NOT been run against the real jar/model, since neither is
  reachable from the build sandbox.

**Left for you to do, on a machine with internet access:**
1. Export the model to ONNX (one-time, ~2 minutes)
2. Get the real class label list (one-time, ~1 minute)
3. Fill in `data/model_class_to_disease.txt` with the real labels
4. Download the ONNX Runtime Java jar
5. Compile `src-optional/` and run

## The model

**linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification**
https://huggingface.co/linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification

- MobileNetV2 fine-tuned on the Kaggle "New Plant Diseases Dataset"
  version of PlantVillage (38 classes, including healthy leaves)
- Reported eval accuracy: 95.4%
- Ships as PyTorch weights (`pytorch_model.bin`), NOT already in ONNX —
  you convert it once with Hugging Face's official exporter (below)

This is a real, publicly documented, verifiable model — not invented for
this project. The exact preprocessing this code uses (resize shortest
edge to 256, center-crop 224×224, rescale to [0,1], normalize with
mean=0.5/std=0.5 per channel) comes directly from `MobileNetV2ImageProcessor`'s
documented defaults in the `huggingface/transformers` source code, not
guessed.

## Step 1 — Export the model to ONNX

```bash
pip install optimum[exporters]
optimum-cli export onnx --model linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification model/
```

This downloads the model and writes `model/model.onnx` (exact filename
may vary slightly by Optimum version — check the output folder and
update `MODEL_PATH` in `GuiMain.java` if it differs from
`model/plant_disease_mobilenetv2.onnx`, or just rename the exported
file to match).

## Step 2 — Extract the real class labels

```bash
python tools/extract_labels.py
```

This writes `model/labels.txt` with the 38 class labels in the exact
order the model outputs them (read from the model's own `config.json`,
not guessed). This is a **one-time offline script**, not a background
service — the deployed app never runs Python.

## Step 3 — Update the disease-name mapping

Open `model/labels.txt` (now populated) and `data/model_class_to_disease.txt`
(currently has illustrative example rows). Edit the mapping file so the
left-hand side of every row **exactly** matches a label from
`labels.txt` (case and underscores included), and the right-hand side
is a disease name that exists in `data/diseases.txt`. Any predicted
class with no row here will be shown to the user as an unmapped
prediction rather than silently guessed — so it's fine to leave some
unmapped at first and fill them in as you test.

## Step 4 — Get the ONNX Runtime Java jar

```bash
mvn dependency:copy-dependencies -Dartifact=com.microsoft.onnxruntime:onnxruntime:1.26.0 -DoutputDirectory=lib
```

Or download it manually and place it in `lib/`:
https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.26.0/onnxruntime-1.26.0.jar

(Check https://central.sonatype.com/artifact/com.microsoft.onnxruntime/onnxruntime
for the latest version if 1.26.0 is no longer current.)

## Step 5 — Compile and run

Your existing build of `src/` is completely unaffected by any of this —
it has zero dependency on ONNX Runtime and keeps working exactly as
before, with or without any of the steps above. To additionally enable
the real photo-identification feature:

```powershell
# Step A (unchanged) — build everything in src/ as always:
javac -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })

# Step B (new) — additionally compile the real classifier, now that
# the ONNX Runtime jar is available:
javac -cp "lib\onnxruntime-1.26.0.jar;out" -d out (Get-ChildItem -Recurse -Filter *.java src-optional | ForEach-Object { $_.FullName })

# Run the GUI with the ONNX jar on the classpath:
java -cp "out;lib\onnxruntime-1.26.0.jar" GuiMain
```

If Step B is skipped, `GuiMain` detects that `vision.OnnxDiseaseClassifier`
isn't compiled and falls back to the honest "unavailable" message — the
rest of the app is unaffected either way.

## How to verify it's actually working

1. Launch the GUI, go to "Identify Disease from Photo"
2. Select a real leaf photo (e.g. a sample image from the PlantVillage
   dataset, or the Kaggle "New Plant Diseases Dataset" test folder)
3. Click "Identify Disease" — you should see a "Detecting..." state
   briefly, then a predicted disease name, a confidence percentage,
   and (if mapped) symptoms/treatment from `diseases.txt`
4. If you see "Feature unavailable" with a specific reason (missing
   model file, missing labels, etc.), that message tells you exactly
   which step above wasn't completed

## Known limitations even after setup

- Prediction quality depends entirely on the pretrained model's own
  accuracy (95.4% reported on its own PlantVillage-style eval set) —
  it will not reliably recognize diseases outside its 38 trained
  classes, or non-leaf images.
- Class names not yet added to `data/model_class_to_disease.txt` will
  show as an unmapped raw label rather than a disease name.