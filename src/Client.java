import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;

 class Client {

	private volatile static Client client;
	 
	 private volatile boolean verbose;
	 private volatile ArrayList<String> headers;
	 private volatile ArrayList<String> data;
	 private volatile PrintWriter fileOutput;
	
	 private volatile long firstSeqNum;
	 private volatile long sequenceNumber;
	 private volatile long ackNumber;
	 private volatile int windowSize;
	
	  private volatile ArrayList<Timer> timers;
	  private volatile ArrayList<Packet> packets;
	  private volatile boolean isSender;
	  private volatile boolean isHandShaking;
	  private volatile boolean isReceiver;
	  private volatile int index; 
	
	  private volatile InetSocketAddress server;
	  private volatile InetSocketAddress router;
	  private volatile DatagramChannel channel;
	
	  private volatile BufferedReader reader;
	  private volatile Scanner file;
	  private volatile ByteArrayOutputStream pw;
	  
	 private Client() {
		 this.verbose = false;
		 this.headers = new ArrayList<String>();
		 this.data = new ArrayList<String>();
		 this.fileOutput = null;
		
		 this.firstSeqNum = 0L;
		 this.sequenceNumber = 0L;
		 this.ackNumber = 0L;
		 this.windowSize = 0;
		
		 this.timers = new ArrayList<Timer>();
		 this.packets = new ArrayList<Packet>();
		 this.setReceiver(false);
		 this.setSender(false);
		 this.setHandShaking(true);
		 this.index = 0; 
		
		 this.server = null;
		 this.router = null;
		 this.channel = null;
		 
		 this.reader = null;
		 this.file = null;
		 this.pw = null;

	}
	 
	public synchronized static Client getInstance() {
	    if (client == null) {
	        client = new Client();
	    }
	    return client;
	}
	
	
	public synchronized boolean isVerbose() {
		return verbose;
	}

	public synchronized void setVerbose(boolean verbose) {
		
		this.verbose = verbose;
	}

	public synchronized ArrayList<String> getHeaders() {
		return headers;
	}

	public synchronized void setHeaders(ArrayList<String> headers) {
		
		this.headers = headers;
	}

	public synchronized ArrayList<String> getData() {
		return data;
	}

	public synchronized void setData(ArrayList<String> data) {
		
		this.data = data;
	}

	public synchronized PrintWriter getFileOutput() {
		return fileOutput;
	}

	public synchronized void setFileOutput(PrintWriter fileOutput) {
		
		this.fileOutput = fileOutput;
	}

	public synchronized long getFirstSeqNum() {
		return firstSeqNum;
	}

	public synchronized void setFirstSeqNum(long firstSeqNum) {
		
		this.firstSeqNum = firstSeqNum;
	}

	public synchronized long getSequenceNumber() {
		return sequenceNumber;
	}

	public synchronized void setSequenceNumber(long sequenceNumber) {
		
		this.sequenceNumber = sequenceNumber;
	}

	public synchronized long getAckNumber() {
		return ackNumber;
	}

	public synchronized void setAckNumber(long ackNumber) {
		
		this.ackNumber = ackNumber;
	}

	public synchronized int getWindowSize() {
		return windowSize;
	}

	public synchronized void setWindowSize(int windowSize) {
		
		this.windowSize = windowSize;
	}

	public synchronized ArrayList<Timer> getTimers() {
		return timers;
	}

	public synchronized void setTimers(ArrayList<Timer> timers) {
		
		this.timers = timers;
	}

	public synchronized ArrayList<Packet> getPackets() {
		return packets;
	}

	public synchronized void setPackets(ArrayList<Packet> packets) {
		this.packets = packets;
	}

	public synchronized boolean isSender() {
		try {
			File file = new File("isSender.txt");
			if (file.createNewFile()) {
				
				return isSender;
			} else {
				Scanner scanner = new Scanner(file);
				this.isSender = scanner.nextBoolean();
				scanner.close();
			}
		}
		catch(IOException e) {
		//	e.printStackTrace();
		}
		return isSender;
	}

	public synchronized void setSender(boolean isSender) {
		try {
			File file = new File("isSender.txt");
			file.delete();
			file.createNewFile();
			
			PrintWriter pw = new PrintWriter(file);
			pw.write(new Boolean(isSender).toString());
			pw.close();
		}
		catch(IOException e) {
		//	e.printStackTrace();
		}
		
		this.isSender = isSender;
	}

	public synchronized boolean isHandShaking() {
		try {
			File file = new File("isHandShaking.txt");
			if (file.createNewFile()) {
				return isHandShaking;
			}
			
			Scanner scanner = new Scanner(file);
			if (file.length() > 0)
				this.isHandShaking = scanner.nextBoolean();
			scanner.close();
		}
		catch(IOException e) {
		//	e.printStackTrace();
		}
		
		return isHandShaking;
	}

	public synchronized void setHandShaking(boolean isHandShaking) {
		try {
			File file = new File("isHandShaking.txt");
			file.delete();
			file.createNewFile();
			
			PrintWriter pw = new PrintWriter(file);
			pw.write(new Boolean(isHandShaking).toString());
			pw.close();
		}
		catch(IOException e) {
		//	e.printStackTrace();
		}
		
		this.isHandShaking = isHandShaking;
	}

	public synchronized int getIndex() {
		return index;
	}

	public synchronized void setIndex(int index) {
		this.index = index;
	}

	public synchronized InetSocketAddress getServer() {
		return server;
	}

	public synchronized boolean isReceiver() {
		try {
			File file = new File("isReceiver.txt");
			if (file.createNewFile()) {
				return isReceiver;
			}
			
			Scanner scanner = new Scanner(file);
			if (file.length() > 0)
				this.isReceiver = scanner.nextBoolean();
			scanner.close();
		}
		catch(IOException e) {
		//	e.printStackTrace();
		}
		
		return isReceiver;
	}

	public synchronized void setReceiver(boolean isReceiver) {
		try {
			File file = new File("isReceiver.txt");
			file.delete();
			file.createNewFile();
			
			PrintWriter pw = new PrintWriter(file);
			pw.write(new Boolean(isReceiver).toString());
			pw.close();
		}
		catch(IOException e) {
		//	e.printStackTrace();
		}
		
		this.isReceiver = isReceiver;
	}

	public synchronized void setServer(InetSocketAddress server) {
		this.server = server;
	}

	public synchronized InetSocketAddress getRouter() {
		return router;
	}

	public synchronized void setRouter(InetSocketAddress router) {
		
		this.router = router;
	}

	public synchronized DatagramChannel getChannel() {
		return channel;
	}

	public synchronized void setChannel(DatagramChannel channel) {
		this.channel = channel;
	}

	public synchronized BufferedReader getReader() {
		return reader;
	}

	public synchronized void setReader(BufferedReader reader) {
		this.reader = reader;
	}

	public synchronized Scanner getFile() {
		return file;
	}

	public synchronized void setFile(Scanner file) {
		this.file = file;
	}

	public synchronized ByteArrayOutputStream getPw() {
		return pw;
	}

	public synchronized void setPw(ByteArrayOutputStream pw) {
		this.pw = pw;
	}

}
