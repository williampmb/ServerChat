/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import minichat.data.Client;
import static sun.print.ServiceDialog.getMsg;

/**
 *
 * @author William
 */
public class ResponseHandler extends Thread {

    DataOutputStream wayOut = null;

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

        int idClient = Integer.valueOf(getTag(full, "id"));
        String msg = getTag(full, "msg");
        String name = getTag(full, "name");
        String msgBuilder = "message:-" + name + " : " + msg;

        List<Client> clients = minichat.MiniChat.clientService.getClient();
        for (Client c : clients) {
            if (c.getId() != idClient) {
                try {
                    wayOut = MiniChat.map.get(c.getId());
                    wayOut.writeUTF(msgBuilder);
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
                wayOut.writeUTF(msgBuilder);
            } catch (IOException ex) {
                Logger.getLogger(ResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

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
