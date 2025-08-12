package client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientGUI {
    private final JFrame frame;
    private final JTextPane chatArea;
    private final JTextField messageField;
    private final JTextField serverField;
    private final JTextField portField;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton connectButton;
    private final JButton loginButton;
    private final JButton sendButton;
    private final JList<String> userList;
    private final DefaultListModel<String> userListModel;
    private final JButton privateMessageButton;

    private final ChatClient client;

    public ClientGUI() {
        client = new ChatClient(this);

        // Create the main window
        frame = new JFrame("Fick Talk");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Connection panel (top)
        JPanel connectionPanel = new JPanel(new FlowLayout());
        serverField = new JTextField("localhost", 10);
        portField = new JTextField("8888", 5);
        usernameField = new JTextField(10);
        passwordField = new JPasswordField(10);
        connectButton = new JButton("Connect");
        loginButton = new JButton("Login");
        loginButton.setEnabled(false);

        connectionPanel.add(new JLabel("Server:"));
        connectionPanel.add(serverField);
        connectionPanel.add(new JLabel("Port:"));
        connectionPanel.add(portField);
        connectionPanel.add(connectButton);
        connectionPanel.add(new JLabel("Username:"));
        connectionPanel.add(usernameField);
        connectionPanel.add(new JLabel("Password:"));
        connectionPanel.add(passwordField);
        connectionPanel.add(loginButton);

        // Chat panel (center)
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(250, 250, 250));
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // User list (right)
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0));
        privateMessageButton = new JButton("PM");
        privateMessageButton.setEnabled(false);

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(new JLabel("Online Users:"), BorderLayout.NORTH);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        userPanel.add(privateMessageButton, BorderLayout.SOUTH);

        // Message panel (bottom)
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setEnabled(false);
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        // Add all panels to the frame
        frame.add(connectionPanel, BorderLayout.NORTH);
        frame.add(chatPanel, BorderLayout.CENTER);
        frame.add(userPanel, BorderLayout.EAST);
        frame.add(messagePanel, BorderLayout.SOUTH);

        // Apply color theme
        applyColorTheme();

        // Add action listeners
        connectButton.addActionListener(e -> connect());
        loginButton.addActionListener(e -> login());
        sendButton.addActionListener(e -> sendMessage());
        privateMessageButton.addActionListener(e -> sendPrivateMessage());

        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isConnected()) {
                    client.disconnect();
                }
            }
        });

        // Show the frame
        frame.setVisible(true);
    }

    private void applyColorTheme() {
        Color backgroundColor = new Color(245, 245, 255);
        Color connectionPanelColor = new Color(220, 220, 255);
        Color userListColor = new Color(240, 248, 255);
        Color messagePanelColor = new Color(230, 230, 250);
        Color buttonColor = new Color(100, 149, 237); // cornflower blue
        Color buttonTextColor = Color.BLACK;

        frame.getContentPane().setBackground(backgroundColor);

        serverField.setBackground(Color.WHITE);
        portField.setBackground(Color.WHITE);
        usernameField.setBackground(Color.WHITE);
        passwordField.setBackground(Color.WHITE);
        messageField.setBackground(Color.WHITE);
        userList.setBackground(userListColor);

        // Buttons
        JButton[] buttons = {connectButton, loginButton, sendButton, privateMessageButton};
        for (JButton button : buttons) {
            button.setBackground(buttonColor);
            button.setForeground(buttonTextColor);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(65, 105, 225)); // Royal Blue on hover
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(buttonColor);
                }
            });
        }
    }

    private void connect() {
        String server = serverField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            displayMessage("Invalid port number");
            return;
        }

        connectButton.setEnabled(false);
        displayMessage("Connecting to " + server + ":" + port + "...");

        if (client.connect(server, port)) {
            serverField.setEnabled(false);
            portField.setEnabled(false);
            loginButton.setEnabled(true);
            displayMessage("Connected to server");
        } else {
            connectButton.setEnabled(true);
        }
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            displayMessage("Username and password cannot be empty");
            return;
        }

        loginButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        displayMessage("Logging in as " + username + "...");

        if (!client.login(username, password)) {
            loginButton.setEnabled(true);
            usernameField.setEnabled(true);
            passwordField.setEnabled(true);
            displayMessage("Failed to send login request");
        }
    }

    public void loginSuccessful() {
        SwingUtilities.invokeLater(() -> {
            messageField.setEnabled(true);
            sendButton.setEnabled(true);
            privateMessageButton.setEnabled(true);
            displayMessage("Login successful. Welcome to the chat!");
        });
    }

    public void loginFailed(String reason) {
        SwingUtilities.invokeLater(() -> {
            loginButton.setEnabled(true);
            usernameField.setEnabled(true);
            passwordField.setEnabled(true);
            displayMessage("Login failed: " + reason);
        });
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            displayMessage("(Me): " + message);
            messageField.setText("");
        }
    }

    private void sendPrivateMessage() {
        String selectedUser = userList.getSelectedValue();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame, "Please select a user from the list");
            return;
        }

        if (selectedUser.equals(client.getUsername())) {
            JOptionPane.showMessageDialog(frame, "You cannot send a private message to yourself");
            return;
        }

        String message = JOptionPane.showInputDialog(frame, "Enter message for " + selectedUser);
        if (message != null && !message.trim().isEmpty()) {
            client.sendPrivateMessage(selectedUser, message);
        }
    }

    public void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatArea.getStyledDocument();
            SimpleAttributeSet attributes = new SimpleAttributeSet();

            boolean isMyMessage = message.startsWith("(Me):") || message.contains("to [");

            // Simulate rounded bubble background colors
            Color bubbleColor = isMyMessage ? new Color(220, 248, 198) : new Color(255, 255, 255);
            Color textColor = Color.BLACK;

            // Apply styles for rounded corners
            StyleConstants.setForeground(attributes, textColor);
            StyleConstants.setBackground(attributes, bubbleColor);
            StyleConstants.setLeftIndent(attributes, isMyMessage ? 100 : 10);
            StyleConstants.setRightIndent(attributes, isMyMessage ? 10 : 100);
            StyleConstants.setAlignment(attributes, isMyMessage ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            StyleConstants.setSpaceAbove(attributes, 5);
            StyleConstants.setSpaceBelow(attributes, 5);
            StyleConstants.setFontSize(attributes, 14);
            StyleConstants.setFontFamily(attributes, "Segoe UI");

            // Extra padding for bubble effect
            chatArea.setMargin(new Insets(12, 12,12, 12));

            // Use a custom background with rounded corners (workaround)
            try {
                int length = doc.getLength();
                doc.insertString(length, message + "\n", attributes);
                doc.setParagraphAttributes(length, message.length(), attributes, false);
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }



    public void updateOnlineUsers(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.isEmpty()) {
                    userListModel.addElement(user);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
