/**
 * 
 */
package com.bobboau.GateApp;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.corpora.CorpusImpl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class AncTfidf implements Tfidf{
	private Map<String, Double> term_frequency = new HashMap<String, Double>();
	private boolean corp_freq_set = false;
	private CorpusImpl corpus = null;
	private Map<String,Double> tfidf = new TreeMap<String,Double>();
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
		try {
			setTermsMap(doc_idx);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<Double,String> tfidf_backwards = new TreeMap<Double,String>();
		
		for (Map.Entry entry : tfidf.entrySet()) { 
			tfidf_backwards.put(tfidf.get(entry.getKey()), (String) entry.getKey());
		}
		
		ArrayList<Double> sorted_tfidf = new ArrayList<Double>(tfidf_backwards.keySet());
		Collections.sort(sorted_tfidf);
		
		ArrayList<String> terms_ordered = new ArrayList<String>();
		for (int i = sorted_tfidf.size()-1; i >= 0; i--){
			terms_ordered.add(tfidf_backwards.get(sorted_tfidf.get(i)));
		}
		
		return terms_ordered;
	}
	
	/**
	 * get the score of the given term in he context of the given document
	 * @param term the term we want a scrote for
	 * @param doc_idx which document we want the score calculated with respect to
	 * @return
	 */
	public double getScore(String term, int doc_idx){
		try {
			setTermsMap(doc_idx);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tfidf.get(term);
	}
	
	
	/*
	 * This takes the OANC corpus and brings the information into a 
	 * HashMap, this allows TF-IDf to be calculated quickly
	 * The TF-IDF values has been changed to TF-ITTF a variant
	 * of TF-IDF, the specifics can be found here: 
	 * http://www.soi.city.ac.uk/~ser/idfpapers/Robertson_idf_JDoc.pdf
	 */
	private void corp_freq (){
		Map<String, Double> term_frequency = new HashMap<String, Double>();
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("ANC-token-word.txt"));
			
			String line;
			while ((line = br.readLine()) != null) {
			   String[] split = line.split("\\s+");
			   split[0] = split[0].replaceAll("[\\s\\-()]", "");
			   //System.out.println(split[0] + split[1]);
			   
			   term_frequency.put(split[0], Double.parseDouble(split[1]));
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
		
		this.term_frequency = term_frequency;	
	}
	
	/*
	 * Given a document this will calculate the tf-idf
	 * values of the document and store these 
	 * in the tfidf data structure
	 */
	private void setTermsMap(int doc_idx) throws IOException{
		if(!corp_freq_set){
			corp_freq_set = true;
			corp_freq();
		}

		String most_common = "08798757650874208764200065";
		double max_freq = -1; 

		double doc_num = 22164985; // total number of documents
		Map<String, Double> term_frequency = new HashMap<String, Double>(); // number of times the term occurs in the document
		Map<String, Double> doc_frequency = this.term_frequency; // number of documents the term occurs in 





		ArrayList<String> terms = (ArrayList<String>) getTerms(doc_idx);

		for (int i = 0; i<terms.size(); i++){

			if(term_frequency.containsKey(terms.get(i))){
				double val = term_frequency.get(terms.get(i));
				val++;
				term_frequency.remove(terms.get(i));
				term_frequency.put(terms.get(i), val);

				if (val > max_freq){
					max_freq = val;
					most_common = terms.get(i);
				}
			}else{
				term_frequency.put(terms.get(i),(double)1);
				doc_num++;

				if (i==0) {
					most_common = terms.get(i);
					max_freq = 1;
				}
			}


		}

		Map<String,Double> tfidf_temp = new TreeMap<String,Double>();
		for (Map.Entry entry : term_frequency.entrySet()) { 
			double tf = 0.5 + ((0.5 * term_frequency.get(entry.getKey())) / max_freq);
			double idf = Math.log(doc_num/doc_frequency.get(entry.getKey())) / Math.log(10);


			//System.out.print("tf-idf" + tf*idf);

			//System.out.println("key,val: " + entry.getKey() + "," + entry.getValue()); 

			tfidf_temp.put((String) entry.getKey(), tf*idf);

		}

		//System.out.println(most_common);
		//System.out.println(max_freq);

		//System.out.println(doc_frequency.get("westside"));

		tfidf = tfidf_temp;
	}
}
