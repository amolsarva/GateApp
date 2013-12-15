/**
 * 
 */
package com.bobboau.GateApp;

import gate.util.InvalidOffsetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.bobboau.GateApp.TermBlocks.Block;

/**
 * @author Bobboau
 *
 */
public class BlockFormatter
{

	
	/**
	 * class that represents a section of text in a document
	 * @author Bobboau
	 */
	private class Blob {
		public long start;
		public long end;
	}

	/**
	 * returns a list of strings ordered from greatest to least score
	 * @param idx the document to get blocks for
	 * @param merge_threshold if two blocks are within this ordinal distance and overlap, merge them into the higher position
	 * @param term_blocks the block generator, the source of the blocks to format
	 * @return a list of strings extracted from the document that should be reasonable summarizations
	 */
	public List<String> getBlocksAsStrings(int idx, int merge_threshold, TermBlocks term_blocks) {
		
		ArrayList<Block> doc_blocks = getScoreSortedBlocks(idx, term_blocks);
		
		ArrayList<Blob> blobs = mergeLocalText(merge_threshold, doc_blocks);
		
		return blobsToStrings(idx, blobs, term_blocks);
	}

	/**
	 * given a bunch of blobs and a document return a bunch of strings from that document
	 * @param document_idx
	 * @param blobs
	 * @return
	 */
	private static List<String> blobsToStrings(int document_idx, ArrayList<Blob> blobs, TermBlocks term_blocks)
	{
		ArrayList<String> ret = new ArrayList<String>();
		for(Blob blob : blobs){
			try
			{
				ret.add(
					term_blocks.getCorpus().get(document_idx).getContent().getContent(
						new Long(blob.start),
						new Long(blob.end)
					).toString().replaceAll("[\\r\\n\\s]+", "  ")
				);
			}
			catch (InvalidOffsetException e)
			{
				e.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * given a bunch of blocks make a bunch of blobs that don't have any overlapping text near by (defined by merge threshold)
	 * @param merge_threshold
	 * @param doc_blocks
	 * @return
	 */
	private ArrayList<Blob> mergeLocalText(int merge_threshold, ArrayList<Block> doc_blocks)
	{
		//convert blocks to blobs
		ArrayList<Blob> blobs = new ArrayList<Blob>();
		for(Block block : doc_blocks){
			Blob new_blob = new Blob();
			new_blob.start = block.getDocumentStart();
			new_blob.end = block.getDocumentStop();
			blobs.add(new_blob);
		}
		
		//while we keep finding overlaps keep mergeing
		boolean merges_happened = true;
		while(merges_happened){
			merges_happened = false;
			merges_happened = merges_happened || mergeOverlappingBlobs(blobs, merge_threshold);
		}
		
		return blobs;
	}

	/**
	 * do a sweep of the blobs merge any that are overlapping within the threshold
	 * @param blobs
	 * @param merge_threshold
	 * @return
	 */
	private static boolean mergeOverlappingBlobs(ArrayList<Blob> blobs, int merge_threshold)
	{
		boolean merged = false;
		for(int i = 0; i<blobs.size(); i++){
			for(int j = i+1; j<merge_threshold && j<blobs.size(); j++){
				Blob old_blob = blobs.get(i);
				Blob new_blob = blobs.get(j);
				if( //if they overlap
					new_blob.start > old_blob.start && new_blob.start < old_blob.end
					||
					new_blob.end > old_blob.start && new_blob.end < old_blob.end
				){
					//merge the new blob into the old blob, get rid of the new blob
					old_blob.start = Math.min(old_blob.start, new_blob.start);
					old_blob.end = Math.min(old_blob.end, new_blob.end);
					blobs.remove(j);
					
					//jump back on in the j loop to take into account removing the new blob
					j--;
					
					//we did merge
					merged = true;
				}
			}
		}
		return merged;
	}

	/**
	 * get a list of blocks ordered by score
	 * @param idx
	 * @param term_blocks 
	 * @return
	 */
	private static ArrayList<Block> getScoreSortedBlocks(int idx, TermBlocks term_blocks)
	{
		ArrayList<Block> doc_blocks = new ArrayList<Block>();
		
		for(Block block : term_blocks.getBlocks(idx)){
			doc_blocks.add(block);
		}
		
		Collections.sort(doc_blocks, new Comparator<Block>(){
			@Override
			public int compare(Block a, Block b) {
				double diff = a.getScore() - b.getScore();
				if(diff == 0.0){
					return 0;
				}
				return diff < 0.0 ? 1 : -1;
			}
		});
		return doc_blocks;
	}

}
