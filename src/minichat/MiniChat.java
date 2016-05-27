/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import minichat.data.ClientService;

/**
 *
 * @author William
 */
public class MiniChat {

    static ServerSocket server;
    static public ClientService clientService;
    static public List<String> msgs = new ArrayList<String>();
    static public Map<Integer, OutputStream> map = new HashMap<Integer,OutputStream>();
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
    
    public static byte[] intToBytes(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }
    
     public static String processRead(InputStream is) throws IOException {
        
        byte[] bufferSize = new byte[4];
        int byteSize = is.read(bufferSize);
        
        ByteBuffer wrapped = ByteBuffer.wrap(bufferSize);
        int size = wrapped.getInt();
            
        byte[] bufferMsg = new byte[size];
        int byteMsg = is.read(bufferMsg);
        String msgIn = new String(bufferMsg,0,byteMsg);
    
      
        return msgIn;
    }
     
      public static byte[] concatenateBytes(byte[] lengthBytes, byte[] msgOutBytes) {
        byte[] result = new byte[lengthBytes.length + msgOutBytes.length];
        System.arraycopy(lengthBytes, 0, result, 0, lengthBytes.length);
        System.arraycopy(msgOutBytes, 0, result, lengthBytes.length, msgOutBytes.length);
        return result;
    }

}
