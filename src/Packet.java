import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packet {
	
    public static final int MIN_LEN = 11;
    public static final int MAX_LEN = 1024;

    private int type;
    private long sequenceNumber;
    private InetAddress peerAddress;
    private int peerPort;
    private byte[] payload;
    private boolean ack;
    
	public Packet(int type, long sequenceNumber, InetAddress peerAddress, int peerPort, byte[] payload) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
        this.payload = payload;
        this.ack = false;
    }
	
	public void setAck(boolean ack) {
		this.ack = ack;
	}
    
    public void setType(int type) {
		this.type = type;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public void setPeerAddress(InetAddress peerAddress) {
		this.peerAddress = peerAddress;
	}

	public void setPeerPort(int peerPort) {
		this.peerPort = peerPort;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	public static int getMinLen() {
		return MIN_LEN;
	}

	public static int getMaxLen() {
		return MAX_LEN;
	}

	public int getType() {
		return type;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public InetAddress getPeerAddress() {
		return peerAddress;
	}

	public int getPeerPort() {
		return peerPort;
	}

	public byte[] getPayload() {
		return payload;
	}
    
	 public ByteBuffer toBuffer() {
		 ByteBuffer buf = ByteBuffer.allocate(MAX_LEN).order(ByteOrder.BIG_ENDIAN);
		 buf.put((byte) type);
		 buf.putInt((int) sequenceNumber);
		 buf.put(peerAddress.getAddress());
		 buf.putShort((short) peerPort);
		 buf.put(payload);
		 buf.flip();
		 return buf;
	 }
	 
	 public static Packet fromBuffer(ByteBuffer buf) throws IOException {
	        if (buf.limit() < MIN_LEN || buf.limit() > MAX_LEN) {
	            throw new IOException("Invalid length");
	        }

	        Packet p = new Packet(0, 0, null, 0, null);

	        p.setType(Byte.toUnsignedInt(buf.get()));
	        p.setSequenceNumber(Integer.toUnsignedLong(buf.getInt()));

	        byte[] host = new byte[]{buf.get(), buf.get(), buf.get(), buf.get()};
	        p.setPeerAddress(Inet4Address.getByAddress(host));
	        p.setPeerPort(Short.toUnsignedInt(buf.getShort()));

	        byte[] payload = new byte[buf.remaining()];
	        buf.get(payload);
	        p.setPayload(payload);
	        return p;
	 }

	public boolean isAck() {
		return ack;
	}



}
