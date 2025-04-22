/*
 * Copyright (c) 2010-2024. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package plugins.fab.ROIPool;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.extension.plugin.abstract_.PluginActionable;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginName;
import org.bioimageanalysis.icy.gui.GuiUtil;
import org.bioimageanalysis.icy.gui.component.ComponentUtil;
import org.bioimageanalysis.icy.gui.component.button.ColorChooserButton;
import org.bioimageanalysis.icy.gui.dialog.MessageDialog;
import org.bioimageanalysis.icy.gui.frame.IcyFrame;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/**
 * This plugin is created to let a user have a number of pre-defined ROI and beeing able to load them at any time,
 * just in one click. Or grab an existing ROI by selecting it and clicking 'add current selected ROI'
 * Everything is saved in an XML file.
 *
 * @author Fabrice de Chaumont
 */
@IcyPluginName("ROI Pool")
public class ROIPool extends PluginActionable implements ActionListener {
    ArrayList<ROI> ROIList = new ArrayList<>();

    JLabel currentSelectedROI = new JLabel("current selected ROI");
    File XMLFile = null;
    JPanel panelROIList = new JPanel();
    ArrayList<Element> roiElements = new ArrayList<>();
    JButton addCurrentSelectedROIToPool = new JButton("Add current selected ROI");
    JPanel mainPanel;
    Document document;
    IcyFrame mainFrame;
    JCheckBox alwaysOnTopCheckBox = new JCheckBox("Keep this window on top", true);

    @Override
    public void run() {
        mainPanel = new JPanel();
        mainFrame = GuiUtil.generateTitleFrame("ROI Pool", mainPanel, new Dimension(200, 70), true, true, true, true);
        mainFrame.detach();
        mainFrame.setAlwaysOnTop(true);
        mainPanel.setLayout(new BorderLayout());

        final JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.PAGE_AXIS));

        commandPanel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        commandPanel.add(GuiUtil.createLineBoxPanel(addCurrentSelectedROIToPool));
        commandPanel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        commandPanel.add(GuiUtil.createLineBoxPanel(alwaysOnTopCheckBox));
        commandPanel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));

        ComponentUtil.setFixedHeight(addCurrentSelectedROIToPool, 30);
        addCurrentSelectedROIToPool.addActionListener(this);

        alwaysOnTopCheckBox.addActionListener(this);
        // Init XML File

        String fileName = System.getProperty("user.home");
        fileName += FileUtil.separator + "icy" + FileUtil.separator + "ROI pool plugin";
        FileUtil.createDir(new File(fileName));
        fileName += FileUtil.separator + "ROIPool.xml";
        XMLFile = new File(fileName);

        // ROI list

        panelROIList.setLayout(new BoxLayout(panelROIList, BoxLayout.PAGE_AXIS));

        //commandPanel.add( GuiUtil.createLineBoxPanel( panelROIList ) );

        // Tag List

        // Init frame

        mainPanel.add(commandPanel, BorderLayout.NORTH);

        roiScrollPanel = new JScrollPane(panelROIList);
        roiScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(roiScrollPanel, BorderLayout.CENTER);
        roiScrollPanel.setBorder(new TitledBorder("ROI List"));

        document = loadXMLDocument();
        refreshPanelROIList();

        ComponentUtil.setFixedHeight(roiScrollPanel, 500);

        mainFrame.pack();
        mainFrame.addToDesktopPane();
        //mainFrame.center();
        mainFrame.setVisible(true);
        mainFrame.requestFocus();

        mainFrame.center();

        //new WindowPositionSaver( mainFrame, "fab/ROIPOOL", mainFrame.getLocation() );
    }

    JScrollPane roiScrollPanel;

    private @NotNull Document loadXMLDocument() {
        Document document = XMLUtil.loadDocument(XMLFile);
        roiElements = new ArrayList<>();

        if (document == null) {
            document = XMLUtil.createDocument(true);
            // create a pool for further improvement like multi pool.
            final Element poolElement = XMLUtil.addElement(document.getDocumentElement(), "Pool");
            poolElement.setAttribute("name", "default");
        }
        return document;
    }

    private void refreshPanelROIList() {
        //System.out.println("refresh panel ROI");

        roiElements = new ArrayList<>();

        final ArrayList<Element> poolList = XMLUtil.getElements(document.getDocumentElement(), "Pool");

        for (final Element poolElement : poolList) {
            final ArrayList<Element> roiList = XMLUtil.getElements(poolElement, "ROI");
            roiElements.addAll(roiList);

        }

        panelROIList.removeAll();

        for (final Element roiElement : roiElements) {
            panelROIList.add(GuiUtil.createLineBoxPanel(createROIButton(roiElement)));
        }

        panelROIList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));

        if (roiElements.isEmpty()) {
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("The list is empty.")));
            panelROIList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        }

        if (roiElements.size() < 5) {
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("To add a ROI, select it by clicking on it and use")));
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("the above button 'add current selected ROI'")));
            panelROIList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("The ROIs will then be listed here, clicking a ROI")));
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("in the list will put it back in the current")));
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("sequence selected.")));
            panelROIList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("This ROI list is automaticaly saved on your")));
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("computer in the file home/icy/ROI Pool.xml")));
            panelROIList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("This help message will not be displayed as")));
            panelROIList.add(GuiUtil.createLineBoxPanel(new JLabel("you will have add some ROIs in the list.")));
            panelROIList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        }

        panelROIList.repaint();
        roiScrollPanel.validate();
    }

    private @NotNull JPanel createROIButton(final Element roiElement) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        final Element nameElement = XMLUtil.getElement(roiElement, "name");
        final String name = XMLUtil.getValue(nameElement, "roi default name");

        final JButton roiButton = new JButton(name);

        roiButton.addActionListener(e -> {
            final ROI roi = ROI.createFromXML(roiElement);
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            if (sequence != null) {
                sequence.addROI(roi);
            }

        });

        final JButton removeButton = new JButton("x");
        removeButton.setToolTipText("Remove this ROI from this list.");
        removeButton.addActionListener(e -> {
            XMLUtil.removeAllChildren(roiElement);
            XMLUtil.removeNode(roiElement.getParentNode(), roiElement);
            XMLUtil.saveDocument(document, XMLFile);
            refreshPanelROIList();
        });

        final JButton renameButton = new JButton("r");
        renameButton.setToolTipText("Rename ROI");
        renameButton.addActionListener(e -> {
            final Element nameElement1 = XMLUtil.getElement(roiElement, "name");
            final String name1 = XMLUtil.getValue(nameElement1, "roi default name");

            try {
                mainFrame.setVisible(false);
                final TextDialog textDialog = new TextDialog("Rename", "New name of ROI:", name1);
                textDialog.pack();
                //textDialog.setSize( 200 , 200 );

                textDialog.setLocationRelativeTo(null);
                textDialog.requestFocus();
                textDialog.setVisible(true);

                if (textDialog.isOk) {

                    XMLUtil.setValue(nameElement1, textDialog.getText());
                    //nameElement.setNodeValue( textDialog.getText() );
                    XMLUtil.saveDocument(document, XMLFile);
                    refreshPanelROIList();
                }
            }
            finally {
                mainFrame.setVisible(true);
            }

        });

        final ColorChooserButton colorChooserButton = new ColorChooserButton();
        final Element colorElement = XMLUtil.getElement(roiElement, ROI.ID_COLOR);
        //System.out.println( XMLUtil.getValue( colorElement, "0" ) );
        final Color color = new Color(Integer.parseInt(XMLUtil.getValue(colorElement, "0")));
        colorChooserButton.setColor(color);

        colorChooserButton.addColorChangeListener(e -> {
            final Element colorElement1 = XMLUtil.getElement(roiElement, ROI.ID_COLOR);
            XMLUtil.setValue(colorElement1, "" + colorChooserButton.getColor().getRGB());
            XMLUtil.saveDocument(document, XMLFile);
            refreshPanelROIList();
        });

        ComponentUtil.setFixedWidth(roiButton, 130);
        ComponentUtil.setFixedWidth(removeButton, 30);
        ComponentUtil.setFixedWidth(renameButton, 30);
        ComponentUtil.setFixedWidth(colorChooserButton, 30);
        panel.add(GuiUtil.createLineBoxPanel(roiButton, removeButton, renameButton, colorChooserButton));

        ComponentUtil.setFixedHeight(panel, 20);

        return panel;
    }

    @Override
    public void actionPerformed(final @NotNull ActionEvent e) {
        if (e.getSource() == alwaysOnTopCheckBox) {
            mainFrame.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
        }

        if (e.getSource() == addCurrentSelectedROIToPool) {

            final Element poolNode = XMLUtil.getElements(document.getDocumentElement(), "Pool").getFirst();

            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final ArrayList<ROI> roiList = sequence.getROIs();
                boolean anROIAsBeenAdded = false;
                for (final ROI roi : roiList) {
                    if (roi.isSelected()) {
                        anROIAsBeenAdded = true;

                        final Element roiElement = XMLUtil.addElement(poolNode, "ROI");
                        roi.saveToXML(roiElement);
                    }
                }

                if (!anROIAsBeenAdded) {
                    MessageDialog.showDialog("No ROI is selected. To select a ROI, just click on it.", MessageDialog.INFORMATION_MESSAGE);
                }

                XMLUtil.saveDocument(document, XMLFile);

                refreshPanelROIList();
            }
            else {
                MessageDialog.showDialog("There is no active sequence.", MessageDialog.WARNING_MESSAGE);
            }

//			mainPanel.validate();
//			mainPanel.updateUI();
        }
    }
}
