import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

public class HttpFileServer {
	public static void main(String[] args) {
		
		ServerSocket serverSocket = null;
		Socket socket = null;
		PrintWriter pw = null;
		BufferedReader reader = null;
		boolean debugMsg = false;
		
		if (!args[0].equals("httpfs")) {
				System.exit(1);
		}
		
		if (args[0].equals("httpfs") && args[1].equals("help")) {
			System.out.println("httpfs is a simple file server.\n");
			System.out.println("usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]\n\n");
			System.out.println("   -v   Prints debugging messages.\n");
			System.out.println("   -p   Specifies the port number that the server will listen and serve at.\r\n" + 
					"\n        Default is 8080.\r\n" + 
					"\n");
			System.out.println("   -d   Specifies the directory that the server will use to read/write\r\n" + 
					"requested files. Default is the current directory when launching the\r\n" + 
					"application.");
			System.exit(1);
	}
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-v")) {
				debugMsg = true;
			}
		}
		
		try {
			serverSocket = new ServerSocket(8080);
		}
		catch(IOException e) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}
		
		while (true) {
			try {
				socket = serverSocket.accept();
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
			
			try {
				pw = new PrintWriter(socket.getOutputStream());
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
			
			String str = "";
			if (debugMsg == true) {
				try {
					str = reader.readLine();
				}
				catch (IOException e) {
					System.err.println("I/O error... Program will terminate");
					System.exit(1);
					}
				
				while (!str.isEmpty()) {
					System.out.println(str);
					try {
						str = reader.readLine();
					} catch (IOException e) {
						System.err.println("I/O error... Program will terminate");
						System.exit(1);
					}
				
				}
			}
			pw.println("HTTP/1.0 200 OK");
			pw.println("Content-Type: text/html; charset=utf-8");
			pw.println("Server: COMP 445 Assignment #2 Server");
			pw.println("");
			pw.println("<H1>HELLO</H1>");
			
			pw.close();
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
		}
		
	}
}
