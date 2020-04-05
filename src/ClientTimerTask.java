import static java.nio.channels.SelectionKey.OP_READ;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public class ClientTimerTask extends java.util.TimerTask {

	private int index;
	private long sequenceNumber;
	private int type;
	
	public ClientTimerTask(int index){
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
		if (HttpClient.isHandShaking) {
			try {
				HttpClient.channel.send(HttpClient.packets.get(index).toBuffer(), HttpClient.router);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				HttpClient.channel.configureBlocking(false);
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
				HttpClient.channel.register(selector, OP_READ);
			} catch (ClosedChannelException e1) {
				e1.printStackTrace();
			}
			try {
				selector.select(4000);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			Set<SelectionKey> keys = selector.selectedKeys();
			
	        if(keys.isEmpty()){
	            System.err.println("No response after timeout");
	            return;
	        }
			
			 ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN);
			 try {
				HttpClient.channel.receive(byteBuffer);
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
	         
	         if (HttpClient.packets.get(index).getType() == 2 && response.getType() == 3 && response.getSequenceNumber() == HttpClient.packets.get(index).getSequenceNumber()) {
	        	 HttpClient.packets.get(index).setAck(true);
	        	 HttpClient.timers.get(index + 1).scheduleAtFixedRate(new ClientTimerTask(index + 1), 0, 5000);
	        	 HttpClient.timers.get(index).cancel();
	         }
	         else if (HttpClient.packets.get(index).getType() == 1 && response.getType() == 1 && response.getSequenceNumber() == HttpClient.packets.get(index).getSequenceNumber()) {
	        	 HttpClient.packets.get(index).setAck(true);
	        	 HttpClient.isHandShaking = false;
	        	 HttpClient.timers.get(index).cancel();
	         }
		}
		else if (HttpClient.isSender) {
			try {
				HttpClient.channel.send(HttpClient.packets.get(index).toBuffer(), HttpClient.router);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				HttpClient.channel.configureBlocking(false);
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
				HttpClient.channel.register(selector, OP_READ);
			} catch (ClosedChannelException e1) {
				e1.printStackTrace();
			}
			try {
				selector.select(5000);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			Set<SelectionKey> keys = selector.selectedKeys();
			
	        if(keys.isEmpty()){
	            System.err.println("No response after timeout");
	            return;
	        }
			
			 ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN);
			 try {
				HttpClient.channel.receive(byteBuffer);
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
	         
	         if (response.getType() == 1 && response.getSequenceNumber() == HttpClient.packets.get(index).getSequenceNumber()) {
	        	 HttpClient.packets.get(index).setAck(true);
	        	 HttpClient.timers.get(index).cancel();
	         }
		}
	}

}
