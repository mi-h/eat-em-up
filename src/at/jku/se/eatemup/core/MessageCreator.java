package at.jku.se.eatemup.core;

import java.util.ArrayList;

import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.JsonParseException;
import at.jku.se.eatemup.core.json.JsonTool;
import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.logging.Logger;

public class MessageCreator {

	public static MessageContainer createMsgContainer(Message message,
			ArrayList<String> receivers) {
		MessageContainer container = new MessageContainer();
		container.direction = message.getDirection();
		container.message = message;
		container.receivers = receivers;
		container.type = message.getType();
		container.castType = message.getCastType();
		container.sender = new Sender("server","null");
		return container;
	}

	public static MessageContainer createMsgContainer(String message,
			String sessionId) {
		try {
			TempMessageContainer temp = JsonTool
					.CreateTempMessageContainer(message);
			ArrayList<String> sender = new ArrayList<>();
			sender.add(sessionId);
			MessageContainer container = JsonTool.CreateMessageContainer(temp,
					DirectionType.Incoming, sender);
			return container;
		} catch (JsonParseException jex) {
			Logger.log(jex.getLogText());
			return null;
		}

	}
}
