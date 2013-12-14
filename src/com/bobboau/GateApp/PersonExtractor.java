/**
 * 
 */
package com.bobboau.GateApp;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * @author Bobboau
 *
 */
public class PersonExtractor
{
	private Tfidf tfidf = null;//I have NO idea why this is needed but getScore is called in one place and I don't want to have to reverse engineer this whole thing -mike
	private ArrayList<ArrayList<Vertex_people>> Peoples = null;
	private ArrayList<ArrayList<edge_relation>> Relations = null;
	
	/**
	 * @param corpus
	 */
	public void setCorpus(Corpus corpus){
		Peoples = new  ArrayList<ArrayList<Vertex_people>>();
		Relations = new  ArrayList<ArrayList<edge_relation>>();
		for(int i = 0; i<corpus.size(); i++)
		{
			ArrayList<Vertex_people> doc_people = new ArrayList<Vertex_people>();
			ArrayList<edge_relation> doc_relation = new ArrayList<edge_relation>();
			Document document = corpus.get(i);
			doc_people = getpeoples(document);
			doc_relation = getRelations(document,corpus);
			Peoples.add(doc_people);
			Relations.add(doc_relation);
		}
	}
	
	/**
	 * @param tfidf
	 */
	public void setTfidf(Tfidf tfidf){
		this.tfidf = tfidf;
	}
	
	/**
	 * @param a
	 * @return -
	 */
	public ArrayList<Vertex_people> get_people(int a){
		return Peoples.get(a);
	}
	/**
	 * @param a
	 * @return -
	 */
	public ArrayList<edge_relation> get_relation(int a){
		return Relations.get(a);
	}
	
	/**
	 * @param document
	 * @param corpus
	 * @return -
	 */
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
			Term_sub_score.put(Feature_string.get(i).get(j), tfidf.getScore(Feature_string.get(i).get(j),doc_id));			
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
	
	/**
	 * @param document
	 * @return -
	 */
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
	
	
	/**
	 * @param doc
	 * @param corpus
	 * @return -
	 */
	public int get_document_id(Document doc, Corpus corpus){
		for (int i = 0; i<corpus.size();i++){
			if(corpus.get(i).equals(doc))
				return i;
		}
		return 0;
		
	}
}
