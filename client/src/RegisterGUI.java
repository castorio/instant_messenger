import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;

@SuppressWarnings("serial")
public class RegisterGUI extends JFrame implements ActionListener{
	
	private JPanel panel;
	private JTextField username;
	private JPasswordField password;
	private JPasswordField password2;
	private JButton registerButton;
	
	private Handler handler;
	
	public RegisterGUI(Handler handler){
		super("Register");
		this.handler = handler;
		
		panel = new JPanel();
		username = new JTextField();
		username.setPreferredSize(new Dimension(200,25));
		panel.add(new JLabel("Username"));
		panel.add(username);
		
		password = new JPasswordField();
		password.setPreferredSize(new Dimension(200,25));
		panel.add(new JLabel("Password"));
		panel.add(password);
		
		password2 = new JPasswordField();
		password2.setPreferredSize(new Dimension(200,25));
		panel.add(new JLabel("Confirm Password"));
		panel.add(password2);
		
		registerButton = new JButton("Register");
		panel.add(registerButton);
		add(panel);
		
		username.addActionListener(this);
		password.addActionListener(this);
		password2.addActionListener(this);
		registerButton.addActionListener(this);
		
		setVisible(true);
		setSize(200, 250);
		setResizable(false);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if(Arrays.equals(password.getPassword(), password2.getPassword())){
				System.out.println("Trying to register...");
				if(handler.register(username.getText(), password.getPassword())){
					System.out.println("Register success!");
					setVisible(false);
				}else{
					System.out.println("Register unsuccessful");
					JOptionPane.showMessageDialog(this, "Username taken. Choose another or I will kill your family.");
				}
			}else{
				JOptionPane.showMessageDialog(this, "Passwords are not the same.");
			}
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
