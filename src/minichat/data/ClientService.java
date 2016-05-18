/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat.data;

import java.net.Socket;
import java.util.List;

/**
 *
 * @author William
 */
public class ClientService {

    private static ClientService firstInstance = null;
    private ClientDao dbClients =null;
    
    private ClientService(){
        dbClients = ClientDao.getInstance();
    }
    
    public static ClientService getInstance() {
        if (firstInstance == null) {
            firstInstance = new ClientService();
        }
        return firstInstance;
    }

    public Client createClient(Socket connection, String name) {
       return dbClients.createClient(connection, name);
    }

    public String getClientStrId(Client client) {
        return String.valueOf(client.getId());
    }

    public List<Client> getClient() {
       return dbClients.getClients();
    }

    public Client getClientBySocket(Socket socket) {
        return dbClients.getClientBySocket(socket);
    }

    public void delete(Client client) {
        dbClients.delete(client);
    }

    public Client createClient(String name) {
       return dbClients.createClient(name);
    }

}
