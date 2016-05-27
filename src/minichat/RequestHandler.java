/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import minichat.data.Client;
import static minichat.naobloqueante.ChatServerNonBlocking.msgs;

/**
 *
 * @author William
 */
class RequestHandler extends Thread {

    Socket socket;
    String addTag = "/";
    Client client;

    RequestHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            String msgIn = MiniChat.processRead(is);

            String[] tags = msgIn.split(":-");

            client = MiniChat.clientService.createClient(socket, tags[1]);
            String msgOut = "registration:-your id:";
            msgOut += client.getId();
            MiniChat.map.put(client.getId(), os);

            int length = msgOut.getBytes().length;
            System.out.println("lenght:" + length);
            System.out.println("msg:" + msgOut);

            byte[] lengthBytes = MiniChat.intToBytes(length);
            byte[] msgOutBytes = msgOut.getBytes();
            byte[] fullBytes = MiniChat.concatenateBytes(lengthBytes, msgOutBytes);

            os.write(fullBytes);
            os.flush();

            Date date = new Date();
            DateFormat format = new SimpleDateFormat("HHmm");
            String time = format.format(date);
            
            

            MiniChat.addMsg("addPerson->id:0/time:" + time);

            MiniChat.addMsg("newMsg->id:0/name:Servidor/time:" + time + "/msg:o usuário " + client.getName() + " entrou na sala.");

            while (true) {
                String in = MiniChat.processRead(is);

                in = "newMsg->" + in;
                MiniChat.addMsg(in);
            }

        } catch (Exception e) {
            Date date = new Date();
            DateFormat format = new SimpleDateFormat("HHmm");
            String time = format.format(date);

            MiniChat.addMsg("newMsg->id:0/name:Servidor/time:" + time + "/msg:o usuário " + client.getName() + " desconectou.");
            MiniChat.clientService.delete(client);
            MiniChat.addMsg("addPerson->id:0/time:" + time);
        }
    }

}
