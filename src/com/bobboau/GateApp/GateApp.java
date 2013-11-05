/*
 *  GateApp.java
  */

package com.bobboau.GateApp;


import gate.Gate;
import gate.corpora.CorpusImpl;
import gate.corpora.DocumentImpl;
import gate.util.GateException;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.*;

/**
 * @author Bobboau
 *
 */
public class GateApp
{
	/**
	 * @author Bobboau
	 * a bunch of callbacks that some external object can get feedback from
	 */
	public interface GateAppListener
	{
		/**
		 * called when a new corpus has finished loading
		 * @param app 
		 * @param files 
		 */
		void onCorpusLoaded(GateApp app, Iterable<File> files);
	}
	
	/**
	 * things that are listening to our events
	 */
	private List<GateAppListener> listeners = new ArrayList<GateAppListener>();
	
	/**
	 * key/value pair configuration object
	 */
	private Config config;
	
	/**
	 * this should be replaced with CorpusImpl or something similar ASAP this is stub functionality
	 */
	private Iterable<File> TEMPORARY_FILE_LIST = new ArrayList<File>();
	
	/**
	 * set of documents we are working on
	 */
	private CorpusImpl corpus = null;
	
	/**
	 * constructor, starts up the Gate application
	 * @throws GateException 
	 */
	public GateApp() throws GateException
	{
		construct();
	}
	
	/**
	 * constructor, starts up the Gate application
	 * @param listener 
	 * @throws GateException 
	 */
	public GateApp(GateAppListener listener) throws GateException
	{
		addListener(listener);
		construct();
	}
	
	/**
	 * construction common code
	 * @throws GateException 
	 */
	private void construct() throws GateException{
		Gate.init();
		corpus = new CorpusImpl();
		this.config = Config.load("GateApp.conf"); //load the application configuration settings	
		setCorpus(this.config.get("loaded_files", new ArrayList<File>()));//load up what ever corpus we had last time, default to nothing
	}
	
	/**
	 * @param gate_listener
	 */
	public void addListener(GateAppListener gate_listener)
	{
		this.listeners.add(gate_listener);
	}
	
	/**
	 * loads up a list of files
	 * @param <S> 
	 * @param files
	 */
	public <S extends Serializable & Iterable<File>> 
	void setCorpus(S files)
	{
		for(File file : files)
		{
			try {
				DocumentImpl document = new DocumentImpl();
				document.setSourceUrl(file.toURI().toURL());
				corpus.add(document);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.TEMPORARY_FILE_LIST = files;
		//TODO: load up a corpus with the listed files
		
		this.config.set("loaded_files", files);
		for(GateAppListener gate_listener : this.listeners)
		{
			gate_listener.onCorpusLoaded(this, getCorpus());
		}
	}
	
	/**
	 * @return list of files loaded into the corpus
	 */
	public Iterable<File> getCorpus()
	{
		return this.TEMPORARY_FILE_LIST;
	}
	
	/**
	 * @param file
	 * @return string, contents of file
	 */
	public String getDocumentContent(File file){
		return "contents of "+file.getName()+" here";
	}
	
	/**
	 * @param file
	 * @return string, contents of file
	 */
	public String getDocumentSubject(File file){
		return "Cool NLP stuff about "+file.getName();
	}

} // class GateApp
