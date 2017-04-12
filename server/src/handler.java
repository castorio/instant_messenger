import java.util.*;
import java.util.List;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class Handler{
	
	private ServerSocket server;
	private List<Client> clients;
	private List<ClientConnection> connections;
	public ServerGUI gui;
	private final int port = 1050;
	Random rand;
	
	public Handler(boolean guitrue){
		new Thread(new Runnable(){
			public void run(){
				Scanner scanner = new Scanner(System.in);
				String in = "";
				do{
					System.out.println("Type 'exit' to stop the server");
					in = scanner.nextLine();
				}while(in ==null && !in.equals("exit"));
				scanner.close();
				stop();
			}
		}).start();;
		
		System.out.println("Gui is "+(guitrue?"on":"off"));
		
		rand = new Random();
		clients = read();
		if(clients==null){
			clients = new ArrayList<Client>();
		}
		for(Client c : clients){
			c.setOnline(false);
		}
		if(guitrue){
			gui=new ServerGUI();
			gui.menuItems[0].addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(JOptionPane.showConfirmDialog(gui, "Do you really want to reset the client list?", "Confirmation", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
						clients = new ArrayList<Client>();
						for (ClientConnection cc : connections) {
							try {
								cc.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						save();
						JOptionPane.showMessageDialog(gui, "Client list was removed.");
					}	
					
				}
			});
			gui.menuItems[1].addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					save();
					JOptionPane.showMessageDialog(gui, "Client list was saved");
				}
			});
			gui.menuItems[2].addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					save();
					JOptionPane.showMessageDialog(gui, "Client list was saved");
				}
			});
		}
		connections = new ArrayList<ClientConnection>();
	}
	
	public void showMessage(String message){
		if(gui!=null){
			gui.showMessage(message);
		}
		if(message.endsWith("\n"))
			System.out.println(message.substring(0, message.length()-1));
		else
			System.out.print(message);
	}
	
	public void run(){
		try{
			server = new ServerSocket(port, 100);
			showMessage("Server initialized\n");
			while(true){
					try{
						Socket connection = server.accept();
						showMessage(connection.getInetAddress().getHostName()+" just connected\n");
						ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
						output.flush();
						ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
						ClientConnection cc = new ClientConnection(connection, output, input, this);
						connections.add(cc);
						Thread client = new Thread(connections.get(connections.indexOf(cc)));
						client.start();
					}catch(Exception e){
						e.printStackTrace();
					}
				
			}
		}catch(BindException be){
			JOptionPane.showMessageDialog(gui, "Can not bind port "+port+". Maybe another server is open?");
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean checkUsername(String message) {
		for(Client c : clients){
			if(c.getUsername().equals(message)){
				return false;
			}
		}
		return true;
	}
	
	public Client getClient(String username) {
		for(Client c : clients){
			if(c.getUsername().equals(username)){
				return c;
			}
		}
		return null;
	}
	
	public Client getClient(int id){
		return clients.get(id);
	}
	
	public List<Client> getClient(){
		return clients;
	}
	
	public ClientConnection getClientConnection(Client client){
		for(ClientConnection cc : connections){
			if(cc.getClient()==client){
				return cc;
			}
		}
		return null;
	}
	
	public int login(Message login) {
		for(Client s : clients){
			if(s.login(login)){
				if(!s.getOnline()){
					return 1;
				}else{
					return 2;
				}
			}
		}
		return 0;
	}

	public void sendMessage(Message message, boolean conversation) {
		for(ClientConnection cc : connections){
			if(cc.getClient().getUsername().equals(message.getTo())){
				try {
					cc.sendMessage(message);
					if(conversation){
						cc.addPossibleConversation(message.getFrom());
					}
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
		}
		System.err.println("Could not find client to send message to. "+message.toString());
	}
	
 	private void stop() {
		save();
		showMessage("Closing connections...\n");
		try{
			for(ClientConnection cc : connections){
				cc.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		showMessage("Closing server\n");
		System.exit(0);
	}
		
	public void save(){
		if(clients==null)
			return;
		try {
			showMessage("Saving to file...\n");
			FileOutputStream file = new FileOutputStream("Clients.im");
			ObjectOutputStream stream = new ObjectOutputStream(file);
			stream.writeObject(clients);
			stream.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Client> read(){
		try{
			FileInputStream file = new FileInputStream("Clients.im");
			ObjectInputStream stream = new ObjectInputStream(file);
			ArrayList<Client> tmpList = (ArrayList<Client>) stream.readObject();
			stream.close();
			return tmpList;
		}catch(InvalidClassException e){
			JOptionPane.showMessageDialog(gui, "Client class changed. Creating new Clients.im file. Accounts will be reset.");
			return null;
		}catch(Exception e){
			e.printStackTrace();
			showMessage("Creating clients.im file.../n");
			return null;
		}
	}
	
	public ServerGUI getGUI(){
		return gui;
	}

	public void addClient(Client c) {
		clients.add(c);
	}

	public void removeConnection(ClientConnection cc) {
		connections.remove(cc);
	}

	public int isOnline(String client) {
		for(Client c : clients){
			if(c.getUsername().equals(client)){
				if(c.getOnline()){
					return 1;
				}else{
					return -1;
				}
			}
		}
		return 0;
	}

	public String getRandomClient(int minusclients) {
		if(connections.size()<=1+minusclients){
			return null;
		}
		return connections.get(rand.nextInt(connections.size())).getClient().getUsername();
	}

	
	public boolean checkBlock(String at, String username) {
		if(getClient(at) == null)
			return false;
		return getClient(at).getBlockList().contains(username);
	}
}
