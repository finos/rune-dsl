/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
