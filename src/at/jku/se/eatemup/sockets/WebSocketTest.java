package at.jku.se.eatemup.sockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import at.jku.se.eatemup.core.BattleCreator;
import at.jku.se.eatemup.core.MessageCreator;
import at.jku.se.eatemup.core.MessageHandler;
import at.jku.se.eatemup.core.TempMessageContainer;
import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.JsonTool;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.messages.BattleStartMessage;
import at.jku.se.eatemup.core.json.messages.GameEndMessage;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.core.model.Battle;

@ServerEndpoint("/websocketTest")
public class WebSocketTest {

	@OnClose
	public void onClose() {
		System.out.println("Connection closed");
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException,
			InterruptedException {
		try {
			GameEndMessage msg = new GameEndMessage();
			msg.teamRedWin = true;
			HashMap<String, Object> map1 = new HashMap<>();
			map1.put("test", 1);
			map1.put("bool", true);
			map1.put("string", "hello world");
			HashMap<String, Object> map2 = new HashMap<>();
			map2.put("test2", 2);
			map2.put("bool2", false);
			map2.put("string2", "bye world");
			msg.playerResults = new ArrayList<>();
			msg.playerResults.add(map1);
			msg.playerResults.add(map2);
			String serial1 = JsonTool.SerializeMessage(msg);
			TempMessageContainer temp = new TempMessageContainer();
			temp.message = serial1;
			temp.type = MessageType.GameEnd;
			String serial2 = JsonTool.SerializeTempMessageContainer(temp);
			Battle b1 = BattleCreator.CreateBattle("testuser1", "testuser2");
			Battle b2 = BattleCreator.CreateBattle("testuser1", "testuser3");
			Battle b3 = BattleCreator.CreateBattle("testuser2", "testuser3");
			BattleStartMessage bsm1 = new BattleStartMessage();
			bsm1.setBattle(b1);
			BattleStartMessage bsm2 = new BattleStartMessage();
			bsm2.setBattle(b2);
			BattleStartMessage bsm3 = new BattleStartMessage();
			bsm3.setBattle(b3);
			ArrayList<String> receivers = new ArrayList<>(1);
			receivers.add(session.getId());
			SessionStore.saveSession(session);
			// Print the client message for testing purposes
			System.out.println("Received: " + message);

			session.getBasicRemote().sendText(
					"This is the first server message, your session-id is: "
							+ session.getId());
			session.getBasicRemote().sendText(serial2);
			session.getBasicRemote().sendText(
					"three async BattleStartMessage are incoming!!!!11elf");
			MessageHandler.PushMessage(MessageCreator.createMsgContainer(bsm1,
					receivers));
			MessageHandler.PushMessage(MessageCreator.createMsgContainer(bsm2,
					receivers));
			MessageHandler.PushMessage(MessageCreator.createMsgContainer(bsm3,
					receivers));
		} catch (Exception ex) {
			Logger.log("websockettest died");
		}
	}

	@OnOpen
	public void onOpen() {
		System.out.println("Client connected");
	}
}