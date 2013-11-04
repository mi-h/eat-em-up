package at.jku.se.eatemup.servlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.jku.se.eatemup.core.database.DataStore2;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.core.model.Account;
import at.jku.se.eatemup.core.model.GoodiePoint;
import at.jku.se.eatemup.core.model.Position;

/**
 * Servlet implementation class DbTest
 */
@WebServlet("/DbTest")
public class DbTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DbTest() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DataStore2 ds = new DataStore2();
		//ds.createTables();
		//Account acc = new Account();
		//acc.setName("tester");
		//acc.setPassword("password");
		//ds.addAccount(acc);
		/*
		Account acc = ds.getAccountByUsername("tester");
		ArrayList<Account> high = ds.getHighscore(100);
		String password = ds.getUserPassword("tester");
		Position pos = new Position();
		pos.setLatitude(14.61644);
		pos.setLongitude(43.71461);
		ds.addGoodiePosition(pos);
		ArrayList<GoodiePoint> points = ds.getGoodiePoints();
		ds.addUserPoints("tester", 55);
		*/
		Logger.log("testlogmsg");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
