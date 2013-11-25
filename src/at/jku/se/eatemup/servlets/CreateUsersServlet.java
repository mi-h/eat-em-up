package at.jku.se.eatemup.servlets;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.jku.se.eatemup.core.database.DataStore2;
import at.jku.se.eatemup.core.model.Account;
import at.jku.se.eatemup.core.model.AccountType;

/**
 * Servlet implementation class CreateUsersServlet
 */
@WebServlet("/CreateUsersServlet")
public class CreateUsersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CreateUsersServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	private void addAccountIfNotExists(String username, String password,
			DataStore2 dataStore) {
		if (dataStore.getAccountByUsername(username) == null) {
			Account temp = new Account();
			temp.setUsername(username);
			temp.setPassword(password);
			temp.setPoints(0);
			temp.setType(AccountType.Standard);
			temp.setId(UUID.randomUUID().toString());
			dataStore.addAccount(temp);
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		DataStore2 ds = new DataStore2();
		addAccountIfNotExists("stefan", "isteinediva", ds);
		addAccountIfNotExists("einediva", "iststefan", ds);
		addAccountIfNotExists("christian", "tutnix", ds);
		addAccountIfNotExists("java", "stinkt", ds);
		addAccountIfNotExists("alles", "kaputt", ds);
		addAccountIfNotExists("seppforcher", "istalt", ds);
		addAccountIfNotExists("karlmoik", "ausbraunau", ds);
		addAccountIfNotExists("markus", "hatkeinelustmehr", ds);
		addAccountIfNotExists("michael", "tutfastnix", ds);
		ds.closeConnection();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
