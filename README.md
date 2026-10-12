# 🌾 Farm Assist — Agricultural Decision Support System

Farm Assist is a Java-based agricultural assistance system designed to help users explore agricultural information, search crop and disease knowledge, recommend crops under resource constraints, and identify plant diseases from images. The project combines **Data Structures and Algorithms (DSA), Java Swing, file-based agricultural datasets, and machine-learning-based image classification** in one application.

## 🎯 Objectives

- Provide accessible information about crops, plant diseases, and fertilizers.
- Recommend suitable crop selections using optimization techniques.
- Search agricultural text efficiently using string-matching algorithms.
- Support resource allocation through graph algorithms.
- Identify possible plant diseases from images using a trained deep-learning model.
- Demonstrate the practical application of DSA in an agricultural use case.

## ✨ Features

- **Crop Recommendation:** Uses dynamic programming to select crops under specified constraints.
- **Agricultural Information:** Loads crop, disease, fertilizer, symptom, and requirement data from text files.
- **Disease and Symptom Search:** Supports exact, multi-pattern, and approximate text matching.
- **Agricultural Corpus Search:** Searches a larger agricultural text corpus and displays occurrence counts and contextual snippets.
- **Resource Allocation:** Applies a maximum-flow algorithm to model resource or fertilizer allocation.
- **Symptom Coverage:** Uses a greedy set-cover approach to select items that cover relevant symptoms.
- **Image-Based Disease Identification:** Uses a MobileNetV2 ONNX model through ONNX Runtime to classify plant images.
- **Desktop Interface:** Provides a Java Swing graphical user interface.
- **Localization Support:** Includes Hindi and Telugu translation resources.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Core application and algorithm implementation |
| Java Swing | Desktop graphical user interface |
| Data Structures and Algorithms | Searching, optimization, and resource allocation |
| Text files | Agricultural datasets and reference information |
| MobileNetV2 | Image-based plant disease classification |
| ONNX Runtime | Executes the ONNX image-classification model |
| Git and GitHub | Version control and collaboration |

## 🧠 Algorithms Used

| Algorithm | Application |
|---|---|
| Knuth–Morris–Pratt (KMP) | Single-pattern text searching |
| Rabin–Karp | Hash-based single-pattern searching |
| Aho–Corasick | Searching for multiple patterns in one pass |
| Fuzzy Matching / Levenshtein Distance | Approximate text matching |
| Dynamic Programming — Knapsack | Crop selection under resource constraints |
| Edmonds–Karp | Maximum-flow-based resource allocation |
| Greedy Set Cover | Selecting items that cover relevant symptoms |
| MobileNet
