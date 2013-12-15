/**
 * this is the UI of the gate application
 */
package com.bobboau.GateApp;


import gate.Annotation;
import gate.util.GateException;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import gate.util.GateException;
//import graph.Vertex_people;
//import graph.edge_relation;
//import graph.graph_visualizer;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
	
	private List<Vertex_people> Peoples = new ArrayList<Vertex_people>();
	private List<edge_relation> relation = new ArrayList<edge_relation>();
	private  VisualizationViewer vv =null;

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
		
		
		// yilin's gadget
//		frame1.setSize(200, 200);
//				graph_visualizer abc = get_graph();	
//				final VisualizationViewer<Integer,Number> vv = abc.vv;
//				vv = abc.vv;
//				this.panel = new GraphZoomScrollPane(vv);
//				ModalGraphMouse gm = new DefaultModalGraphMouse<Integer,Number>();
//		    	vv.setGraphMouse(gm);
//		    	JButton plus = new JButton("+");
//		    	plus.addActionListener(new ActionListener() {
//		    	public void actionPerformed(ActionEvent e) {
//		    		ScalingControl scaler = new CrossoverScalingControl();
//		             scaler.scale(vv, 1.1f, vv.getCenter());
//		          }
//		      });
//		      JButton minus = new JButton("-");
//		      minus.addActionListener(new ActionListener() {
//		          public void actionPerformed(ActionEvent e) {
//		        	  ScalingControl scaler = new CrossoverScalingControl();
//		              scaler.scale(vv, 1/1.1f, vv.getCenter());
//		          }
//		      });
//
//		      
//		      
//		      controls.add(plus);
//		      controls.add(minus);
//		      controls.add(((DefaultModalGraphMouse<Integer,Number>) gm).getModeComboBox());
////		      JSplitPane mid_elements = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		      JSP.add(this.panel);
//		      JSP.setVisible(true);
//		      mid_elements.add(JSP);
//		      mid_elements.add(new JScrollPane(controls));
		      
		//	
		JSplitPane sub_contents = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,(new JScrollPane(document_list_holder)),new JScrollPane(right_elements));
	   	

		//now set up the main layout using the right layout
//		JSplitPane contents = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(sub_contents),new JScrollPane(mid_elements));

		add(sub_contents);

		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				right_elements.setDividerLocation(0.5);
			}
		});
		
		this.document_list.addListSelectionListener(new ListSelectionListener(){
			private int old_document = -1;
			@Override
			public void valueChanged(ListSelectionEvent event)
			{
				int new_document = GateFrame.this.document_list.getSelectedIndex();
				if(this.old_document != new_document){
					GateFrame.this.onSelectedDocumentChanged(GateFrame.this.document_list.getSelectedIndex());
					this.old_document = new_document;
				}
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
		this.the_app.getDocumentContent(document, new GateAppType.ResultRetriever<String>(){
			@Override
			public void value(final String value)
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
		this.the_app.getDocumentSubject(document, new GateAppType.ResultRetriever<String>(){
			@Override
			public void value(final String value)
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
		this.the_app.getDocumentPeople(document, new GateAppType.ResultRetriever<List<Vertex_people>>(){
			@Override
			public void value(final List<Vertex_people> people) {
				EventQueue.invokeLater(new Runnable()
				{
					@Override					
					public void run()
					{   
						
					}
				});
			}	
		});
		
		this.the_app.getDocumentRelations(document, new GateAppType.ResultRetriever<List<edge_relation>>(){
			@Override
			public void value(final List<edge_relation> relations) {
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						
						GateFrame.this.relation = relations;
						Generate_graph();

												
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

	/**
	 * tell the UI that processing has started
	 */
	@Override
	public void onCorpusProcessStart() {
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				GateFrame.this.progress_amount = -1;
				GateFrame.this.document_progress = 1;
				GateFrame.this.progress = new ProgressMonitor(GateFrame.this, "Processing...", "", 0, 101);
				
				GateFrame.this.setEnabled(false);
			}
		});
	}

	/**
	 * tell the UI that some processing has happened
	 */
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

	/**
	 * tell the UI that processing is done
	 */
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
	
	/**
	 * have the user change the block size
	 */
	void onChangeBlockSize(){
		String new_size = JOptionPane.showInputDialog(this, "Enter new block size", this.the_app.getBlockSize());
		try{
			this.the_app.setBlockSize(Integer.parseInt(new_size));
		}
		catch(Exception e){
			//parse error, do nothing
		}
	}

	/**
	 * change the TF/IDF algorithm to the local implementation
	 */
	private void onUseLocalTFIDF() {
		this.the_app.setTFIDF("Local");
	}

	/**
	 * change the TF/IDF algorithm to the ANC implementation
	 */
	private void onUseAncTFIDF() {
		this.the_app.setTFIDF("ANC");
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
		
		setupAlgorithmMenu(menu_bar);
		
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
		
		//load a corpus from a directory
		addMenuItem(menu, new JMenuItem("Load Corpus",KeyEvent.VK_C), "Select files to process", ActionEvent.CTRL_MASK,
				new ActionListener(){public void actionPerformed(ActionEvent Event){
					onLoadCorpus();
				}});
	}
	
	/**
	 * function for setting up the algorithm in the file menu
	 * @param menu_bar
	 */
	private void setupAlgorithmMenu(JMenuBar menu_bar)
	{
		JMenu menu = new JMenu("Algorithm");
		menu_bar.add(menu);
		
		//change the size of the tfidf blocks
		addMenuItem(menu, new JMenuItem("Change Block Size",KeyEvent.VK_B), "Change block size", ActionEvent.CTRL_MASK,
				new ActionListener(){public void actionPerformed(ActionEvent Event){
					onChangeBlockSize();
				}});
		
		//change the size of the tfidf blocks
		addMenuItem(menu, new JMenuItem("Use Local TF/IDF",KeyEvent.VK_L), "Local TF/IDF", ActionEvent.CTRL_MASK,
				new ActionListener(){public void actionPerformed(ActionEvent Event){
					onUseLocalTFIDF();
				}});
		
		//change the size of the tfidf blocks
		addMenuItem(menu, new JMenuItem("Use ANC TF/IDF",KeyEvent.VK_L), "ANC TF/IDF", ActionEvent.CTRL_MASK,
				new ActionListener(){public void actionPerformed(ActionEvent Event){
					onUseAncTFIDF();
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
	 * @return -
	 */
	// set visulization part of the graph
	public graph_visualizer get_graph()
    {	
		if (!this.relation.isEmpty()){
		this.Peoples.removeAll(this.Peoples);
			for(int i = 0 ;i<this.relation.size(); i++){		
				edge_relation Private_relation =null;
				Private_relation = this.relation.get(i);
				String people1 = Private_relation.get_First();
				String people2 = Private_relation.get_Second();
				Vertex_people new_comer = new Vertex_people(people1);
				if(!this.Peoples.contains(new_comer))
				{
					this.Peoples.add(new_comer);
				}
				
				Vertex_people new_comer2 = new Vertex_people(people2);
				if(!this.Peoples.contains(new_comer2))
				{
					this.Peoples.add(new_comer2);
				}
				
			}
			
			
			
			
			graph_visualizer abc = new graph_visualizer((ArrayList)this.Peoples,(ArrayList)this.relation,this.relation.size());
			abc.vv.setName("1");
			return abc;
			}else{
    	ArrayList<Vertex_people> VP = new ArrayList<Vertex_people>();
    	ArrayList<edge_relation> ER = new ArrayList<edge_relation>();
    	VP.add(new Vertex_people("Lily"));
    	VP.add(new Vertex_people("Lucy"));
    	VP.add(new Vertex_people("Jon"));
    	VP.add(new Vertex_people("Jone"));
    	Vertex_people mike = new Vertex_people("mike");
    	ArrayList<String> mike_F = new ArrayList<String>();
    	mike_F.add("Feature 1");
    	mike_F.add("Feature 2");
    	mike_F.add("Feature 3");
    	mike.set_feature(mike_F);
    	VP.add(mike);
    	
//    	one vertex
    	Vertex_people tommy =new Vertex_people("tommy");
    	ArrayList<String> tommy_F = new ArrayList<String>();
    	tommy_F.add("Feature 1");
    	tommy_F.add("Feature 2");
    	tommy.set_feature(tommy_F);
    	VP.add(tommy);
    	
    	
    	ER.add(new edge_relation("tommy","Lily"));
    	ER.add(new edge_relation("mike","Lily"));
    	ER.add(new edge_relation("mike","Lucy"));
    	ER.add(new edge_relation("mike","tommy"));
    	ER.add(new edge_relation("tommy","Lucy"));
    	ER.add(new edge_relation("Jon","Jone"));
    	ER.add(new edge_relation("Jone","Lucy"));
    	ER.add(new edge_relation("Lucy","Jon"));
    	ER.add(new edge_relation("tommy","Jon"));
    	edge_relation abc1 =  new edge_relation("Lucy","tommy");
    	ArrayList<String> abc1_R = new ArrayList<String>();
    	abc1_R.add("Relation_1");
    	abc1_R.add("Relation_2");
    	
    	abc1.set_Relations(abc1_R);
    	
    	ER.add(abc1);
    	graph_visualizer abc = new graph_visualizer(VP,ER,9);
    	return null;
			}
		

    	
    }  
	
	/**
	 * 
	 */
	public void Generate_graph(){
		JPanel controls = new JPanel();
		GraphZoomScrollPane panel = null;
		JPanel JSP = new JPanel();
		
		JFrame frame1 = new JFrame();
		JSplitPane mid_elements = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		frame1.setSize(200, 200);
		graph_visualizer abc = get_graph();	
		if (abc!= null){

		this.vv = abc.vv;
		panel = new GraphZoomScrollPane(this.vv);
		ModalGraphMouse gm = new DefaultModalGraphMouse<Integer,Number>();
    	this.vv.setGraphMouse(gm);
    	JButton plus = new JButton("+");
    	plus.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		ScalingControl scaler = new CrossoverScalingControl();
             scaler.scale(GateFrame.this.vv, 1.1f, GateFrame.this.vv.getCenter());
          }
      });
      JButton minus = new JButton("-");
      minus.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
        	  ScalingControl scaler = new CrossoverScalingControl();
              scaler.scale(GateFrame.this.vv, 1/1.1f, GateFrame.this.vv.getCenter());
          }
      });				      
      
      controls.add(plus);
      controls.add(minus);
      controls.add(((DefaultModalGraphMouse<Integer,Number>) gm).getModeComboBox());
//      JSplitPane mid_elements = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      JSP.add(panel);
      JSP.setVisible(true);
      mid_elements.add(JSP);
      mid_elements.add(new JScrollPane(controls));
//      frame1.setDefaultCloseOperation(frame1.EXIT_ON_CLOSE);
      frame1.add(mid_elements);
	  frame1.pack();
	  frame1.setVisible(true);
		
	}

	
	
}
}
