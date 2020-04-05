import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;

import static java.nio.channels.SelectionKey.OP_READ;

public class HttpClient {
	
	private static boolean verbose = false;
	private static ArrayList<String> headers = new ArrayList<String>();
	private static ArrayList<String> data = new ArrayList<String>();
	private static PrintWriter fileOutput = null;
	
	public static long firstSeqNum = 0L;
	public static long sequenceNumber = 0L;
	public static long ackNumber = 0L;
	public static int windowSize;
	
	public static ArrayList<Timer> timers = new ArrayList<Timer>();
	public static ArrayList<Packet> packets = new ArrayList<Packet>();
	public static boolean isSender = true;
	public static boolean isHandShaking = true;
	public static int index = 0; 
	
	public static InetSocketAddress server = null;
	public static InetSocketAddress router = null;
	public static DatagramChannel channel = null;
	
	public static BufferedReader reader = null;
	public static Scanner file = null;
	public static ByteArrayOutputStream pw = null;
	
	//TYPES: DATA = 0, ACK = 1, SYN = 2, SYN-ACK = 3
	
	private static void getOperation() throws IOException {
		String version = "HTTP/1.0";
		String output = "";
		
		System.out.println("");
		
		pw.write(("GET " + "/README.md " + version +"\r\n").getBytes());
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
		
		if (isHandShaking){
			
			timers.add(new Timer());
			timers.add(new Timer());
			Packet syn = new Packet(2, 0L, server.getAddress(), server.getPort(), new byte[11]);
			Packet ack = new Packet(1, 1L, server.getAddress(), server.getPort(), new byte[11]);
			packets.add(syn);
			packets.add(ack);
			
			timers.get(0).scheduleAtFixedRate(new ClientTimerTask(0), 0, 5000); //start with syn packet
			
			while (isHandShaking) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.getMessage();
				}
			}
			System.exit(0);
		}
		
		byte[] array = pw.toByteArray();
		int size = array.length;
		int chunks = 0;
		
		for (int i = 10; i > 0; i--) {
			if (size % i == 0) {
				chunks = i;
				break;
			}
		}
		
		int chunk = size / chunks;

		for (int i = 0; i <= array.length - chunk; i += chunk) {
			Packet packet = new Packet(0, 0, server.getAddress(), server.getPort(), Arrays.copyOfRange(array, i, (i + chunk)));
			packets.add(packet);
		}
		
		if (packets.size() % 2 == 0)
			windowSize = (int)(packets.size() / 2);
		else
			windowSize = (int)(packets.size() / 2) + 1;
		long seqNum = 0L;
		
		for (int i = 0; i < packets.size(); i++) {
			if (seqNum == 2 * windowSize - 1) {
				seqNum = 0;
			}
			packets.get(i).setSequenceNumber(seqNum);
			seqNum++;
		}
		
		while (isSender) {
		
         }
		
		while (!isSender) {
			
		}
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
	
	private static void postOperation() throws IOException {
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
		
		Packet packet = new Packet(0, sequenceNumber, server.getAddress(), server.getPort(), pw.toByteArray());
		
		channel.send(packet.toBuffer(), router);
		
		channel.configureBlocking(false);
		Selector selector = Selector.open();
		channel.register(selector, OP_READ);
		selector.select(5000);
		
		Set<SelectionKey> keys = selector.selectedKeys();
		
        if(keys.isEmpty()){
            System.err.println("No response after timeout");
            return;
        }
		
		 ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN);
         channel.receive(byteBuffer);
         byteBuffer.flip();
         Packet response = Packet.fromBuffer(byteBuffer);
         
         String str = new String(response.getPayload(), "UTF-8");
         
         System.out.println(str);
         
		//socket.shutdownOutput();
		/*if (fileOutput == null) {
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
		//socket.shutdownInput();*/
	}
	
	public static void main(String[] args) {
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
				getOperation();
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
					postOperation();
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
		}
	}
	
}
