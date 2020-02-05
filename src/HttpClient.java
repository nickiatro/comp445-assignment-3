import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpClient {
	

	public static void getOperation(Socket socket, PrintWriter pw, BufferedReader reader, URL url) throws IOException {
		String resId = url.getPath() + ((url.getQuery() != null) ? url.getQuery() : "") ;
		String version = "HTTP/1.0";
		String output = "";
		
		System.out.println("");
		
		pw.write("GET " + resId + " " + version +"\r\n");
		pw.write("Host: " + socket.getInetAddress().getHostName() + "\r\n\r\n" );
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
	
	public static void postOperation(Socket socket, PrintWriter pw, BufferedReader reader, URL url) throws IOException {
		String resId = url.getHost();
		String version = "HTTP/1.0";
		String data = "";
		String output = "";
		
		System.out.println("");
		
		pw.write("POST " + resId + " " + version +"\r\n");
		pw.write("Host: "+ socket.getInetAddress().getHostName() +" \r\n");
		pw.write("Content-Length: " + data.length() + "\r\n");
		pw.write("Content-Type: application/x-www-form-urlencoded \r\n");
		pw.write("\r\n");
		pw.write(data);
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
	
	public static void main(String[] args) {
		Socket socket = null;
		PrintWriter pw = null;
		BufferedReader reader = null;
		URL url = null;
		String hostName = "";
		int port = 80;
		
		if (!args[0].equals("httpc") || (!args[1].equals("get") && !args[1].equals("post"))) {
			System.exit(1);
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
		
		String method = args[1];
		
		
		if (method.equalsIgnoreCase("get")) {
			try {
				getOperation(socket, pw, reader, url);
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
		}
		else if (method.equalsIgnoreCase("post")) {
			try {
				postOperation(socket, pw, reader, url);
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
		}
	}
	
}
