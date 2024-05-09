package zad1.Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class AdminView extends JFrame {
    private final Admin admin;
    private final JTextArea outputTextArea;

    public AdminView(Admin admin) throws IOException {
        super("Admin View");

        this.admin = admin;
        admin.getAllTopics();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Center the window

        // Create components
        JPanel mainPanel = new JPanel(new BorderLayout());

        outputTextArea = new JTextArea(10, 30);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1));
        JButton addTopicButton = new JButton("Add new topic");
        JButton removeTopicButton = new JButton("Remove topic");
        JButton displayAllTopicsButton = new JButton("Display all topics");
        JButton sendMessageButton = new JButton("Send message to all users");
        JButton exitButton = new JButton("Exit");

        addTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String topic = JOptionPane.showInputDialog(null, "Enter new topic name:");
                try {
                    admin.addNewTopic(topic);
                    outputTextArea.append("New topic added: " + topic + "\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        removeTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String topic = JOptionPane.showInputDialog(null, "Enter topic name to remove:");
                try {
                    admin.removeTopic(topic);
                    outputTextArea.append("Topic removed: " + topic + "\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        displayAllTopicsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputTextArea.append("All topics:\n");
                try {
                    for (String topic : admin.getAllTopics()) {

                        outputTextArea.append(topic + "\n");
                        System.out.println(topic);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String topic = JOptionPane.showInputDialog(null, "Enter topic name:");
                String message = JOptionPane.showInputDialog(null, "Enter message:");
                try {
                    admin.sendMessageToAllUsers(topic, message);
                    outputTextArea.append("Message sent to all users in topic " + topic + "\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                admin.exit();
                dispose(); // Close the window
            }
        });

        buttonPanel.add(addTopicButton);
        buttonPanel.add(removeTopicButton);
        buttonPanel.add(displayAllTopicsButton);
        buttonPanel.add(sendMessageButton);
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.EAST);

        getContentPane().add(mainPanel);

        setVisible(true);
    }

}
