/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import minichat.data.ClientService;

/**
 *
 * @author William
 */
public class MiniChat {

    static ServerSocket server;
    static public ClientService clientService;
    static public List<String> msgs = new ArrayList<String>();
    static public Map<Integer, DataOutputStream> map = new HashMap<Integer,DataOutputStream>();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            System.out.println("Blokcing");
            server = new ServerSocket(9000);
            clientService = ClientService.getInstance();
           
            
            ResponseHandler rep = new ResponseHandler();
            rep.start();
            
            while (true) {
                Socket connected = waitForConnection();

                RequestHandler rh = new RequestHandler(connected);
                rh.start();
                
               // ResponseHandler2 response = new ResponseHandler2(connected);
                //response.start();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Socket waitForConnection() {
        Socket connection = null;
        try {
            connection = server.accept();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    static void addMsg(String in) {
        
        System.out.println("adicionar a msg: " + in + " - dentro das mensagens");
        msgs.add(in);
    }

    static synchronized String getNextMsg() {
        if(msgs.isEmpty()){
            return "";
        }
        
        String nextMsg = msgs.get(0);
        msgs.remove(0);
        return nextMsg;
    }

}
