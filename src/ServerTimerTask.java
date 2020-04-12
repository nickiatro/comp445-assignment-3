import static java.nio.channels.SelectionKey.OP_READ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public class ServerTimerTask extends java.util.TimerTask{
	private static long timeout = 5000L;
	private static PrintWriter pw = null;
	private int index;
	
	public ServerTimerTask() {
		super();
		this.index = 0;
	}
	
	public ServerTimerTask(int index) {
		this.index = index;
	}
	
	//TYPES: DATA = 0, ACK = 1, SYN = 2, SYN-ACK = 3, FIN = 4, FIN-ACK = 5

	@Override
	public void run() {
			ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
			File file = new File("client-port.txt");
			if (pw == null) {
				try {
					pw = new PrintWriter(file);
				} catch (FileNotFoundException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
			}
				while (Client.getInstance().isHandShaking() && !Client.getInstance().isReceiver()) {
					buffer.clear();
					try {
						HttpFileServer.getChannel().receive(buffer);
					} catch (IOException e4) {
						e4.printStackTrace();
					}
					
					buffer.flip();
					Packet packet = null;
					
					try {
						packet = Packet.fromBuffer(buffer);
					} catch (IOException e4) {
						HttpFileServer.getTimers().get(index).cancel();
						return;
					}
					
					if (packet.getType() == 2 && packet.getSequenceNumber() == 0L) {
						Packet resp = packet;
						resp.setType(3);
						try {
							pw.write(new Integer(resp.getPeerPort()).toString());
							HttpFileServer.getChannel().send(resp.toBuffer(), HttpFileServer.getRouter());
							pw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					else if (packet.getType() == 1 && packet.getSequenceNumber() == 1L) {
						Packet resp = packet;
						resp.setType(1);
						try {
							pw.write(new Integer(resp.getPeerPort()).toString());
							HttpFileServer.getChannel().send(resp.toBuffer(), HttpFileServer.getRouter());
							pw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			}
			while (Client.getInstance().isSender() && !Client.getInstance().isReceiver()) {
				buffer.clear();
				try {
					HttpFileServer.getChannel().receive(buffer);
				} catch (IOException e4) {
					e4.printStackTrace();
				}
				
				buffer.flip();
				Packet packet = null;
				
				try {
					packet = Packet.fromBuffer(buffer);
				} catch (IOException e4) {
					return;
				}
				
				if (packet.getType() == 0) {
					if (HttpFileServer.isUnique(packet))
					{
						HttpFileServer.getPackets().add(packet);
					}
					Packet resp = packet;
					resp.setType(1);
					try {
						HttpFileServer.getChannel().send(resp.toBuffer(), HttpFileServer.getRouter());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (Client.getInstance().isReceiver()) {
				SelectionKey key = null;
				try {
				try {
					HttpFileServer.getChannel().send(HttpFileServer.getPackets().get(index).toBuffer(), HttpFileServer.getRouter());
				} catch (IOException e1) {
					
				}
				
				try {
					HttpFileServer.getChannel().configureBlocking(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Selector selector = null;
				try {
					selector = Selector.open();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				try {
					key = HttpFileServer.getChannel().register(selector, OP_READ);
				} catch (ClosedChannelException e1) {
					e1.printStackTrace();
				} catch (IllegalBlockingModeException e2) {
					
				}
				try {
					selector.select(timeout);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				Set<SelectionKey> keys = selector.selectedKeys();
				
		        if(keys.isEmpty()){
		            return;
		        }
				
				 ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN);
				 try {
					HttpFileServer.getChannel().receive(byteBuffer);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		         byteBuffer.flip();
		         Packet response = null;
				try {
					response = Packet.fromBuffer(byteBuffer);
				} catch (IOException e) {
					return;
				}
		         
		         if (response.getType() == 1 && response.getSequenceNumber() == HttpFileServer.getPackets().get(index).getSequenceNumber()) {
		        	 HttpFileServer.getPackets().get(index).setAck(true);
		        	 HttpFileServer.getTimers().get(index).cancel();
		         }
		         try {
					selector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				}finally {
					try{
						key.cancel();
					} catch (NullPointerException e) {
						
					}
				}
			}
			while (Client.getInstance().isConnectionTermination()) {
					buffer.clear();
					try {
						HttpFileServer.getChannel().configureBlocking(true);
						HttpFileServer.getChannel().receive(buffer);
					} catch (IOException e4) {
						e4.printStackTrace();
					}
					
					buffer.flip();
					Packet packet = null;
					
					try {
						packet = Packet.fromBuffer(buffer);
					} catch (IOException e4) {
						return;
					}
					
					if (packet.getType() == 4 && packet.getSequenceNumber() == 0L) {
						Packet resp = packet;
						resp.setType(5);
						try {
							pw.write(new Integer(resp.getPeerPort()).toString());
							HttpFileServer.getChannel().send(resp.toBuffer(), HttpFileServer.getRouter());
							pw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					else if (packet.getType() == 1 && packet.getSequenceNumber() == 1L) {
						Packet resp = packet;
						resp.setType(1);
						try {
							pw.write(new Integer(resp.getPeerPort()).toString());
							HttpFileServer.getChannel().send(resp.toBuffer(), HttpFileServer.getRouter());
							pw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
