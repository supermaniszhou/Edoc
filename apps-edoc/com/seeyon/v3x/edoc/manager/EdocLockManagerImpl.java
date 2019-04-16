package com.seeyon.v3x.edoc.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class EdocLockManagerImpl implements EdocLockManager {

	private static Map<Long, Long> locksMap = new ConcurrentHashMap<Long, Long>();
	private static Map<String, Long> locksSendMap = new ConcurrentHashMap<String, Long>();
	private final Object lockObject = new Object();

	@Override
	public Long canGetLock(Long lockId, Long userId) {
		synchronized (lockObject) {
			Long lockUserId = getLockUserId(lockId);
			if(lockUserId != null) {
				return locksMap.get(lockId);
			}else{
				locksMap.put(lockId, userId);
				return null;
			}
		}
	}
	
	@Override
	public void unlock(Long lockId) {
		synchronized (lockObject) {
			locksMap.remove(lockId);
		}
	}
	
	private Long getLockUserId(Long lockId) {
		synchronized (lockObject) {
			return locksMap.get(lockId);
		}
	}
	

	@Override
	public Long canGetLock(String subject, Long userId) {
		synchronized (lockObject) {
			String lockId = subject + String.valueOf(userId);
			Long lockUserId = getLockUserId(lockId);
			if(lockUserId != null) {
				return locksSendMap.get(lockId);
			}else{
				locksSendMap.put(lockId, userId);
				return null;
			}
		}
	}
	
	@Override
	public void unlock(String subject, Long userId) {
		synchronized (lockObject) {
			String lockId = subject + String.valueOf(userId);
			locksSendMap.remove(lockId);
		}
	}
	
	private Long getLockUserId(String lockId) {
		synchronized (lockObject) {
			return locksSendMap.get(lockId);
		}
	}
	
}
