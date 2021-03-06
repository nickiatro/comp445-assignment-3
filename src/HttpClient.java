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
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;

import static java.nio.channels.SelectionKey.OP_READ;

public class HttpClient {
	
	private static String resId = "";
	private static long timeout = 5000L;	
	
	//TYPES: DATA = 0, ACK = 1, SYN = 2, SYN-ACK = 3, FIN = 4, FIN-ACK = 5
	
	public static int clientPort() {
		int port = 0;
		Scanner scanner = null;
		File file = new File("client-port.txt");
		
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		port = scanner.nextInt();
		scanner.close();
		return port;
	}
	
	public static boolean isUnique(Packet packet) {
		for (int i = 0; i < Client.getInstance().getPackets().size(); i++)
		{
			if (packet.getSequenceNumber() == Client.getInstance().getPackets().get(i).getSequenceNumber()) {
				return false;
			}
		}
		
		return true;
	}
	
	public static void packetsInOrder() {
		ArrayList<Packet> temp = new ArrayList<Packet>();
		for (int i = 0; i < Client.getInstance().getPackets().size(); i++) {
			temp.add(Client.getInstance().getPackets().get(i));
		}
		Client.getInstance().getPackets().clear();
		for (int i = 0; i < temp.size(); i++) {
			for (int j = 0; j < temp.size(); j++) {
				if (i == temp.get(j).getSequenceNumber()) {
					Client.getInstance().getPackets().add(temp.get(j));
				}
			}
		}
	}
	
	private static void getOperation() throws IOException {
		String version = "HTTP/1.0";
		String output = "";
		
		Client.getInstance().getPw().write(("GET " + resId + " " + version +"\r\n").getBytes());
		Client.getInstance().getPw().write(("Host: " + Client.getInstance().getServer().getHostName() + "\r\n").getBytes());
		
		if (!Client.getInstance().getHeaders().isEmpty()) {
			for (int i = 0; i < Client.getInstance().getHeaders().size(); i++) {
				Client.getInstance().getPw().write((Client.getInstance().getHeaders().get(i) + "\r\n").getBytes());
			}
		}
		Client.getInstance().getPw().write(("\r\n").getBytes());
		Client.getInstance().getPw().flush();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.getMessage();
		}
		
		if (Client.getInstance().isHandShaking()){
			
			Client.getInstance().getTimers().add(new Timer());
			Client.getInstance().getTimers().add(new Timer());
			Packet syn = new Packet(2, 0L, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), new byte[11]);
			Packet ack = new Packet(1, 1L, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), new byte[11]);
			Client.getInstance().getPackets().add(syn);
			Client.getInstance().getPackets().add(ack);
			
			Client.getInstance().getTimers().get(0).scheduleAtFixedRate(new ClientTimerTask(0), 0, timeout); //start with syn packet
			
			while (Client.getInstance().isHandShaking()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.getMessage();
				}
			}
		}
		
		Client.getInstance().setHandShaking(false);
		
		Client.getInstance().getPackets().clear();
		Client.getInstance().getTimers().clear();
		
		Client.getInstance().setSender(true);

		byte[] array = Client.getInstance().getPw().toByteArray();
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
			Packet packet = new Packet(0, 0, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), Arrays.copyOfRange(array, i, (i + chunk)));
			Client.getInstance().getPackets().add(packet);
		}
		
		Client.getInstance().setWindowSize((int)(Client.getInstance().getPackets().size() / 2));
		
		for (int i = 0; i < Client.getInstance().getPackets().size(); i++) {
			Client.getInstance().getPackets().get(i).setSequenceNumber(Long.parseLong(new Integer(i).toString()));
		}
		
		
		if (Client.getInstance().isSender()) {
			for (int i = 0; i < Client.getInstance().getPackets().size(); i++) {
				Client.getInstance().getTimers().add(new Timer());
				
				try {
					Thread.sleep(timeout / 2);
				} catch (InterruptedException e) {
					e.getMessage();
				}
				
				Client.getInstance().getTimers().get(i).scheduleAtFixedRate(new ClientTimerTask(i), 0, timeout);
			}
			
			while (Client.getInstance().isSender()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.getMessage();
				}
				for (int i = 0; i < Client.getInstance().getPackets().size(); i++) {
					if (!Client.getInstance().getPackets().get(i).isAck())
						break;
					else if (i ==  Client.getInstance().getPackets().size() - 1 && Client.getInstance().getPackets().get(i).isAck()) {
						Client.getInstance().setSender(false);
						Client.getInstance().setReceiver(true);
					}
				}
			}
			
		}
		
		Client.getInstance().getPackets().clear();
		Client.getInstance().getTimers().clear();
		
		
		//Client.getInstance().setReceiver(true);
		
		Client.getInstance().getTimers().add(new Timer());
		Client.getInstance().getTimers().get(0).schedule(new ClientTimerTask(0), 0);
		
		while (Client.getInstance().isReceiver()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.getMessage();
			}
		}
		
		String str = "";
		
		packetsInOrder();
		
		if (Client.getInstance().isVerbose()) {
			for (int i = 0; i < Client.getInstance().getPackets().size(); i++) {
				try {
					str += new String(Client.getInstance().getPackets().get(i).getPayload(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		else if (!Client.getInstance().isVerbose()) {
			for (int i = 1; i < Client.getInstance().getPackets().size(); i++) {
				try {
					str += new String(Client.getInstance().getPackets().get(i).getPayload(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println(str);
		Client.getInstance().getPackets().clear();
		Client.getInstance().getTimers().clear();
		
		if (Client.getInstance().isConnectionTermination()){
			
			Client.getInstance().getTimers().add(new Timer());
			Client.getInstance().getTimers().add(new Timer());
			Packet fin = new Packet(4, 0L, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), new byte[11]);
			Packet ack = new Packet(1, 1L, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), new byte[11]);
			Client.getInstance().getPackets().add(fin);
			Client.getInstance().getPackets().add(ack);
			
			Client.getInstance().getTimers().get(0).scheduleAtFixedRate(new ClientTimerTask(0), 0, timeout); //start with fin packet
			
			while (Client.getInstance().isConnectionTermination()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.getMessage();
				}
			}
			
			System.exit(0);
		}
		
	}
	
	private static void postOperation() throws IOException {
		String version = "HTTP/1.0";
		String output = "";
		int length = 0;
		
		if (!Client.getInstance().getData().isEmpty()) {
			for (int i = 0; i < Client.getInstance().getData().size(); i++) {
				length += Client.getInstance().getData().get(i).length();
			}
		}
		
		Client.getInstance().getPw().write(("POST " + resId +" " + version +"\r\n").getBytes());
		Client.getInstance().getPw().write(("Host: " + Client.getInstance().getServer().getHostName()  + " \r\n").getBytes());
		Client.getInstance().getPw().write(("Content-Length: " + length + "\r\n").getBytes());
		if (!Client.getInstance().getHeaders().isEmpty()) {
			for (int i = 0; i < Client.getInstance().getHeaders().size(); i++) {
				Client.getInstance().getPw().write((Client.getInstance().getHeaders().get(i) + "\r\n").getBytes());
			}
		}
		Client.getInstance().getPw().write(("\r\n").getBytes());
		if (!Client.getInstance().getData().isEmpty()) {
			for (int i = 0; i < Client.getInstance().getData().size(); i++) {
				Client.getInstance().getPw().write(Client.getInstance().getData().get(i).getBytes());
			}
		}
		Client.getInstance().getPw().flush();
		
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.getMessage();
		}
		
		if (Client.getInstance().isHandShaking()){
			
			Client.getInstance().getTimers().add(new Timer());
			Client.getInstance().getTimers().add(new Timer());
			Packet syn = new Packet(2, 0L, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), new byte[11]);
			Packet ack = new Packet(1, 1L, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), new byte[11]);
			Client.getInstance().getPackets().add(syn);
			Client.getInstance().getPackets().add(ack);
			
			Client.getInstance().getTimers().get(0).scheduleAtFixedRate(new ClientTimerTask(0), 0, timeout); //start with syn packet
			
			while (Client.getInstance().isHandShaking()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.getMessage();
				}
			}
		}
		
		Client.getInstance().setHandShaking(false);
		
		Client.getInstance().getPackets().clear();
		Client.getInstance().getTimers().clear();
		
		Client.getInstance().setSender(true);

		byte[] array = Client.getInstance().getPw().toByteArray();
		int size = array.length;
		
			int chunks = 0;
			
			if (size <= 1013) {
				for (int i = 10; i > 0; i--) {
					if (size % i == 0) {
						chunks = i;
						break;
					}
				}
			}
			else {
				chunks = (int) Math.ceil((double)size / (double)1013);
			}
		
		int chunk = size / chunks;
			
		for (int i = 0; i <= array.length - chunk; i += chunk) {
			Packet packet = new Packet(0, 0, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), Arrays.copyOfRange(array, i, (i + chunk)));
			Client.getInstance().getPackets().add(packet);
		}
		
		Client.getInstance().setWindowSize((int)(Client.getInstance().getPackets().size() / 2));
		
		for (int i = 0; i < Client.getInstance().getPackets().size(); i++) {
			Client.getInstance().getPackets().get(i).setSequenceNumber(Long.parseLong(new Integer(i).toString()));
		}
		
		
		if (Client.getInstance().isSender()) {
			for (int i = 0; i < Client.getInstance().getPackets().size(); i++) {
				Client.getInstance().getTimers().add(new Timer());
				
				try {
					Thread.sleep(timeout / 2);
				} catch (InterruptedException e) {
					e.getMessage();
				}
				
				Client.getInstance().getTimers().get(i).scheduleAtFixedRate(new ClientTimerTask(i), 0, timeout);
			}
			ArrayList<Packet> packets = Client.getInstance().getPackets();
			ArrayList<Timer> timers = Client.getInstance().getTimers();
			while (Client.getInstance().isSender()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.getMessage();
				}
				for (int i = 0; i < Client.getInstance().getPackets().size(); i++) {
					if (!Client.getInstance().getPackets().get(i).isAck())
						break;
					else if (i ==  Client.getInstance().getPackets().size() - 1 && Client.getInstance().getPackets().get(i).isAck()) {
						Client.getInstance().setSender(false);
						Client.getInstance().setReceiver(true);
					}
				}
			}
			
		}
		
		Client.getInstance().getPackets().clear();
		Client.getInstance().getTimers().clear();
		
		
		//Client.getInstance().setReceiver(true);
		
		Client.getInstance().getTimers().add(new Timer());
		Client.getInstance().getTimers().get(0).schedule(new ClientTimerTask(0), 0);
		
		while (Client.getInstance().isReceiver()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.getMessage();
			}
		}
		
		String str = "";
		
		if (Client.getInstance().isVerbose()) {
			for (int i = 0; i < Client.getInstance().getPackets().size(); i++) {
				try {
					str += new String(Client.getInstance().getPackets().get(i).getPayload(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		else if (!Client.getInstance().isVerbose()) {
			for (int i = 1; i < Client.getInstance().getPackets().size(); i++) {
				try {
					str += new String(Client.getInstance().getPackets().get(i).getPayload(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println(str);
		Client.getInstance().getPackets().clear();
		Client.getInstance().getTimers().clear();
		
		if (Client.getInstance().isConnectionTermination()){
			
			Client.getInstance().getTimers().add(new Timer());
			Client.getInstance().getTimers().add(new Timer());
			Packet fin = new Packet(4, 0L, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), new byte[11]);
			Packet ack = new Packet(1, 1L, Client.getInstance().getServer().getAddress(), Client.getInstance().getServer().getPort(), new byte[11]);
			Client.getInstance().getPackets().add(fin);
			Client.getInstance().getPackets().add(ack);
			
			Client.getInstance().getTimers().get(0).scheduleAtFixedRate(new ClientTimerTask(0), 0, timeout); //start with fin packet
			
			while (Client.getInstance().isConnectionTermination()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.getMessage();
				}
			}
			System.exit(0);
		}
		
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
					"usage: httpc get [-v] [-h key:value] InetAddress\nGet executes a HTTP GET request for a given InetAddress.\n  -v\t\tPrints the detail of the response such as protocol, status,\n and Client.getInstance().getHeaders().\n  -h key:value\tAssociates Client.getInstance().getHeaders() to HTTP Request with the format\n'key:value'.");
			System.exit(0);
		}
		
		if (args[1].equals("help") && args[2].equals("post") && args.length == 3) {
			System.out.println("httpc help post\r\n" +  
					"usage: httpc post [-v] [-h key:value] [-d inline-Client.getInstance().getData()] [-f Client.getInstance().getFile()] InetAddress\nPost executes a HTTP POST request for a given InetAddress with inline Client.getInstance().getData() or from\nfile.\n\n  -v\t\t  Prints the detail of the response such as protocol, status,\nand Client.getInstance().getHeaders().\n  -h key:value\t  Associates Client.getInstance().getHeaders() to HTTP Request with the format\n'key:value'.\n  -d string\t  Associates an inline Client.getInstance().getData() to the body HTTP POST request.\n  -f Client.getInstance().getFile()\t  Associates the content of a Client.getInstance().getFile() to the body HTTP POST\nrequest.\n\nEither [-d] or [-f] can be used but not both.");
			System.exit(0);
		}
		
		try {
			Client.getInstance().setServer(new InetSocketAddress(InetAddress.getByName(args[args.length - 1].substring(0, args[args.length - 1].indexOf('/'))), port));
			resId = args[args.length - 1].substring(args[args.length - 1].indexOf('/'));
		}
		catch (UnknownHostException e) {
			System.out.println("Bad InetAddress... program will terminate");
			System.exit(1);
		}
		
		try {
			Client.getInstance().setRouter(new InetSocketAddress(InetAddress.getByName(args[args.length - 1].substring(0, args[args.length - 1].indexOf('/'))), routerPort));
		}
		catch (UnknownHostException e) {
			System.out.println("Bad InetAddress... program will terminate");
			System.exit(1);
		}
		
		
		try {
			Client.getInstance().setChannel(DatagramChannel.open());
		}
		catch(IOException e2) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}
		

		Client.getInstance().setPw(new ByteArrayOutputStream());

		
		if (args[1].equalsIgnoreCase("get")) {
			try {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equals("-v")) {
						Client.getInstance().setVerbose(true);
					}
					if (args[i].equals("-o")) {
						try {
							Client.getInstance().setFileOutput(new PrintWriter(new File(args[i+1])));
						}
						catch (IOException e) {
							System.err.println("I/O error... Program will terminate");
							System.exit(1);
						}
					}
					if (args[i].equals("-h")) {
						for (int j = i + 1; j < args.length; j++) { 
							if (args[j].matches("\\S+:\\S+") && !args[j].equals(Client.getInstance().getServer().toString())) {
								Client.getInstance().getHeaders().add(args[j]);
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
						Client.getInstance().setVerbose(true);
					}
					if (args[i].equals("-o")) {
						try {
							Client.getInstance().setFileOutput(new PrintWriter(new File(args[i+1])));
						}
						catch (IOException e) {
							System.err.println("I/O error... Program will terminate");
							System.exit(1);
						}
					}
					if (args[i].equals("-h")) {
						for (int j = i + 1; j < args.length; j++) { 
							if (args[j].matches("\\S+:\\S+") && !args[j].equals(Client.getInstance().getServer().toString())) {
								Client.getInstance().getHeaders().add(args[j]);
							}
						}
					}
					if (args[i].equals("-d")) {
						for (int j = i + 1; j < args.length; j++) {
							if (args[j].matches("\\{\"\\S+\":\\s.\\}") || args[j].matches("\\{\"\\S+\":\\s\".\"\\}")) {
								Client.getInstance().getData().add(args[j]);
							}
						}
					}
					if (args[i].equals("-f")) {
						try {
							Client.getInstance().setFile(new Scanner(new File(args[i+1])));
						}
						catch (IOException e) {
							System.err.println("I/O error... Program will terminate");
							System.exit(1);
						}
						
						while (Client.getInstance().getFile().hasNext()) {
							fileContents += Client.getInstance().getFile().next();
						}
						
						StringTokenizer tokens = new StringTokenizer(fileContents, ",");
						
						while(tokens.hasMoreTokens()) {
							Client.getInstance().getData().add(tokens.nextToken());
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
