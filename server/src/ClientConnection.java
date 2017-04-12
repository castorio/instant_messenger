
import java.io. *;
import java.net.*;
import java.util.*;

public class ClientConnection implements Runnable{
	

	private ObjectOutputStream output;
	private ObjectInputStream input;
	public Socket connection;
	private Client client;
	private Handler handler;
	
	private List<String> conversationsWith;
	
	public ClientConnection(Socket connection, ObjectOutputStream output, ObjectInputStream input, Handler handler){
		this.connection=connection;
		this.output=output;
		this.input=input;
		this.handler=handler;
		conversationsWith = new ArrayList<String>();
	}
	
	public void run(){
		try{
			boolean b = true;
			//Before login
			do{
				Message login = (Message) input.readObject();
				if(login.getType()==Message.LOGIN){
					int t = handler.login(login);
					if(t==1){
						handler.showMessage(connection.getInetAddress().getHostName()+" just logged in as "+login.getMessage()+"\n");
						output.writeObject(new Message(1)); //CORRECT
						output.flush();
						this.client = handler.getClient(login.getMessage());
						client.setOnline(true);
						output.writeObject(new Message(client.getFriendsList()));
						output.flush();
						b=false;
					}else if(t==0){
						output.writeObject(new Message(-1)); //INCORRECT
						output.flush();
					}else if(t==2){
						output.writeObject(new Message(0)); //LOGGEDIN
						output.flush();
					}
				}else if(login.getType()==Message.REGISTER){
					if(handler.checkUsername(login.getMessage())){
						Client c =new Client(handler.getClient().size(), login.getMessage(), login.getPassword(), handler);
						handler.addClient(c);
						output.writeObject(new Message(1)); //CORRECT
						output.flush();
						handler.save();
					}else{
						output.writeObject(new Message(-1)); //INCORRECT
						output.flush();
					}
				}else{
					System.err.println("unknown");
				}
			}while(b);
			
			//After login
			while(true){
				Message message = (Message) input.readObject();
				if(message.getType()==Message.CONVERSATION_MESSAGE || message.getType()==Message.FILE){//Sending message
					handler.sendMessage(message, true);
					addPossibleConversation(message.getTo());
				}else if(message.getType()==Message.CONVERSATION_REQUEST){//Requesting conversation
					/*
					 * Returns 1 for online
					 * Returns 0 for user doesn't exist
					 * Returns -1 for offline
					 * -2 for blocked
					 */
					if(handler.checkBlock(message.getTo(), client.getUsername())){
						output.writeObject(new Message(-2, message.getTo()));
						output.flush();
					}else{
						output.writeObject(new Message(handler.isOnline(message.getTo()), message.getTo()));
						output.flush();
					}
				}else if(message.getType()==Message.FRIEND){
					client.addFriend(message.getMessage());
					handler.save();
				}else if(message.getType()==Message.CONVERSATION_RANDOM){
					int i = message.getConfirmation();
					String username;
					do{
						username = handler.getRandomClient(i);
						i++;
					}while(username!=null&&handler.checkBlock(username, client.getUsername()));
					output.writeObject(new Message(2, username));
					output.flush();
				}else if(message.getType()==Message.BLOCK){
					client.addBlock(message.getMessage());
					handler.sendMessage(new Message(message.getMessage(), client.getUsername(), Message.BLOCK), false);
				}
			}
		}catch(EOFException eof){
			try {
				handler.showMessage(eof.toString()+"\n");
				closeConnection();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch(Exception e){
			try {
				e.printStackTrace();
				handler.showMessage(e.toString()+"\n");
				closeConnection();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void addPossibleConversation(String user) {
		if(conversationsWith.contains(user)){
			return;
		}else{
			conversationsWith.add(user);
		}
	}

	public void closeConnection() throws IOException{
		if(client!=null){
			client.setOnline(false);
		}
		handler.removeConnection(this);
		for(String s : conversationsWith){
			handler.sendMessage(new Message(s, client.getUsername()), false);
		}
		output.close();
		input.close();
		connection.close();
	}

	public void sendMessage(Message message) throws IOException{
		output.writeObject(message);
		output.flush();
		if(message.getType()==Message.CONVERSATION_CLOSE){
			conversationsWith.remove(message.getFrom());
		}
	}
	
	public void close() throws IOException{
		output.close();
		input.close();
		connection.close();
	}

	
	
	public Client getClient() {
		return client;
	}

	public ObjectInputStream getInput() {
		return input;
	}

	public ObjectOutputStream getOutput() {
		return output;
	}
	
}
