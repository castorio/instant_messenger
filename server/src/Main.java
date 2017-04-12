
public class Main {

	public static void main(String[] args) {
		String arg1 = args[0];
		if(arg1!=null&&arg1.equals("nogui"))
			new Handler(false).run();
		else
			new Handler(true).run();
		
	}

}
