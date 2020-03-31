import java.net.InetAddress;

public class Packet {
	
    public static final int MIN_LEN = 11;
    public static final int MAX_LEN = 11 + 1024;

    private final int type;
    private final long sequenceNumber;
    private final InetAddress peerAddress;
    private final int peerPort;
    private final byte[] payload;
    
    public Packet(int type, long sequenceNumber, InetAddress peerAddress, int peerPort, byte[] payload) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
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
    
    


}
