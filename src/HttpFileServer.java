import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class HttpFileServer {
	public static void main(String[] args) {
		
		ServerSocket serverSocket = null;
		Socket socket = null;
		PrintWriter pw = null;
		PrintWriter pwPost = null;
		BufferedReader reader = null;
		Scanner fileScanner = null;
		
		boolean debugMsg = false;
		String directory = "./";
		int port = 8080;
		
		boolean ok = true;
		boolean notFound = false;
		boolean forbidden = false;
		
		if (!args[0].equals("httpfs")) {
				System.exit(1);
		}
		
		if (args.length == 2 && args[0].equals("httpfs") && args[1].equals("help")) {
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
			if (args[i].equals("-p")) {
				port = Integer.parseInt(args[i + 1]);
			}
			if (args[i].equals("-d")) {
				directory = args[i + 1];
			}
		}
		
		try {
			serverSocket = new ServerSocket(port);
		}
		catch(IOException e) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}
		
		while (true) {
			ok = true;
			notFound = false;
			forbidden = false;
			
			if (directory.contains("..") || !directory.startsWith("./")) {
				forbidden = true;
				ok = false;
				notFound = false;
			}
			
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
			ArrayList<String> lines = new ArrayList<String>();
			
			
			try {
				str = reader.readLine();
				lines.add(str);
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");					
				System.exit(1);
			}
				
			while (str != null && str.length() > 0) {
				try {
					str = reader.readLine();
					
					if (str != null) {
						lines.add(str);
					}
				} 
				catch (IOException e) {
					System.err.println("I/O error... Program will terminate");
					System.exit(1);
				}
				
			}
			
			if (lines.get(0) == null) {
				continue;
			}

			StringTokenizer tokens = null;
			
			if (lines.get(0) != null) {
				tokens = new StringTokenizer(lines.get(0), " ");
				String method = tokens.nextToken();
				String item = tokens.nextToken();
				
				ArrayList<String> fileContents = new ArrayList<String>();
				
				if (method.equals("GET") && !item.equals("/") && !item.isEmpty()) {
					File file = new File(directory + item);
					try {
						fileScanner = new Scanner(new FileInputStream(file));
						
						fileContents.add(fileScanner.nextLine());
						
						while (fileScanner.hasNextLine()) {
							fileContents.add(fileScanner.nextLine());
						}
						
						fileScanner.close();
						
					} catch (FileNotFoundException e1) {
						notFound = true;
						ok = false;
					}
					catch (NoSuchElementException e2) {
						notFound = true;
						ok = false;
					}
					
				}
				
				if (ok == true) {
					pw.println("HTTP/1.0 200 OK");
					pw.println("Content-Type: text/html; charset=utf-8");
					pw.println("Server: COMP 445 Assignment #2 Server");
					pw.println("");
				}
				else if (notFound == true) {
					pw.println("HTTP/1.0 404 NOT FOUND");
					pw.println("Content-Type: text/html; charset=utf-8");
					pw.println("Server: COMP 445 Assignment #2 Server");
					pw.println("");
					pw.println("Error 404: Not Found");
				}
				else if (forbidden == true) {
					pw.println("HTTP/1.0 403 FORBIDDEN");
					pw.println("Content-Type: text/html; charset=utf-8");
					pw.println("Server: COMP 445 Assignment #2 Server");
					pw.println("");
					pw.println("Error 403: Forbidden");
				}
				
				if (method.equals("GET") && ok == true && item.equals("/")) {
					File folder = new File(directory);
					File[] filesInFolder = folder.listFiles();
					
					pw.println("<H1>List of Files in Directory: " + directory +"</H1>");
					
					pw.println("<ul>");
					for (File file : filesInFolder) {
						if (file.isFile()) {
							pw.println("<li>" + file.getName() + "</li>");
						}
					}
					pw.println("</ul>");
				}
				else if (method.equals("GET") && !item.equals("/") && !item.isEmpty()) {
					for (String fileLine : fileContents) {
						pw.println(fileLine);
					}
				}
				
				else if (method.equals("POST")) {
					try {
						str = reader.readLine();
						lines.add(str);
					}
					catch (IOException e) {
						System.err.println("I/O error... Program will terminate");					
						System.exit(1);
					}
					
					while (str != null && str.length() > 0) {
						try {
							str = reader.readLine();
							
							if (str != null) {
								lines.add(str);
							}
						} 
						catch (IOException e) {
							System.err.println("I/O error... Program will terminate");
							System.exit(1);
						}
						
					}
					
					File file = new File(directory + item);
					try {
						pwPost = new PrintWriter(file);
						
					} catch (FileNotFoundException e1) {
						notFound = true;
						ok = false;
					}
					catch (NoSuchElementException e2) {
						notFound = true;
						ok = false;
					}
					
					for (int i = 0; i < lines.size(); i++) {
						if (lines.get(i).equals("")) {
							for (int j = i + 1; j < lines.size(); j++) {
								pwPost.println(lines.get(j));
							}
							break;
						}
					}
					
					pwPost.close();
					
				}
			}
			
			if (debugMsg == true) {
				for (String line : lines) {
					System.out.println(line);
				}
			}
			
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
