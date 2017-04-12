
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.sound.sampled.*;
import javax.swing.*;

public class Handler{
	
	private ObjectOutputStream output;
	public ObjectInputStream input;
	private Socket connection;
	
	private Thread listener;
	
	private String username;
	
	private List<Conversation> conversations;
	public LoginGUI loginGUI;
	public MainGUI gui;
	private String serverIP;
	private int port;
	private Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	
	/*
	Contructor
	arguments: ip, port
	*/
	public Handler(String host, int port){
		serverIP = host;
		this.port=port;
		if(!connect()){
			JOptionPane.showMessageDialog(loginGUI, "Connection to server has failed.");
			System.exit(0);
		}
		
		conversations =new ArrayList<Conversation>();
		listener = new Thread(new Listener(this));
		
		loginGUI = new LoginGUI(this);
		loginGUI.setLocation(screensize.width/3, screensize.height/3);
		loginGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginGUI.setVisible(true);
	}
	
	public void loginSuccess(){
		gui = new MainGUI(getUsername());
		gui.startConversation.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				try {
					String s = JOptionPane.showInputDialog("With who would you like to talk?");
					Conversation c= getConversation(s);
					if(c == null){
						requestConversation(s);
					}else{
						c.setVisible(true);
						c.userText.requestFocus();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		gui.menuItems[0].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				try{
					requestRandomConversation(0);
				}catch(IOException ex){
					ex.printStackTrace();
				}
			}
		});
		gui.friendsList.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount()==2 || e.getClickCount()==3){
					try {
						String s = (String) gui.friendsList.getSelectedValue();
						Conversation c= getConversation(s);
						if(c == null){
							requestConversation(s);
						}else{
							c.setVisible(true);
							c.userText.requestFocus();
						}
						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setLocation(loginGUI.getLocation().x+20, loginGUI.getLocation().y+20);
		loginGUI.setVisible(false);
		loginGUI = null;
		gui.setVisible(true);
		try {
			Message friendsList = (Message)input.readObject();
			if(friendsList.getType()!=Message.FRIENDS){
				System.err.println("Type is not FRIENDS");
			}
			gui.setFriendsList(friendsList);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		listener.start();
	}
	
	public void requestRandomConversation(int h) throws IOException{
		output.writeObject(new Message(h, Message.CONVERSATION_RANDOM));
		output.flush();
	}

	public int login(String username, char[] password) throws ClassNotFoundException, IOException{
		if(username==null || password ==null){
			return -1;
		}else{
			output.writeObject(new Message(username, password, Message.LOGIN));
			output.flush();
			Message m = (Message) input.readObject();
			if(m.getType()!=Message.CONFIRMATION){
				System.err.println("MESSAGE NOT CONFIRMATION");
				return -1;
			}else{
				if(m.getConfirmation()==1){
					this.username = username;
				}
				return m.getConfirmation();
			}
		}
	}

	public boolean register(String username, char[] password) throws ClassNotFoundException, IOException {
		if(username==null || password ==null){
			return false;
		}else{
			
			output.writeObject(new Message(username, password, Message.REGISTER));
			output.flush();
			Message m = (Message) input.readObject();
			if(m.getType()!=Message.CONFIRMATION){
				System.err.println("MESSAGE NOT CONFIRMATION");
				return false;
			}else if(m.getConfirmation()==1){
				return true;
			}else if(m.getConfirmation()==-1){
				return false;
			}else{
				System.err.println("UNEXPECTED REACTION: "+m.getConfirmation());
				return false;
			}
		}
	}
	
	public void requestConversation(String name) throws IOException, ClassNotFoundException{
		if(name==null){
			return;
		}else{
			output.writeObject(new Message(name));
			output.flush();
		}
	}
	
	public class ConnectingThread implements PropertyChangeListener, Runnable{
		public JDialog dialog;
		
		public void run() {
			JOptionPane pane = new JOptionPane("Connecting to server..", JOptionPane.INFORMATION_MESSAGE);
			pane.setOptions(new String[]{"Cancel"});
			pane.setOptionType(JOptionPane.DEFAULT_OPTION);
			dialog = pane.createDialog(null, "Connecting");
			pane.addPropertyChangeListener(this);
			dialog.setVisible(true);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			String prop = evt.getPropertyName();
			if (JOptionPane.VALUE_PROPERTY.equals(prop)){
				System.exit(-1);
			}
		}
		
	}
	public boolean connect() {
		try{ 
			ConnectingThread runnable = new ConnectingThread();
			Thread thread = new Thread(runnable);
			thread.start();
			connection = new Socket();
			connection.connect(new InetSocketAddress(InetAddress.getByName(serverIP), port), 0);

			output = new ObjectOutputStream(connection.getOutputStream());
			input = new ObjectInputStream(connection.getInputStream());
			output.flush();
			
			System.out.println("Connected to "+serverIP);
				
			runnable.dialog.setVisible(false);
			runnable = null;
			thread.interrupt();
			thread = null;
			
			return true;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
	}

	public void stop() {
		System.out.println("Closing connection...");
		try {
			input.close();
			output.close();
			connection.close();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createConversation(final String from) {
		final Conversation c = new Conversation(from);
		c.setLocation(gui.getLocationOnScreen().x+20, gui.getLocationOnScreen().y+20);
		conversations.add(c);
		c.userText.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					if (e.getActionCommand()!=null) {
						sendMessage(new Message(c.getWho(), e.getActionCommand(), getUsername()));
						c.showMessage(e.getActionCommand(), getUsername());
						c.userText.setText("");
					}else{
						c.showErrorMessage("Do you have anything useful to say?", true);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		//http://iharder.sourceforge.net/current/java/filedrop/
		new FileDrop(null, c.chatWindow, new FileDrop.Listener() {
			public void filesDropped(File[] files) {
				
				byte[][] byteArrays = new byte[files.length][];
				String[] filenames = new String[files.length];
				for(int i = 0 ; i<files.length;i++){
					try{
						String[] partparts = files[i].toPath().toString().split("/");
						//http://stackoverflow.com/questions/5257459/java-split-function
						String[] parts = partparts[partparts.length-1].split("\\\\");
						filenames[i] = parts[parts.length-1];
						byteArrays[i] = Files.readAllBytes(files[i].toPath());
					}catch(IOException e){
						JOptionPane.showMessageDialog(c, filenames[i]+" is not a file, but a directory. ");
					}
				}
				for(int d= 0; d<files.length;d++){
					try {
						if(byteArrays[d]!=null){
							sendMessage(new Message(byteArrays[d], filenames[d], c.getWho(), getUsername()));
							c.showText("Sent file "+filenames[d], false);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		c.menuItems[0].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				addFriend(c.getWho());
			}
		});
		c.menuItems[1].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				c.setOffline();
				conversations.remove(c);
				c.dispose();
				c.setVisible(false);
			}
		});
		c.menuItems[2].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(JOptionPane.showConfirmDialog(gui, "Are you sure you want to block "+c.getWho()+"? You won't be able to unblock him/her.")==JOptionPane.YES_OPTION){
					try {
						blockUsername(c.getWho());
						JOptionPane.showMessageDialog(gui, c.getWho()+" was blocked.");
						c.setOffline();
						conversations.remove(c);
						c.dispose();
						c.setVisible(false);
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		c.addWindowListener(new WindowListener(){
			public void windowOpened(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				c.setVisible(false);
			}
			public void windowClosed(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
		});

	}
	
	public void blockUsername(String who) throws IOException {
		
		output.writeObject(new Message(who, Message.BLOCK));
		output.flush();
	}

	public void addFriend(String who) {
		try{
			output.writeObject(new Message(who, Message.FRIEND));
			output.flush();
			gui.addFriend(who);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void removeFriend(String who){
		gui.removeFriend(who);
	}

	public void sendMessage(Message message) throws IOException {
		output.writeObject(message);
		output.flush();
	}

	public synchronized void receiveMessage(Message message){
		for(Conversation c : conversations){
			if(c.getWho().equals(message.getFrom())){
				c.showMessage(message.getMessage(), message.getFrom());
				if(!c.isFocused()){
					c.setVisible(true);
					c.userText.requestFocus();
					playSound();
				}
				return;
			}
		}
		createConversation(message.getFrom());
		receiveMessage(message);
	}
	
	public synchronized void receiveFile(Message message){
		for(Conversation c : conversations){
			if(c.getWho().equals(message.getFrom())){
				c.showFile(message);
				if(!c.isFocused()){
					c.userText.requestFocus();
					playSound();
				}
				return;
			}
		}
		createConversation(message.getFrom());
		receiveMessage(message);
	}
	
	public Conversation getConversation(String with){
		for(Conversation c : conversations){
			if(c.getWho().equals(with)){
				return c;
			}
		}
		return null;
	}

	public synchronized void closeConversation(String who) {
		for(Conversation c : conversations){
			if(c.getWho().equals(who)){
				c.setOffline();
				conversations.remove(c);
				final Conversation s = c;
				c.addWindowListener(new WindowListener(){
					public void windowOpened(WindowEvent e) {}
					public void windowClosing(WindowEvent e) {
						s.dispose();
					}
					public void windowClosed(WindowEvent e) {}
					public void windowIconified(WindowEvent e) {}
					public void windowDeiconified(WindowEvent e) {}
					public void windowActivated(WindowEvent e) {}
					public void windowDeactivated(WindowEvent e) {}
				});
			}
		}
	}
	
	public static synchronized void playSound() {
		  new Thread(new Runnable() {
		    public void run() {
		      try {
		        Clip clip = AudioSystem.getClip();
		        InputStream bufferedIn = new BufferedInputStream(getClass().getResourceAsStream("/media/notification.wav"));
		        AudioInputStream inputStream = AudioSystem.getAudioInputStream(bufferedIn);
		        clip.open(inputStream);
		        clip.start(); 
		      } catch (Exception e) {
		        e.printStackTrace();
		      }
		    }
		  }).start();
		}

	public String getUsername() {
		return username;
	}
}
