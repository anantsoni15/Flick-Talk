package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {
    private final int port;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final Map<String, User> users = new HashMap<>();
    
    public ChatServer(int port) {
        this.port = port;
        // Add some demo users
        users.put("anant", new User("anant", "password123"));
        users.put("rohan", new User("rohan", "password123"));
        users.put("aman", new User("aman", "password123"));
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender && client.isLoggedIn()) {
                client.sendMessage(message);
            }
        }
    }

    public void privateMessage(String message, String recipient, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client.getUsername() != null && client.getUsername().equals(recipient)) {
                client.sendMessage("(Private from " + sender.getUsername() + "): " + message);
                return;
            }
        }
        // If we get here, recipient wasn't found
        sender.sendMessage("User '" + recipient + "' is not online.");
    }

    public boolean authenticateUser(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        if (clientHandler.getUsername() != null) {
            broadcast(clientHandler.getUsername() + " has left the chat.", null);
            updateOnlineUsers();
        }
    }

    public void updateOnlineUsers() {
        StringBuilder userList = new StringBuilder("ONLINE_USERS:");
        for (ClientHandler client : clients) {
            if (client.isLoggedIn()) {
                userList.append(client.getUsername()).append(",");
            }
        }

        String userListMessage = userList.toString();
        if (userListMessage.endsWith(",")) {
            userListMessage = userListMessage.substring(0, userListMessage.length() - 1);
        }

        for (ClientHandler client : clients) {
            if (client.isLoggedIn()) {
                client.sendMessage(userListMessage);
            }
        }
    }

    public static void main(String[] args) {
        int port = 8888;
        ChatServer server = new ChatServer(port);
        server.start();
    }
}