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

	public static void getOperation(Socket socket, PrintWriter pw, BufferedReader reader) {
		String resId = "";
		String version = "";
		
		System.out.print("Please specify the resource ID: ");
		resId = keyboard.nextLine();
		
		while (resId.equals("")) {
			System.err.println("Resource ID is required...");
			System.out.print("Please specify the resource ID: ");
			resId = keyboard.nextLine();
		}
		
		System.out.println("Please specify the HTTP version in the format HTTP/X.X, where X is a digit: ");
		version = keyboard.nextLine();
		
		while (version.equals("") || !version.matches("HTTP\\/\\d.\\d")) {
			System.err.println("HTTP Version is required...");
			System.out.print("Please specify the HTTP version in the format HTTP/X.X, where X is a digit: ");
			version = keyboard.nextLine();
		}
		
		
	}
	
	public static void postOperation(Socket socket, PrintWriter pw, BufferedReader reader) {
		
	}
	
	public static void main(String[] args) {
		Socket socket = null;
		PrintWriter pw = null;
		BufferedReader reader = null;
		String hostName = "";
		int port = 0;
		
		System.out.print("Please enter a host name: ");
		hostName = keyboard.nextLine();
		
		while (hostName.equals("")) {
			System.err.print("\nHost name is required.");
			System.out.print("\nPlease enter a host name: ");
			hostName = keyboard.nextLine();
		}
		
		System.out.print("Would you like to use the default value 80? [y/n]: ");
		String str = keyboard.nextLine();
		
		while (!str.equalsIgnoreCase("n") && !str.equalsIgnoreCase("y")) {
			System.err.println("\nPlease enter y or n to continue");
			System.out.print("Would you like to use the default value 80? [y/n]: ");
			str = keyboard.nextLine();
		}
		
		if (str.equalsIgnoreCase("n")) {
			System.out.print("Please enter a port number: ");
			try{
				port = keyboard.nextInt();
			}
			catch (InputMismatchException e) {
				System.err.println("Input not an integer... Program will terminate");
				System.exit(1);
			}
		}
		else if (str.equalsIgnoreCase("y")) {
			port = 80;
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
			getOperation(socket, pw, reader);
		}
		else if (method.equalsIgnoreCase("post")) {
			postOperation(socket, pw, reader);
		}
	}
	
}
