package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private final ClientGUI gui;
    private boolean connected = false;

    public ChatClient(ClientGUI gui) {
        this.gui = gui;
    }

    public boolean connect(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            // Start a thread to receive messages
            new Thread(this::receiveMessages).start();

            return true;
        } catch (IOException e) {
            gui.displayMessage("Error connecting to server: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String username, String password) {
        if (!connected) {
            return false;
        }

        this.username = username;
        writer.println("LOGIN:" + username + ":" + password);
        return true;
    }

    public void sendMessage(String message) {
        if (connected && message != null && !message.trim().isEmpty()) {
            writer.println(message);
            System.out.println("DEBUG: Message sent: " + message); // Add this line
        }
    }

    public void sendPrivateMessage(String recipient, String message) {
        if (connected && recipient != null && message != null && !message.trim().isEmpty()) {
            writer.println("PRIVATE:" + recipient + ":" + message);
            gui.displayMessage("(Private to " + recipient + "): " + message);
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("DEBUG: Client received: " + message); // Add this line
                if (message.equals("LOGIN_SUCCESS")) {
                    gui.loginSuccessful();
                } else if (message.startsWith("LOGIN_FAILED:")) {
                    gui.loginFailed(message.substring("LOGIN_FAILED:".length()));
                } else if (message.startsWith("ONLINE_USERS:")) {
                    String[] users = message.substring("ONLINE_USERS:".length()).split(",");
                    gui.updateOnlineUsers(users);
                } else {
                    gui.displayMessage(message);
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.out.println("DEBUG: Exception in receiveMessages: " + e.getMessage()); // Add this line
                gui.displayMessage("Disconnected from server: " + e.getMessage());
                disconnect();
            }
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            gui.displayMessage("Error disconnecting: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getUsername() {
        return username;
    }
}