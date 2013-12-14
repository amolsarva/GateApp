package com.bobboau.GateApp;


import java.lang.Object;
import java.util.*;

/**
 *
 */
public class edge_relation extends Object {
private String FirstP;
private String SecondP;
private String [] Relations = {} ;

/**
 * @param people1
 * @param people2
 */
public edge_relation( String people1, String people2){
	FirstP = people1;
	SecondP = people2;

};
/**
 * @return -
 */
public String get_First(){
	return FirstP;
}
/**
 * @return -
 */
public String get_Second(){
	return SecondP;
}

/**
 * @param input 
 * @return -
 *
 */
public boolean equals(Object input){
	if ((((edge_relation) input).get_First().equals(FirstP)) && (((edge_relation) input).get_Second().equals(SecondP))){
		return true;}
	else{ 
		return false;}
	
}

/**
 * @param input_Relations
 */
public void set_Relations(ArrayList<String> input_Relations){
	if (input_Relations.size()!= 0){
	Relations = new String[input_Relations.size()];
	int i = 0;
	while(!input_Relations.isEmpty()){
		Relations[i] = input_Relations.remove(0);
		i ++;
	}
	}
}

/**
 * @return -
 */
public ArrayList<String> get_Relations(){
	 ArrayList<String> out_Relations = new ArrayList<String>();
	 if (!Relations.equals(null)){
	 for(int i=0; i<Relations.length; i ++){
		 out_Relations.add(Relations[i]);
	 }
	 return out_Relations;	}	
	 else{return out_Relations;}
}
 
//public boolean exist_relations(edge_relation new_relation){
//	if (this.FirstP.equals(new_relation.FirstP)&&this.SecondP.equals(new_relation.SecondP)){
//		return true;
//	}else{
//		return false;
//	}
//}

}

