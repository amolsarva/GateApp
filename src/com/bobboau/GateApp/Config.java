package com.bobboau.GateApp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Bobboau
 * simple persistent configuration object
 */
public class Config implements Serializable{
	
	/**
	 * desired by the serialization interface (eclipse actually)
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * the loaded configs
	 */
	static HashMap<String, Config> configs = new HashMap<String, Config>();
	
	/**
	 * the values of a specific config
	 */
	private HashMap<String, Serializable> values = new HashMap<String, Serializable>();
	
	/**
	 * the filename we were opened with
	 */
	private String filename;

	/**
	 * default constructor, set defaults in case there is no config in place
	 */
	private Config(String filename) {
		this.filename = filename;
		//after setting config save file
		save();
	}
	
	/**
	 * @param filename 
	 * @return a/the config object
	 */
	public static Config load(String filename){
		if(!configs.containsKey(filename)){
		    try(
				InputStream file = new FileInputStream(filename);
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream (buffer);
			){
		    	configs.put(filename,(Config)input.readObject());
			}
			catch(ClassNotFoundException | IOException ex){
				configs.put(filename, new Config(filename));
			}
		}
		return configs.get(filename);
	}
	
	/**
	 * serialize the config object to file
	 */
	public void save(){
	    try (
			OutputStream file = new FileOutputStream(this.filename);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
		){
			output.writeObject(this);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * sets and saves a config value
	 * @param key
	 * @param value
	 */
	public void set(String key, Serializable value){
		this.values.put(key, value);
		save();
	}
	
	/**
	 * @param <T> 
	 * @param key
	 * @param default_value
	 * @return a more or less generic object of some sort
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T get(String key, T default_value){
		if(!this.values.containsKey(key)){
			this.set(key, default_value);
		}
		return (T)this.values.get(key);
	}

}
