
import javax.swing.UIManager;

public class Main {
	
	public static final String host = "86.84.8.122";
	public static final int port = 1050;
	
	public static void main(String[] args){
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiTeX");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
		new Handler(host, port);
	}
}
