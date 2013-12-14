package com.bobboau.GateApp;
import gate.util.GateException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
//import graph


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
		
		/**
		 * called when processing starts
		 */
		void onCorpusProcessStart();
		
		/**
		 * called when one of the documents has finished processing
		 * @param progress 
		 */
		void onCorpusDocumentProcessed(int progress);
		
		/**
		 * called when processing is done
		 */
		void onProcessingFinished();
	}
	
	/**
	 * simple interface for getting data
	 * @param <Type> the type of result we are returning
	 */
	public interface ResultRetriever<Type>{
		/**
		 * gets you a string value
		 * @param value 
		 */
		void value(Type value);
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
	public void getDocumentContent(int idx, ResultRetriever<String> results);
	/**
	 * @param idx
	 * @param results
	 */
	public void getDocumentPeople(int idx, ResultRetriever<List<Vertex_people>> results);
	/**
	 * @param idx
	 * @param results
	 */
	public void getDocumentRelations(int idx, ResultRetriever<List<edge_relation>> results);
	
	/**
	 * @param idx
	 * @param results NLP magic
	 */
	public void getDocumentSubject(int idx, ResultRetriever<String> results);
	
	/**
	 * sets the size of the tfidf blocks
	 * @param size
	 */
	public void setBlockSize(int size);
	
	/**
	 * sets the size of the tfidf blocks
	 * @return the size of the block
	 */
	public int getBlockSize();

	/**
	 * sets the implementation of tfidf to one of the specific implementations
	 * @param implementation
	 */
	public void setTFIDF(String implementation);
}
