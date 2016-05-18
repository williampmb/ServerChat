/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minichat.data;

import java.net.Socket;

/**
 *
 * @author William
 */
public class Client {

    private int id;
    private String name;
    private Socket connection;

    Client(Socket connection, String name, int id) {
        this.id = id;
        this.name = name;
        this.connection = connection;
    }

    Client(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Socket getConnection() {
        return connection;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }

}
