package gui;

import algorithms.flow.EdmondsKarp;
import models.Fertilizer;
import models.Requirement;
import service.FarmAssistService;
import service.TranslationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Fertilizer Allocation screen. Shows available supply and crop
 * requirements, then runs FarmAssistService.allocateFertilizers(),
 * which is backed by the unchanged Edmonds-Karp max-flow algorithm.
 * Allocation results are shown in a table since they're naturally
 * tabular (fertilizer -> crop -> amount). Only the visual
 * presentation changed here.
 */
public class FertilizerAllocationPanel extends JPanel implements Localizable {

    private final FarmAssistService service;
    private final TranslationService translation;

    private final JLabel heading, supplyHeading, requirementHeading;
    private final JTextArea supplyArea, requirementArea;
    private final StyledButton runBtn;
    private final JButton backBtn;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel summaryLabel;

    public FertilizerAllocationPanel(FarmAssistService service, TranslationService translation, MainFrame frame) {
        this.service = service;
        this.translation = translation;

        setLayout(new BorderLayout(0, Theme.SPACE_MD));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG));

        heading = new JLabel();
        heading.setFont(Theme.h1());
        heading.setForeground(Theme.TEXT_DARK);
        heading.setIcon(IconPainter.of(IconPainter.Kind.DROPLET, 22, Theme.PRIMARY));
        heading.setIconTextGap(10);

        supplyHeading = new JLabel();
        supplyHeading.setFont(Theme.h2());
        supplyHeading.setForeground(Theme.TEXT_DARK);
        requirementHeading = new JLabel();
        requirementHeading.setFont(Theme.h2());
        requirementHeading.setForeground(Theme.TEXT_DARK);

        supplyArea = new JTextArea(6, 20);
        supplyArea.setEditable(false);
        supplyArea.setFont(Theme.body());
        supplyArea.setForeground(Theme.TEXT_DARK);
        supplyArea.setBackground(Theme.SURFACE);
        requirementArea = new JTextArea(6, 20);
        requirementArea.setEditable(false);
        requirementArea.setFont(Theme.body());
        requirementArea.setForeground(Theme.TEXT_DARK);
        requirementArea.setBackground(Theme.SURFACE);

        RoundedPanel supplyBox = new RoundedPanel(Theme.RADIUS_MD, true);
        supplyBox.setCardBackground(Theme.SURFACE);
        supplyBox.setLayout(new BorderLayout(0, Theme.SPACE_SM));
        supplyBox.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));
        supplyBox.add(supplyHeading, BorderLayout.NORTH);
        JScrollPane supplyScroll = new JScrollPane(supplyArea);
        supplyScroll.setBorder(null);
        supplyBox.add(supplyScroll, BorderLayout.CENTER);

        RoundedPanel reqBox = new RoundedPanel(Theme.RADIUS_MD, true);
        reqBox.setCardBackground(Theme.SURFACE);
        reqBox.setLayout(new BorderLayout(0, Theme.SPACE_SM));
        reqBox.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));
        reqBox.add(requirementHeading, BorderLayout.NORTH);
        JScrollPane reqScroll = new JScrollPane(requirementArea);
        reqScroll.setBorder(null);
        reqBox.add(reqScroll, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(1, 2, Theme.SPACE_MD, Theme.SPACE_MD));
        infoPanel.setOpaque(false);
        infoPanel.add(supplyBox);
        infoPanel.add(reqBox);

        runBtn = new StyledButton("Submit");
        backBtn = new JButton();
        backBtn.setFont(Theme.body());
        backBtn.setForeground(Theme.PRIMARY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_SM, 0));
        buttons.setOpaque(false);
        buttons.add(runBtn);
        buttons.add(backBtn);

        JPanel top = new JPanel(new BorderLayout(0, Theme.SPACE_MD));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(infoPanel, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new Object[]{"Fertilizer", "Crop", "Amount"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(Theme.body());
        table.setRowHeight(26);
        table.setForeground(Theme.TEXT_DARK);
        table.setBackground(Theme.SURFACE);
        table.setGridColor(Theme.BORDER);
        table.setSelectionBackground(Theme.SURFACE_ALT);
        table.setSelectionForeground(Theme.TEXT_DARK);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(Theme.bodyBold());
        tableHeader.setBackground(Theme.SURFACE_ALT);
        tableHeader.setForeground(Theme.TEXT_DARK);

        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(Theme.bodyBold());
        summaryLabel.setForeground(Theme.PRIMARY);
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_SM, 0, 0, 0));

        RoundedPanel tableCard = new RoundedPanel(Theme.RADIUS_MD, true);
        tableCard.setCardBackground(Theme.SURFACE);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(null);
        tableCard.add(tableScroll, BorderLayout.CENTER);
        tableCard.add(summaryLabel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        backBtn.addActionListener(e -> frame.showCard(MainFrame.DASHBOARD));
        runBtn.addActionListener(e -> runAllocation());

        loadStaticInfo();
        refreshLanguage();
    }

    private void loadStaticInfo() {
        StringBuilder supplySb = new StringBuilder();
        for (Fertilizer f : service.getFertilizers()) {
            supplySb.append(f.getName()).append(": ").append(f.getAvailableSupply()).append("\n");
        }
        supplyArea.setText(supplySb.toString());

        StringBuilder reqSb = new StringBuilder();
        for (Requirement r : service.getRequirements()) {
            reqSb.append(r.toString()).append("\n");
        }
        requirementArea.setText(reqSb.toString());
    }

    private void runAllocation() {
        EdmondsKarp.AllocationResult result = service.allocateFertilizers();

        tableModel.setRowCount(0);
        for (String[] allocation : result.allocations) {
            tableModel.addRow(allocation);
        }

        int unmet = result.getUnmetDemand();
        if (unmet > 0) {
            summaryLabel.setText(translation.t("Total Allocated") + ": " + result.maxFlow
                    + "   |   " + translation.t("Unmet Demand") + ": " + unmet);
        } else {
            summaryLabel.setText(translation.t("Total Allocated") + ": " + result.maxFlow
                    + "   |   " + translation.t("All demands satisfied"));
        }
    }

    @Override
    public void refreshLanguage() {
        heading.setText(translation.t("Fertilizer Allocation"));
        supplyHeading.setText(translation.t("Supply"));
        requirementHeading.setText("Requirements");
        runBtn.setText(translation.t("Submit"));
        backBtn.setText("\u2190 " + translation.t("Back"));
    }
}