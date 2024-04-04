import java.io.IOException;
import java.net.*;

public class ElectionVoteCasting {
    static final String MULTICAST_ADDRESS = "230.0.0.0"; // Multicast IP address
    static final int PORT = 5000; // Multicast port
    static final int NUM_ELECTORATES = 5; // Number of electorates
    
    public static void main(String[] args) throws IOException {
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        
        // Create electorates
        for (int i = 1; i <= NUM_ELECTORATES; i++) {
            final int electorateNumber = i;
            new Thread(() -> {
                try {
                    // Each electorate joins the multicast group
                    MulticastSocket socket = new MulticastSocket(PORT);
                    socket.joinGroup(group);
                    
                    // Generate vote (A or B)
                    char vote = (Math.random() < 0.5) ? 'A' : 'B';
                    System.out.println("Electorate " + electorateNumber + " voted for: " + vote);
                    
                    // Send vote to other electorates
                    DatagramPacket packet = new DatagramPacket(new byte[]{(byte) vote}, 1, group, PORT);
                    socket.send(packet);
                    
                    // Receive votes from other electorates
                    byte[] buffer = new byte[1];
                    int aVotes = 0, bVotes = 0;
                    for (int j = 0; j < NUM_ELECTORATES; j++) {
                        socket.receive(new DatagramPacket(buffer, 1));
                        char receivedVote = (char) buffer[0];
                        if (receivedVote == 'A') {
                            aVotes++;
                        } else if (receivedVote == 'B') {
                            bVotes++;
                        }
                    }
                    
                    // Determine winner
                    char winner = (aVotes > bVotes) ? 'A' : 'B';
                    System.out.println("Electorate " + electorateNumber + " declares winner: " + winner);
                    
                    socket.leaveGroup(group);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
