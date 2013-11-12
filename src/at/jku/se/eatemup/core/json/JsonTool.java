package at.jku.se.eatemup.core.json;

import java.util.ArrayList;

import at.jku.se.eatemup.core.Avatar;
import at.jku.se.eatemup.core.MessageContainer;
import at.jku.se.eatemup.core.ReceivedMessageContainer;
import at.jku.se.eatemup.core.Sender;
import at.jku.se.eatemup.core.TempMessageContainer;
import at.jku.se.eatemup.core.json.messages.*;

import com.google.gson.Gson;

public class JsonTool {

	public static TempMessageContainer CreateTempMessageContainer(String message)
			throws JsonParseException {
		Gson gson = new Gson();
		try {
			TempMessageContainer temp = gson.fromJson(message,
					TempMessageContainer.class);
			return temp;
		} catch (Exception ex) {
			JsonParseException jpe = new JsonParseException();
			jpe.setStackTrace(ex.getStackTrace());
			throw jpe;
		}
	}

	public static MessageContainer CreateMessageContainer(
			TempMessageContainer tempContainer, DirectionType direction,
			ArrayList<String> connIds) throws JsonParseException {
		MessageContainer container = new MessageContainer();
		try {
			container.direction = direction;
			container.type = tempContainer.type;
			container.message = createMessage(tempContainer.message,
					container.type);
			if (direction == DirectionType.Incoming) {
				container.sender = new Sender(
						((IncomingMessage) container.message).username,
						connIds.get(0));
			} else {
				container.receivers = connIds;
			}
			return container;
		} catch (Exception ex) {
			JsonParseException jpe = new JsonParseException();
			jpe.setStackTrace(ex.getStackTrace());
			throw jpe;
		}
	}

	public static String SerializeMessage(Message message)
			throws JsonCreateException {
		try {
			Gson gson = new Gson();
			return gson.toJson(message);
		} catch (Exception ex) {
			JsonCreateException jce = new JsonCreateException();
			jce.setStackTrace(ex.getStackTrace());
			throw jce;
		}
	}

	public static String SerializeTempMessageContainer(
			TempMessageContainer container) throws JsonCreateException {
		try {
			Gson gson = new Gson();
			return gson.toJson(container);
		} catch (Exception ex) {
			JsonCreateException jce = new JsonCreateException();
			jce.setStackTrace(ex.getStackTrace());
			throw jce;
		}
	}

	private static Message createMessage(String message, MessageType type) {
		Gson gson = new Gson();
		switch (type) {
		case BattleAnswer:
			return gson.fromJson(message, BattleAnswerMessage.class);
		case BattleResult:
			return gson.fromJson(message, BattleResultMessage.class);
		case BattleStart:
			return gson.fromJson(message, BattleStartMessage.class);
		case GameEnd:
			return gson.fromJson(message, GameEndMessage.class);
		case GameStandbyUpdate:
			return gson.fromJson(message, GameStandbyUpdateMessage.class);
		case GameState:
			return gson.fromJson(message, GameStateMessage.class);
		case GameStartSurvey:
			return gson.fromJson(message, GameStartSurveyMessage.class);
		case Login:
			return gson.fromJson(message, LoginMessage.class);
		case PlayerHasEaten:
			return gson.fromJson(message, PlayerHasEatenMessage.class);
		case PlayerMoved:
			return gson.fromJson(message, PlayerMovedMessage.class);
		case Position:
			return gson.fromJson(message, PositionMessage.class);
		case ReadyForGame:
			return gson.fromJson(message, ReadyForGameMessage.class);
		case RequestForGameStart:
			return gson.fromJson(message, RequestForGameStartMessage.class);
		case SpecialActionActivated:
			return gson.fromJson(message, SpecialActionActivatedMessage.class);
		case SpecialActionDeactivated:
			return gson
					.fromJson(message, SpecialActionDeactivatedMessage.class);
		case TimerUpdate:
			return gson.fromJson(message, TimerUpdateMessage.class);
		case Exit:
			return gson.fromJson(message, ExitMessage.class);
		case Logout:
			return gson.fromJson(message, LogoutMessage.class);
		case GoodieCreated:
			return gson.fromJson(message, GoodieCreatedMessage.class);
		case HighscoreRequest:
			return gson.fromJson(message, HighscoreRequestMessage.class);
		case Highscore:
			return gson.fromJson(message, HighscoreMessage.class);
		case Play:
			return gson.fromJson(message, PlayMessage.class);
		case GameStateRequest:
			return gson.fromJson(message, GameStateRequestMessage.class);
		default:
			return null;
		}
	}
	
	public static String SerializeAvatar(Avatar avatar){
		Gson gson = new Gson();
		return gson.toJson(avatar);
	}
	
	public static Avatar DeSerializeAvatar(String avatar){
		Gson gson = new Gson();
		return gson.fromJson(avatar, Avatar.class);
	}

	public static String convertMessage(String message) {
		Gson gson = new Gson();
		ReceivedMessageContainer temp = gson.fromJson(message, ReceivedMessageContainer.class);
		TempMessageContainer temp2 = new TempMessageContainer();
		temp2.message = gson.toJson(temp.message);
		temp2.type = temp.type;
		return gson.toJson(temp2);
	}
}
