/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat.naobloqueante;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import minichat.data.Client;
import minichat.data.ClientService;

/**
 *
 * @author William
 */
public class ChatServerNonBlocking {

    static public ClientService clientService;
    static public List<String> msgs = new ArrayList<String>();
    static public Map<SocketChannel, Integer> map = new HashMap<SocketChannel, Integer>();
    String addTag = "/";
    Client client;
    static String nextMsg = "";
    static boolean hasNextMsg = false;

    public static void main(String[] args) {
        System.out.println("Non-Blocking");
        clientService = ClientService.getInstance();
        InetAddress hostIPAddress;
        try {
            hostIPAddress = InetAddress.getByName("localhost");

            int port = 9001;

            Selector selector = Selector.open();

            ServerSocketChannel ssChannel = ServerSocketChannel.open();

            ssChannel.configureBlocking(false);

            ssChannel.socket().bind(new InetSocketAddress(hostIPAddress, port));

            ssChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {

                if (selector.select() <= 0) {
                    continue;
                }
                if (!msgs.isEmpty()) {
                    nextMsg = msgs.get(0);
                    msgs.remove(0);
                    hasNextMsg = true;
                } else {
                    hasNextMsg = false;
                }
                //TODO: erease sout
                System.out.println("It's new msg: " + hasNextMsg + " the new msg: " + nextMsg);
                processReadySet(selector.selectedKeys());

            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(ChatServerNonBlocking.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            Logger.getLogger(ChatServerNonBlocking.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private static void processReadySet(Set<SelectionKey> readySet) throws IOException {
        Iterator iterator = readySet.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = (SelectionKey) iterator.next();
            iterator.remove();

            if (key.isAcceptable()) {
                ServerSocketChannel sschChannel = (ServerSocketChannel) key.channel();
                SocketChannel sChannel = (SocketChannel) sschChannel.accept();
                sChannel.configureBlocking(false);
                sChannel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                String msgIn = processRead(sChannel);
                String[] tags = msgIn.split(":-");
                System.out.println("Prazer, meu nome é : " + tags[1]);

                Client client = clientService.createClient(tags[1]);
                map.put(sChannel, client.getId());
                String msgOut = "registration:-your id:";
                msgOut += client.getId();
                ByteBuffer buffer = ByteBuffer.wrap(msgOut.getBytes());
                sChannel.write(buffer);
            }

            if (key.isReadable()) {
                String newMsg = processRead(key);
                System.out.println("Mensagem Recebida: " + newMsg);
                if (newMsg.length() > 0) {
                    msgs.add(newMsg);
                }
            }
            if (key.isWritable() && hasNextMsg) {
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer = ByteBuffer.wrap("Enviando resposta".getBytes());
                sc.write(buffer);
            }
        }
    }

    private static String processRead(SelectionKey key) throws IOException {
        SocketChannel sChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesCount = sChannel.read(buffer);
        if (bytesCount > 0) {
            buffer.flip();
            return new String(buffer.array());
        }
        return "NoMessage";
    }

    private static String processRead(SocketChannel sChannel) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesCount = sChannel.read(buffer);
        if (bytesCount > 0) {
            buffer.flip();
            return new String(buffer.array());
        }
        return "NoMessage";
    }
}
