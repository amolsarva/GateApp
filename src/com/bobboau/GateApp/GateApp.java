/*
 *  GateApp.java
  */

package com.bobboau.GateApp;

import gate.util.Files;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
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
	 * constructor, starts up the Gate application
	 */
	public GateApp()
	{
		construct();
	}
	
	/**
	 * constructor, starts up the Gate application
	 * @param listener 
	 */
	public GateApp(GateAppListener listener)
	{
		addListener(listener);
		construct();
	}
	
	/**
	 * construction common code
	 */
	private void construct(){
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
