import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ServerTimerTask extends java.util.TimerTask{

	//TYPES: DATA = 0, ACK = 1, SYN = 2, SYN-ACK = 3
	
	@Override
	public void run() {
			ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
		
			while (HttpClient.isHandShaking) {
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
					e4.printStackTrace();
				}
				
				if (packet.getType() == 2 && packet.getSequenceNumber() == 0L) {
					Packet resp = packet;
					resp.setType(3);
					try {
						HttpFileServer.channel.send(resp.toBuffer(), HttpFileServer.router);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				else  if (packet.getType() == 1 && packet.getSequenceNumber() == 1L) {
					Packet resp = packet;
					resp.setType(1);
					try {
						HttpFileServer.channel.send(resp.toBuffer(), HttpFileServer.router);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		}
	}

}
