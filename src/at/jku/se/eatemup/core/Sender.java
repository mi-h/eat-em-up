package at.jku.se.eatemup.core;

public class Sender {
	public String username;
	public String userid;
	public String session;
	
	public Sender(){
		
	}
	
	public Sender(String username, String session, String userid){
		this.username = username;
		this.session = session;
		this.userid = userid;
	}
}
