package com.creatifcubed.simpleapi.misc;

public abstract class Pointer<T> {
	
	public static final int NULL_HASHCODE = 0;
	
	private volatile T object;
	
	public Pointer(T object) {
		this.object = object;
	}
	
	public synchronized T get() {
		return this.object;
	}
	
	public synchronized void set(T object) {
		this.object = object;
	}
	
	/**
	 * @param o Object to compare with, as a pointer
	 * @return Whether this reference's object == pointer o's object. This method is null-safe. If this object's reference is null, and o is null, it will return true
	 */
	@Override
	public synchronized boolean equals(Object o) {
		if (o instanceof Pointer) {
			return this.object == ((Pointer<?>) o).get();
		}
		return false;
	}
	
	/**
	 * @param o Object to compare with
	 * @return Whether
	 */
	public synchronized boolean pointsTo(Object o) {
		return this.object == o;
	}
	
	/**
	 * @return the implemented hashcode. This method is null safe. It will return NULL_HASHCODE if this reference's object is null
	 */
	@Override
	public synchronized int hashCode() {
		return this.object == null ? NULL_HASHCODE : this.object.hashCode();
	}
	
	/**
	 * 
	 * @return the system hashcode. On Sun's JVM, this is the address in memory
	 */
	public synchronized int systemHashCode() {
		return System.identityHashCode(this.object);
	}
}
