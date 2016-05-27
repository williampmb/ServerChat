/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import minichat.data.Client;

/**
 *
 * @author William
 */
public class ResponseHandler extends Thread {

    OutputStream wayOut = null;

    public ResponseHandler() {
    }

    public void run() {
        while (true) {
            String nextMsg = MiniChat.getNextMsg();

            if (!nextMsg.equals("")) {
                String[] full = nextMsg.split("->");
                switch (full[0]) {
                    case "newMsg":
                        sendMsg(full[1]);
                        break;
                    case "addPerson":
                        whoOnline();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void sendMsg(String full) {

       
        String time ="";

        time = getTag(full, "time");
        if (!time.equals("0")) {
            time = time.substring(0, 2) + ":" + time.substring(2, time.length());
        }

        int idClient = Integer.valueOf(getTag(full, "id"));
        String msg = getTag(full, "msg");
        String name = getTag(full, "name");
        String msgBuilder = "message:-" + name + "[" + time + "] : " + msg;

        List<Client> clients = minichat.MiniChat.clientService.getClient();
        for (Client c : clients) {
            if (c.getId() != idClient) {
                try {
                    wayOut = MiniChat.map.get(c.getId());

                    int length = msgBuilder.getBytes().length;
                    System.out.println(msgBuilder);

                    byte[] lengthBytes = MiniChat.intToBytes(length);
                    byte[] msgOutBytes = msgBuilder.getBytes();
                    byte[] fullBytes = MiniChat.concatenateBytes(lengthBytes, msgOutBytes);

                    wayOut.write(fullBytes);
                    wayOut.flush();

                } catch (IOException ex) {
                    Logger.getLogger(ResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void whoOnline() {
        List<Client> clients = MiniChat.clientService.getClient();
        String msgBuilder = "";
        for (Client c : clients) {
            msgBuilder += c.getName() + "--";
        }
        msgBuilder = "online:-" + msgBuilder;

        List<Client> c2 = minichat.MiniChat.clientService.getClient();
        for (Client c : c2) {

            try {
                wayOut = MiniChat.map.get(c.getId());

                int length = msgBuilder.getBytes().length;
                byte[] lengthBytes = MiniChat.intToBytes(length);
                byte[] msgOutBytes = msgBuilder.getBytes();
                byte[] fullBytes = MiniChat.concatenateBytes(lengthBytes, msgOutBytes);

                wayOut.write(fullBytes);
                wayOut.flush();

            } catch (IOException ex) {
                Logger.getLogger(ResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    //FIXME: change the tag of split: it brakes the message that comes  with : character
    private String getTag(String msg, String tag) {
        String[] tags = msg.split("/");
        for (int i = 0; i < tags.length; i++) {
            String[] flag = tags[i].split(":");
            if (flag[0].equals(tag)) {
                return flag[1];
            }
        }
        return null;
    }

}
