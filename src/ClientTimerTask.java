import static java.nio.channels.SelectionKey.OP_READ;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public class ClientTimerTask extends java.util.TimerTask {

	private static long timeout = 5000L;
	private int index;
	private long sequenceNumber;
	private int type;
	
	public ClientTimerTask(int index) {
		super();
		this.index = index;
		this.sequenceNumber = 0L;
		this.type = 0;
	}
	
	public ClientTimerTask(int index, long sequenceNumber) {
		super();
		this.index = index;
		this.sequenceNumber = sequenceNumber;
		this.type = 0;
	}
	
	public ClientTimerTask(int index, long sequenceNumber, int type) {
		super();
		this.index = index;
		this.sequenceNumber = sequenceNumber;
		this.type = type;
	}

	@Override
	public void run() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
		if (Client.getInstance().isHandShaking()) {
			try {
				Client.getInstance().getChannel().send(Client.getInstance().getPackets().get(index).toBuffer(), Client.getInstance().getRouter());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				Client.getInstance().getChannel().configureBlocking(false);
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
				Client.getInstance().getChannel().register(selector, OP_READ);
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
			
			 
			 try {
				Client.getInstance().getChannel().receive(byteBuffer);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	         byteBuffer.flip();
	         Packet response = null;
			try {
				response = Packet.fromBuffer(byteBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
	         
	         if (Client.getInstance().getPackets().get(index).getType() == 2 && response.getType() == 3 && response.getSequenceNumber() == Client.getInstance().getPackets().get(index).getSequenceNumber()) {
	        	 Client.getInstance().getPackets().get(index).setAck(true);
	        	 Client.getInstance().getTimers().get(index + 1).scheduleAtFixedRate(new ClientTimerTask(index + 1), 0, timeout);
	        	 Client.getInstance().getTimers().get(index).cancel();
	         }
	         else if (Client.getInstance().getPackets().get(index).getType() == 1 && response.getType() == 1 && response.getSequenceNumber() == Client.getInstance().getPackets().get(index).getSequenceNumber()) {
	        	 Client.getInstance().getPackets().get(index).setAck(true);
	        	 Client.getInstance().setHandShaking(false);
	        	 Client.getInstance().getTimers().get(index).cancel();
	         }
		}
		else if (Client.getInstance().isSender()) {
			try {
				Client.getInstance().getChannel().send(Client.getInstance().getPackets().get(index).toBuffer(), Client.getInstance().getRouter());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				Client.getInstance().getChannel().configureBlocking(false);
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
				Client.getInstance().getChannel().register(selector, OP_READ);
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
			
			 try {
				Client.getInstance().getChannel().receive(byteBuffer);
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
	         
	         if (response.getType() == 1 && response.getSequenceNumber() == Client.getInstance().getPackets().get(index).getSequenceNumber()) {
	        	 Client.getInstance().getPackets().get(index).setAck(true);
	        	 Client.getInstance().getTimers().get(index).cancel();
	         }
		}
		else if (Client.getInstance().isReceiver()) {
			try {
				Client.getInstance().getChannel().close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Client.getInstance().setChannel(null);
			try {
				Client.getInstance().setChannel(DatagramChannel.open());
				Client.getInstance().getChannel().bind(new InetSocketAddress(InetAddress.getByName("localhost"), HttpClient.clientPort()));
			} 
			catch (IOException e) {
				System.err.println("I/O error... Program will terminate");
				System.exit(1);
			}
			while (Client.getInstance().isReceiver()) {
				
				byteBuffer.clear();
				try {
					Client.getInstance().getChannel().receive(byteBuffer);
				} catch (IOException e4) {
					e4.printStackTrace();
				}
				
				byteBuffer.flip();
				Packet packet = null;
				
				try {
					packet = Packet.fromBuffer(byteBuffer);
				} catch (IOException e4) {
					e4.printStackTrace();
				}
				
				if (packet.getType() == 0) {
					if (HttpClient.isUnique(packet))
					{
						Client.getInstance().getPackets().add(packet);
					}
					Packet resp = packet;
					resp.setType(1);
					try {
						Client.getInstance().getChannel().send(resp.toBuffer(), Client.getInstance().getRouter());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else if (Client.getInstance().isConnectionTermination()) {
			if (Client.getInstance().getPackets().get(index).isAck()) {
				Client.getInstance().getTimers().get(index).cancel();
			}
			try {
				Client.getInstance().getChannel().send(Client.getInstance().getPackets().get(index).toBuffer(), Client.getInstance().getRouter());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				Client.getInstance().getChannel().configureBlocking(false);
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
				Client.getInstance().getChannel().register(selector, OP_READ);
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
			
			 
			 try {
				Client.getInstance().getChannel().receive(byteBuffer);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	         byteBuffer.flip();
	         Packet response = null;
			try {
				response = Packet.fromBuffer(byteBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
	         
	         if (Client.getInstance().getPackets().get(index).getType() == 4 && response.getType() == 5 && response.getSequenceNumber() == Client.getInstance().getPackets().get(index).getSequenceNumber()) {
	        	 Client.getInstance().getPackets().get(index).setAck(true);
	        	 Client.getInstance().getTimers().get(index + 1).scheduleAtFixedRate(new ClientTimerTask(index + 1), 0, timeout);
	        	 Client.getInstance().getTimers().get(index).cancel();
	         }
	         else if (Client.getInstance().getPackets().get(index).getType() == 1 && response.getType() == 1 && response.getSequenceNumber() == Client.getInstance().getPackets().get(index).getSequenceNumber()) {
	        	 Client.getInstance().getPackets().get(index).setAck(true);
	        	 Client.getInstance().setConnectionTermination(false);
	        	 Client.getInstance().getTimers().get(index).cancel();
	         }
		}
	}

}
