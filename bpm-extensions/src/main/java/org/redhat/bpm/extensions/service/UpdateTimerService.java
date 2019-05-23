package org.redhat.bpm.extensions.service;


import org.redhat.bpm.extensions.entity.TimerPayload;

import java.util.List;

public interface UpdateTimerService {

	void setAsTriggered(String identifier, long piid);

	List<TimerPayload> cancelTimer(String identifier, long piid);

	void cancelTimerAndReload(String identifier, long piid);

	void updateTimerNode(Long piid, String identifier, long delay, long period, int repeatLimit);

	// when using java 8 use interface default methods instead ;)
	public static class Factory {
		private static UpdateTimerService updateTimerService;

		static {
			updateTimerService = new UpdateTimerServiceImpl();
		}

		public static UpdateTimerService get() {
			return updateTimerService;
		}
	}
}
