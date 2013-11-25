package at.jku.se.eatemup.core;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.websocket.Session;

import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.sockets.SessionStore;

public class MessageSender {
	private LinkedBlockingQueue<Runnable> tasks;
	private ArrayList<Thread> workers;
	private boolean started;
	private static final int workerCount = 2;
	private static final int sleepTime = 50;

	public MessageSender() {
		tasks = new LinkedBlockingQueue<>();
		workers = new ArrayList<>(workerCount);
		started = false;
		start();
	}

	public void addMessage(String sessionid, String message) {
		MessageExecutor temp = new MessageExecutor(sessionid, message);
		try {
			tasks.put(temp);
		} catch (InterruptedException e) {
			Logger.log("failed to queue message for sending. "
					+ Logger.stringifyException(e));
		}
	}

	private class MessageExecutor implements Runnable {
		private String session;
		private String message;

		public MessageExecutor(String sessionid, String message) {
			this.session = sessionid;
			this.message = message;
		}

		@Override
		public void run() {
			try {
				Session ses = SessionStore.getSession(session);
				if (ses != null) {
					ses.getAsyncRemote().sendText(message);
					Logger.log("message sent to " + session);
				} else {
					Logger.log("failed sending message to " + session);
				}
			} catch (Exception e) {
				Logger.log("message sending error. "
						+ Logger.stringifyException(e));
			}
		}
	}

	private synchronized void start() {
		if (!started) {
			started = true;
			if (workers.size() != 0) {
				for (Thread worker : workers) {
					worker.interrupt();
				}
				workers.clear();
			}
			for (int i = 0; i < workerCount; i++) {
				Thread worker = new Thread() {
					public void run() {
						while (true) {
							Runnable temp = tasks.poll();
							if (temp != null) {
								try {
									temp.run();
								} catch (Exception e) {
									Logger.log("task throws exception. "
											+ Logger.stringifyException(e));
								}
							} else {
								try {
									Thread.sleep(sleepTime);
								} catch (InterruptedException e) {
									Logger.log("worker interrupted. "
											+ Logger.stringifyException(e));
								}
							}
						}
					}
				};
				workers.add(worker);
			}
			for (Thread worker : workers) {
				worker.start();
				try {
					Thread.sleep(sleepTime/2);
				} catch (InterruptedException e) {
					//die silently
				}
			}
		}
	}
}
