package mangaview;

public class Comment {

	public Comment(String user, String ts, String icon, String content) {
		this.user = user;
		this.icon = icon;
		this.content = content;
		this.timestamp = ts;
	}
	String getContent() {return content;}
	String getUser() {return user;}
	String getIcon() {return icon;}
	String getTimestamp() { return timestamp;}
	
	String content, user, icon, timestamp;
}
