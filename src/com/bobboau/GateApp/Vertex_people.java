package com.bobboau.GateApp;

//package graph;
import java.lang.Object;
import java.util.*;
public class Vertex_people extends Object{
	private String name;
	private String [] features = {};

	public Vertex_people (String input_name){
		name = input_name;		
	}
	
	public boolean equals(Object ano_people){
		if (((Vertex_people)ano_people).name.equals(this.name)){
			return true;
		}else
		{
			return false;
		}
	}
	public void set_feature(ArrayList<String> input_feature){
		if (input_feature.size() !=0){
		features = new String[input_feature.size()];
		int i = 0;
		while(!input_feature.isEmpty()){
			features[i] = input_feature.remove(0);
			i ++;
		}
		}
	}
	public ArrayList<String> get_feature(){
		 ArrayList<String> out_feature = new ArrayList<String>();
		 if (!features.equals(null)){
		 for(int i=0; i<features.length; i ++){
		 out_feature.add(features[i]);
		 }
		 return out_feature;}
		 else 
		 return out_feature;
	}
	
	public String get_name(){
		return(name);
	}

}
