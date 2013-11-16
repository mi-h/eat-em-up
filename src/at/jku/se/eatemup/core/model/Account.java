package at.jku.se.eatemup.core.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "accounts")
public class Account {
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private AccountType type;
	@DatabaseField(index=true)
	private String username;
	@DatabaseField
	private String password;
	@DatabaseField(index=true)
	private int points;
	@DatabaseField(index=true)
	private String facebookId;
	@DatabaseField(canBeNull=true)
	private String avatar;
	@DatabaseField(canBeNull=true,dataType=DataType.BYTE_ARRAY)
	private byte[] avatarImage;
	
	public Account(){
		
	}

	public void addPoints(int points){
		this.points += points;
	}

	public String getAvatar() {
		return avatar;
	}

	public byte[] getAvatarImage() {
		return avatarImage;
	}

	public String getFacebookId() {
		return facebookId;
	}

	public String getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}
	
	public int getPoints() {
		return points;
	}
	
	public AccountType getType() {
		return type;
	}

	public String getUsername() {
		return username;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public void setAvatarImage(byte[] avatarImage) {
		this.avatarImage = avatarImage;
	}

	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	public void setUsername(String name) {
		this.username = name;
	}
}
