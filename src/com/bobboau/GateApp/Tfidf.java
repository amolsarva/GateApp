/**
 * 
 */
package com.bobboau.GateApp;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.corpora.CorpusImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author bobboau
 *
 */
public class Tfidf {
	
	private CorpusImpl corpus = null;
	/**
	 * set a new corpus
	 * @param corpus
	 */
	
	// shouldn't we pass in CorpusImpl? 
	public void setCorpus(Corpus corpus){
		this.corpus = (CorpusImpl) corpus;
		//AnnotationSet annotations = corpus.get(idx).getAnnotations().get("Term");
	}

	/**
	 * get a list of allterms in all documents
	 * @return
	 */
	public List<String> getTerms(){
		List<String> allterms = new ArrayList<String>();
		
		for (int i = 0; i < corpus.size(); i++){
			AnnotationSet annotations = corpus.get(i).getAnnotations().get("Term"); 
			
			Iterator<Annotation> index = annotations.iterator();
			
			while(index.hasNext()){
				Annotation first = index.next();
				allterms.add((String) first.getFeatures().get("string"));
			}
		}
		
		return allterms;
	}

	/**
	 * get an unsorted list of all terms in the specified document
	 * @param doc_idx which document we want the terms for in no particular order
	 * @return
	 */
	public List<String> getTerms(int doc_idx){

		List<String> allterms = new ArrayList<String>();
		
		AnnotationSet annotations = corpus.get(doc_idx).getAnnotations().get("Term"); 
		
		Iterator<Annotation> index = annotations.iterator();
		
		while(index.hasNext()){
			Annotation first = index.next();
			allterms.add((String) first.getFeatures().get("string"));
		}
		
		return allterms;
	}

	/**
	 * get a sorted list of all terms in the specified document, sorted in  order from highest score to lowest
	 * @param doc_idx which document we want the terms for in no particular order
	 * @return
	 */
	public List<String> getTermsOrdered(int doc_idx){
		return null;
	}
	
	/**
	 * get the score of the given term in he context of the given document
	 * @param term the term we want a scrote for
	 * @param doc_idx which document we want the score calculated with respect to
	 * @return
	 */
	public double getScore(String term, int doc_idx){
		return 0.0;
	}
}
