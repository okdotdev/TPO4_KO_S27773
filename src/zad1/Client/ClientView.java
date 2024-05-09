package zad1.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientView extends JFrame {
    private final Client client;
    private final JTextArea outputTextArea;

    public ClientView(Client client) throws IOException {
        super("Client View");

        this.client = client;
        //   client.getAllTopics();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);


        JPanel mainPanel = new JPanel(new BorderLayout());

        outputTextArea = new JTextArea(10, 30);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1));
        JButton subscribeToTopicButton = new JButton("Subscribe  topic");
        JButton unsubscribeTopicButton = new JButton("Unsubscribe topic");
        JButton displayAllTopicsButton = new JButton("Display all topics");
        JButton exitButton = new JButton("Exit");

        subscribeToTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String topic = JOptionPane.showInputDialog(null, "Enter new topic name:");
                try {
                    client.subscribeTopic(topic);
                    outputTextArea.append("Subscribed: " + topic + "\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        unsubscribeTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String topic = JOptionPane.showInputDialog(null, "Enter topic name to remove:");
                try {
                    client.unsubscribedTopic(topic);
                    outputTextArea.append("Unsubscribed: " + topic + "\n");
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
                    for (String topic : client.getAllTopics()) {

                        outputTextArea.append(topic + "\n");
                        System.out.println(topic);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.exit();
                dispose();
            }
        });

        buttonPanel.add(subscribeToTopicButton);
        buttonPanel.add(unsubscribeTopicButton);
        buttonPanel.add(displayAllTopicsButton);
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.EAST);

        getContentPane().add(mainPanel);

        setVisible(true);
    }

    public void appendText(String text) {
        outputTextArea.append(text);
    }
}
