/**
 * 
 */
package com.bobboau.GateApp;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


/**
 * @author bobboau
 * this is a TFIDF calculator the works only with the corpus at hand
 */
public class LocalTfidf implements Tfidf {
	
	/**
	 * the counts of documents for all terms
	 * term=>number of documents with it
	 */
	private HashMap<String,Integer> document_counts = null;
	
	/**
	 * the counts of terms for a given document
	 * [document idx]term=>number of times term shows up in document idx
	 */
	private ArrayList<HashMap<String,Integer>> term_counts = null;
	
	/**
	 * set a new corpus
	 * @param corpus
	 */
	public void setCorpus(Corpus corpus){
		
		document_counts = new HashMap<String,Integer>();
		term_counts = new ArrayList<HashMap<String,Integer>>();
		
		for(Document document : corpus)
		{
			HashMap<String,Integer> term_count = new HashMap<String,Integer>();
			AnnotationSet annotations = document.getAnnotations().get("Term");
			for(Annotation term : annotations)
			{
				String term_string = term.getFeatures().get("string").toString().toLowerCase().replaceAll("[^A-Za-z0-9]", "");
				
				if(term_count.containsKey(term_string)){
					term_count.put(term_string, term_count.get(term_string)+1);
				}
				else{
					term_count.put(term_string, 1);
				}
			}
			term_counts.add(term_count);
			for(String term : term_count.keySet()){
				if(document_counts.containsKey(term)){
					document_counts.put(term, document_counts.get(term)+1);
				}
				else{
					document_counts.put(term, 1);
				}
			}
		}
	}

	/**
	 * get a list of allterms in all documents
	 * @return a list of all terms in all documents
	 */
	public List<String> getTerms(){
		return new ArrayList<String>(document_counts.keySet());
	}

	/**
	 * get an unsorted list of all terms in the specified document
	 * @param doc_idx which document we want the terms for in no particular order
	 * @return a list of all terms in a given document
	 */
	public List<String> getTerms(int doc_idx){
		return new ArrayList<String>(term_counts.get(doc_idx).keySet());
	}

	/**
	 * get a sorted list of all terms in the specified document, sorted in  order from highest score to lowest
	 * @param doc_idx which document we want the terms for in no particular order
	 * @return same as getTerms, but ordered by their tfidf score with highest first
	 */
	public List<String> getTermsOrdered(final int doc_idx){
		List<String> terms = getTerms(doc_idx);
		Collections.sort(terms, new Comparator<String>(){
			@Override
			public int compare(String a, String b) {
				double diff = getScore(a, doc_idx) - getScore(b, doc_idx);
				if(diff == 0.0){
					return 0;
				}
				else
				{
					return diff < 0.0 ? 1 : -1;
				}
			}
		});
		return terms;
	}
	
	/**
	 * get the score of the given term in he context of the given document
	 * @param term the term we want a score for
	 * @param doc_idx which document we want the score calculated with respect to
	 * @return the TF/IDF score of the term with respect to the given document
	 */
	public double getScore(String term, int doc_idx){
		term = term.toString().toLowerCase().replaceAll("[^A-Za-z0-9]", "");
		try{
			double idf = ((double)term_counts.size())/((double)document_counts.get(term));
			double tf = ((double)term_counts.get(doc_idx).get(term))/((double)term_counts.get(doc_idx).size());
			return tf*Math.log10(idf);
		}
		catch(IndexOutOfBoundsException e){
			return 0.0;
		}
	}
}
