package gui;

import service.FarmAssistService;
import service.TranslationService;
import vision.DiseaseImageClassifier;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Top-level Swing window. Structure: a dark sidebar (WEST) for
 * navigation, a header bar (NORTH) with greeting + language selector,
 * and a CardLayout content area (CENTER) with one panel per feature —
 * exactly the same CardLayout/showCard mechanism as before, just
 * restructured visually around a sidebar instead of a simple top bar.
 */
public class MainFrame extends JFrame {

    public static final String DASHBOARD = "DASHBOARD";
    public static final String CROP_SEARCH = "CROP_SEARCH";
    public static final String DISEASE_SEARCH = "DISEASE_SEARCH";
    public static final String FERTILIZER_SEARCH = "FERTILIZER_SEARCH";
    public static final String SYMPTOM_SEARCH = "SYMPTOM_SEARCH";
    public static final String CROP_RECOMMEND = "CROP_RECOMMEND";
    public static final String FERTILIZER_ALLOCATION = "FERTILIZER_ALLOCATION";
    public static final String PHOTO_DISEASE = "PHOTO_DISEASE";
    public static final String ABOUT = "ABOUT";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final List<Localizable> localizablePanels = new ArrayList<>();

    private Sidebar sidebar;

    public MainFrame(FarmAssistService service, TranslationService translation,
                      DiseaseImageClassifier classifier) {
        super("Farm Assist");


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        // ---- Sidebar (navigation) ----
        sidebar = new Sidebar(translation, this::showCard, DASHBOARD);
        add(sidebar, BorderLayout.WEST);
        localizablePanels.add(sidebar);

        // ---- Header (greeting + language selector) ----
        HeaderBar header = new HeaderBar(translation, this::onLanguageChanged);
        add(header, BorderLayout.NORTH);
        localizablePanels.add(header);

        // ---- Content area ----
        cardPanel.setOpaque(false);

        DashboardPanel dashboard = new DashboardPanel(translation, this);
        CropSearchPanel cropSearch = new CropSearchPanel(service, translation, this);
        DiseaseSearchPanel diseaseSearch = new DiseaseSearchPanel(service, translation, this);
        FertilizerSearchPanel fertilizerSearch = new FertilizerSearchPanel(service, translation, this);
        SymptomSearchPanel symptomSearch = new SymptomSearchPanel(service, translation, this);
        CropRecommendationPanel cropRecommend = new CropRecommendationPanel(service, translation, this);
        FertilizerAllocationPanel fertilizerAllocation = new FertilizerAllocationPanel(service, translation, this);
        PhotoDiseasePanel photoDisease = new PhotoDiseasePanel(service, translation, classifier, this);
        AboutPanel about = new AboutPanel(translation, this);

        register(DASHBOARD, dashboard);
        register(CROP_SEARCH, cropSearch);
        register(DISEASE_SEARCH, diseaseSearch);
        register(FERTILIZER_SEARCH, fertilizerSearch);
        register(SYMPTOM_SEARCH, symptomSearch);
        register(CROP_RECOMMEND, cropRecommend);
        register(FERTILIZER_ALLOCATION, fertilizerAllocation);
        register(PHOTO_DISEASE, photoDisease);
        register(ABOUT, about);

        add(cardPanel, BorderLayout.CENTER);
        showCard(DASHBOARD);
    }

    private void register(String name, JPanel panel) {
        cardPanel.add(panel, name);
        if (panel instanceof Localizable localizable) {
            localizablePanels.add(localizable);
        }
    }

    public void showCard(String name) {
        cardLayout.show(cardPanel, name);
        sidebar.setActive(name);
    }

    private void onLanguageChanged() {
        for (Localizable panel : localizablePanels) {
            panel.refreshLanguage();
        }
    }
}