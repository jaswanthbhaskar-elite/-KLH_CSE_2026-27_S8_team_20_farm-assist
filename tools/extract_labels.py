"""
One-time setup helper. NOT part of the running application — this
script is run ONCE on a machine with internet access, to correctly
populate model/labels.txt with the real class order from the model's
own metadata. The Farm Assist application itself never runs Python;
this only prepares a data file that the pure-Java OnnxDiseaseClassifier
reads at runtime.

Usage:
    pip install transformers
    python tools/extract_labels.py

Requires internet access to huggingface.co the first time it runs
(to download the model's config.json); the model weights themselves
are not needed for this step.
"""

from transformers import AutoConfig

MODEL_ID = "linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification"
OUTPUT_PATH = "model/labels.txt"

def main():
    config = AutoConfig.from_pretrained(MODEL_ID)
    id2label = config.id2label  # dict: {0: "label0", 1: "label1", ...}

    num_classes = len(id2label)
    print(f"Found {num_classes} classes in {MODEL_ID}")

    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        f.write(f"# Auto-generated from {MODEL_ID} config.json id2label.\n")
        f.write("# Do not edit by hand; re-run extract_labels.py if the model changes.\n")
        for i in range(num_classes):
            f.write(id2label[i].strip() + "\n")

    print(f"Wrote {num_classes} labels to {OUTPUT_PATH}")
    print("Next: update data/model_class_to_disease.txt so each of these")
    print("raw labels maps to a real disease name in data/diseases.txt.")

if __name__ == "__main__":
    main()