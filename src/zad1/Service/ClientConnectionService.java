package zad1.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class ClientConnectionService implements Runnable {

    private SocketChannel channel;
    private final String serverIp;
    private final int serverPort;
    private final Charset charset;
    private final Scanner scanner;
    private final ByteBuffer inputByteBuffer;

    private final IClient iClient;


    public ClientConnectionService(IClient iClient) {
        serverIp = "localhost";
        serverPort = 12345;
        charset = Charset.forName("ISO-8859-2");
        scanner = new Scanner(System.in);
        inputByteBuffer = ByteBuffer.allocateDirect(1024);
        this.iClient = iClient;
        connectToServer();
    }


    private void connectToServer() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(serverIp, serverPort));
            System.out.println("Waiting fo server connection ... ");
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + serverIp);
            // ...
        } catch (Exception e) {
            e.printStackTrace();
            // ...
        }
    }

    @Override
    public void run() {
        System.out.println("Running" + channel.socket().getRemoteSocketAddress());


        System.out.println("Sending Hand Shake To Server ...");
        try {
            channel.write(charset.encode("ADD@@@" + iClient.getType() + "@@@"));
            label:
            while (true) {
                inputByteBuffer.clear();
                int readBytes = channel.read(inputByteBuffer);
                CharBuffer charBuffer;
                if (readBytes == 0) {
                    continue;
                } else if (readBytes == -1) {
                    System.err.println("Server has closed the connection.");
                    // ...
                    break;
                } else {
                    inputByteBuffer.flip();
                    charBuffer = charset.decode(inputByteBuffer);
                    String message = charBuffer.toString();

                    System.out.println("Incoming Transmission From The Server: " + message);
                    charBuffer.clear();

                    switch (message) {
                        case "PING":
                            channel.write(charset.encode("PONG"));
                            break;
                        case "CLOSE":
                            System.out.println("Server has closed the connection.");
                            break label;
                        case "ADD":
                            System.out.println("Server has added you to the list.");
                            break;
                        case "REMOVE":
                            System.out.println("Server has removed you from the list.");
                            break;
                    }
                }

                String input = scanner.nextLine();
                charBuffer = CharBuffer.wrap(input + "\n");
                ByteBuffer outBuf = charset.encode(charBuffer);
                channel.write(outBuf);

                System.out.println("Sending Message To Server: " + input);
            }

        } catch (IOException e) {
            closeConnection();
            e.printStackTrace();
        }

        closeConnection();
        scanner.close();


    }

   


    private void closeConnection() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
