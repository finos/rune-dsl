package com.rosetta.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author TomForwood
 * A class where a writer can demand that a reader 
 */
public class DemandableLock {
	ReadWriteLock lock = new ReentrantReadWriteLock();
	boolean demanded=false;
	
	public void getWriteLock(boolean demand) {
		this.demanded = demand;
		lock.writeLock().lock();
		this.demanded = false;
	}
	
	public void releaseWriteLock() {
		lock.writeLock().unlock();
	}
	
	public boolean isDemanded() {
		return demanded;
	}
	
	public Lock getReadLock() {
		return lock.readLock();
	}
}
