/**
 * this is the UI of the gate application
 */
package com.bobboau.GateApp;


import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import com.bobboau.GateApp.GateApp.GateAppListener;

/**
 * @author Bobboau
 *
 */
@SuppressWarnings({ "nls", "serial" })
public class GateFrame extends JFrame implements GateAppListener
{
	/**
	 * key/value pair configuration object
	 */
	Config config;
	
	/**
	 * the application object, where most of the 'fun' happens
	 */
	GateApp the_app = null;
	
	/**
	 * a list widget that shows all of the files loaded
	 */
	JList<String> document_list = new JList<String>();
	
	/**
	 * the output, where all the thread content gets displayed
	 */
	JEditorPane thread_output = new JEditorPane();
	
	/**
	 * the output, where all the nlp content gets displayed
	 * this is the thing that shows the stuff that our grade will be determined by
	 */
	JEditorPane nlp_output = new JEditorPane();

	/**
	 * run the application
	 * @param args 
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@SuppressWarnings("unused")
			@Override
			public void run()
			{
				new GateFrame();
			}
		});
	}

	/**
	 * @throws HeadlessException
	 */
	public GateFrame() throws HeadlessException
	{
		super();
		
		this.config = Config.load("GateApp.conf");
		
		setupWidgets();
		setupMenu();
		
		//UI is done, now make the app
		this.the_app = new GateApp(this);
	}

	/**
	 * sets up the visual elements of the UI
	 */
	private void setupWidgets()
	{
		//some basic things to make sure the window is visible
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setSize(500, 700);
		setTitle("GateApp");
		
		//make a few scrolling containers for a few elements
		JScrollPane thread_output_holder = new JScrollPane(this.thread_output, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane document_list_holder = new JScrollPane(this.document_list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		//this will hold the elements on the right side of the UI
		JPanel right_elements = new JPanel();

		//set up the layout for the right
		right_elements.setLayout(new BoxLayout(right_elements, BoxLayout.Y_AXIS));
		right_elements.add(this.nlp_output);
		right_elements.add(thread_output_holder);

		//now set up the main layout using the right layout
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		add(document_list_holder);
		add(right_elements);
	}
	
	private void setupMenu()
	{
		JMenuBar menu_bar = new JMenuBar();
		
		setupFileMenu(menu_bar);
		
		setJMenuBar(menu_bar);
	}
	
	private void setupFileMenu(JMenuBar menu_bar)
	{
		JMenu menu = new JMenu("File");
		menu_bar.add(menu);
		
		//change your color menu option
		addMenuItem(menu, new JMenuItem("Load Corpus",KeyEvent.VK_T), "Select files to process", ActionEvent.CTRL_MASK,
				new ActionListener(){public void actionPerformed(ActionEvent Event){
					onLoadCorpus();
				}});
	}
	
	/**
	 * boilerplate for adding a menu item to a menu
	 * @param menu the menu the item is going to
	 * @param menu_item the item to add
	 * @param description the tooltip text
	 * @param control_mask what other buttons need to be pressed
	 * @param action what to do when clicked
	 */
	private static void addMenuItem(JMenu menu, JMenuItem menu_item, String description, int control_mask, ActionListener action)
	{
		menu_item.setAccelerator(KeyStroke.getKeyStroke(menu_item.getMnemonic(), control_mask));
		menu_item.setToolTipText(description);
		menu_item.addActionListener(action);
		menu.add(menu_item);
	}
	
	/**
	 * the load menu option was selected
	 */
	public void onLoadCorpus()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(this.config.get("corpus_directory", new File (".")));
		chooser.setMultiSelectionEnabled(true);
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			this.config.set("corpus_directory", chooser.getCurrentDirectory());
			this.the_app.setCorpus(new ArrayList<File>(Arrays.asList(chooser.getSelectedFiles())));
		}
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	@Override
	public void onCorpusLoaded(GateApp app, Iterable<File> files) {
		ArrayList<String> list_data = new ArrayList<String>();
		for(File file : files){
			list_data.add(file.getName());
		}
		this.document_list.setListData(list_data.toArray(new String[0]));
		this.document_list.setVisible(false);
		this.document_list.setVisible(true);
	}
}
