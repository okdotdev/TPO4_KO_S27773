package zad1.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;


public class Server {

    private final List<String> topics = new ArrayList<>();
    private final Map<SocketChannel, List<String>> users = new HashMap<>();


    Server() throws IOException {

        topics.add("topic1");
        topics.add("topic2");
        topics.add("topic3");

        // Utworzenie kanału gniazda serwera
        // i związanie go z konkretnym adresem (host+port)
        String host = "localhost";
        int port = 12345;
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(host, port));

        // Ustalenie trybu nieblokującego
        // dla kanału serwera gniazda
        serverChannel.configureBlocking(false);

        // Utworzenie selektora
        Selector selector = Selector.open();

        // Rejestracja kanału gniazda serwera u selektora
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Serwer: czekam ... ");

        // Selekcja gotowych operacji do wykonania i ich obsługa
        // w pętli dzialania serwera
        while (true) {

            // Selekcja gotowej operacji
            // To wywolanie jest blokujące
            // Czeka aż selektor powiadomi o gotowości jakiejś operacji na jakimś kanale
            selector.select();

            // Teraz jakieś operacje są gotowe do wykonania
            // Zbiór kluczy opisuje te operacje (i kanały)
            Set<SelectionKey> keys = selector.selectedKeys();

            // Przeglądamy "gotowe" klucze
            Iterator<SelectionKey> iter = keys.iterator();

            while (iter.hasNext()) {

                // pobranie klucza
                SelectionKey key = iter.next();

                // musi być usunięty ze zbioru (nie ma autonatycznego usuwania)
                // w przeciwnym razie w kolejnym kroku pętli "obsłużony" klucz
                // dostalibyśmy do ponownej obsługi
                iter.remove();

                // Wykonanie operacji opisywanej przez klucz
                if (key.isAcceptable()) { // połaczenie klienta gotowe do akceptacji

                    System.out.println("New client connected ...");
                    // Uzyskanie kanału do komunikacji z klientem
                    // accept jest nieblokujące, bo już klient czeka
                    SocketChannel cc = serverChannel.accept();

                    // Kanał nieblokujący, bo będzie rejestrowany u selektora
                    cc.configureBlocking(false);

                    // rejestrujemy kanał komunikacji z klientem
                    // do monitorowania przez ten sam selektor
                    cc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                    continue;
                }

                if (key.isReadable()) {  // któryś z kanałów gotowy do czytania

                    // Uzyskanie kanału na którym czekają dane do odczytania
                    SocketChannel cc = (SocketChannel) key.channel();

                    serviceRequest(cc);

                    // obsługa zleceń klienta
                    // ...
                    continue;
                }
                if (key.isWritable()) {  // któryś z kanałów gotowy do pisania

                    // Uzyskanie kanału
                    //  SocketChannel cc = (SocketChannel) key.channel();

                    // pisanie do kanału
                    //   Scanner scanner = new Scanner(System.in);
                    //  String message = scanner.next();
                    //   cc.write(charset.encode(message + "\n"));

                }

            }
        }

    }


    // Strona kodowa do kodowania/dekodowania buforów
    private static Charset charset = Charset.forName("ISO-8859-2");
    private static final int BSIZE = 1024;

    // Bufor bajtowy - do niego są wczytywane dane z kanału
    private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);

    // Tu będzie zlecenie do pezetworzenia
    private StringBuffer reqString = new StringBuffer();


    private void serviceRequest(SocketChannel sc) {
        if (!sc.isOpen()) return; // jeżeli kanał zamknięty

        System.out.print("Reading from client:" + sc.socket().getInetAddress());
        // Odczytanie zlecenia
        reqString.setLength(0);
        bbuf.clear();

        try {
            readLoop:
            // Czytanie jest nieblokujące
            while (true) {               // kontynujemy je dopóki
                int n = sc.read(bbuf);   // nie natrafimy na koniec wiersza
                if (n > 0) {
                    bbuf.flip();
                    CharBuffer cbuf = charset.decode(bbuf);
                    while (cbuf.hasRemaining()) {
                        char c = cbuf.get();
                        //System.out.println(c);
                        if (c == '\r' || c == '\n') break readLoop;
                        else {
                            //System.out.println(c);
                            reqString.append(c);
                        }
                    }
                }
            }

            String messageFromClient = reqString.toString();
            System.out.println(reqString);

            String[] tokens = messageFromClient.split("@@@");

            switch (tokens[0]) {
                case "HI" -> {
                    sc.write(charset.encode(CharBuffer.wrap("HI@@@SERVER@@@")));
                    users.put(sc, new ArrayList<>());
                    if (tokens[1].equals("ADMIN")) {
                        users.get(sc).add("ADMIN");
                    }

                }
                case "BYE" -> {
                    sc.write(charset.encode(CharBuffer.wrap("BYE@@@SERVER@@@")));
                    System.out.println("Goodbye TO Client: " + sc.socket().getInetAddress());
                    sc.close();
                    sc.socket().close();
                }
                case "GET" -> {
                    StringBuilder topicsStringBuilder = new StringBuilder();
                    for (String topic : this.topics) {
                        topicsStringBuilder.append(topic).append("@@@");
                    }
                    sc.write(charset.encode(CharBuffer.wrap("TOPICS@@@" + topicsStringBuilder)));
                    System.out.println("Topics sent to client: " + sc.socket().getInetAddress());
                }
                case "NEW" -> {
                    this.topics.add(tokens[1]);
                    System.out.println("Topic added: " + tokens[1]);
                }
                case "REMOVE" -> {
                    this.topics.remove(tokens[1]);
                    System.out.println("Topic removed: " + tokens[1]);
                }
                case "SEND" -> {
                    sendMessageToAllUsers(tokens[1], tokens[2]);
                    System.out.println("Message sent to all users in topic " + tokens[1]);
                }
                case "SUBSCRIBE" -> {
                    users.get(sc).add(tokens[1]);
                    System.out.println("User subscribed to topic: " + tokens[1]);
                }
                case "UNSUBSCRIBE" -> {
                    users.get(sc).remove(tokens[1]);
                    System.out.println("User unsubscribed from topic: " + tokens[1]);
                }
                default -> {
                    System.out.println("Unknown command");
                }
            }


        } catch (Exception exc) { // przerwane polączenie?
            exc.printStackTrace();
            try {
                sc.close();
                sc.socket().close();
            } catch (Exception e) {
            }
        }

    }


    private void sendMessageToAllUsers(String topic, String message) {
        for (Map.Entry<SocketChannel, List<String>> entry : users.entrySet()) {
            SocketChannel user = entry.getKey();
            List<String> userTopics = entry.getValue();
            if (userTopics.contains(topic)) {
                try {
                    user.write(charset.encode(CharBuffer.wrap("MESSAGE@@@" + topic + "@@@" + message + "@@@")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Server();
    }


}