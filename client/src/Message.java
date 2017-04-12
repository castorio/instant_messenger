import java.io.*;
import java.util.List;


public class Message implements Serializable{

	public final static int TESTING = 0;
	public final static int CONVERSATION_REQUEST = 1;
	public final static int CONVERSATION_MESSAGE = 2;
	public final static int CONVERSATION_CLOSE = 3;
	public final static int CONVERSATION_RANDOM = 4;
	public final static int LOGIN = 5;
	public final static int REGISTER = 6;
	
	public final static int CONFIRMATION = 7;
	public final static int REQUEST_ANSWER = 8;
	public final static int FRIENDS = 9;
	public final static int FRIEND = 10;
	
	public final static int FILE = 11;
	public static final int BLOCK = 12;
	
	String message[];
	int confirmation;
	int type;
	String to;
	String from;
	char[] password;
	List<String> list;
	byte[] file;
	String filename;
	
	/*
	 * Creates a message with type FILE. Apart from the file, needs a filename with extension.
	 * Also needs the username of the client who the message is directed to 
	 * and the username of the client where the message is from.
	 */
	public Message(byte[] file, String filename, String to, String from){
		this.file=file;
		this.filename=filename;
		this.to=to;
		this.from=from;
		type=FILE;
	}
	
	public Message(String to, String from, int type){
		this.to=to;
		this.from=from;
		this.type=type;
	}
	
	/*
	 * For all types of messages.
	 */
	public Message(String message, int type){
		String m[] = {message};
		this.message=m;
		this.type=type;
	}
	
	/*
	 * Create a CONFIRMATION message
	 */
	public Message(int confirmation){
		this.confirmation = confirmation;
		type=CONFIRMATION;
	}
	
	/*
	 * Create a message with the type REQUEST_ANSWER.
	 * Needs the confirmation and the client who the conversation would be to.
	 */
	public Message(int confirmation, String from){
		this.confirmation=confirmation;
		this.from=from;
		type=REQUEST_ANSWER;
	}
	
	/*
	 * Create a message with the type LOGIN or REGISTER.
	 * Needs a password and username.
	 */
	public Message(String username, char[] password, int type){
		String m[] = {username};
		this.message=m;
		this.type=type;
		this.password=password;
	}
	
	/*
	 * Generic message. All types
	 */
	public Message(String message[], int type){
		this.message=message;
		this.type=type;
	}
	
	/*
	 * Creates a Message with type CONVERSATION_REQUEST
	 * Needs a client name to who the conversation would be
	 */
	public Message(String to){
		this.to=to;
		this.type=CONVERSATION_REQUEST;
	}
	
	/*
	 * Creates a Message with type CONVERSATION_MESSAGE
	 * Needs the client who the message is to and the message itself.
	 */
	public Message(String to, String message, String from){
		this.to=to;
		String[] m = {message};
		this.message=m;
		this.from=from;
		this.type=CONVERSATION_MESSAGE;
	}
	
	/*
	 * Creates a message with type CONVERSATION_CLOSE
	 */
	public Message(String to, String from){
		this.to=to;
		this.from=from;
		this.type=CONVERSATION_CLOSE;
	}

	/*
	 * Creates a message with type FRIENDS;
	 * used for the list of FRIENDS that the server sends to the client
	 * needs the list of friends(String).
	 */
	public Message(List<String> friendsList) {
		this.list = friendsList;
		type=FRIENDS;
	}

	/*
	 * Creates any message, but is mostly used for conversation_random
	 */
	public Message(int h, int type) {
		this.type=type;
		this.confirmation=h;
	}

	public String getTo() {
		return to;
	}

	public String getFrom() {
		return from;
	}

	public String getMessage(int i) {
		return message[i];
	}
	
	public String getMessage() {
		return message[0];
	}

	public int getType() {
		return type;
	}

	public char[] getPassword() {
		return password;
	}
	
	public int getConfirmation(){
		return confirmation;
	}
	
	@Override
	public String toString(){
		return "Message type: "+type+" message-unknown to: "+to+" from: "+from+" confirmation: "+confirmation;
	}

	public String getFileName() {
		return filename;
	}
	
	public byte[] getFile(){
		return file;
	}
}
