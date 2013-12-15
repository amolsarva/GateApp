package com.bobboau.GateApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.FeatureMap;
import gate.LanguageAnalyser;
import gate.ProcessingResource;
import gate.Resource;
import gate.annotation.AnnotationImpl;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;
import gate.util.SimpleFeatureMapImpl;

/**
 * @author Bobboau
 * this is a processing resource that finds and annotates footers
 * footers are one or more lines that are commonly found at the end of a person's message
 * a footer must be used at least twice before it can be identified
 */
@SuppressWarnings("serial")
@CreoleResource(name = "Footer Finder", comment = "finds footers in properly annotated email bodies")
public class FooterFinderPR extends AbstractLanguageAnalyser 
{
	/**
	 * sender (email address) to footer list map
	 */
	Map<String,Set<String>> footers = new HashMap<String,Set<String>>();
	
	/**
	 * 
	 */
	FooterFinderPR()
	{
		super();
	}
	
	/**
	 * marks all common endings as footers
	 */
	@Override
	public void execute() throws ExecutionException
	{
		if(this.footers == null){
			findFooters();
		}
		for(Annotation body : getDocument().getAnnotations().get("Body"))
		{
			String person = body.getFeatures().get("sender_email").toString().replaceAll("[^A-Za-z@]", "");
			long footer_length = -1;
			try
			{
				String body_text = getDocument().getContent().getContent( body.getStartNode().getOffset(), body.getEndNode().getOffset()).toString();
				footer_length = findFooter(body_text, this.footers.get(person));
			}
			catch (InvalidOffsetException e)
			{
				//stick with the default value for footer_length
				e.printStackTrace();
			}
			
			//make the annotation, if there is one
			if(footer_length > 0){
				try
				{
					getDocument().getAnnotations().add(body.getEndNode().getOffset()-footer_length, body.getEndNode().getOffset(), "Footer", new SimpleFeatureMapImpl());
				}
				catch (InvalidOffsetException e)
				{
					//I seriously doubt this will ever get thrown
					e.printStackTrace();
				}
			}
		}
		fireProcessFinished();
	}

	/**
	 * set the current corpus
	 */
	@Override
	@Optional
	@RunTime
	@CreoleParameter(comment = "The corpus containing the document to process")
	public void setCorpus(Corpus corpus)
	{
		super.setCorpus(corpus);
		this.footers = null;
	}

	/**
	 * build the map of footers
	 */
	private void findFooters()
	{
		this.footers = new HashMap<String,Set<String>>();
		Map<String,List<String>> bodies = findBodies();
		for(String person : bodies.keySet()){
			//make sure all people have an entry
			if(!this.footers.containsKey(person)){
				this.footers.put(person, new HashSet<String>());
			}
			
			//now look at all email bodies for this person and compare them against all other emails for this person
			//look for the greatest common ending
			for(String body : bodies.get(person)){
				String[] lines = body.split("\\r?\\n");				
				for(String other_body : bodies.get(person)){
					if(other_body == body){//note the pointer comparison here is intentional
						continue;
					}
					
					//find the largest (if any) ending
					String largest_ending = "";
					for(int i = lines.length-1; i>-1; i--){
						String ending = "";
						//to make the ending string merge all after i
						for(int k = i; k<lines.length; k++){
							ending += lines[k];
						}
						//clear out newlines/white space for greedier matching
						if(other_body.replaceAll("[\\r\\n\\s]+", "").endsWith(ending.replaceAll("[\\r\\n\\s]+", ""))){
							largest_ending = ending;
						}
					}
					
					//if we found a common ending record it
					if(!largest_ending.equals("")){
						this.footers.get(person).add(largest_ending);
					}
				}
			}
		}
	}

	/**
	 * @return lists of email message bodies mapped to persons
	 * 
	 */
	private Map<String, List<String>> findBodies()
	{
		Map<String,List<String>> bodies = new HashMap<String,List<String>>(); //person=>list of bodies
		
		for(Document document : getCorpus()){
			for(Annotation body : document.getAnnotations().get("Body"))
			{
				try
				{
					String person_email = body.getFeatures().get("sender_email").toString().replaceAll("[^A-Za-z@]", "");
					if(!bodies.containsKey(person_email)){
						bodies.put(person_email, new ArrayList<String>());
					}
					bodies.get(person_email).add(
						document.getContent().getContent( body.getStartNode().getOffset(), body.getEndNode().getOffset()).toString()
					);
				}
				catch (InvalidOffsetException e)
				{
					e.printStackTrace();
					continue;
				}
			}
		}
		return bodies;
	}
	
	/**
	 * given a body string find the longest footer in the list of footers
	 * @param body
	 * @param set list of available footers
	 * @return length of footer or -1 if none
	 */
	private static long findFooter(String body, Set<String> set){
		long size = -1;
		for(String footer : set){
			//clear out newlines/white space for greedier matching
			if(body.replaceAll("[\\r\\n\\s]+", "").endsWith(footer.replaceAll("[\\r\\n\\s]+", ""))){
				long new_size = footer.length();
				if(new_size > size){
					size = new_size;
				}
			}
		}
		return size;
	}
}
