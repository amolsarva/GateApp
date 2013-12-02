/*
 *  GateApp.java
  */

package com.bobboau.GateApp;


import gate.Annotation;
import gate.AnnotationSet;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.corpora.CorpusImpl;
import gate.creole.ANNIEConstants;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.event.CreoleEvent;
import gate.event.ProgressListener;
import gate.util.GateException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;


/**
 * @author Bobboau
 *
 */
public class GateApp implements GateAppType
{	
	/**
	 * things that are listening to our events
	 */
	private List<GateAppType.GateAppListener> listeners = new ArrayList<GateAppType.GateAppListener>();
	
	/**
	 * key/value pair configuration object
	 */
	private Config config;
	
	/**
	 * set of documents we are working on
	 */
	private CorpusImpl corpus = null;
	
	/**
	 * the processing pipeline
	 */
	private Pipeline base_pipeline = null;
	
	/**
	 * the tfidf calculator
	 */
	private Tfidf tfidf = new Tfidf();
	
	/**
	 * constructor, starts up the Gate application
	 */
	public GateApp()
	{
		construct();
	}
	
	/**
	 * constructor, starts up the Gate application
	 * @param listener	 */
	public GateApp(GateAppListener listener)
	{
		addListener(listener);
		construct();
	}
	
	/**
	 * construction common code
	 * @throws GateException 
	 */
	protected void construct()
	{
		
		try
		{
			Gate.init();
			Gate.getCreoleRegister().registerDirectories(new File(System.getProperty("user.dir")).toURI().toURL());
			Gate.getCreoleRegister().registerDirectories(new File(Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR).toURI().toURL());
			
			this.base_pipeline = new AnniePipeline();
			this.base_pipeline.addProgressListener(new ProgressListener(){
				@Override public void processFinished() {
				}

				@Override
				public void progressChanged(int progress) {
					GateApp.this.documentProcessed(progress);
				}
			});
			
			this.corpus = new CorpusImpl(){
				public void resourceLoaded(CreoleEvent e){
					super.resourceLoaded(e);
					GateApp.this.documentLoaded();
				}
			};
			this.config = Config.load("GateApp.conf"); //load the application configuration settings
			
			for(GateAppListener gate_listener : this.listeners)
			{
				gate_listener.onGateInit();
			}
			
			//load up what ever corpus we had last time, default to nothing
			setCorpus(new URL(this.config.get("loaded_files", "")));
		}
		catch (IOException e)
		{
			//if it fails just log it, don't worry about it too much
			e.printStackTrace();
		}
		catch (GateException e1)
		{
			for(GateAppListener gate_listener : this.listeners)
			{
				gate_listener.onGateFailed(e1);
			}

		}
	}
	
	/**
	 * @param gate_listener
	 */
	@Override
	public void addListener(GateAppListener gate_listener)
	{
		this.listeners.add(gate_listener);
	}
	
	/**
	 * loads up a list of files
	 * @param document_directory
	 */
	@Override
	public void setCorpus(URL document_directory)
	{
		FileFilter filter = new FileFilter(){
			@Override
			public boolean accept(File file)
			{
				return file.getName().endsWith(".pdf");
			}
		};
		
		int file_count = new File(document_directory.getFile()).listFiles(filter).length;
		
		for(GateAppListener gate_listener : this.listeners)
		{
			gate_listener.onCorpusLoadStart(file_count);
		}
		
		try
		{
			this.corpus.populate(
				document_directory, 
				filter,
				"UTF-8",
				false
			);
		}
		catch (ResourceInstantiationException | IOException e)
		{
			for(GateAppListener gate_listener : this.listeners)
			{
				gate_listener.onCorpusLoadFailed();
			}
		}
		finally
		{
			for(GateAppListener gate_listener : this.listeners)
			{
				gate_listener.onCorpusLoadComplete(getCorpus());
			}
		}
		
		this.config.set("loaded_files", document_directory.toString());
		
		try
		{
			for(GateAppListener gate_listener : this.listeners)
			{
				gate_listener.onCorpusProcessStart();
			}
			this.base_pipeline.execute(this.corpus);
			this.tfidf.setCorpus(corpus);
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
		finally
		{
			for(GateAppListener gate_listener : this.listeners)
			{
				gate_listener.onProcessingFinished();
			}
		}
	}
	
	/**
	 * called whenever a corpus loads a document
	 */
	public void documentLoaded(){
		for(GateAppListener gate_listener : this.listeners)
		{
			gate_listener.onCorpusDocumentLoaded();
		}
	}
	
	/**
	 * called whenever a corpus loads a document
	 * @param i 
	 */
	public void documentProcessed(int progress){
		for(GateAppListener gate_listener : this.listeners)
		{
			gate_listener.onCorpusDocumentProcessed(progress);
		}
	}
	
	/**
	 * @return list of files loaded into the corpus
	 */
	private List<URL> getCorpus()
	{
		ArrayList<URL> file_list = new ArrayList<URL>();
		for(Document document : this.corpus){
			file_list.add(document.getSourceUrl());
		}
		return file_list;
	}
	
	/**
	 * @param idx
	 */
	@Override
	public void getDocumentContent(int idx, ResultRetriever results){
		results.string(this.corpus.get(idx).getContent().toString());
	}
	
	/**
	 * @param idx
	 */
	@Override
	public void getDocumentSubject(int idx, ResultRetriever results){
		
		AnnotationSet annotations = this.corpus.get(idx).getAnnotations().get("Term");
		List<String> terms = this.tfidf.getTermsOrdered(idx);
		String result = this.corpus.get(idx).getName()+" has:\n"+annotations.size()+" Terms. \top five terms are "+terms.get(0)+", "+terms.get(1)+", "+terms.get(2)+", "+terms.get(3)+", "+terms.get(4);
		
		results.string(result);
	}

} // class GateApp
