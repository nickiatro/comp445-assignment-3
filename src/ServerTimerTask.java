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
						HttpFileServer.channel.receive(buffer);
					} catch (IOException e4) {
						e4.printStackTrace();
					}
					
					buffer.flip();
					Packet packet = null;
					
					try {
						packet = Packet.fromBuffer(buffer);
					} catch (IOException e4) {
						HttpFileServer.timers.get(index).cancel();
						return;
					}
					
					if (packet.getType() == 2 && packet.getSequenceNumber() == 0L) {
						Packet resp = packet;
						resp.setType(3);
						try {
							pw.write(new Integer(resp.getPeerPort()).toString());
							HttpFileServer.channel.send(resp.toBuffer(), HttpFileServer.router);
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
							HttpFileServer.channel.send(resp.toBuffer(), HttpFileServer.router);
							pw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			}
			while (Client.getInstance().isSender() && !Client.getInstance().isReceiver()) {
				buffer.clear();
				try {
					HttpFileServer.channel.receive(buffer);
				} catch (IOException e4) {
					e4.printStackTrace();
				}
				
				buffer.flip();
				Packet packet = null;
				
				try {
					packet = Packet.fromBuffer(buffer);
				} catch (IOException e4) {
					HttpFileServer.timers.get(index).cancel();
					return;
				}
				
				if (packet.getType() == 0) {
					if (packet.getSequenceNumber() != HttpFileServer.packets.size() - 1)
					{
						HttpFileServer.packets.add(packet);
					}
					Packet resp = packet;
					resp.setType(1);
					try {
						HttpFileServer.channel.send(resp.toBuffer(), HttpFileServer.router);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (Client.getInstance().isReceiver()) {
				SelectionKey key = null;
				try {
				if (HttpFileServer.packets.get(index).isAck()) {
					HttpFileServer.timers.get(index).cancel();
				}
				try {
					HttpFileServer.channel.send(HttpFileServer.packets.get(index).toBuffer(), HttpFileServer.router);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				try {
					HttpFileServer.channel.configureBlocking(false);
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
					key = HttpFileServer.channel.register(selector, OP_READ);
				} catch (ClosedChannelException e1) {
					e1.printStackTrace();
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
					HttpFileServer.channel.receive(byteBuffer);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		         byteBuffer.flip();
		         Packet response = null;
				try {
					response = Packet.fromBuffer(byteBuffer);
				} catch (IOException e) {
					HttpFileServer.timers.get(index).cancel();
					return;
				}
		         
		         if (response.getType() == 1 && response.getSequenceNumber() == HttpFileServer.packets.get(index).getSequenceNumber()) {
		        	 HttpFileServer.packets.get(index).setAck(true);
		        	 HttpFileServer.timers.get(index).cancel();
		         }
		         try {
					selector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				}finally {
					key.cancel();
				}
			}
			while (Client.getInstance().isConnectionTermination()) {
					buffer.clear();
					try {
						HttpFileServer.channel.configureBlocking(true);
						HttpFileServer.channel.receive(buffer);
					} catch (IOException e4) {
						e4.printStackTrace();
					}
					
					buffer.flip();
					Packet packet = null;
					
					try {
						packet = Packet.fromBuffer(buffer);
					} catch (IOException e4) {
						e4.printStackTrace();
					}
					
					if (packet.getType() == 4 && packet.getSequenceNumber() == 0L) {
						Packet resp = packet;
						resp.setType(5);
						try {
							pw.write(new Integer(resp.getPeerPort()).toString());
							HttpFileServer.channel.send(resp.toBuffer(), HttpFileServer.router);
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
							HttpFileServer.channel.send(resp.toBuffer(), HttpFileServer.router);
							pw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
