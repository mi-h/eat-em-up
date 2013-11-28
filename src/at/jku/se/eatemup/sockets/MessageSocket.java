package at.jku.se.eatemup.sockets;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import at.jku.se.eatemup.core.MessageContainer;
import at.jku.se.eatemup.core.MessageCreator;
import at.jku.se.eatemup.core.MessageHandler;
import at.jku.se.eatemup.core.json.JsonTool;
import at.jku.se.eatemup.core.logging.Logger;

@ServerEndpoint("/websocket")
public class MessageSocket {
	private static void setSession(Session session) {
		if (!SessionStore.sessionExists(session.getId())) {
			SessionStore.saveSession(session);
		}
	}

	@OnClose
	public void onClose() {
		Logger.log("Connection closed");
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException,
			InterruptedException {
		boolean err = false;
		setSession(session);
		Logger.log("received message from " + session.getId());
		String sesid = session.getId();
		String convertedMessage = JsonTool.convertMessage(message);
		MessageContainer container = MessageCreator.createMsgContainer(
				convertedMessage, sesid);
		if (container != null) {
			if (MessageHandler.ReceiveMessage(container)) {
				session.getBasicRemote().sendText("true");
				Logger.log("accepted message from " + session.getId() + ": " + message);
			} else {
				err = true;
			}
		} else {
			err = true;
		}
		if (err) {
			session.getBasicRemote().sendText("false");
			Logger.log("rejected message from " + session.getId());
		}
	}

	@OnOpen
	public void onOpen() {
		Logger.log("Client connected");
	}
}
