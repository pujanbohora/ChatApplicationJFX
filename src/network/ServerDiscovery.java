package network;

import model.ChatManager;
import model.Message;
import model.User;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles server discovery on the local network using UDP multicast.
 * This class provides methods for both advertising a server and discovering servers.
 */
public class ServerDiscovery {
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    private static final int DISCOVERY_PORT = 8889;
    private static final String DISCOVERY_MESSAGE = "CHAT_SERVER_AVAILABLE";
    private static final String DISCOVERY_REQUEST = "CHAT_SERVER_DISCOVERY";

    private final int serverPort;
    private DatagramSocket socket;
    private MulticastSocket multicastSocket;
    private boolean running;
    private Thread advertisementThread;

    /**
     * Constructor with server port
     *
     * @param serverPort the port the chat server is running on
     */
    public ServerDiscovery(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Starts advertising the server on the local network
     *
     * @throws IOException if an I/O error occurs
     */
    public void startAdvertising() throws IOException {
        if (running) return;

        socket = new DatagramSocket(DISCOVERY_PORT);
        running = true;

        // Start a thread to listen for discovery requests
        advertisementThread = new Thread(this::listenForDiscoveryRequests);
        advertisementThread.setDaemon(true);
        advertisementThread.start();

        System.out.println("Server discovery service started on port " + DISCOVERY_PORT);
    }

    /**
     * Listens for discovery requests and responds with server information
     */
    private void listenForDiscoveryRequests() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (running) {
            try {
                // Wait for a discovery request
                socket.receive(packet);

                // Check if it's a valid discovery request
                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.equals(DISCOVERY_REQUEST)) {
                    // Send a response with the server port
                    String response = DISCOVERY_MESSAGE + ":" + serverPort;
                    byte[] responseData = response.getBytes();

                    DatagramPacket responsePacket = new DatagramPacket(
                            responseData,
                            responseData.length,
                            packet.getAddress(),
                            packet.getPort()
                    );

                    socket.send(responsePacket);

                    System.out.println("Responded to discovery request from " + packet.getAddress().getHostAddress());
                }

            } catch (IOException e) {
                if (running) {
                    System.err.println("Error handling discovery request: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Stops advertising the server
     */
    public void stopAdvertising() {
        running = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        System.out.println("Server discovery service stopped");
    }

    /**
     * Discovers servers on the local network using UDP multicast
     *
     * @param timeoutMs the timeout in milliseconds
     * @return a list of server addresses and ports
     * @throws IOException if an I/O error occurs
     */
    public static List<ServerInfo> discoverServers(int timeoutMs) throws IOException {
        List<ServerInfo> servers = new ArrayList<>();

        try (MulticastSocket socket = new MulticastSocket(DISCOVERY_PORT)) {
            // Join the multicast group
            InetAddress multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(multicastGroup);

            // Send discovery request
            byte[] requestData = DISCOVERY_REQUEST.getBytes();
            DatagramPacket requestPacket = new DatagramPacket(
                    requestData,
                    requestData.length,
                    multicastGroup,
                    DISCOVERY_PORT
            );

            socket.send(requestPacket);

            // Set timeout for responses
            socket.setSoTimeout(timeoutMs);

            // Receive responses
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);

            long endTime = System.currentTimeMillis() + timeoutMs;

            while (System.currentTimeMillis() < endTime) {
                try {
                    socket.receive(responsePacket);

                    // Parse the response
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    if (response.startsWith(DISCOVERY_MESSAGE)) {
                        String[] parts = response.split(":");
                        if (parts.length == 2) {
                            try {
                                int port = Integer.parseInt(parts[1]);
                                String address = responsePacket.getAddress().getHostAddress();

                                ServerInfo serverInfo = new ServerInfo(address, port);
                                if (!servers.contains(serverInfo)) {
                                    servers.add(serverInfo);
                                    System.out.println("Discovered server at " + address + ":" + port);
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port in server response: " + e.getMessage());
                            }
                        }
                    }
                } catch (IOException e) {
                    // Timeout or other error, continue until overall timeout
                }
            }

            // Leave the multicast group
            socket.leaveGroup(multicastGroup);
        }

        return servers;
    }

    /**
     * Class representing server information (address and port)
     */
    public static class ServerInfo {
        private final String address;
        private final int port;

        /**
         * Constructor with address and port
         *
         * @param address the server address
         * @param port    the server port
         */
        public ServerInfo(String address, int port) {
            this.address = address;
            this.port = port;
        }

        /**
         * Gets the server address
         *
         * @return the server address
         */
        public String getAddress() {
            return address;
        }

        /**
         * Gets the server port
         *
         * @return the server port
         */
        public int getPort() {
            return port;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ServerInfo that = (ServerInfo) obj;
            return port == that.port && address.equals(that.address);
        }

        @Override
        public int hashCode() {
            return 31 * address.hashCode() + port;
        }

        @Override
        public String toString() {
            return address + ":" + port;
        }
    }
}
