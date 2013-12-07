/**
 * 
 */
package com.bobboau.GateApp;

import gate.Corpus;
import java.util.List;

/**
 * @author bobboau
 *
 */
public interface Tfidf {
	/**
	 * set a new corpus
	 * @param corpus
	 */
	public void setCorpus(Corpus corpus);

	/**
	 * get a list of allterms in all documents
	 * @return
	 */
	public List<String> getTerms();

	/**
	 * get an unsorted list of all terms in the specified document
	 * @param doc_idx which document we want the terms for in no particular order
	 * @return
	 */
	public List<String> getTerms(int doc_idx);

	/**
	 * get a sorted list of all terms in the specified document, sorted in  order from highest score to lowest
	 * @param doc_idx which document we want the terms for in no particular order
	 * @return
	 */
	public List<String> getTermsOrdered(int doc_idx);
	
	/**
	 * get the score of the given term in he context of the given document
	 * @param term the term we want a scrote for
	 * @param doc_idx which document we want the score calculated with respect to
	 * @return
	 */
	public double getScore(String term, int doc_idx);
}
