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
| MobileNetV2 | Machine-learning-based image classification |

## 🏗️ System Architecture

The application is organized into layers to separate the user interface, business logic, data handling, algorithms, and image processing.

1. **GUI Layer:** Accepts user input and displays results through Java Swing.
2. **Service Layer:** Coordinates application features and connects the interface to the relevant components.
3. **Algorithm Layer:** Implements string searching, crop optimization, resource allocation, and coverage algorithms.
4. **Data and I/O Layer:** Loads structured agricultural data and the larger text corpus.
5. **Vision Layer:** Performs image-based disease classification using the ONNX model and ONNX Runtime.

### Application Flow

```text
User
  |
  v
Java Swing GUI
  |
  v
FarmAssistService
  |
  +----> DataLoader / CorpusLoader
  |
  +----> DSA Algorithms
  |
  +----> Vision / ONNX Runtime
  |
  v
Results displayed to the user
```

## 📁 Project Structure

```text
Farm-assist/
├── src/
│   ├── algorithms/
│   │   ├── flow/
│   │   ├── recommend/
│   │   ├── search/
│   │   └── setcover/
│   ├── gui/
│   ├── io/
│   ├── models/
│   ├── service/
│   ├── vision/
│   ├── Main.java
│   └── GuiMain.java
├── data/
│   ├── crops.txt
│   ├── diseases.txt
│   ├── fertilizers.txt
│   ├── requirements.txt
│   ├── symptoms.txt
│   ├── agricultural_corpus.txt
│   ├── model_class_to_disease.txt
│   ├── translations_hi.txt
│   └── translations_te.txt
├── model/
│   ├── config.json
│   └── labels.txt
├── lib/
├── assets/
├── tools/
├── src-optional/
├── README.md
└── Farm_Assist_Project_Documentation.docx
```

*Note: The tree highlights the main project components; additional files may be present in the repository.*

## ▶️ Running the Project

### Prerequisites

- Java Development Kit (JDK) compatible with the project's source code.
- A Java IDE such as Visual Studio Code, or a terminal with Java compiler tools.
- The project dependencies and files included in the repository.

### Run Using an IDE

1. Clone or download this repository.
2. Open the project root folder in your IDE.
3. Ensure the required library dependencies are available.
4. Run `GuiMain.java` to launch the graphical application.
5. Run `Main.java` to use the console-based application, including the console agricultural corpus search option.

### Image Classification Requirements

The image-classification feature uses the ONNX model and ONNX Runtime library included in the project. Ensure the model and library paths are configured correctly before running this feature.

## 🔎 Agricultural Corpus Search

The project includes a larger agricultural text dataset in `data/agricultural_corpus.txt`. `CorpusLoader` reads this plain-text corpus and makes it available to the search functionality.

The corpus search demonstrates:

- KMP and Rabin–Karp for single-pattern searching.
- Aho–Corasick for multi-pattern searching.
- Occurrence counts and contextual snippets to help users understand the results.

For example, searching for `nitrogen` returns matches found in the agricultural corpus.

## 👥 Project Team

| Roll Number | Team Member |
|---|---|
| 2520030553-B | Jaswanth Bhaskar |
| 2520030503-V | V Deepak |

## 🚀 Future Scope

- Integrate updated agricultural datasets and region-specific crop recommendations.
- Improve disease identification through further model evaluation and testing.
- Expand language support and accessibility.
- Add weather and soil-condition data integrations.
- Develop additional decision-support features for farmers.

## 📌 Conclusion

Farm Assist demonstrates how Java, Data Structures and Algorithms, file-based datasets, a graphical interface, and machine-learning-based image classification can be combined into an agricultural decision-support application. The project provides a practical setting for applying searching, optimization, graph, and coverage algorithms to agricultural information and resource-management problems.

## 📄 Project Documentation

The project report and presentation are available in this repository. Refer to the project documentation for further details about the implementation, design, and evaluation.

---

**Project:** Farm Assist  
**Domain:** Agriculture and Decision Support  
**Language:** Java  
**Repository:** [Farm Assist on GitHub](https://github.com/jaswanthbhaskar-elite/-KLH_CSE_2026-27_S8_team_20_farm-assist.git)
