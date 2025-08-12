package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatServer server;
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private boolean isLoggedIn = false;

    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String message;

            // First message should be LOGIN:username:password
            while ((message = reader.readLine()) != null) {
                if (!isLoggedIn) {
                    if (message.startsWith("LOGIN:")) {
                        String[] parts = message.split(":", 3);
                        if (parts.length == 3) {
                            String username = parts[1];
                            String password = parts[2];
                            if (server.authenticateUser(username, password)) {
                                this.username = username;
                                isLoggedIn = true;
                                sendMessage("LOGIN_SUCCESS");
                                server.broadcast(username + " has joined the chat.", this);
                                server.updateOnlineUsers();
                            } else {
                                sendMessage("LOGIN_FAILED:Invalid username or password");
                            }
                        }
                    }
                } else {
                    if (message.startsWith("PRIVATE:")) {
                        // Private message format: PRIVATE:recipient:message
                        String[] parts = message.split(":", 3);
                        if (parts.length == 3) {
                            String recipient = parts[1];
                            String privateMessage = parts[2];
                            server.privateMessage(privateMessage, recipient, this);
                        }
                    } // Inside the run() method, modify the broadcast message handling:
                    else {
                        // Regular broadcast message
                        System.out.println("DEBUG: Received broadcast from " + username + ": " + message); // Add this line
                        server.broadcast(username + ": " + message, this);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
            server.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String getUsername() {
        return username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}