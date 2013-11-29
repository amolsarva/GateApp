/**
 * 
 */
package com.bobboau.GateApp;

import gate.Corpus;
import gate.creole.ExecutionException;
import gate.event.ProgressListener;

/**
 * @author bobboau
 *
 */
public interface Pipeline {	
	/**
	 * runs the pipeline on the given corpus
	 * @param corpus
	 * @throws ExecutionException 
	 */
	void execute(Corpus corpus) throws ExecutionException;
	
	void addProgressListener(ProgressListener l);
}
