import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpClient {
	
	private static boolean verbose = false;
	private static ArrayList<String> headers = new ArrayList<String>();
	private static ArrayList<String> data = new ArrayList<String>();

	private static void getOperation(Socket socket, PrintWriter pw, BufferedReader reader, URL url) throws IOException {
		String resId = url.getPath() + ((url.getQuery() != null) ? url.getQuery() : "") ;
		String version = "HTTP/1.0";
		String output = "";
		
		System.out.println("");
		
		pw.write("GET " + resId + " " + version +"\r\n");
		pw.write("Host: " + url.getHost() + "\r\n");
		if (!headers.isEmpty()) {
			for (int i = 0; i < headers.size(); i++) {
				pw.write(headers.get(i) + "\r\n");
			}
		}
		pw.write("\r\n");
		pw.flush();
		socket.shutdownOutput();
		
		if (verbose == true) {
			output = reader.readLine();
			System.out.println(output);
			
			while (output != null) {
				output = reader.readLine();
				System.out.println(output);
			}
		}
		socket.shutdownInput();
	}
	
	private static void postOperation(Socket socket, PrintWriter pw, BufferedReader reader, URL url) throws IOException {
		String resId = url.getPath() + ((url.getQuery() != null) ? url.getQuery() : "") ;
		String version = "HTTP/1.0";
		String output = "";
		int length = 0;
		
		if (!data.isEmpty()) {
			for (int i = 0; i < data.size(); i++) {
				length += data.get(i).length();
			}
		}
		
		pw.write("POST " + resId + " " + version +"\r\n");
		pw.write("Host: " + url.getHost()  + " \r\n");
		pw.write("Content-Length: " + length + "\r\n");
		if (!headers.isEmpty()) {
			for (int i = 0; i < headers.size(); i++) {
				pw.write(headers.get(i) + "\r\n");
			}
		}
		pw.write("\r\n");
		if (!data.isEmpty()) {
			for (int i = 0; i < data.size(); i++) {
				pw.write(data.get(i));
			}
		}
		pw.flush();
		socket.shutdownOutput();
		
		if (verbose == true) {
			output = reader.readLine();
			System.out.println(output);
			
			while (output != null) {
				output = reader.readLine();
				System.out.println(output);
			}
		}
		socket.shutdownInput();
	}
	
	public static void main(String[] args) {
		Socket socket = null;
		PrintWriter pw = null;
		BufferedReader reader = null;
		Scanner file = null;
		URL url = null;
		String hostName = "";
		int port = 80;
		String fileContents = "";
		
		if (!args[0].equals("httpc") || (!args[1].equals("get") && !args[1].equals("post") && !args[1].equals("help"))) {
			System.exit(1);
		}
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-d")) {
				for (int j = 0; j < args.length; j++) {
					if (args[j].equals("-f")) {
						System.err.println("Either [-d] or [-f] can be used but not both.\nProgram will terminate...");
						System.exit(1);	
					}	
				}
			}
			else if (args[i].equals("-f")) {
				for (int j = 0; j < args.length; j++) {
					if (args[j].equals("-d")) {
						System.err.println("Either [-d] or [-f] can be used but not both.\nProgram will terminate...");
						System.exit(1);	
					}	
				}
			}
		}	
		
		if (args[1].equals("help") && args.length == 2) {
			System.out.println("httpc is a curl-like application but supports HTTP protocol only.\nUsage:\n    httpc command [arguments]\nThe commands are:\n    get \texecutes a HTTP GET request and prints the response.\n    post \texecutes a HTTP POST request and prints the response.\n    help\tprints this screen.\n\nUse \"httpc help [command]\" for more information about a command.");
			System.exit(0);
		}
		
		if (args[1].equals("help") && args[2].equals("get") && args.length == 3) {
			System.out.println("httpc help get\r\n" + 
					"usage: httpc get [-v] [-h key:value] URL\nGet executes a HTTP GET request for a given URL.\n  -v\t\tPrints the detail of the response such as protocol, status,\n and headers.\n  -h key:value\tAssociates headers to HTTP Request with the format\n'key:value'.");
			System.exit(0);
		}
		
		if (args[1].equals("help") && args[2].equals("post") && args.length == 3) {
			System.out.println("httpc help post\r\n" +  
					"usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\nPost executes a HTTP POST request for a given URL with inline data or from\nfile.\n\n  -v\t\t  Prints the detail of the response such as protocol, status,\nand headers.\n  -h key:value\t  Associates headers to HTTP Request with the format\n'key:value'.\n  -d string\t  Associates an inline data to the body HTTP POST request.\n  -f file\t  Associates the content of a file to the body HTTP POST\nrequest.\n\nEither [-d] or [-f] can be used but not both.");
			System.exit(0);
		}
		try {
			url = new URL(args[args.length - 1]);
		} catch (MalformedURLException e) {
			System.out.println("Bad URL... program will terminate");
			System.exit(1);
		}
		
		hostName = url.getHost();
		
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
		
		if (args[1].equalsIgnoreCase("get")) {
			try {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equals("-v")) {
						verbose = true;
					}
					if (args[i].equals("-h")) {
						for (int j = i + 1; j < args.length; j++) { 
							if (args[j].matches(".:.")) {
								headers.add(args[j]);
							}
						}
					}
				}	
				getOperation(socket, pw, reader, url);
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
		}
		else if (args[1].equalsIgnoreCase("post")) {
			try {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equals("-v")) {
						verbose = true;
					}
					if (args[i].equals("-h")) {
						for (int j = i + 1; j < args.length; j++) { 
							if (args[j].matches(".:.")) {
								headers.add(args[j]);
							}
						}
					}
					if (args[i].equals("-d")) {
						for (int j = i + 1; j < args.length; j++) {
							if (args[j].matches("\\{\"\\S+\":\\s.\\}") || args[j].matches("\\{\"\\S+\":\\s\".\"\\}")) {
								data.add(args[j]);
							}
						}
					}
					if (args[i].equals("-f")) {
						try {
							file = new Scanner(new File(args[i+1]));
						}
						catch (IOException e) {
							System.err.println("I/O error... Program will terminate");
							System.exit(1);
						}
						
						while (file.hasNext()) {
							fileContents += file.next();
						}
						
						StringTokenizer tokens = new StringTokenizer(fileContents, ",");
						
						while(tokens.hasMoreTokens()) {
							data.add(tokens.nextToken());
						}
					}
				}
					postOperation(socket, pw, reader, url);
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
		}
	}
	
}
