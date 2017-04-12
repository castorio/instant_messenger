
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class ServerGUI extends JFrame{
	
	private JTextArea chatWindow;
	private JList<String> clientList;
	private DefaultListModel<String> listModel;
	
	private JMenu menu;
	private JMenuBar menuBar;
	public JMenuItem[] menuItems;
	
	public ServerGUI(){
		super("IM - Instant Messenger (server)");
		listModel = new DefaultListModel<String>();
		clientList = new JList<String>(listModel);
		add(clientList, BorderLayout.EAST);
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		
		menuBar = new JMenuBar();
		menu = new JMenu("File");
		menuBar.add(menu);
		menuItems = new JMenuItem[4];
		menuItems[0]= new JMenuItem("Reset client list");
		menuItems[1]= new JMenuItem("Save current client list");
		menuItems[2]= new JMenuItem("Get client list");
		menuItems[3]= new JMenuItem("Remove client [TODO]");
		for(int i=0; i<menuItems.length; i++){
			menu.add(menuItems[i]);
		}
		
		setJMenuBar(menuBar);
		setSize(300,400);
		setVisible(true);
	}


	public void showMessage(final String text) {
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWindow.append(text);
					chatWindow.setCaretPosition(chatWindow.getDocument().getLength());
				}
			}
		);
	}
	
	public void updateList(String client) {
		listModel.addElement(client);
	}
	
	public void removeFromList(String client){
		listModel.removeElement(client);
	}
	
}
