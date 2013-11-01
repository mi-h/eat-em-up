package at.jku.se.eatemup.core;

import javax.websocket.Session;

import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.JsonTool;
import at.jku.se.eatemup.core.json.MessageContainer;
import at.jku.se.eatemup.core.json.TempMessageContainer;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.sockets.SessionStore;

public class MessageHandler {

	public static void ReceiveMessage(MessageContainer container) {

	}

	public static void PushMessage(final MessageContainer container) {
		if (container.direction == DirectionType.Outgoing) {
			new Thread() {
				public void run() {
					String msg = JsonTool.SerializeMessage(container.message);
					TempMessageContainer temp = new TempMessageContainer();
					temp.message = msg;
					temp.type = container.type;
					for (String id : container.receivers) {
						_sendMsgAsync(id,
								JsonTool.SerializeTempMessageContainer(temp));
					}
				}
			}.start();
		}
	}

	private static void _sendMsgAsync(String session, String message) {
		Session ses = SessionStore.getSession(session);
		if (ses != null) {
			ses.getAsyncRemote().sendText(message);
			Logger.log("message sent to " + session);
		} else {
			Logger.log("failed sending message to " + session);
		}
	}
}
