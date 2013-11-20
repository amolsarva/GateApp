package com.bobboau.GateApp;
import gate.util.GateException;

import java.net.URL;
import java.util.List;



/**
 * @author Bobboau
 *
 */
public interface GateAppType
{

	/**
	 * @author Bobboau
	 * a bunch of callbacks that some external object can get feedback from
	 */
	public interface GateAppListener
	{
		/**
		 * called when gate has irrecoverably failed
		 * @param e1 
		 */
		void onGateFailed(GateException e1);
		
		/**
		 * called when the gate app has finished setting up
		 */
		void onGateInit();
		
		/**
		 * called when a new corpus has finished loading
		 * @param list 
		 */
		void onCorpusLoadComplete(List<URL> list);
		
		/**
		 * called when a new corpus has failed to load
		 */
		void onCorpusLoadFailed();
		
		/**
		 * called when a document has been loaded
		 */
		void onCorpusDocumentLoaded();
		
		/**
		 * called when a corpus load starts
		 * @param document_count 
		 */
		void onCorpusLoadStart(int document_count);
	}
	
	/**
	 * simple interface for getting data
	 */
	public interface ResultRetriever{
		/**
		 * gets you a string value
		 * @param value 
		 */
		void string(String value);
	}
	
	/**
	 * @param gate_listener
	 */
	public void addListener(GateAppListener gate_listener);
	
	/**
	 * loads up a list of files
	 * @param document_directory
	 */
	public void setCorpus(URL document_directory);
	
	/**
	 * @param idx
	 * @param results contents of file
	 */
	public void getDocumentContent(int idx, ResultRetriever results);
	
	/**
	 * @param idx
	 * @param results NLP magic
	 */
	public void getDocumentSubject(int idx, ResultRetriever results);
}
