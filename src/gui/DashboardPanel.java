package gui;

import service.TranslationService;

import javax.swing.*;
import java.awt.*;

/**
 * Premium dashboard: a hero section (welcome message + original plant
 * illustration + CTA) above a grid of feature cards, one per existing
 * application feature. All navigation still goes through
 * frame.showCard(...) exactly as before — only the visual presentation
 * changed, not the CardLayout wiring or any business logic.
 */
public class DashboardPanel extends JPanel implements Localizable {

    private final TranslationService translation;
    private final JLabel heroHeading, heroSubtitle;
    private final StyledButton ctaButton;
    private final JLabel sectionHeading;

    private final FeatureCard cropCard, diseaseCard, fertilizerCard, symptomCard,
            recommendCard, allocationCard, photoCard, aboutCard;

    public DashboardPanel(TranslationService translation, MainFrame frame) {
        this.translation = translation;
        setLayout(new BorderLayout(0, Theme.SPACE_LG));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG));

        // ---- Hero section ----
        RoundedPanel hero = new RoundedPanel(Theme.RADIUS_LG, true);
        hero.setCardBackground(Theme.PRIMARY_DARK);
        hero.setLayout(new BorderLayout());
        hero.setPreferredSize(new Dimension(10, 220));

        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        heroText.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_MD));

        heroHeading = new JLabel("Welcome to Farm Assist");
        heroHeading.setFont(Theme.display());
        heroHeading.setForeground(Theme.TEXT_ON_DARK);
        heroHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroSubtitle = new JLabel();
        heroSubtitle.setFont(Theme.body());
        heroSubtitle.setForeground(Theme.TEXT_ON_DARK_MUTED);
        heroSubtitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        heroSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        ctaButton = new StyledButton("Let's Grow Better Together!", Theme.PRIMARY_LIGHT, Theme.PRIMARY_HOVER);
        ctaButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        ctaButton.addActionListener(e -> frame.showCard(MainFrame.PHOTO_DISEASE));

        heroText.add(heroHeading);
        heroText.add(heroSubtitle);
        heroText.add(ctaButton);

        PlantIllustration illustration = new PlantIllustration(true);
        illustration.setPreferredSize(new Dimension(280, 10));

        hero.add(heroText, BorderLayout.CENTER);
        hero.add(illustration, BorderLayout.EAST);

        // ---- Feature card grid ----
        JPanel gridSection = new JPanel(new BorderLayout(0, Theme.SPACE_MD));
        gridSection.setOpaque(false);

        sectionHeading = new JLabel();
        sectionHeading.setFont(Theme.h1());
        sectionHeading.setForeground(Theme.TEXT_DARK);

        JPanel grid = new JPanel(new GridLayout(2, 4, Theme.SPACE_MD, Theme.SPACE_MD));
        grid.setOpaque(false);

        cropCard = new FeatureCard(IconPainter.Kind.LEAF, () -> frame.showCard(MainFrame.CROP_SEARCH));
        diseaseCard = new FeatureCard(IconPainter.Kind.BUG, () -> frame.showCard(MainFrame.DISEASE_SEARCH));
        fertilizerCard = new FeatureCard(IconPainter.Kind.BAG, () -> frame.showCard(MainFrame.FERTILIZER_SEARCH));
        symptomCard = new FeatureCard(IconPainter.Kind.SEARCH, () -> frame.showCard(MainFrame.SYMPTOM_SEARCH));
        recommendCard = new FeatureCard(IconPainter.Kind.CHART, () -> frame.showCard(MainFrame.CROP_RECOMMEND));
        allocationCard = new FeatureCard(IconPainter.Kind.DROPLET, () -> frame.showCard(MainFrame.FERTILIZER_ALLOCATION));
        photoCard = new FeatureCard(IconPainter.Kind.CAMERA, () -> frame.showCard(MainFrame.PHOTO_DISEASE));
        aboutCard = new FeatureCard(IconPainter.Kind.INFO, () -> frame.showCard(MainFrame.ABOUT));

        grid.add(cropCard);
        grid.add(diseaseCard);
        grid.add(fertilizerCard);
        grid.add(symptomCard);
        grid.add(recommendCard);
        grid.add(allocationCard);
        grid.add(photoCard);
        grid.add(aboutCard);

        gridSection.add(sectionHeading, BorderLayout.NORTH);
        gridSection.add(grid, BorderLayout.CENTER);

        add(hero, BorderLayout.NORTH);
        add(gridSection, BorderLayout.CENTER);

        refreshLanguage();
    }

    @Override
    public void refreshLanguage() {
        heroHeading.setText(translation.t("Welcome to Farm Assist"));
        heroSubtitle.setText(translation.t("Your intelligent companion for smarter farming decisions."));
        ctaButton.setText(translation.t("Identify Disease from Photo"));
        sectionHeading.setText(translation.t("What would you like to do today?"));

        cropCard.setTitle(translation.t("Crop Information"));
        cropCard.setDescription(translation.t("Explore crop details and growing conditions."));

        diseaseCard.setTitle(translation.t("Disease Information"));
        diseaseCard.setDescription(translation.t("Learn symptoms and treatment for crop diseases."));

        fertilizerCard.setTitle(translation.t("Fertilizer Information"));
        fertilizerCard.setDescription(translation.t("Check fertilizer types and proper usage."));

        symptomCard.setTitle(translation.t("Search by Multiple Symptoms"));
        symptomCard.setDescription(translation.t("Search multiple symptoms for likely diseases."));

        recommendCard.setTitle(translation.t("Crop Recommendation"));
        recommendCard.setDescription(translation.t("Get the best crop mix for your land and budget."));

        allocationCard.setTitle(translation.t("Fertilizer Allocation"));
        allocationCard.setDescription(translation.t("Optimize fertilizer supply across your crops."));

        photoCard.setTitle(translation.t("Identify Disease from Photo"));
        photoCard.setDescription(translation.t("Identify diseases from a leaf photo instantly."));

        aboutCard.setTitle(translation.t("About"));
        aboutCard.setDescription(translation.t("Learn how Farm Assist works."));
    }
}