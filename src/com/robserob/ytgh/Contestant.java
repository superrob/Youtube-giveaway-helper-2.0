package com.robserob.ytgh;

public class Contestant {
	public String username;
	public String message;
	public int messageID;
	public String profileURL;
	Contestant(String username, String message, int messageID, String profileURL) {
		this.username = username;
		this.message = message;
		this.messageID = messageID;
		this.profileURL = profileURL;
	}
}
