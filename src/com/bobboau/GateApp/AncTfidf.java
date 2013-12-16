/**
 * 
 */
package com.bobboau.GateApp;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.corpora.CorpusImpl;
//import graph.Vertex_people;
//import graph.edge_relation;

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
import java.util.NoSuchElementException;
import java.util.TreeMap;


/**
 * 
 *
 */
public class AncTfidf implements Tfidf{
	private Map<String, Double> term_frequency = null; // a Map to keep track of word frequencies in the OANC corpsu
	private CorpusImpl corpus = null; // The corpus of documents from which text is extracted
	private Map<String,Double> tfittf = null; // Given a document this keeps track of the TF-ITTF value of terms
	double unique_terms = 239208; // The total number of unique terms in the corpus
	double words_counted = 22164985; // Total number of words in the corpus
	
	/**
	 * set a new corpus
	 * @param corpus
	 */
	
	// Initializes the corpus for the class
	// shouldn't we pass in CorpusImpl? 
	public void setCorpus(Corpus corpus){
		this.corpus = (CorpusImpl) corpus;
		//AnnotationSet annotations = corpus.get(idx).getAnnotations().get("Term");
	}

	/**
	 * get a list of allterms in all documents
	 * @return a list of  all terms
	 * The terms of the document are returned in no particular order
	 * The document comes from the corpus
	 */
	public List<String> getTerms(){
		// A check to make sure that the corpus has been set
		if(corpus == null){
			System.out.println("ERROR ERROR ERROR!!!! corpus not set");
			return null;
		}
		// A check to make sure that the OANC corpus has been loaded along with 
		// word frequency
		if(this.term_frequency == null){
			corp_freq();
		}
		
		
		List<String> allterms = new ArrayList<String>();
		
		// Loops through each of the documents in the corpus and extracts all of the
		// terms and adds these terms to the ArrayList allterms
		for (int i = 0; i < corpus.size(); i++){
			AnnotationSet annotations = corpus.get(i).getAnnotations().get("Term"); 
			
			Iterator<Annotation> index = annotations.iterator();
			
			while(index.hasNext()){
				Annotation first = index.next();
				allterms.add((String) first.getFeatures().get("string"));
			}
		}
		
		return allterms;
		// all of the terms in each document in the corpus is now returned
	}

	/**
	 * get an unsorted list of all terms in the specified document
	 * @param doc_idx which document we want the terms for in no particular order
	 * @return a list of all terms in a given document
	 */
	public List<String> getTerms(int doc_idx){
		
		// checks to see if a corpus has been given, if not the program will terminate
		// with junk return value
		if(corpus == null){
			System.out.println("ERROR ERROR ERROR!!!! corpus not set");
			return null;
		}
		
		// Checks to see if the OANC corpus has been loaded along with word frequency
		// counts
		if(this.term_frequency == null){
			corp_freq();
		}
		

		List<String> allterms = new ArrayList<String>();
		
		AnnotationSet annotations = corpus.get(doc_idx).getAnnotations().get("Term"); 
		
		Iterator<Annotation> index = annotations.iterator();
		
		// Loops through the document specified adding all of the terms to the 
		// ArrayList all terms 
		while(index.hasNext()){
			Annotation first = index.next();
			allterms.add((String) first.getFeatures().get("string"));
		}
		
		return allterms; // this now returns all the terms within the document specified
	}

	/**
	 * get a sorted list of all terms in the specified document, sorted in  order from highest score to lowest
	 * @param doc_idx which document we want the terms for in no particular order
	 * @return sorted list of terms in a given document
	 */
	public List<String> getTermsOrdered(int doc_idx){
		
		// First calculates the TF-ITTF values for all of the terms within
		// doc _idx
		try {
			setTermsMap(doc_idx);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<Double,String> tfidf_backwards = new TreeMap<Double,String>();
		
		// In order to sort the values based on the highest TF-ITTF values
		// we reverse the key value order of a Map to extract the TF-ITTF values
		// this relationship allows us to look up the orignal term after sorting
		for (Map.Entry entry : tfittf.entrySet()) { 
			tfidf_backwards.put(tfittf.get(entry.getKey()), (String) entry.getKey());
		}
		
		ArrayList<Double> sorted_tfidf = new ArrayList<Double>(tfidf_backwards.keySet());
		// The list is now sorted by TF-ITTF values
		Collections.sort(sorted_tfidf);
		
		// Given a value now we want to add the term from which the value was derived from
		ArrayList<String> terms_ordered = new ArrayList<String>();
		for (int i = sorted_tfidf.size()-1; i >= 0; i--){
			terms_ordered.add(tfidf_backwards.get(sorted_tfidf.get(i)));
		}
		
		// This returns the terms in sorted order from lowest to highest
		return terms_ordered;
	}
	
	/**
	 * get the score of the given term in he context of the given document
	 * @param term the term we want a scrote for
	 * @param doc_idx which document we want the score calculated with respect to
	 * @return the tfidf score of a given term in respect to a given document
	 */
	public double getScore(String term, int doc_idx){
		// a check is set to see if the corpus is null, if no data was passed in this
		// function doesn't have anything to process on
		if(this.corpus == null){
			System.out.println("ERROR ERROR ERROR!!!! corpus not set");
			return (Double) null;
		}
		
		// checks to see if the OANC corpus was loaded along with term frequencies for
		// each word within the corpus
		if(this.term_frequency == null){
			corp_freq();
		}
		
		// Calculates the TF-ITTF value for each of each term within the document
		try {
			setTermsMap(doc_idx);
		} catch (IOException e) {
			System.out.println("TFITTF map never set");
			e.printStackTrace();
		}
		/*
		Boolean flag = false;
		ArrayList<String> terms = (ArrayList<String>) getTerms(doc_idx);
		for (int i =0; i<terms.size(); i++){
			if (terms.get(i).equals(term)){
				flag = true;
			}
		}*/
		
		/*
		try{
			return this.tfidf.get(term);
		}
		catch (NullPointerException f){
			if(!flag){
				System.out.println("DOESN'T EXIST DOESN'T EXIST DOESN'T EXIST");
				System.out.println("The term causing the crash is: " + term);
			}
		}*/
		
		ArrayList<String> terms = (ArrayList<String>) getTerms(doc_idx);
		
		// checks to see if the term is actually in the document specified, if it isn't
		// then append the term and calculate at TF-ITTF Value, if it is just return the value
		if(this.tfittf.containsKey(term)){
			return this.tfittf.get(term);
		}else{
			
			term_frequency.put(term,1.0);
			words_counted++;
			unique_terms++;
			
			double tf = 1.0 / ((double) terms.size());
			double ittf = Math.log10(words_counted / 1.0);
			return (tf*ittf);
		}
		
		

	}
	
	
	/**
	 * This takes the OANC corpus and brings the information into a 
	 * HashMap, this allows TF-IDf to be calculated quickly
	 * The TF-IDF values has been changed to TF-ITTF a variant
	 * of TF-IDF, the specifics can be found here: 
	 * http://www.soi.city.ac.uk/~ser/idfpapers/Robertson_idf_JDoc.pdf
	 */
	private void corp_freq (){
		Map<String, Double> term_frequency = new HashMap<String, Double>();
		// a temporary map to hold term frequencies inside of the OANC corpus
		
		BufferedReader br;
		try {
			// reads the OANC corpus
			br = new BufferedReader(new FileReader("ANC-token-word.txt"));
			
			String line;
			// adds each word and its term frequency to the temporary Map
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
		
		
	
		// Set the class term frequency variable to the temporary
		// Map with term frequencies, now all of the term frequencies within
		// the OANC corpus is recorded
		this.term_frequency = term_frequency;	
	}
	
	/**
	 * Given a document this will calculate the tf-idf
	 * values of the document and store these 
	 * in the tfidf data structure
	 */
	private void setTermsMap(int doc_idx) throws IOException{
		// checks to see if a corpus has been given, if not the program will terminate
		// with junk return value
		if(corpus == null){
			System.out.println("ERROR ERROR ERROR!!!! corpus not set");
			return;
		}
		
		// checks to see if the OANC corpus was loaded along with term frequencies for
		// each word within the corpus
		if(this.term_frequency == null){
			corp_freq();
		}

		 // total number of documents
		Map<String, Double> term_frequency = new HashMap<String, Double>(); // number of times the term occurs in the document
		
		
		// This is an array which holds all of the terms in the specified document
		ArrayList<String> terms = (ArrayList<String>) getTerms(doc_idx);
		
		// This will loop through all of the terms within the document
		// and record all the unique terms and their counts, it will also
		// update the term frequency of the corpus by adding the values to the
		// corpus
		for (int i = 0; i<terms.size(); i++){
			
			// recording unique terms and their counts
			if(term_frequency.containsKey(terms.get(i))){
				double val = term_frequency.get(terms.get(i));
				val++;
				term_frequency.remove(terms.get(i));
				term_frequency.put(terms.get(i), val);
				
			
			}else{
				term_frequency.put(terms.get(i),(double)1);
								
			}
			
			// adding terms to the corpus
			if(this.term_frequency.containsKey(terms.get(i))){
				double val2 = term_frequency.get(terms.get(i));
				val2++;
				this.term_frequency.remove(terms.get(i));
				this.term_frequency.put(terms.get(i), val2);
				
				words_counted++;
				unique_terms++;
				
			}else{
				this.term_frequency.put(terms.get(i),(double)1);
				words_counted++;
				
			}


		}
		
		
		// loop through all of the terms and insert their TF-ITTF values
		// into the TreeMap
		Map<String,Double> tfidf_temp = new TreeMap<String,Double>();
		for (Map.Entry entry : term_frequency.entrySet()) { 
			String current_string = (String) entry.getKey();
			
			// This is the actual algorithm for calculating TF-ITTF
			double tf = (term_frequency.get(current_string) / terms.size());
			double idf = Math.log10(words_counted/this.term_frequency.get(current_string)) ;
			double tf_ittf = tf*idf;

			//System.out.print("tf-idf" + tf*idf);

			//System.out.println("key,val: " + entry.getKey() + "," + entry.getValue()); 

			tfidf_temp.put(current_string, tf_ittf);

		}

		//System.out.println(most_common);
		//System.out.println(max_freq);

		//System.out.println(doc_frequency.get("westside"));
		
		
		// now all of the tf-ittf values for the terms within the document is stored
		// within a variable of the class 
		tfittf = tfidf_temp;
	}

	//@Override
	/**
	 * 
	 * @param document
	 * @return -
	 */
	public ArrayList<Vertex_people> getpeoples(Document document) {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
	/**
	 * 
	 * @param document
	 * @param corpus
	 * @return -
	 */
	public ArrayList<edge_relation> getRelations(Document document,
			Corpus corpus) {
		ArrayList<edge_relation> relation_map = new ArrayList<edge_relation>();		
		ArrayList <Annotation> EmailFrom = new ArrayList<Annotation>();
		ArrayList <Annotation> EmailToR = new ArrayList<Annotation>();
		ArrayList<Annotation> EmailTo = new ArrayList<Annotation>();
		AnnotationSet annotations1 = document.getAnnotations().get("MessageHeader");
		AnnotationSet annotations2 = document.getAnnotations().get("NamedEmail");
		AnnotationSet annotations = document.getAnnotations().get("Term");	
//		System.out.println(document.getAnnotations().getAllTypes());
//		Body, Break, Token, SpaceToken, Footer, ThreadHeader, Term, MessageHeader, Split, Date, NamedEmail, Quote, Sentence, URL
		AnnotationSet annotations3 = document.getAnnotations().get("Token");
//		getAllTyps : find all annotation types
//		get("MessageHeader")
		int doc_id = get_document_id(document, corpus);
		

			// EmailFrom
		ArrayList<Annotation> abc1 = new ArrayList<Annotation>();
		
		for(Annotation term : annotations1){
			abc1.add(term);					
		}
		int[] idnum =  new int[abc1.size()];
		int num =0;
		while(!abc1.isEmpty()){
			int id = abc1.get(0).getStartNode().getId();

			java.util.Iterator<Annotation> a = abc1.iterator();
			Annotation needRemove = null;
			while(a.hasNext()){
				Annotation efg = a.next();
				if (efg.getStartNode().getId()<=id){
					id = efg.getStartNode().getId();
					 needRemove =efg;
				}
			}
//			System.out.println("id"+id);
			idnum[num] = (int)(needRemove.getStartNode().getId());
			EmailFrom.add(needRemove);
			abc1.remove(needRemove);
			num ++;
		}
		
			//		 String in this Email 
		ArrayList<ArrayList<String>> Feature_string =new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> Term_score = new ArrayList<ArrayList <String>>();
		for(int i = 0; i<idnum.length;i++){
			ArrayList<String> Feature_set = new ArrayList<String>();
			Feature_string .add(Feature_set);
			Term_score.add(Feature_set);
		}
		for(Annotation term : annotations)
		{	for(int i = 0; i<idnum.length;i++)	
			if(i!=idnum.length-1){
			if((term.getStartNode().getId()<idnum[i+1]) && (idnum[i]<term.getStartNode().getId()) ){
				Feature_string.get(i).add(term.getFeatures().get("string").toString().toLowerCase().replaceAll("[^A-Za-z0-9]", ""));
			}
			}else{
				if(idnum[i]<term.getStartNode().getId()){
					Feature_string.get(i).add(term.getFeatures().get("string").toString().toLowerCase().replaceAll("[^A-Za-z0-9]", ""));
					
				}
			}	
		}
		for (int i = 0 ; i<Feature_string.size();i++){
		
		TreeMap<String,Double> Term_sub_score = new  TreeMap <String,Double>();	
		for(int j = 0; j <Feature_string.get(i).size();j++){
			Term_sub_score.put(Feature_string.get(i).get(j), getScore(Feature_string.get(i).get(j),doc_id));			
		}
		try{
		Term_score.get(i).add(Term_sub_score.lastKey());
		}
		catch(NoSuchElementException e){
			Term_score.get(i).add("");
		}
		}

		

		// EmailToR
		ArrayList<Annotation> abc2 = new ArrayList<Annotation>();
		for(Annotation term : annotations2){
			abc2.add(term);			
		}		
		while(!abc2.isEmpty()){
			int id = abc2.get(0).getStartNode().getId();
			java.util.Iterator<Annotation> a = abc2.iterator();
			Annotation needRemove = null;
			while(a.hasNext()){
				Annotation efg = a.next();
				if (efg.getStartNode().getId()<=id){
					id = efg.getStartNode().getId();
					 needRemove = efg;
				}
			}
			EmailToR.add(needRemove);
			abc2.remove(needRemove);
		}
		
		//EmailTo
		int i = 0;
		while(EmailTo.size()<EmailFrom.size()){
			java.util.Iterator<Annotation> a = EmailToR.iterator();
			while(a.hasNext()){
			Annotation efg1 = a.next();
			if(efg1.getStartNode().getId() >idnum[i]){
				EmailTo.add(efg1);
				i++;
//				System.out.println(efg1);
				break;
			}
			}
			
		}		
		
//		System.out.println(EmailFrom.size()+"size");
		while(!EmailFrom.isEmpty()){
			String people1 =(String) EmailFrom.remove(0).getFeatures().get("sender_name");
			String people2 =(String) EmailTo.remove(0).getFeatures().get("name");
//			String featue_12 = (String) Term_score.remove(0).get(0);
			edge_relation new_relation = new edge_relation(people1,people2);
			new_relation.set_Relations(Term_score.remove(0));
			if(!relation_map.contains(new_relation))
			{
				relation_map.add(new_relation);
				
			}
		}
		System.out.println(document.getName());
		System.out.println(relation_map.size()+"relation");
		return relation_map;	
	}
	
	/**
	 * 
	 * @param doc
	 * @param corpus
	 * @return -
	 */
	
	// Given a document and a corpus, this returns the index of the document within
	// the corpus 
	public int get_document_id(Document doc, Corpus corpus){
		for (int i = 0; i<corpus.size();i++){
			if(corpus.get(i).equals(doc))
				return i;
		}
		return 0;
		
	}
}
