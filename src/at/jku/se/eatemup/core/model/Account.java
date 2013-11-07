package at.jku.se.eatemup.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "accounts")
public class Account {
	@DatabaseField(id=true)
	private String name;
	@DatabaseField
	private String password;
	@DatabaseField(index=true)
	private int points;
	@DatabaseField(canBeNull=true)
	private String avatar;
	@DatabaseField(canBeNull=true)
	private byte[] avatarImage;
	
	public Account(){
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void addPoints(int points){
		this.points += points;
	}
	
	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public byte[] getAvatarImage() {
		return avatarImage;
	}

	public void setAvatarImage(byte[] avatarImage) {
		this.avatarImage = avatarImage;
	}
}
