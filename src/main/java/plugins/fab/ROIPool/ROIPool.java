package plugins.fab.ROIPool;

import icy.file.FileUtil;
import icy.gui.component.ComponentUtil;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.dialog.MessageDialog;
import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.gui.util.WindowPositionSaver;
import icy.main.Icy;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginImageAnalysis;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.util.XMLUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * This plugin is created to let a user have a number of pre-defined ROI and beeing able to load them at any time,
 * just in one click. Or grab an existing ROI by selecting it and clicking 'add current selected ROI'
 * Everything is saved in an XML file.
 * 
 * @author Fabrice de Chaumont
 *
 */
public class ROIPool extends Plugin implements PluginImageAnalysis , ActionListener {

	ArrayList<ROI> ROIList = new ArrayList<ROI>();
	
	JLabel currentSelectedROI = new JLabel("current selected ROI");	
	File XMLFile = null;
	JPanel panelROIList = new JPanel();
	ArrayList<Element> roiElements = new ArrayList<Element>();
	JButton addCurrentSelectedROIToPool = new JButton("Add current selected ROI");
	JPanel mainPanel;
	Document document;
	IcyFrame mainFrame;
	JCheckBox alwaysOnTopCheckBox = new JCheckBox("Keep this window on top" , true );
	@Override
	public void compute() {
		
		mainPanel = new JPanel();		
		mainFrame = GuiUtil.generateTitleFrame("ROI Pool", mainPanel, new Dimension( 200,70 ), true, true , true , true );	
		mainFrame.detach();
		mainFrame.setAlwaysOnTop( true );
		mainPanel.setLayout( new BorderLayout() );
				
		JPanel commandPanel = new JPanel();
		commandPanel.setLayout( new BoxLayout( commandPanel , BoxLayout.PAGE_AXIS ) );
				
		commandPanel.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		commandPanel.add( GuiUtil.createLineBoxPanel( addCurrentSelectedROIToPool ) );	
		commandPanel.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		commandPanel.add( GuiUtil.createLineBoxPanel( alwaysOnTopCheckBox ) );
		commandPanel.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		
		ComponentUtil.setFixedHeight( addCurrentSelectedROIToPool , 30 );
		addCurrentSelectedROIToPool.addActionListener( this );
		
		alwaysOnTopCheckBox.addActionListener( this );
		// Init XML File
		
		String fileName = System.getProperty("user.home") ;
		fileName += FileUtil.separator + "icy" + FileUtil.separator + "ROI pool plugin";
		FileUtil.createDir( new File( fileName ) );		
		fileName += FileUtil.separator + "ROIPool.xml";
		XMLFile = new File( fileName );
		
		// ROI list
		
		panelROIList.setLayout( new BoxLayout( panelROIList , BoxLayout.PAGE_AXIS ) );		
		
		//commandPanel.add( GuiUtil.createLineBoxPanel( panelROIList ) );
		
		// Tag List
		
		// Init frame
		
		mainPanel.add( commandPanel , BorderLayout.NORTH );
		
		roiScrollPanel = new JScrollPane( panelROIList );
		roiScrollPanel.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		mainPanel.add( roiScrollPanel , BorderLayout.CENTER );
		roiScrollPanel.setBorder( new TitledBorder( "ROI List" ) );

		document = loadXMLDocument();
		refreshPanelROIList();
		
		ComponentUtil.setFixedHeight( roiScrollPanel , 500 );
		
		mainFrame.pack();
		mainFrame.addToMainDesktopPane();
		//mainFrame.center();
		mainFrame.setVisible( true );
		mainFrame.requestFocus();

		mainFrame.center();
		
		//new WindowPositionSaver( mainFrame, "fab/ROIPOOL", mainFrame.getLocation() );		
		
	}
	
	JScrollPane roiScrollPanel;

	private Document loadXMLDocument()
	{
		Document document = XMLUtil.loadDocument( XMLFile );
		roiElements = new ArrayList<Element>();
		
		if ( document == null )
		{			
			document = XMLUtil.createDocument( true );
			// create a pool for further improvement like multi pool.
			Element poolElement = XMLUtil.addElement( document.getDocumentElement() , "Pool" ); 
			poolElement.setAttribute("name", "default");
		}
		return document;
	}
	
	private void refreshPanelROIList() {
		//System.out.println("refresh panel ROI");

		roiElements = new ArrayList<Element>();

		ArrayList<Element> poolList = XMLUtil.getSubElements( document.getDocumentElement() , "Pool" );
		
		for ( Element poolElement : poolList )
		{
			ArrayList<Element> roiList = XMLUtil.getSubElements( poolElement , "ROI" );
			for ( Element roiElement : roiList )
			{
				roiElements.add( roiElement );				
			}
			
		}
				
		panelROIList.removeAll();
		
		for ( Element roiElement : roiElements )
		{		
			panelROIList.add( GuiUtil.createLineBoxPanel( createROIButton( roiElement ) ) );
		}
		
		panelROIList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		
		if ( roiElements.size() == 0 )
		{
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("The list is empty.") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		}
		
		if ( roiElements.size() < 5 )
		{
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("To add a ROI, select it by clicking on it and use") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("the above button 'add current selected ROI'") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("The ROIs will then be listed here, clicking a ROI") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("in the list will put it back in the current") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("sequence selected.") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("This ROI list is automaticaly saved on your") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("computer in the file home/icy/ROI Pool.xml") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("This help message will not be displayed as") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( new JLabel("you will have add some ROIs in the list.") ) );
			panelROIList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		}
				
		panelROIList.repaint();
		roiScrollPanel.validate();
		
	}

	private JPanel createROIButton(final Element roiElement) {
		
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel , BoxLayout.PAGE_AXIS ) );
		
		Element nameElement = XMLUtil.getSubElement( roiElement , "name" );
		String name = XMLUtil.getValue( nameElement, "roi default name" );
	
		JButton roiButton = new JButton( name );

		roiButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				ROI roi = ROI.createFromXML( roiElement );
				Sequence sequence = Icy.getMainInterface().getFocusedSequence();
				if ( sequence != null )
				{
					sequence.addROI( roi );
				}
				
			}
		});
		
		JButton removeButton = new JButton("x");
		removeButton.setToolTipText("Remove this ROI from this list.");
		removeButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				XMLUtil.removeAllChilds( roiElement );
				XMLUtil.removeNode( roiElement.getParentNode() , roiElement ) ;
				XMLUtil.saveDocument( document , XMLFile ) ;
				refreshPanelROIList();
			}
			
		});
		
		JButton renameButton = new JButton("r");
		renameButton.setToolTipText("Rename ROI");
		renameButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				Element nameElement = XMLUtil.getSubElement( roiElement , "name" );
				String name = XMLUtil.getValue( nameElement, "roi default name" );
				
				try
				{
					mainFrame.setVisible( false );
					TextDialog textDialog = new TextDialog("Rename" , "New name of ROI:" , name );
					textDialog.pack();
					//textDialog.setSize( 200 , 200 );

					textDialog.setLocationRelativeTo( null );
					textDialog.requestFocus();
					textDialog.setVisible(true);

					if ( textDialog.isOk )
					{
												
						XMLUtil.setValue(nameElement, textDialog.getText() );
						//nameElement.setNodeValue( textDialog.getText() );
						XMLUtil.saveDocument( document , XMLFile ) ;
						refreshPanelROIList();
					}
				}
				finally
				{
					mainFrame.setVisible( true );
				}
				
			}
			
		});
		
		final ColorChooserButton colorChooserButton = new ColorChooserButton();
		Element colorElement = XMLUtil.getSubElement( roiElement , ROI.ID_COLOR );		
		//System.out.println( XMLUtil.getValue( colorElement, "0" ) );
		Color color = new Color( Integer.parseInt( XMLUtil.getValue( colorElement, "0" ) ) )   ;
		colorChooserButton.setColor( color );
		
		colorChooserButton.addColorChangeListener( new ColorChangeListener() {
			
			@Override
			public void colorChanged(ColorChooserButton source) {

				Element colorElement = XMLUtil.getSubElement( roiElement , ROI.ID_COLOR );
				XMLUtil.setValue(colorElement, ""+colorChooserButton.getColor().getRGB() );
				XMLUtil.saveDocument( document , XMLFile ) ;
				refreshPanelROIList();
			}
		});
		
		ComponentUtil.setFixedWidth( roiButton , 130 );
		ComponentUtil.setFixedWidth( removeButton , 30 );
		ComponentUtil.setFixedWidth( renameButton , 30 );
		ComponentUtil.setFixedWidth( colorChooserButton , 30 );
		panel.add( GuiUtil.createLineBoxPanel( roiButton , removeButton ,renameButton , colorChooserButton ) );
	
		ComponentUtil.setFixedHeight( panel , 20 );
		
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if ( e.getSource() == alwaysOnTopCheckBox )
		{
			mainFrame.setAlwaysOnTop( alwaysOnTopCheckBox.isSelected() );			
		}
		
		if ( e.getSource() == addCurrentSelectedROIToPool )
		{			
			
			Element poolNode = XMLUtil.getSubElements( document.getDocumentElement() , "Pool" ).get( 0 );			
			
			Sequence sequence = Icy.getMainInterface().getFocusedSequence();
			
			if ( sequence!=null )
			{
				ArrayList<ROI> roiList = sequence.getROIs();
				boolean anROIAsBeenAdded = false;
				for ( ROI roi : roiList )
				{
					if ( roi.isSelected() )
					{
						anROIAsBeenAdded = true;

						Element roiElement = XMLUtil.addElement( poolNode , "ROI" ); 
						roi.saveToXML( roiElement );
					}
				}

				if ( !anROIAsBeenAdded )
				{
					MessageDialog.showDialog("No ROI is selected. To select a ROI, just click on it.", MessageDialog.INFORMATION_MESSAGE );
				}
				
				XMLUtil.saveDocument( document , XMLFile );

				refreshPanelROIList();
			}else
			{
				MessageDialog.showDialog("There is no active sequence.", MessageDialog.WARNING_MESSAGE );
			}
			
//			mainPanel.validate();
//			mainPanel.updateUI();
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
