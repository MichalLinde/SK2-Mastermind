package com.company;

import java.net.*;
import java.io.*;

public class Connection {
    Socket socket;

    public Connection(String ipAddress, int port){
        try{
            this.socket = new Socket(ipAddress, port);
        } catch (IOException e) {
            System.out.println("Failed to create socket! " + e.getMessage());
        }
    }

    public String readMessage(){
        BufferedReader reader;

        try {
            //System.out.println("before reading message");
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = reader.readLine();
            //System.out.println(message);
            //System.out.println("after reading message");
            return message;
        } catch (IOException e) {
            System.out.println("Failed to recieve message! " + e.getMessage());
            return "";
        }
    }

    public void sendMessage(String message){
        try {
            //System.out.println("before sending message");
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            byte[] bytes = message.getBytes();
            dataOutputStream.write(bytes);
            dataOutputStream.flush();
            //System.out.println(message);
            //System.out.println("after sending message");
        } catch (IOException e){
            System.out.println("Failed to send message! " + e.getMessage());
        }
    }

    public void closeSocket(){
        try {
            if (socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch (IOException e){
            System.out.println("Failed to close socket! " + e.getMessage());
        }
    }


}
