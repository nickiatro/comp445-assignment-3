import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;

public class HttpClient {

	public void getOperation(Socket socket, PrintWriter pw, BufferedReader reader) {
	
	}
	
	public void postOperation() {
		
	}
	
	public static void main(String[] args) {
		Socket socket = null;
		PrintWriter pw = null;
		BufferedReader reader = null;
		Scanner keyboard = new Scanner(System.in);
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
	}
	
}
