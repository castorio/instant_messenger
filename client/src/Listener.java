import java.io.*;
import javax.swing.*;


public class Listener implements Runnable{
	
	private Handler handler;
	
	public Listener(Handler handler) {
		this.handler=handler;
	}
	
	public void run(){
		while(true){
			try{
				Message m = (Message)handler.input.readObject();
				System.out.println("Received "+m.toString());
				
				if(m.getType()==Message.CONVERSATION_MESSAGE){
					handler.receiveMessage(m);
				}else if(m.getType()==Message.REQUEST_ANSWER){
					if(m.getConfirmation()==1){//Online
						handler.createConversation(m.getFrom());
					}else if(m.getConfirmation()==-1){//Offline
						JOptionPane.showMessageDialog(handler.gui, m.getFrom()+" is not online.");
					}else if(m.getConfirmation()==0){//Non existent
						JOptionPane.showMessageDialog(handler.gui, "The user: "+m.getFrom()+" does not exist.");
					}else if(m.getConfirmation()==-2){
						JOptionPane.showMessageDialog(handler.gui, "The user: "+m.getFrom()+" has blocked you.");
					}else if(m.getConfirmation()==2){//Random
						int b = 0;
						do{
							if(m.getFrom()==null){
								JOptionPane.showMessageDialog(handler.gui, "There are not enough people online.");
								b=-1;
							}else if(m.getFrom().equals(handler.getUsername())){
								handler.requestRandomConversation(b);
								m = (Message)handler.input.readObject();
							}else if(handler.getConversation(m.getFrom())!=null){
								b++;
								handler.requestRandomConversation(b);
								m = (Message)handler.input.readObject();
							}else if(m.getFrom()!=null){
								handler.createConversation(m.getFrom());
								b=-1;
							}
							
						}while(b>=0);
					}
				}else if(m.getType()==Message.CONVERSATION_CLOSE){
					handler.closeConversation(m.getFrom());
				}else if(m.getType()==Message.FILE){
					handler.receiveFile(m);
				}else if(m.getType()==Message.BLOCK){
					JOptionPane.showMessageDialog(handler.gui, "You have been blocked by "+m.getFrom()+". You will not be able to contact him. \n Behave yourself!");
					handler.closeConversation(m.getFrom());
					handler.removeFriend(m.getFrom());
				}else{
					System.err.println("Unexpected answer of type "+m.getType());
				}
			}catch(EOFException eof){
				JOptionPane.showMessageDialog(handler.gui, "Server is down.");
				System.exit(0);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
