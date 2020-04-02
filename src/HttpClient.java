import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class HttpClient {
	
	private static boolean verbose = false;
	private static ArrayList<String> headers = new ArrayList<String>();
	private static ArrayList<String> data = new ArrayList<String>();
	private static PrintWriter fileOutput = null;

	private static void getOperation(DatagramChannel channel, ByteArrayOutputStream pw, BufferedReader reader, InetSocketAddress server, InetSocketAddress router) throws IOException {
		String version = "HTTP/1.0";
		String output = "";
		
		System.out.println("");
		
		pw.write(("GET " + "/" + version +"\r\n").getBytes());
		pw.write(("Host: " + server.getHostName() + "\r\n").getBytes());
		if (!headers.isEmpty()) {
			for (int i = 0; i < headers.size(); i++) {
				pw.write((headers.get(i) + "\r\n").getBytes());
			}
		}
		pw.write(("\r\n").getBytes());
		pw.flush();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.getMessage();
		}
		
		Packet packet = new Packet(0, 1L, server.getAddress(), server.getPort(), pw.toByteArray());
		
		ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN);
		buffer.put((byte) packet.getType());
		buffer.putInt((int)packet.getSequenceNumber());
		buffer.put(server.getAddress().getAddress());
		buffer.putShort((short)server.getPort());
		buffer.put(packet.getPayload());
		
		channel.send(buffer, router);

	//	socket.shutdownOutput();
		/*
		if (fileOutput == null) {
			if (verbose == true) {
				String firstLine = reader.readLine(); 
				output = (firstLine != null) ? firstLine : "" ;
				System.out.println(output);
				
				while (output != null) {
					output = reader.readLine();
					System.out.println((output != null ? output : ""));
				}
			}
			else if (verbose == false) {
				String firstLine = reader.readLine(); 
				output = (firstLine != null) ? firstLine : "" ;
				
				while (!output.isEmpty()) {
					output = reader.readLine();
				}
				
				while (output != null) {
					output = reader.readLine();
					System.out.println((output != null ? output : ""));
				}
			}
		}
		if (fileOutput != null) {
			if (verbose == true) {
				String firstLine = reader.readLine(); 
				output = (firstLine != null) ? firstLine : "" ;
				fileOutput.println(output);
				
				while (output != null) {
					output = reader.readLine();
					fileOutput.println((output != null ? output : ""));
				}
			}
			else if (verbose == false) {
				String firstLine = reader.readLine(); 
				output = (firstLine != null) ? firstLine : "" ;
				
				while (!output.isEmpty()) {
					output = reader.readLine();
				}
				
				while (output != null) {
					output = reader.readLine();
					fileOutput.println((output != null ? output : ""));
				}
			}
		}
		if (fileOutput != null) {
			fileOutput.close();
		} */
		//socket.shutdownInput();
	}
	
	private static void postOperation(DatagramSocket socket, ByteArrayOutputStream pw, BufferedReader reader, InetSocketAddress server) throws IOException {
		//String resId = server.getPath() + ((server.getQuery() != null) ? server.getQuery() : "") ;
		String version = "HTTP/1.0";
		String output = "";
		int length = 0;
		
		if (!data.isEmpty()) {
			for (int i = 0; i < data.size(); i++) {
				length += data.get(i).length();
			}
		}
		
		//pw.write("POST " + resId + " " + version +"\r\n");
		pw.write(("Host: " + server.getHostName()  + " \r\n").getBytes());
		pw.write(("Content-Length: " + length + "\r\n").getBytes());
		if (!headers.isEmpty()) {
			for (int i = 0; i < headers.size(); i++) {
				pw.write((headers.get(i) + "\r\n").getBytes());
			}
		}
		pw.write(("\r\n").getBytes());
		if (!data.isEmpty()) {
			for (int i = 0; i < data.size(); i++) {
				pw.write(data.get(i).getBytes());
			}
		}
		pw.flush();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.getMessage();
		}
		
		//socket.shutdownOutput();
		if (fileOutput == null) {
			if (verbose == true) {
				String firstLine = reader.readLine(); 
				output = (firstLine != null) ? firstLine : "" ;
				System.out.println(output);
				
				while (output != null) {
					output = reader.readLine();
					System.out.println((output != null ? output : ""));
				}
			}
			else if (verbose == false) {
				String firstLine = reader.readLine(); 
				output = (firstLine != null) ? firstLine : "" ;
				
				while (!output.isEmpty()) {
					output = reader.readLine();
				}
				
				while (output != null) {
					output = reader.readLine();
					System.out.println((output != null ? output : ""));
				}
			}
		}
		if (fileOutput != null) {
			if (verbose == true) {
				String firstLine = reader.readLine(); 
				output = (firstLine != null) ? firstLine : "" ;
				fileOutput.println(output);
				
				while (output != null) {
					output = reader.readLine();
					fileOutput.println((output != null ? output : ""));
				}
			}
			else if (verbose == false) {
				String firstLine = reader.readLine(); 
				output = (firstLine != null) ? firstLine : "" ;
				
				while (!output.isEmpty()) {
					output = reader.readLine();
				}
				
				while (output != null) {
					output = reader.readLine();
					fileOutput.println((output != null ? output : ""));
				}
			}
		}
		if (fileOutput != null) {
			fileOutput.close();
		}
		//socket.shutdownInput();
	}
	
	public static void main(String[] args) {
		DatagramChannel channel = null;
		BufferedReader reader = null;
		Scanner file = null;
		InetSocketAddress server = null;
		InetSocketAddress router = null;
		ByteArrayOutputStream pw = null;
		
		final int port = 8007;
		final int routerPort = 3000;
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
					"usage: httpc get [-v] [-h key:value] InetAddress\nGet executes a HTTP GET request for a given InetAddress.\n  -v\t\tPrints the detail of the response such as protocol, status,\n and headers.\n  -h key:value\tAssociates headers to HTTP Request with the format\n'key:value'.");
			System.exit(0);
		}
		
		if (args[1].equals("help") && args[2].equals("post") && args.length == 3) {
			System.out.println("httpc help post\r\n" +  
					"usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] InetAddress\nPost executes a HTTP POST request for a given InetAddress with inline data or from\nfile.\n\n  -v\t\t  Prints the detail of the response such as protocol, status,\nand headers.\n  -h key:value\t  Associates headers to HTTP Request with the format\n'key:value'.\n  -d string\t  Associates an inline data to the body HTTP POST request.\n  -f file\t  Associates the content of a file to the body HTTP POST\nrequest.\n\nEither [-d] or [-f] can be used but not both.");
			System.exit(0);
		}
		
		try {
			server = new InetSocketAddress(InetAddress.getByName(args[args.length - 1]), port);
		}
		catch (UnknownHostException e) {
			System.out.println("Bad InetAddress... program will terminate");
			System.exit(1);
		}
		
		try {
			router = new InetSocketAddress(InetAddress.getByName(args[args.length - 1]), routerPort);
		}
		catch (UnknownHostException e) {
			System.out.println("Bad InetAddress... program will terminate");
			System.exit(1);
		}
		
		
		//hostName = server.getHostName();
		
		try {
			channel = DatagramChannel.open();
		}
		//catch(UnknownHostException e1) {
			//System.err.println("Unknown host... Program will terminate");
			//System.exit(1);
		//}
		catch(IOException e2) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}
		

		pw = new ByteArrayOutputStream();
		/*
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch (IOException e) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}*/
		
		if (args[1].equalsIgnoreCase("get")) {
			try {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equals("-v")) {
						verbose = true;
					}
					if (args[i].equals("-o")) {
						try {
							fileOutput = new PrintWriter(new File(args[i+1]));
						}
						catch (IOException e) {
							System.err.println("I/O error... Program will terminate");
							System.exit(1);
						}
					}
					if (args[i].equals("-h")) {
						for (int j = i + 1; j < args.length; j++) { 
							if (args[j].matches("\\S+:\\S+") && !args[j].equals(server.toString())) {
								headers.add(args[j]);
							}
						}
					}
				}	
				getOperation(channel, pw, reader, server, router);
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
					if (args[i].equals("-o")) {
						try {
							fileOutput = new PrintWriter(new File(args[i+1]));
						}
						catch (IOException e) {
							System.err.println("I/O error... Program will terminate");
							System.exit(1);
						}
					}
					if (args[i].equals("-h")) {
						for (int j = i + 1; j < args.length; j++) { 
							if (args[j].matches("\\S+:\\S+") && !args[j].equals(server.toString())) {
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
					postOperation(channel.socket(), pw, reader, server);
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
		}
	}
	
}
