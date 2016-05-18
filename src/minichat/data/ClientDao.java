/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat.data;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author William
 */
public class ClientDao {

    private static ClientDao firstInstance = null;
    private List<Client> clients = new ArrayList<Client>();
    static private int id = 0;

    private ClientDao() {

    }

    public static ClientDao getInstance() {
        if (firstInstance == null) {
            firstInstance = new ClientDao();
        }
        return firstInstance;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setDbCliets(List<Client> clients) {
        this.clients = clients;
    }

    public Client createClient(Socket connection, String name) {
        id++;
        Client newOne = new Client(connection, name, id);
        clients.add(newOne);
        return newOne;
    }

    Client getClientBySocket(Socket socket) {
        for(Client c : clients){
            if(socket == c.getConnection()){
                return c;
            }
            
        }
        return null;
    }

    void delete(Client client) {
       for(Client c : clients){
           if(client.getId() == c.getId()){
               clients.remove(c);
               return;
           }
       }
        System.out.println("Cliente Não Encontrado");
    }

}
