/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat.naobloqueante;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    static public Map<Integer, SocketChannel> map = new HashMap<Integer, SocketChannel>();
    private static boolean sendListOnline;
    private static String msgRule;

    String addTag = "/";
    static String nextMsg = "";
    static boolean hasNextMsg = false;
    static int idSender;
    static String msg;
    static String senderName;
    static String time;

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

                    String[] tags = nextMsg.split("->");
                    msgRule = tags[0];
                    if (msgRule.equals("newMsg")) {
                        System.out.println("tagsp[1]:" + tags[1]);
                        idSender = Integer.valueOf(getTag(tags[1], "id"));
                        msg = getTag(tags[1], "msg");
                        senderName = getTag(tags[1], "name");
                        time = getTag(tags[1], "time");
                        if (!time.equals("0")) {
                            time = time.substring(0, 2) + ":" + time.substring(2, time.length());
                        }

                        nextMsg = "message:-" + senderName + "[" + time + "]: " + msg;
                    }

                } else {
                    hasNextMsg = false;
                }

                processReadySet(selector.selectedKeys());

            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(ChatServerNonBlocking.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            Logger.getLogger(ChatServerNonBlocking.class.getName()).log(Level.SEVERE, null, e);

        }

    }

    private static void processReadySet(Set<SelectionKey> readySet) {
        Iterator iterator = readySet.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = (SelectionKey) iterator.next();
            iterator.remove();

            try {
                if (key.isAcceptable()) {
                    //TODO: eraese debug
                    System.out.println("conexao aceita");
                    ServerSocketChannel sschChannel = (ServerSocketChannel) key.channel();
                    SocketChannel sChannel = (SocketChannel) sschChannel.accept();
                    sChannel.configureBlocking(false);
                    sChannel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                    String msgIn = processRead(sChannel);

                    String[] tags = msgIn.split(":-");
                    if (tags.length < 2) {
                        continue;
                    }
                    System.out.println("Prazer, meu nome é : " + tags[1]);

                    Client client = clientService.createClient(tags[1]);
                    map.put(client.getId(), sChannel);

                    String msgOut = "registration:-your id:";
                    msgOut += client.getId();

                    ByteBuffer buffer1 = ByteBuffer.wrap(msgOut.getBytes());
                    int lengt = buffer1.array().length;
                    byte[] lengtBytes = intToBytes(lengt);
                    ByteBuffer buffer2 = ByteBuffer.wrap(lengtBytes);
                    sChannel.write(buffer2);
                    sChannel.write(buffer1);

                    Date date = new Date();
                    DateFormat format = new SimpleDateFormat("HHmm");
                    String time = format.format(date);

                    msgs.add("addPerson->id:0/time:" + time);

                    msgs.add("newMsg->id:0/name:Servidor/time:" + time + "/msg:o usuário " + client.getName() + " entrou na sala.");
                }
            } catch (IOException io) {
                System.out.println("IO na conexão");
            }

            if (key.isReadable()) {
                SocketChannel sChannel = (SocketChannel) key.channel();
                map.get(key);
                String newMsg = processRead(sChannel);
                switch (newMsg) {
                    case "Cancelled Channel":
                        break;
                    default:
                        System.out.println("Mensagem Recebida: " + newMsg);
                        newMsg = "newMsg->" + newMsg;
                        if (newMsg.length() > 0) {
                            msgs.add(newMsg);
                        }
                        break;

                }

            }

            try {
                if (key.isWritable() && hasNextMsg) {
                    SocketChannel scReceiver = (SocketChannel) key.channel();

                    switch (msgRule) {
                        case "newMsg":
                            SocketChannel scSender = map.get(idSender);
                            if (scSender == null || !scReceiver.getRemoteAddress().equals(scSender.getRemoteAddress())) {

                                ByteBuffer buffer1 = ByteBuffer.wrap(nextMsg.getBytes());
                                int lengt = buffer1.array().length;
                                byte[] lengtBytes = intToBytes(lengt);
                                ByteBuffer buffer2 = ByteBuffer.wrap(lengtBytes);
                                scReceiver.write(buffer2);
                                scReceiver.write(buffer1);
                            }

                            break;
                        case "addPerson":
                            String listOnline = whoOnline();

                            ByteBuffer buffer1 = ByteBuffer.wrap(listOnline.getBytes());
                            int lengt = buffer1.array().length;
                            byte[] lengtBytes = intToBytes(lengt);
                            ByteBuffer buffer2 = ByteBuffer.wrap(lengtBytes);
                            scReceiver.write(buffer2);
                            scReceiver.write(buffer1);

                            break;

                    }

                }
            } catch (IOException io) {

            } catch (CancelledKeyException cancelledKey) {
                System.out.println("Key cancelled");
            }

        }
    }

    private static String processRead(SocketChannel sChannel) {
        String msgIn = "NoMessage";
        try {
            ByteBuffer bufferlenght = ByteBuffer.allocate(4);
            bufferlenght.clear();

            int bytesCount = 0;
            while (bufferlenght.hasRemaining()) {
                bytesCount += sChannel.read(bufferlenght);
            }
            bufferlenght.flip();
            int lenght = bufferlenght.getInt();
            bufferlenght.clear();

            ByteBuffer bufferMsg = ByteBuffer.allocate(lenght);
            bufferMsg.clear();

            int bytesCountMsg = 0;
            while (bufferMsg.hasRemaining()) {
                bytesCountMsg += sChannel.read(bufferMsg);
            }

            bufferMsg.flip();
            msgIn = new String(bufferMsg.array());
            System.out.println("msgIn: " + msgIn);
            return msgIn;

        } catch (IOException io) {
            Set<Integer> keySet = map.keySet();
            Iterator<Integer> iterator = keySet.iterator();
            int idClientQuit = 0;
            while (iterator.hasNext()) {
                int next = (int) iterator.next();
                SocketChannel get = map.get(next);
                if (get.equals(sChannel)) {
                    idClientQuit = next;
                    break;
                }

            }
            Client client = clientService.getClient(idClientQuit);
            if (client != null) {

                msgs.add("newMsg->id:0/name:Servidor/msg:o usuário " + client.getName() + " desconectou.");
                clientService.delete(client);
                msgs.add("addPerson->");
            }
            try {
                sChannel.close();
            } catch (IOException ex) {
                System.out.println("ja fechado channel");
                Logger.getLogger(ChatServerNonBlocking.class.getName()).log(Level.SEVERE, null, ex);
            }
            msgIn = "Cancelled Channel";

        }
        return msgIn;
    }

    public static byte[] intToBytes(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    private static String getTag(String msg, String tag) {
        String[] tags = msg.split("/");
        for (int i = 0; i < tags.length; i++) {
            String[] flag = tags[i].split(":");
            if (flag[0].equals(tag)) {
                return flag[1];
            }
        }
        return "0";
    }

    private static String whoOnline() {
        List<Client> clients = clientService.getClient();
        String msgBuilder = "";
        for (Client c : clients) {
            msgBuilder += c.getName() + "--";
        }
        msgBuilder = "online:-" + msgBuilder;

        return msgBuilder;
    }

}
