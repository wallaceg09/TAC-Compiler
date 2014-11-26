//KeyVal.java

package edu.utt.wallace.syntax;

import java.util.Comparator;

/**
 * @author Glen
 *
 * @param <KEY>
 * @param <VALUE>
 * 
 * Description:
 * 		KeyVal is a generic container to hold key, value pairs.
 * 		This class implements Comparable which allows it to be sorted.
 */
public class KeyVal <KEY extends Comparable<KEY>, VALUE extends Comparable<VALUE>> implements Comparable<KeyVal<KEY, VALUE>>{
	private KEY key = null; private VALUE value = null;
	
	/**Constructor
	 * @param first
	 * @param second
	 */
	public KeyVal(KEY first, VALUE second){
		this.key = first;
		this.value = second;
	}
	
	/**
	 * Getter for the Key.
	 * @return A final copy of the key.
	 */
	public KEY key(){
		final KEY returnVal = key;
		return returnVal;
	}
	
	/**
	 * Getter for the value.
	 * @return A final copy of the value
	 */
	public VALUE value(){
		final VALUE returnVal = value;
		return returnVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Overridden compareTo method inherited from Comparable
	 * 
	 * It compares the key first, 
	 * if the keys are identical, then it returns the comparison of the values
	 * otherwise the comparison of the keys is returned
	 */
	@Override
	public int compareTo(KeyVal<KEY, VALUE> o) {
		int output = 0;
		output = this.key.compareTo(o.key);
		if(output == 0){
			output = this.value.compareTo(o.value);
		}
		return output;
	}
		
	@Override
	public String toString() {
		return "KeyVal [key=" + key + ", value=" + value + "]";
	}

	/**
	 * @author Glen
	 * 
	 * KeyComparator subclass
	 * Description:
	 * 		This subclass allows the programmer to compare only the keys of a KeyVal system.
	 *
	 */
	public static class KeyComparator implements Comparator<KeyVal>{

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 * 
		 * returns the comparison of the keys of two KeyVal objects.
		 */
		@Override
		public int compare(KeyVal o1, KeyVal o2) {
			return o1.key.compareTo(o2.key);
		}		
	}
	
	/**
	 * @author Glen
	 * ValueComparator subclass
	 * Description
	 * 		This subclass allows the programmer to compare only the values of a KeyVal system.
	 *
	 */
	public static class ValueComparator implements Comparator<KeyVal>{

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 * 
		 * returns the comparison of the values of two KeyVal objects.
		 */
		@Override
		public int compare(KeyVal o1, KeyVal o2) {
			return o1.value.compareTo(o2.value);
		}
		
	}

}
//End KeyVal.java
