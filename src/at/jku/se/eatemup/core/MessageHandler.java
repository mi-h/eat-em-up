package at.jku.se.eatemup.core;

import javax.websocket.Session;

import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.JsonCreateException;
import at.jku.se.eatemup.core.json.JsonTool;
import at.jku.se.eatemup.core.json.MessageContainer;
import at.jku.se.eatemup.core.json.TempMessageContainer;
import at.jku.se.eatemup.core.json.messages.*;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.core.logic.GameEngine;
import at.jku.se.eatemup.sockets.SessionStore;

public class MessageHandler {

	public static boolean ReceiveMessage(MessageContainer container) {
		try {
			switch (container.type) {
			case Login:
				return GameEngine.acceptLogin((LoginMessage) container.message,
						container.sender);
			case RequestForGameStart:
				return GameEngine.acceptRequestForGameStart(
						(RequestForGameStartMessage) container.message,
						container.sender);
			case Position:
				return GameEngine.acceptPosition(
						(PositionMessage) container.message, container.sender);
			case BattleAnswer:
				return GameEngine.acceptBattleAnswer(
						(BattleAnswerMessage) container.message,
						container.sender);
			default:
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}

	public static void PushMessage(final MessageContainer container) {
		if (container.direction == DirectionType.Outgoing) {
			new Thread() {
				public void run() {
					try {
						String msg = JsonTool
								.SerializeMessage(container.message);
						TempMessageContainer temp = new TempMessageContainer();
						temp.message = msg;
						temp.type = container.type;
						for (String id : container.receivers) {
							_sendMsgAsync(
									id,
									JsonTool.SerializeTempMessageContainer(temp));
						}
					} catch (JsonCreateException jce) {
						Logger.log(jce.getLogText());
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
