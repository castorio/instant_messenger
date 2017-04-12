
import java.awt.*;
import java.util.List;

import javax.swing.*;

@SuppressWarnings("serial")
public class MainGUI extends JFrame {
	
	public JList<String> friendsList;
	public JButton startConversation;
	public List<String> requests;
	public DefaultListModel<String> friends;
	public JMenuBar menuBar;
	public JMenu menu;
	public JMenuItem[] menuItems;
	
	
	public MainGUI(String name){
		super("IM - Logged in as "+name);
		friends = new DefaultListModel<String>();
		friendsList= new JList<String>(friends);
		
		add(new JScrollPane(friendsList), BorderLayout.CENTER);
		startConversation = new JButton("Start conversation with...");
		add(startConversation, BorderLayout.NORTH);
		
		menuBar = new JMenuBar();
		menu = new JMenu("Option");
		menuBar.add(menu);
		menuItems = new JMenuItem[1];
		menuItems[0]= new JMenuItem("Start random conversation");
		for(int i=0; i<menuItems.length; i++){
			menu.add(menuItems[i]);
		}
		setJMenuBar(menuBar);
		setSize(300,500);
	}
	
	public void addFriend(String request) {
		friends.addElement(request);
	}

	public void setFriendsList(Message friendsList) {
		if(friendsList.list.isEmpty()){
			JOptionPane.showMessageDialog(this, "You have no friends. How about you make some?");
			return;
		}
		for(String s : friendsList.list){
			System.out.println(s);
			friends.addElement(s);
		}
	}

	public void removeFriend(String who) {
		friends.removeElement(who);
	}
}
