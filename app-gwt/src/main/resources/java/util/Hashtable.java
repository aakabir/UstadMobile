package java.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Hashtable<K,V> {

	HashMap map;
	
	public Hashtable(){
		this.map = new HashMap();
		System.out.println("Deferred Binding Success");
	}
	
	public Hashtable(Map<? extends K, ? extends V> t) {
		this.map = new HashMap();
        System.out.println("Deferred Binding success");
    }
	
	public int size(){
		return map.size();
	}
	
	public boolean isEmpty(){
		return this.map.isEmpty();
	}
	
	public Enumeration keys(){
		//tODO
		return null;
	}
	
	public Object get(Object key){
		return this.map.get(key);
	}
	
	public Object put(Object key, Object value){
		Object previousValue = this.map.get(key);
		this.map.put(key, value);
		return previousValue;
	}
	
	public Object remove(Object key){
		Object previousValue = this.map.get(key);
		this.map.remove(key);
		return previousValue;		
	}
	
	public boolean containsKey(Object key){
		return this.map.containsKey(key);
	}
	
	public boolean contains(String value){
		retutn this.map.containsValue(value);
	}
}
