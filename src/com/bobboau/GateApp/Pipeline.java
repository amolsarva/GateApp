/**
 * 
 */
package com.bobboau.GateApp;

import gate.Corpus;
import gate.creole.ExecutionException;

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
}
