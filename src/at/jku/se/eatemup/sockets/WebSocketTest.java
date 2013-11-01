package at.jku.se.eatemup.sockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.JsonTool;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.TempMessageContainer;
import at.jku.se.eatemup.core.json.messages.GameEndMessage;

@ServerEndpoint("/websocketTest")
public class WebSocketTest {

  @OnMessage
  public void onMessage(String message, Session session) 
    throws IOException, InterruptedException {
	  
	  GameEndMessage msg = new GameEndMessage();
		msg.castType = CastType.Broadcast;
		msg.teamRedWin = true;
		HashMap<String,Object> map1 = new HashMap<>();
		map1.put("test", 1);
		map1.put("bool", true);
		map1.put("string", "hello world");
		HashMap<String,Object> map2 = new HashMap<>();
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
  
    // Print the client message for testing purposes
    System.out.println("Received: " + message);
  
    // Send the first message to the client
    session.getBasicRemote().sendText("This is the first server message, your session-id is: "+session.getId());  
    // Send a final message to the client
    session.getBasicRemote().sendText(serial2);
  }
  
  @OnOpen
  public void onOpen() {
    System.out.println("Client connected");
  }

  @OnClose
  public void onClose() {
    System.out.println("Connection closed");
  }
}