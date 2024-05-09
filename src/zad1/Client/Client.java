package zad1.Client;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {

    private CharBuffer buff = null;
    private final Charset charset = Charset.forName("ISO-8859-2");
    private SocketChannel channel = null;

    private final List<String> topics = new ArrayList<>();

    private final List<ClientView> eventListeners = new ArrayList<>();

    public Client() {
        String server = "localhost"; // adres hosta serwera
        int port = 12345; // numer portu

        try {
            // Utworzenie kanału
            channel = SocketChannel.open();

            // Ustalenie trybu nieblokującego
            channel.configureBlocking(false);

            // połączenie kanału
            channel.connect(new InetSocketAddress(server, port));

            System.out.print("Connecting to server...");

            while (!channel.finishConnect()) {
                System.out.print(".");
            }

        } catch (UnknownHostException exc) {
            System.err.println("Unknown host " + server);
            // ...
        } catch (Exception exc) {
            exc.printStackTrace();
            // ...
        }
    }

    public void run() throws IOException {

        System.out.println("Connected To Server...");
        Scanner scanner = new Scanner(System.in);
        int rozmiar_bufora = 1024;
        ByteBuffer inBuf = ByteBuffer.allocateDirect(rozmiar_bufora);


        System.out.println("Greeting The Server");
        sendMessage("HI@@@Client@@@");

        while (true) {
            inBuf.clear();
            int readBytes = channel.read(inBuf);
            if (readBytes == 0) {
                continue;
            } else if (readBytes == -1) {

                break;
            } else {
                inBuf.flip();
                buff = charset.decode(inBuf);

                String messageFromServer = buff.toString();

                String[] tokens = messageFromServer.split("@@@");

                System.out.println("Incoming Transmission: " + messageFromServer);
                buff.clear();

                switch (tokens[0]) {
                    case "BYE" -> exit();
                    case "TOPICS" -> {
                        if (topics.size() > 0) {
                            topics.clear();
                        }
                        topics.addAll(Arrays.asList(tokens).subList(1, tokens.length));
                        System.out.println("Topics: " + topics);
                    }
                    case "MESSAGE" -> {
                        System.out.println("Message from admin on topic: " + tokens[1] + " : " + tokens[2]);
                        for (ClientView listener : eventListeners) {
                            listener.appendText("Message on topic: " + tokens[1] + " : " + tokens[2] + "\n");
                        }
                    }
                }

            }

        }

        scanner.close();

    }

    public void subscribeTopic(String topic) throws IOException {
        topics.add(topic);
        sendMessage("SUBSCRIBE@@@" + topic + "@@@");
    }

    public void unsubscribedTopic(String topic) throws IOException {
        topics.remove(topic);
        sendMessage("UNSUBSCRIBE@@@" + topic + "@@@");
    }


    public List<String> getAllTopics() throws IOException {
        sendMessage("GET@@@TOPICS@@@");
        return topics;
    }

    public void exit() {
        try {
            sendMessage("BYE@@@CLIENT@@@");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public void sendMessage(String message) throws IOException {
        buff = CharBuffer.wrap(message + "\n");
        ByteBuffer outBuf = charset.encode(buff);
        channel.write(outBuf);
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        new ClientView(client);
        client.addEventListener(new ClientView(client));
        try {
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addEventListener(ClientView listener) {
        eventListeners.add(listener);
    }


}
