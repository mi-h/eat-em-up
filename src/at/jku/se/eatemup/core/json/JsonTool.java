package at.jku.se.eatemup.core.json;

import java.util.ArrayList;

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
			if (direction == DirectionType.Incoming) {
				container.sender = connIds.get(0);
			} else {
				container.receivers = connIds;
			}
			container.message = createMessage(tempContainer.message,
					container.type);
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
		case GameStart:
			return gson.fromJson(message, GameStartMessage.class);
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
		case SpecialAbilityActivated:
			return gson.fromJson(message, SpecialAbilityActivatedMessage.class);
		case SpecialAbilityDeactivated:
			return gson.fromJson(message,
					SpecialAbilityDeactivatedMessage.class);
		case TimerUpdate:
			return gson.fromJson(message, TimerUpdateMessage.class);
		default:
			return null;
		}
	}
}
