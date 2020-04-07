import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Timer;


public class HttpFileServer {
	
	
	public static long sequenceNumber = 0L;
	public static long ackNumber = 0L;
	public static ArrayList<Packet> packets = new ArrayList<Packet>();
	public static ArrayList<Timer> timers = new ArrayList<Timer>();
	public static Timer timer = new Timer();
	
	public static DatagramChannel channel = null;
	public static ByteArrayOutputStream pw = null;
	public static PrintWriter pwPost = null;
	public static ByteArrayInputStream reader = null;
	public static ByteBuffer buffer = null;
	public static Scanner fileScanner = null;
	public static InetSocketAddress server = null;
	public static InetSocketAddress router = null;
	
	//TYPES: DATA = 0, ACK = 1, SYN = 2, SYN-ACK = 3
	
	public static void main(String[] args) {
		
		//ServerSocket serverSocket = null;
		
		boolean debugMsg = true;
		String directory = "./";
		int port = 8007;
		final int routerPort = 3000;
		
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
					"\n        Default is 8007.\r\n" + 
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
		
		/*try {
			serverSocket = new ServerSocket(port);
		}
		catch(IOException e) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}*/
		
		try {
			server = new InetSocketAddress(InetAddress.getByName("localhost"), port);
		}
		catch (UnknownHostException e) {
			System.out.println("Bad InetAddress... program will terminate");
			System.exit(1);
		}
		
		try {
			router = new InetSocketAddress(InetAddress.getByName("localhost"), routerPort);
		}
		catch (UnknownHostException e) {
			System.out.println("Bad InetAddress... program will terminate");
			System.exit(1);
		}
		
		try {
			channel = DatagramChannel.open();
			channel.bind(server);
		} 
		catch (IOException e) {
			System.err.println("I/O error... Program will terminate");
			System.exit(1);
		}
		

		buffer = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
		
		while (true) {
			ok = true;
			notFound = false;
			forbidden = false;
			
			if (directory.contains("..") || !directory.startsWith("./")) {
				forbidden = true;
				ok = false;
				notFound = false;
			}
			/*
			try {
				reader = new ByteArrayInputStream(buffer.array());
			}
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
			*/
			
			buffer.clear();
			
			timer.schedule(new ServerTimerTask(), 0);
			
			while (Client.getInstance().isHandShaking() == true) {
				
			}
			
			while (Client.getInstance().isSender() == true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.getMessage();
				}
				
			}
			
			
			buffer.flip();
			pw = new ByteArrayOutputStream();
			
			String str = "";
			
			for (int i = 0; i < packets.size(); i++) {
				try {
					str += new String(packets.get(i).getPayload(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			
			ArrayList<String> lines = null;
			StringTokenizer tokens = null;
			
			if (buffer.array().length > 0) {
				//try {
					//str = new String(packet.getPayload(), "UTF-8").trim();
					//System.out.println(str);
				//} catch (UnsupportedEncodingException e3) {
					//e3.printStackTrace();
					//System.exit(1);
				//}
				lines = new ArrayList<String>();
				tokens = new StringTokenizer(str, "\n");
				
				while (tokens.hasMoreTokens())
				{
					lines.add(tokens.nextToken());
				}
			//}
			/*try {
				str = reader.read();
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
*/
			tokens = null;
			if (!str.isEmpty() && lines.get(0) != null) {
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
				try {
				if (ok == true) {
					pw.write(("HTTP/1.0 200 OK" + "\n").getBytes());
					pw.write(("Content-Type: text/html; charset=utf-8" + "\n").getBytes());
					pw.write(("Server: COMP 445 Assignment #3 Server" + "\n").getBytes());
					pw.write(("\n").getBytes());
					
					packets.get(0).setPayload(pw.toByteArray());
					packets.get(0).setSequenceNumber(19L);
					//channel.send(response.toBuffer(), router);
					timers.add(new Timer());
					timers.get(0).scheduleAtFixedRate(new ServerTimerTask(0), 0, 5000);
				}
				else if (notFound == true) {
					pw.write(("HTTP/1.0 404 NOT FOUND" + "\n").getBytes());
					pw.write(("Content-Type: text/html; charset=utf-8" + "\n").getBytes());
					pw.write(("Server: COMP 445 Assignment #3 Server" + "\n").getBytes());
					pw.write(("\n").getBytes());
					pw.write(("Error 404: Not Found" + "\n").getBytes());
					
					packets.get(0).setPayload(pw.toByteArray());
					packets.get(0).setSequenceNumber(19L);
					//channel.send(response.toBuffer(), router);
					timers.add(new Timer());
					timers.get(0).scheduleAtFixedRate(new ServerTimerTask(0), 0, 5000);
				}
				else if (forbidden == true) {
					pw.write(("HTTP/1.0 403 FORBIDDEN" + "\n").getBytes());
					pw.write(("Content-Type: text/html; charset=utf-8" + "\n").getBytes());
					pw.write(("Server: COMP 445 Assignment #3 Server" + "\n").getBytes());
					pw.write(("\n").getBytes());
					pw.write(("Error 403: Forbidden").getBytes());
					
					packets.get(0).setPayload(pw.toByteArray());
					packets.get(0).setSequenceNumber(19L);
					//channel.send(response.toBuffer(), router);
					timers.add(new Timer());
					timers.get(0).scheduleAtFixedRate(new ServerTimerTask(0), 0, 5000);
				}
				
				if (method.equals("GET") && ok == true && item.equals("/")) {
					File folder = new File(directory);
					File[] filesInFolder = folder.listFiles();
					
					pw.write(("<H1>List of Files in Directory: " + directory +"</H1>" + "\n").getBytes());
					
					pw.write(("<ul>" + "\n").getBytes());
					for (File file : filesInFolder) {
						if (file.isFile()) {
							pw.write(("<li>" + file.getName() + "</li>" + "\n").getBytes());
						}
					}
					pw.write(("</ul>" + "\n").getBytes());
					
					packets.get(1).setPayload(pw.toByteArray());
					packets.get(1).setSequenceNumber(20L);
					//channel.send(response.toBuffer(), router);
					timers.add(new Timer());
					timers.get(1).scheduleAtFixedRate(new ServerTimerTask(1), 0, 5000);
				}
				else if (method.equals("GET") && !item.equals("/") && !item.isEmpty()) {
					for (String fileLine : fileContents) {
						pw.write((fileLine + "\n").getBytes());
					}
					packets.get(1).setPayload(pw.toByteArray());
					packets.get(1).setSequenceNumber(20L);
					//channel.send(response.toBuffer(), router);
					timer.scheduleAtFixedRate(new ServerTimerTask(1), 0, 5000);
					//Packet response = packet;
					//response.setPayload(pw.toByteArray());
					//channel.send(response.toBuffer(), router);
				}
				
				else if (method.equals("POST")) {
					/*try {
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
						
					}*/
					
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
					
					//for (int i = 0; i < lines.size(); i++) {
						//if (lines.get(i).equals("")) {
						//	for (int j = i + 1; j < lines.size(); j++) 
								for (int j = 0; j < lines.size(); j++){
								pwPost.println(lines.get(j));
							}
						//	break;
						//}
					//}
					
					pwPost.close();
					
					}
				}
				catch(IOException e) {
					System.err.println("I/O error... Program will terminate");					
					System.exit(1);
				}
			}
			
			if (debugMsg == true) {
				for (String line : lines) {
					System.out.println(line);
				}
			}
			
			try {
				pw.close();
			} catch (IOException e) {
				System.err.println("I/O error... Program will terminate");					
				System.exit(1);
			}
		
		}
		
	}
	}
}
