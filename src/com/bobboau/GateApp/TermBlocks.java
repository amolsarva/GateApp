package com.bobboau.GateApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
//import graph.*;
/**
 * @author Bobboau
 * finds blocks of terms and generates a score for them, basically this is the list of all possible extractions
 */
public class TermBlocks {
	private class Block{
		Block(String[] values, double score){
			this.values = new String[block_size];
			System.arraycopy(values, 0, this.values, 0, block_size);
			this.score = score;
		}
		String[] values;
		double score;
	}
	
	/**
	 * the tfidf calculator
	 */
	private Tfidf tfidf = null;
	
	/**
	 * the corpus
	 */
	private Corpus corpus = null;
	
	/**
	 * the blocks
	 */
	private ArrayList<ArrayList<Block>> blocks = null;
	private ArrayList<ArrayList<Vertex_people>> Peoples = null;
	private ArrayList<ArrayList<edge_relation>> Relations = null;
	
	/**
	 * block size
	 */
	private int block_size = 0;
	
	/**
	 * change block size
	 * @param block_size
	 */
	public void setBlockSize(int block_size){
		this.block_size = block_size;
		calculate();
	}
	
	/**
	 * get block size
	 * @return size of the block as it is currently set
	 */
	public int getBlockSize(){
		return this.block_size;
	}

	/**
	 * sets the corpus
	 * @param corpus
	 */
	public void setCorpus(Corpus corpus){
		tfidf.setCorpus(corpus);
		this.corpus = corpus;
		calculate();
	}
	
	/**
	 * have to do this because for some reason inDocumentOrder is not available like the documentation describes
	 * @param annotation_set
	 * @return
	 */
	private ArrayList<Annotation> getOrderedAnnotations(AnnotationSet annotation_set){
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		for(Annotation annotation : annotation_set){
			annotations.add(annotation);
		}
		Collections.sort(annotations, new Comparator<Annotation>(){
			@Override
			public int compare(Annotation a, Annotation b) {
				return (int) (a.getStartNode().getOffset() - b.getStartNode().getOffset());
			}
		});
		return annotations;
	}
	
	private void calculate(){
		if(this.block_size < 1 || this.corpus == null){
			return;
		}
		blocks = new ArrayList<ArrayList<Block>>();
		Peoples = new  ArrayList<ArrayList<Vertex_people>>();
		Relations = new  ArrayList<ArrayList<edge_relation>>();
		String[] working_set = new String[this.block_size];
		for(int i = 0; i<corpus.size(); i++)
		{
			clearWorkingSet(working_set);
			ArrayList<Block> doc_blocks = new ArrayList<Block>(); 
			ArrayList<Vertex_people> doc_people = new ArrayList<Vertex_people>();
			ArrayList<edge_relation> doc_relation = new ArrayList<edge_relation>();
			
			
			Document document = corpus.get(i);
			Set<String> types = new HashSet<String>();
			types.add("Term");
			types.add("MessageHeader");
			
			
			for(Annotation annotation : getOrderedAnnotations(document.getAnnotations().get(types))){
				if(annotation.getType().equals("MessageHeader"))
				{
					if(!workingSetIsReady(working_set)){
						doc_blocks.add(new Block(working_set, workingSetScore(working_set, i)));
					}
					clearWorkingSet(working_set);
				}
				else if(annotation.getType().equals("Term"))
				{
					pushWorkingSet(working_set, annotation.getFeatures().get("string").toString());
					if(workingSetIsReady(working_set)){
						doc_blocks.add(new Block(working_set, workingSetScore(working_set, i)));
					}
				}
			}
			doc_people = this.tfidf.getpeoples(document);
			doc_relation = this.tfidf.getRelations(document,this.corpus);
			blocks.add(doc_blocks);
			Peoples.add(doc_people);
			Relations.add(doc_relation);
		}
	}
	
	/**
	 * get the value of the working set
	 */
	private double workingSetScore(String[]ws, int doc_idx){
		double value = 0.0;
		for(int i = 0; i<this.block_size; i++){
			if(ws[i] != null){
				value += tfidf.getScore(ws[i], doc_idx);
			}
		}
		return value;
	}
	
	/**
	 * tells if the working set is filled with enough stuff to count yet
	 */
	private boolean workingSetIsReady(String[]ws){
		return ws[0] != null;
	}
	
	/**
	 * removes all state info
	 * @param ws
	 */
	private void clearWorkingSet(String[]ws){
		for(int i = 0; i<this.block_size; i++){
			ws[i] = null;
		}
	}
	
	/**
	 * adds a new value to the working set
	 * @param ws
	 */
	private void pushWorkingSet(String[]ws, String value){
		for(int i = 1; i<this.block_size; i++){
			ws[i-1] = ws[i];
		}
		ws[this.block_size - 1] = value;
	}

	/**
	 * returns a list of strings ordered from greatest to least score
	 * @param idx -- the document to get blocks for
	 * @return a list of strings extracted from the document that should be reasonable summarizations
	 */
	public List<String> getBlocksAsStrings(int idx) {
		if(this.block_size < 1 || this.corpus == null){
			return new ArrayList<String>();
		}
		
		ArrayList<Block> doc_blocks = new ArrayList<Block>();
		
		for(Block block : blocks.get(idx)){
			doc_blocks.add(block);
		}
		
		Collections.sort(doc_blocks, new Comparator<Block>(){
			@Override
			public int compare(Block a, Block b) {
				double diff = a.score - b.score;
				if(diff == 0.0){
					return 0;
				}
				else
				{
					return diff < 0.0 ? 1 : -1;
				}
			}
		});
		
		ArrayList<String> ret = new ArrayList<String>();
		for(Block block : doc_blocks){
			String r = "";
			for(int i = 0; i<this.block_size; i++){
				if(block.values[i] != null){
					r += " "+block.values[i];
				}
			}
			ret.add(r);
		}
		return ret;
	}
	
	void setTfidf(String implementation){
		switch(implementation){
			case "Local":
				this.tfidf = new LocalTfidf();
			break;
			case "ANC":
				this.tfidf = new AncTfidf();
			break;
		}
		if(this.corpus != null){
			setCorpus(this.corpus);
		}
	}
	public ArrayList<Vertex_people> get_people(int a){
		return Peoples.get(a);
	}
	public ArrayList<edge_relation> get_relation(int a){
		return Relations.get(a);
	}
}
