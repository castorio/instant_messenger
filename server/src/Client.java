
import java.io. *;
import java.net.*;
import java.util.*;

public class Client implements Serializable {

	private String username;
	private char[] password;
	private int id;
	private boolean online;
	private List<String> friendsList;
	private List<String> blockList;
	
	public Client(int id, String username, char[] password, Handler handler){
		this.username=username;
		this.password=password;
		friendsList = new ArrayList<String>();
		blockList = new ArrayList<String>();
	}
	
	public Client(){
		username = "";
		char[] p = {'-'};
		password = p;
		id = 0;
		online=false;
		friendsList = new ArrayList<String>();
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getID(){
		return id;
	}

	public boolean login(Message login) {
		if(username.equals(login.getMessage()) && Arrays.equals(password, login.getPassword())){
			return true;
		}else{
			return false;
		}
	}

	public void setOnline(boolean b) {
		online = b;
	}

	public boolean getOnline() {
		return online;
	}

	public List<String> getFriendsList() {
		return friendsList;
	}

	public void addFriend(String friend) {
		friendsList.add(friend);
	}
	
	public List<String> getBlockList(){
		return blockList;
	}
	
	public void addBlock(String who){
		blockList.add(who);
	}
	
}
