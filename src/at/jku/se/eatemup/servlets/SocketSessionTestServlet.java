package at.jku.se.eatemup.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;

import at.jku.se.eatemup.sockets.SessionStore;

/**
 * Servlet implementation class SocketSessionTestServlet
 */
@WebServlet("/SocketSessionTestServlet")
public class SocketSessionTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SocketSessionTestServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Servlet reached...");
		StringBuilder sb = new StringBuilder();
		sb.append("sessionstore content: ");
		for (String s : SessionStore.getKeyList()) {
			sb.append(s);
			sb.append(" ");
		}
		sb.append("[");
		sb.append(SessionStore.getKeyList().size());
		sb.append("]");
		System.out.println(sb.toString());
		System.out.println("try sending messages...");
		try {
			if (SessionStore.getKeyList().size() > 0) {
				String id = SessionStore.getKeyList().get(0);
				Session session = SessionStore.getSession(id);
				System.out.println("try sending sync message...");
				session.getBasicRemote().sendText("servlet sync message");
				System.out.println("try sending async message...");
				session.getAsyncRemote().sendText("servlet async message");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("finished");
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
