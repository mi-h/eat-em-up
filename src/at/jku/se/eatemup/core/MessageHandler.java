package at.jku.se.eatemup.core;

import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.JsonCreateException;
import at.jku.se.eatemup.core.json.JsonTool;
import at.jku.se.eatemup.core.json.messages.BattleAnswerMessage;
import at.jku.se.eatemup.core.json.messages.ExitMessage;
import at.jku.se.eatemup.core.json.messages.FollowGameRequestMessage;
import at.jku.se.eatemup.core.json.messages.GameStateRequestMessage;
import at.jku.se.eatemup.core.json.messages.HighscoreRequestMessage;
import at.jku.se.eatemup.core.json.messages.LoginMessage;
import at.jku.se.eatemup.core.json.messages.PlayMessage;
import at.jku.se.eatemup.core.json.messages.PongMessage;
import at.jku.se.eatemup.core.json.messages.PositionMessage;
import at.jku.se.eatemup.core.json.messages.RequestForGameStartMessage;
import at.jku.se.eatemup.core.logging.Logger;

public class MessageHandler {

	private static MessageSender sendManager = new MessageSender();

	private static void _sendMsgAsync(String session, String message) {
		try {
			message = message.replace("\\\"", "\"");
			message = message.replace("\"{", "{").replace("}\"", "}");
		} catch (Exception ex) {
			Logger.log("preparing message for sending failed. "
					+ Logger.stringifyException(ex));
		}
		sendManager.addMessage(session, message);
	}

	public static void PushMessage(final MessageContainer container) {
		if (container.direction == DirectionType.Outgoing) {
			try {
				String msg = JsonTool.SerializeMessage(container.message);
				TempMessageContainer temp = new TempMessageContainer();
				temp.message = msg;
				temp.type = container.type;
				for (String id : container.receivers) {
					_sendMsgAsync(id,
							JsonTool.SerializeTempMessageContainer(temp));
				}
			} catch (JsonCreateException jce) {
				Logger.log(jce.getLogText());
			}
		}
	}

	public static boolean ReceiveMessage(MessageContainer container) {
		try {
			switch (container.type) {
			case Login: {
				return Engine.acceptLogin((LoginMessage) container.message,
						container.sender);
			}
			case RequestForGameStart:
				return Engine.acceptRequestForGameStart(
						(RequestForGameStartMessage) container.message,
						container.sender);
			case Position:
				return Engine.acceptPosition(
						(PositionMessage) container.message, container.sender);
			case BattleAnswer:
				return Engine.acceptBattleAnswer(
						(BattleAnswerMessage) container.message,
						container.sender);
			case Exit:
				return Engine.acceptExit((ExitMessage) container.message,
						container.sender);
			case Play:
				return Engine.acceptPlay((PlayMessage) container.message,
						container.sender);
			case FollowGameRequest:
				return Engine.acceptFollowGameRequest(
						(FollowGameRequestMessage) container.message,
						container.sender);
			case HighscoreRequest:
				return Engine.acceptHighscoreRequest(
						(HighscoreRequestMessage) container.message,
						container.sender);
			case GameStateRequest:
				return Engine.acceptGameStateRequest(
						(GameStateRequestMessage) container.message,
						container.sender);
			case Pong:
				return Engine.acceptPong((PongMessage) container.message,
						container.sender);
			default:
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}
}
