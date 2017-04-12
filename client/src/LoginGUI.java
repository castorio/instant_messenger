import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

@SuppressWarnings("serial")
public class LoginGUI extends JFrame implements ActionListener{
	
	private JPanel panel;
	private JTextField username;
	private JPasswordField password;
	private JButton loginButton;
	private JButton registerButton;
	
	public Handler handler;
	
	public LoginGUI(Handler handl){
		super("Log in");
		handler = handl;
		
		panel = new JPanel();
		username = new JTextField();
		username.setPreferredSize(new Dimension(200,25));
		panel.add(new JLabel("Username"));
		panel.add(username);
		password = new JPasswordField();
		password.setPreferredSize(new Dimension(200,25));
		panel.add(new JLabel("Password"));
		panel.add(password);
		loginButton = new JButton("Log in");
		panel.add(loginButton);
		registerButton = new JButton("Register");
		panel.add(registerButton);
		add(panel);
		
		username.addActionListener(this);
		password.addActionListener(this);
		loginButton.addActionListener(this);
		registerButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				RegisterGUI gui = new RegisterGUI(handler);
				gui.setLocation(getLocationOnScreen().x+20, getLocationOnScreen().y+20);
			}
		});
		
		setVisible(true);
		setSize(200, 250);
		setResizable(false);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			System.out.println("Trying to log in...");
			int l = handler.login(username.getText(), password.getPassword());
			if(l==1){
				System.out.println("Login success!");
				handler.loginSuccess();
			}else if(l==-1){
				System.out.println("Login unsuccessful");
				JOptionPane.showMessageDialog(this, "Login unsuccessful. Wrong password or wrong username, maybe? Try again.");
			}else if(l==0){
				System.out.println("Login unsuccessful");
				JOptionPane.showMessageDialog(this, "This user is already logged in elsewhere.");
				
			}
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
