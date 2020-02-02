import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;

public class HttpClient {

	public void getOperation(Socket socket, PrintWriter writer, BufferedReader reader) {
	
	}
	
	public void postOperation() {
		
	}
	
	public static void main(String[] args) {
		Socket socket = null;
		PrintWriter pw = null;
		BufferedReader reader = null;
		Scanner keyboard = new Scanner(System.in);
		String hostName = "httpbin.org";
		int port = 80;
		
		try {
			socket = new Socket(hostName, port);
		}
		catch(UnknownHostException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		}
		catch(IOException e2) {
			System.err.println(e2.getMessage());
			System.exit(1);
		}
	}
	
}
