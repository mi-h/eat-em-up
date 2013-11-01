package at.jku.se.eatemup.core;

import java.util.ArrayList;

import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.json.MessageContainer;

public class MessageCreator {

	public static MessageContainer createMsgContainer(Message message,
			ArrayList<String> receivers) {
		MessageContainer container = new MessageContainer();
		container.direction = message.getDirection();
		container.message = message;
		container.receivers = receivers;
		container.type = message.getType();
		container.castType = message.getCastType();
		container.sender = "server";
		return container;
	}
}
