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
import gate.util.InvalidOffsetException;

/**
 * @author Bobboau
 * finds blocks of terms and generates a score for them, basically this is the list of all possible extractions
 */
public class TermBlocks {
	
	/**
	 * represents a contiguous sequence of terms
	 * @author Bobboau
	 *
	 */
	public class Block{
		
		/**
		 * the annotations that make up this block
		 */
		private Annotation[] values;
		
		/**
		 * the cumulative score of the annotations
		 */
		private double score;
		
		/**
		 * @param values
		 * @param score
		 */
		public Block(Annotation[] values, double score){
			this.values = new Annotation[TermBlocks.this.block_size];
			System.arraycopy(values, 0, this.values, 0, TermBlocks.this.block_size);
			this.score = score;
		}
		
		/**
		 * @return position in the document this block starts
		 */
		public long getDocumentStart(){
			for(int i = 0; i < TermBlocks.this.block_size; i++){
				if(this.values[i] != null){
					return this.values[i].getStartNode().getOffset().longValue();
				}
			}
			return 0L;
			
		}
		
		/**
		 * @return position in the document this block stops
		 */
		public long getDocumentStop(){
			for(int i = TermBlocks.this.block_size-1; i>-1; i--){
				if(this.values[i] != null){
					return this.values[i].getEndNode().getOffset().longValue();
				}
			}
			return 0L;
		}
		
		/**
		 * 
		 * @return the score of this block
		 */
		double getScore(){
			return this.score;
		}
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
		this.tfidf.setCorpus(corpus);
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

		this.blocks = new ArrayList<ArrayList<Block>>();
		Annotation[] working_set = new Annotation[this.block_size];
		for(int i = 0; i<this.corpus.size(); i++)
		{
			clearWorkingSet(working_set);
			ArrayList<Block> doc_blocks = new ArrayList<Block>(); 
			Document document = this.corpus.get(i);

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
					pushWorkingSet(working_set, annotation);
					if(workingSetIsReady(working_set)){
						doc_blocks.add(new Block(working_set, workingSetScore(working_set, i)));
					}
				}
			}
			this.blocks.add(doc_blocks);
		}
	}
	
	/**
	 * get the value of the working set
	 */
	private double workingSetScore(Annotation[]ws, int doc_idx){
		double value = 0.0;
		for(int i = 0; i<this.block_size; i++){
			if(ws[i] != null){
				value += this.tfidf.getScore(ws[i].getFeatures().get("string").toString(), doc_idx);
			}
		}
		return value;
	}
	
	/**
	 * tells if the working set is filled with enough stuff to count yet
	 */
	private boolean workingSetIsReady(Annotation[]ws){
		return ws[0] != null;
	}
	
	/**
	 * removes all state info
	 * @param ws
	 */
	private void clearWorkingSet(Annotation[]ws){
		for(int i = 0; i<this.block_size; i++){
			ws[i] = null;
		}
	}
	
	/**
	 * adds a new value to the working set
	 * @param ws
	 */
	private void pushWorkingSet(Annotation[]ws, Annotation value){
		for(int i = 1; i<this.block_size; i++){
			ws[i-1] = ws[i];
		}
		ws[this.block_size - 1] = value;
	}
	
	/**
	 * change the tfidf implementation
	 * @param new_tfidf
	 */
	void setTfidf(Tfidf new_tfidf){
		this.tfidf = new_tfidf;
		if(this.corpus != null){
			setCorpus(this.corpus);
		}
	}

	/**
	 * get the blocks for the passed document
	 * @param idx
	 * @return all of the blocks for the passed document in no particular order
	 */
	public List<Block> getBlocks(int idx)
	{
		return this.blocks.get(idx);
	}

	/**
	 * @return get theset corpus
	 */
	public Corpus getCorpus()
	{
		return this.corpus;
	}
}
