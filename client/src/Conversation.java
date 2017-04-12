import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Conversation extends JFrame{
	private String with;
	
	public JTextField userText;
	private JMenuBar menuBar;
	private JMenu menu;
	public JMenuItem[] menuItems;
	public JTextPane chatWindow;
	private JScrollPane scrollPane;
	public StyledDocument doc;
	public StyleContext context;

	private boolean talking;
	private boolean firstMessage = true;
	
	MutableAttributeSet selfMessage;
	MutableAttributeSet icon;
	MutableAttributeSet otherMessage;
	MutableAttributeSet selfName;
	MutableAttributeSet otherName;
	MutableAttributeSet standard;
	
	String[][] smileys = {{"ok", "oke", "okey", "okay", "okee", "okee"}, {"boobs", "tieten", "boob", "tiet"}, {"lol", "haha"}};
	String[] smileyURLS = {"media/smileys/thumbsUp.gif", "media/smileys/boobs.png", "media/smileys/lol.gif"};
	
	public Conversation(String name){
		super("Conversation with: "+name);
		userText = new JTextField();
		add(userText, BorderLayout.SOUTH);
		this.with=name;
		
		menuBar = new JMenuBar();
		menu = new JMenu("Settings");
		menuBar.add(menu);
		menuItems = new JMenuItem[3];
		menuItems[0]= new JMenuItem("Add "+with+" as friend");
		menuItems[1]= new JMenuItem("Close this conversation");
		menuItems[2]= new JMenuItem("Block this person");
		
		for(int i=0; i<menuItems.length; i++){
			menu.add(menuItems[i]);
		}
		
		context = new StyleContext();
		doc = new DefaultStyledDocument(context);
		chatWindow=new JTextPane(doc);
		chatWindow.setEditable(false);
		chatWindow.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e)
	        {
	            Element ele = doc.getCharacterElement(chatWindow.viewToModel(e.getPoint()));
	            AttributeSet as = ele.getAttributes();
	            URLLink fla = (URLLink)as.getAttribute("linkSet");
	            URLLink fla2 = (URLLink)as.getAttribute("linkSet2");
	            FileLink fl = (FileLink)as.getAttribute("fileLinkSet");
	            if(fla != null)
	            {
	                fla.execute();
	            }else if(fla2 !=null){
	            	fla2.execute();
	            }else if(fl !=null){
	            	fl.execute();
	            }
	        }
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		this.setMinimumSize(new Dimension(300,400));
		
		scrollPane = new JScrollPane(chatWindow);
		add(scrollPane, BorderLayout.CENTER);
		this.setJMenuBar(menuBar);
		setSize(400,600);
		setVisible(true);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		styles();
		
	}
	
	public void styles(){
		standard = doc.addStyle("standard", null);
		StyleConstants.setFontSize(standard, 15);
		StyleConstants.setFontFamily(standard, "Arial, sans-serif");
		selfMessage = doc.addStyle("selfMessage", (Style) standard);
		StyleConstants.setAlignment(selfMessage, StyleConstants.ALIGN_RIGHT);
		otherMessage = doc.addStyle("otherMessage", (Style) standard);
		StyleConstants.setForeground(otherMessage, Color.BLUE);
		
		selfName = doc.addStyle("selfName", (Style) selfMessage);
		StyleConstants.setBold(selfName, true);
		otherName = doc.addStyle("otherName", (Style) otherMessage);
		StyleConstants.setBold(otherName, true);
	}

	public String getSmileyURL(String text){
		for(int i=0; i<smileys.length;i++){
			for(int d=0; d<smileys[i].length; d++){
				if(smileys[i][d].equals(text)){
					return smileyURLS[i];
				}
			}
		}
		return null;
	}
	
	public MutableAttributeSet getSmiley(String smiley){
		URL url = Main.class.getResource(smiley);
		MutableAttributeSet mas = doc.getStyle(StyleContext.DEFAULT_STYLE);
		Icon icon = new ImageIcon(url);
		StyleConstants.setIcon(mas, icon);
		return mas;
	}
	
	public void showMessage(final String text, final String who) {
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						formatText(text, who);	
						chatWindow.setCaretPosition(chatWindow.getDocument().getLength());
					}
				}
			);
	}
	
	protected void formatText(String text, String who) {
		boolean other = false;
		int i = doc.getLength();
		try{
			if(who.equals(with)){
				other = true;
				if(talking ||firstMessage){
					doc.insertString(doc.getLength(), who+"\n", doc.getStyle("otherName"));
					talking=false;
					firstMessage=false;
				}
			}else{
				if(!talking||firstMessage){
					doc.insertString(doc.getLength(), who+"\n", doc.getStyle("selfName"));
					talking=true;
					firstMessage=false;
				}
			}
		}catch(BadLocationException e){
			
		}
		String [] parts = text.split("\\s+");
		for( String item : parts ) try {
			URL url = new URL(item);
            showURL(url, other);   
        } catch (MalformedURLException e) {
        	String smileyUrl = getSmileyURL(item);
        	if(smileyUrl!=null){
    			showSmiley(smileyUrl, other);
    		}else{
    			showText(item, other);
    		}
        }
		try{
			doc.insertString(doc.getLength(), "\n", other ? doc.getStyle("otherMessage"): doc.getStyle("selfMessage"));
			doc.setParagraphAttributes(i, doc.getLength()-i, other?doc.getStyle("otherMessage"): doc.getStyle("selfMessage"), false);
		
		}catch(BadLocationException e){
		}
	}
	
	public void showURL(URL url, boolean other){
		try{
			if(other){
				
				MutableAttributeSet linkSet = doc.addStyle("linkSet", (Style) otherMessage);
				linkSet.addAttribute("linkSet", new URLLink(url));
				StyleConstants.setUnderline(doc.getStyle("linkSet"), true);
				StyleConstants.setForeground(doc.getStyle("linkSet"), Color.GREEN);
				MutableAttributeSet linkSet2 = doc.addStyle("linkSet2", (Style) linkSet);
				doc.insertString(doc.getLength(), " ", null);
				for(int p=0;p<url.toString().length();p++)
					doc.insertString(doc.getLength(), url.toString().substring(p, p+1), p%2==0?linkSet2:linkSet);
				doc.insertString(doc.getLength(), " ", null);
			}else{
				
				MutableAttributeSet linkSet = doc.addStyle("linkSet", (Style) selfMessage);
				linkSet.addAttribute("linkSet", new URLLink(url));
				StyleConstants.setUnderline(doc.getStyle("linkSet"), true);
				StyleConstants.setForeground(doc.getStyle("linkSet"), Color.GREEN);
				MutableAttributeSet linkSet2 = doc.addStyle("linkSet2", (Style) linkSet);
				doc.insertString(doc.getLength(), " ", null);
				for(int p=0;p<url.toString().length();p++)
					doc.insertString(doc.getLength(), url.toString().substring(p, p+1), p%2==0?linkSet2:linkSet);
				doc.insertString(doc.getLength(), " ", null);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void showSmiley(String smileyUrl, boolean other){
		try{
			if(other){
				doc.insertString(doc.getLength(), " ", getSmiley(smileyUrl));
				doc.insertString(doc.getLength(), " ", null);
			}else{
				doc.insertString(doc.getLength(), " ", null);
				doc.insertString(doc.getLength(), " ", getSmiley(smileyUrl));
			}
		}catch(BadLocationException e){
			e.printStackTrace();
		}
	}
	
	public void showText(String text, boolean other){
		try{
			if(other){
				for(int p=0;p<text.length();p++)
					doc.insertString(doc.getLength(), text.substring(p, p+1), p%2==0?null:doc.getStyle("otherMessage"));
				doc.insertString(doc.getLength(), " ", null);
			}else{
				doc.insertString(doc.getLength(), " ", null);
				for(int p=0;p<text.length();p++)
					doc.insertString(doc.getLength(), text.substring(p, p+1), p%2==0?null:doc.getStyle("selfMessage"));
				
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public MutableAttributeSet getAttributesetFromImage(ImageIcon image){
		MutableAttributeSet mas = doc.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setIcon(mas, image);
		return mas;
	}
	
	public void showFile(final Message message) {
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						try{
							try{
								if(talking ||firstMessage){
									doc.insertString(doc.getLength(), getWho()+"\n", doc.getStyle("otherName"));
									talking=false;
									firstMessage=false;
								}
							}catch(BadLocationException e){
								
							}
							String extension =  message.getFileName().substring(message.getFileName().lastIndexOf("."), message.getFileName().length());
							if(extension.equals(".jpeg") || extension.equals(".jpg") || extension.equals(".gif") || extension.equals(".png")){
								Image image = new ImageIcon(message.getFile()).getImage();
								ImageIcon resizedImage = new ImageIcon(image.getScaledInstance(280, image.getHeight(null)*280/image.getWidth(null), Image.SCALE_SMOOTH));
								doc.insertString(doc.getLength(), " ", getAttributesetFromImage(resizedImage));
								doc.insertString(doc.getLength(), "\n", doc.getStyle("otherMessage"));
							}
							
							MutableAttributeSet linkSet = doc.addStyle("fileLinkSet", (Style) otherMessage);
							linkSet.addAttribute("fileLinkSet", new FileLink(message));
							StyleConstants.setUnderline(doc.getStyle("fileLinkSet"), true);
							StyleConstants.setForeground(doc.getStyle("fileLinkSet"), Color.GREEN);
							doc.insertString(doc.getLength(), "Click to save:"+(message.getFileName().toString().length()>=26?message.getFileName().substring(0, 15)+"..."+message.getFileName().substring(message.getFileName().lastIndexOf("."), message.getFileName().length()):message.getFileName()), doc.getStyle("fileLinkSet"));
							doc.insertString(doc.getLength(), "\n", doc.getStyle("otherMessage"));
							chatWindow.setCaretPosition(chatWindow.getDocument().getLength());
							
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			);
	}
	
	public void receiveFile(Message message) {
		String path = promptForFolder();
		if(path!=null){
			try {
				Files.write(Paths.get(path+"/"+message.getFileName()), message.getFile(), StandardOpenOption.CREATE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String promptForFolder(){
	    JFileChooser fc = new JFileChooser();
	    fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

	    if( fc.showOpenDialog(chatWindow) == JFileChooser.APPROVE_OPTION )
	    {
	        return fc.getSelectedFile().getAbsolutePath();
	    }

	    return null;
	}
	
	public void showErrorMessage(final String text, final boolean red) {
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						try {
							MutableAttributeSet style = chatWindow.addStyle("message", null);
							if(red) StyleConstants.setForeground(style, Color.RED);
							MutableAttributeSet style2 = chatWindow.addStyle("message", null);
							if(red) StyleConstants.setForeground(style2, Color.RED);
							for(int p=0;p<text.length();p++)
								doc.insertString(doc.getLength(), text.substring(p, p+1), p%2==0?style2:style);
							doc.insertString(doc.getLength(), "\n", null);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
				}
			);
	}
	
	public void setOffline(){
		userText.setEditable(false);
		showErrorMessage(with+" just went offline.", true);
	}
	
	public String getWho(){
		return with;
	}
	
	class URLLink extends AbstractAction
    {
        private URL textLink;

        URLLink(URL textLink)
        {
            this.textLink = textLink;
        }

        protected void execute()
        {
        	Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(textLink.toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void actionPerformed(ActionEvent e)
        {
            execute();
        }
    }
	class FileLink extends AbstractAction
    {
        private Message fileMessage;

        FileLink(Message message)
        {
            this.fileMessage = message;
        }

        protected void execute()
        {
        	receiveFile(fileMessage);
        }

        public void actionPerformed(ActionEvent e)
        {
            execute();
        }
    }

}
