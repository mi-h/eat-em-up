package at.jku.se.eatemup.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.JsonTool;
import at.jku.se.eatemup.core.json.MessageContainer;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.TempMessageContainer;
import at.jku.se.eatemup.core.json.messages.GameEndMessage;
import at.jku.se.eatemup.core.logging.Logger;

/**
 * Servlet implementation class Jsontest
 */
@WebServlet("/JsonTest")
public class JsonTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JsonTestServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
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
			TempMessageContainer temp2 = JsonTool
					.CreateTempMessageContainer(serial2);
			ArrayList<String> tList = new ArrayList<>();
			tList.add("testid");
			MessageContainer container = JsonTool.CreateMessageContainer(temp2,
					DirectionType.Incoming, tList);
		} catch (Exception ex) {
			Logger.log("jsontest died");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
