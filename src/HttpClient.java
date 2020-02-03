import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpClient {
	
	private static Scanner keyboard = new Scanner(System.in);

	public static void getOperation(Socket socket, PrintWriter pw, BufferedReader reader) throws IOException {
		String resId = "";
		String version = "";
		String output = "";
		
		System.out.print("Please specify the resource ID: ");
		resId = keyboard.nextLine();
		
		while (resId.equals("")) {
			System.err.println("Resource ID is required...");
			System.out.print("Please specify the resource ID: ");
			resId = keyboard.nextLine();
		}
		
		System.out.print("Please specify the HTTP version in the format HTTP/X.X, where X is a digit: ");
		version = keyboard.nextLine();
		
		while (version.equals("") || !version.matches("HTTP\\/\\d.\\d")) {
			System.err.println("HTTP Version is required...");
			System.out.print("Please specify the HTTP version in the format HTTP/X.X, where X is a digit: ");
			version = keyboard.nextLine();
		}
		
		System.out.println("");
		
		pw.write("GET " + resId + " " + version +"\r\n");
		pw.write("\r\n");
		pw.flush();
		socket.shutdownOutput();
		
		output = reader.readLine();
		System.out.println(output);
		
		while (output != null) {
			output = reader.readLine();
			System.out.println(output);
		}
		
		socket.shutdownInput();
	}
	
	public static void postOperation(Socket socket, PrintWriter pw, BufferedReader reader) {
		
	}
	
	public static void main(String[] args) {
		Socket socket = null;
		PrintWriter pw = null;
		BufferedReader reader = null;
		String hostName = "";
		int port = 80;
		
		System.out.print("Please enter a host name: ");
		hostName = keyboard.nextLine();
		
		while (hostName.equals("")) {
			System.err.print("\nHost name is required.");
			System.out.print("\nPlease enter a host name: ");
			hostName = keyboard.nextLine();
		}
		
		try {
			socket = new Socket(hostName, port);
		}
		catch(UnknownHostException e1) {
			System.err.println("Unknown host... Program will terminate");
			System.exit(1);
		}
		catch(IOException e2) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}
		
		try {
			pw = new PrintWriter(socket.getOutputStream());
		}
		catch (IOException e) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}
		
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch (IOException e) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}
		
		String method = "";
		System.out.print("GET or POST?: ");
		method = keyboard.nextLine();
		
		while (!method.equalsIgnoreCase("get") && !method.equalsIgnoreCase("post")) {
			System.err.println("\nPlease enter GET or POST to continue");
			System.out.print("GET OR POST?: ");
			method = keyboard.nextLine();
		} 
		
		if (method.equalsIgnoreCase("get")) {
			try {
				getOperation(socket, pw, reader);
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
		}
		else if (method.equalsIgnoreCase("post")) {
			postOperation(socket, pw, reader);
		}
	}
	
}
