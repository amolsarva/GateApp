/**
 * this is the UI of the gate application
 */
package com.bobboau.GateApp;


import gate.util.GateException;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author Bobboau
 *this is the UI for the Gate application
 */
@SuppressWarnings({"serial", "synthetic-access"})
public class GateFrame extends JFrame implements GateAppType.GateAppListener
{
	/**
	 * key/value pair configuration object
	 */
	private Config config;
	
	/**
	 * the application object, where most of the 'fun' happens
	 */
	private GateAppType the_app = null;
	
	/**
	 * a list widget that shows all of the files loaded
	 */
	private JList<File> document_list = new JList<File>();
	
	/**
	 * the output, where all the thread content gets displayed
	 */
	private JEditorPane thread_output = new JEditorPane();
	
	/**
	 * the output, where all the nlp content gets displayed
	 * this is the thing that shows the stuff that our grade will be determined by
	 */
	private JEditorPane nlp_output = new JEditorPane();
	
	/**
	 * progress display for when we are loading files
	 */
	private ProgressMonitor progress = null;
	
	/**
	 * the above apparently is deficient in it's ability to track progress so we have to do it for it
	 */
	int progress_amount = 0;
	
	/**
	 * how many documents have been processed
	 */
	int document_progress = 0;

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
				try {
					new GateFrame();
				} catch (HeadlessException e) {
					e.printStackTrace();
					System.exit(ABORT);
				}
			}
		});
	}

	/**
	 * 
	 */
	@Override
	public void onGateFailed(GateException e1)
	{
		JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
		System.exit(1);
	}

	/**
	 * constructor for the Gate application UI
	 * @throws HeadlessException
	 */
	public GateFrame() throws HeadlessException
	{
		super();
		
		this.config = Config.load("GateApp.conf");
		
		setupWidgets();
		setupMenu();
		
		this.the_app = new ThreadedGateApp(this);
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
		
		this.document_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		this.thread_output.setEditable(false);
		this.nlp_output.setEditable(false);
		
		//make a few scrolling containers for a few elements
		JScrollPane thread_output_holder = new JScrollPane(this.thread_output, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane document_list_holder = new JScrollPane(this.document_list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		//this will hold the elements on the right side of the UI
		final JSplitPane right_elements = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		//set up the layout for the right
		right_elements.add(this.nlp_output);
		right_elements.add(thread_output_holder);

		//now set up the main layout using the right layout
		JSplitPane contents = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		contents.add(document_list_holder);
		contents.add(right_elements);
		add(contents);
		
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				right_elements.setDividerLocation(0.5);
			}
		});
		
		this.document_list.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent event)
			{
				GateFrame.this.onSelectedDocumentChanged(GateFrame.this.document_list.getSelectedIndex());
			}
		});
		
		this.setEnabled(false);//start out disabled because gate needs to init first
	}


	/**
	 * the gate app has finished setting it's self up
	 */
	@Override
	public void onGateInit()
	{
		this.setEnabled(true);
	}
	
	/**
	 * what to do when the selected document changes
	 * @param document
	 */
	private void onSelectedDocumentChanged(int document)
	{
		this.the_app.getDocumentContent(document, new GateAppType.ResultRetriever(){
			@Override
			public void string(final String value)
			{
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						GateFrame.this.thread_output.setText(value);
					}
				});
			}
		});
		this.the_app.getDocumentSubject(document, new GateAppType.ResultRetriever(){
			@Override
			public void string(final String value)
			{
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						GateFrame.this.nlp_output.setText(value);
					}
				});
			}
		});
	}

	/**
	 * the load menu option was selected
	 */
	private void onLoadCorpus()
	{
		JFileChooser chooser = new JFileChooser();
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		
		chooser.setCurrentDirectory(this.config.get("corpus_directory", new File (".")));
		
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			this.config.set("corpus_directory", chooser.getCurrentDirectory());
			try
			{
				URL url = chooser.getSelectedFile().toURI().toURL();
				this.the_app.setCorpus(url);
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
			
		}
	}

	/**
	 * called when a corpus has started to load
	 */
	@Override
	public void onCorpusLoadStart(final int document_count)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				GateFrame.this.progress_amount = 0;
				GateFrame.this.progress = new ProgressMonitor(GateFrame.this, "Loading...", "", 0, document_count);
				GateFrame.this.setEnabled(false);
			}
		});
	}
	
	/**
	 * called when a corpus has finished loading one file
	 */
	@Override
	public void onCorpusDocumentLoaded()
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				GateFrame.this.progress_amount++;
				GateFrame.this.progress.setProgress(GateFrame.this.progress_amount);
			}
		});
	}
	
	/**
	 * a corpus has been loaded in the gate application
	 */
	@Override
	public void onCorpusLoadComplete(final List<URL> files) {
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				ArrayList<File> list_data = new ArrayList<File>();
				for(URL file : files){
					//add the file, but overload it so that the toString function displays what we want
					list_data.add(new File(file.getPath()){
						public String toString(){
							return this.getName();
						}
					});
				}
				GateFrame.this.setEnabled(true);
				GateFrame.this.document_list.setListData(list_data.toArray(new File[0]));
				GateFrame.this.document_list.setVisible(false);
				GateFrame.this.document_list.setVisible(true);
				GateFrame.this.progress.close();
				GateFrame.this.progress = null;
			}
		});
	}

	/**
	 * 
	 */
	@Override
	public void onCorpusLoadFailed()
	{
		JOptionPane.showMessageDialog(null,"Could not open files in directory ");
		this.setEnabled(true);
	}

	@Override
	public void onCorpusProcessStart() {
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				GateFrame.this.progress_amount = -1;
				GateFrame.this.document_progress = 0;
				GateFrame.this.progress = new ProgressMonitor(GateFrame.this, "Processing...", "", 0, 101);
				
				GateFrame.this.setEnabled(false);
			}
		});
	}

	@Override
	public void onCorpusDocumentProcessed(final int progress) {
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if(GateFrame.this.progress_amount > progress){
					GateFrame.this.document_progress++;
					GateFrame.this.progress.setNote("Annotating ("+GateFrame.this.document_progress+"/"+GateFrame.this.document_list.getModel().getSize()+")...");
				}
				GateFrame.this.progress_amount = progress;
				GateFrame.this.progress.setProgress(progress);
			}
		});
	}

	@Override
	public void onProcessingFinished() {
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				GateFrame.this.progress.close();
				GateFrame.this.progress = null;
				GateFrame.this.setEnabled(true);
			}
		});
	}
	
	/*
	 /-------------------------------\
	 | Never Ending Menu Boilerplate |
	 \-------------------------------/
	 */
	
	/**
	 * main function for setting up all menu options, this focuses on top level
	 */
	private void setupMenu()
	{
		JMenuBar menu_bar = new JMenuBar();
		
		setupFileMenu(menu_bar);
		
		setJMenuBar(menu_bar);
	}
	
	/**
	 * function for setting up the options in the file menu
	 * @param menu_bar
	 */
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
}
