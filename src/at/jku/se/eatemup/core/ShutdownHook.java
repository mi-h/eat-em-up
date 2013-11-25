package at.jku.se.eatemup.core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import at.jku.se.eatemup.core.logging.Logger;

public class ShutdownHook implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		Logger.closeConnection();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {

	}

}
