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
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.text.html.HTMLDocument.Iterator;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.util.persistence.PersistenceManager;
import gate.corpora.RepositioningInfo;
//import graph.*;
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
	private int doc_id = 0;
	
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
			AnnotationSet annotations1 = document.getAnnotations().get("MessageHeader");
			AnnotationSet annotations2 = document.getAnnotations().get("NamedEmail");
//			System.out.println(document.getAnnotations().getAllTypes());
//			Body, Break, Token, SpaceToken, Footer, ThreadHeader, Term, MessageHeader, Split, Date, NamedEmail, Quote, Sentence, URL
			AnnotationSet annotations3 = document.getAnnotations().get("Token");
			AnnotationSet annotations4 = document.getAnnotations().get("NamedEmail");

			getRelations(document,corpus);
			getpeoples( document);
			
			for(Annotation term : annotations)
			{
				String term_string = term.getFeatures().get("string").toString().toLowerCase().replaceAll("[^A-Za-z0-9]", "");
//				 System.out.println(annotations1.getAllTypes());
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
		catch(NullPointerException e){
			return 0.0;
		}
	}
	public ArrayList<edge_relation> getRelations(Document document,Corpus corpus){
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
		Term_score.get(i).add(Term_sub_score.firstKey());
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
	
	public ArrayList<Vertex_people> getpeoples(Document document){
		ArrayList<Vertex_people> Vertex_peoples = new ArrayList<Vertex_people>();		
		ArrayList <Annotation> EmailFrom = new ArrayList<Annotation>();
		ArrayList <Annotation> EmailToR = new ArrayList<Annotation>();
		ArrayList<Annotation> EmailTo = new ArrayList<Annotation>();
		AnnotationSet annotations1 = document.getAnnotations().get("MessageHeader");
		AnnotationSet annotations2 = document.getAnnotations().get("NamedEmail");
//		System.out.println(document.getAnnotations().getAllTypes());
//		Body, Break, Token, SpaceToken, Footer, ThreadHeader, Term, MessageHeader, Split, Date, NamedEmail, Quote, Sentence, URL
		AnnotationSet annotations3 = document.getAnnotations().get("Token");
//		getAllTyps : find all annotation types
//		get("MessageHeader")

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
			idnum[num] = (int)(needRemove.getStartNode().getId());
			EmailFrom.add(needRemove);
			abc1.remove(needRemove);
			num ++;
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
				break;
			}
			}
			
		}		
		while(!EmailFrom.isEmpty()){
			String people1 =(String) EmailFrom.remove(0).getFeatures().get("sender_name");
			Vertex_people new_comer = new Vertex_people(people1);
			if(!Vertex_peoples.contains(new_comer))
			{
				Vertex_peoples.add(new_comer);
			}
		}
		while(!EmailTo.isEmpty()){
			String people2 =(String) EmailTo.remove(0).getFeatures().get("name");
			Vertex_people new_comer = new Vertex_people(people2);
			if(!Vertex_peoples.contains(new_comer))
			{
				Vertex_peoples.add(new_comer);
			}
		}
				
	return Vertex_peoples;
	
	}
	
	
	public int get_document_id(Document doc, Corpus corpus){
		for (int i = 0; i<corpus.size();i++){
			if(corpus.get(i).equals(doc))
				return i;
		}
		return 0;
		
	}
	
	
	
}
