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
//import graph.*;

import com.bobboau.GateApp.BlockFormatter.Blob;
import com.bobboau.GateApp.TermBlocks.Block;

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
	 * calculates blocks of high TF/IDF
	 */
	private TermBlocks term_blocks = new TermBlocks();
	
	/**
	 * TermBlocks is not the appropriate place to extract people
	 */
	PersonExtractor person_extractor = new PersonExtractor();

	/**
	 * number of results we will be showing
	 */
	private int	result_size;
	
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
			Gate.getCreoleRegister().registerDirectories(new File(Gate.getPluginsHome(), "Tools").toURI().toURL());
			Gate.getCreoleRegister().registerComponent(FooterFinderPR.class);
		
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
			
			setTFIDF(this.config.get("tfidf_implementation", "Local"));
			
			this.term_blocks.setBlockSize(this.config.get("block_size", 5));
			this.result_size = this.config.get("result_size", 5);

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
			this.term_blocks.setCorpus(this.corpus);
			this.person_extractor.setCorpus(this.corpus);
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
	 * @param progress how far along is it
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
	public void getDocumentContent(int idx, ResultRetriever<String> results){
		results.value(this.corpus.get(idx).getContent().toString());
	}
	/**
	 */
	@Override
	public void getDocumentPeople(int idx, ResultRetriever<List<Vertex_people>> results){
		results.value(this.person_extractor.get_people(idx));
	}
	/**
	 */
	@Override
	public void getDocumentRelations(int idx, ResultRetriever<List<edge_relation>> results){
		results.value(this.person_extractor.get_relation(idx));
	}
	/**
	 * @param idx
	 */
	@Override
	public void getDocumentSubject(int idx, ResultRetriever<String> results){

		List<Block> doc_blocks = BlockFormatter.getScoreSortedBlocks(idx, this.term_blocks);
		List<Blob> blobs = BlockFormatter.mergeLocalText(this.result_size, doc_blocks);

		blobs = blobs.subList(0, Math.min(this.result_size, blobs.size()));
		
		BlockFormatter.documentOrderSort(blobs);
		
		List<String> terms = BlockFormatter.blobsToStrings(idx, blobs, this.term_blocks);
		
		String result = "email thread: "+this.corpus.get(idx).getName()+":\n\n";

		for(int i = 0; i<terms.size(); i++){
			result+=terms.get(i);
			if(i+1<terms.size()){
				result+="\n\n";
			}
		}
		
		results.value(result);
	}
	
	
	
	/**
	 * set the block size
	 */
	@Override
	public void setBlockSize(int size) {
		this.term_blocks.setBlockSize(size);
		this.config.set("block_size", size);
	}

	/**
	 * get the block size
	 */
	@Override
	public int getBlockSize() {
		return this.term_blocks.getBlockSize();
	}
	
	
	
	/**
	 * set the results count
	 */
	@Override
	public void setResultSize(int size) {
		this.result_size = size;
		this.config.set("result_size", size);
	}

	/**
	 * get the block size
	 */
	@Override
	public int getResultSize() {
		return this.result_size;
	}

	/**
	 * set the tfidf implementation
	 */
	@Override
	public void setTFIDF(String implementation) {
		this.config.set("tfidf_implementation", implementation);
		Tfidf new_tfidf = Tfidf.factory.make(implementation);
		this.term_blocks.setTfidf(new_tfidf);
		this.person_extractor.setTfidf(new_tfidf);
	}

} // class GateApp
