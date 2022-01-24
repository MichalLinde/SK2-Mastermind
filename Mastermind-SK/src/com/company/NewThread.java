package com.company;

public class NewThread extends Thread{
    Thread thread;
    Connection connection;
    GamePanel gamePanel;

    public NewThread(Connection connection, GamePanel gamePanel){
        this.connection = connection;
        this.gamePanel = gamePanel;
    }

    public void run(){
        while (!Thread.currentThread().isInterrupted()){
            //System.out.println("new thread!!!");
            if (!gamePanel.canPlay){
                try{
                    String message = this.connection.readMessage();
                    System.out.println(message);
                    gamePanel.messageRecieved = message;
                    if (gamePanel.player == 1){
                        gamePanel.encryptorTurn += 1;
                    } else if (gamePanel.player == 0){
                        System.out.println("received");
                    }
                    gamePanel.canPlay = true;
                    gamePanel.startTurn = true;
                } catch (Exception ex){
                    thread.interrupt();
                    thread.stop();
                    System.out.println("Thread failed! " + ex.getMessage());
                }
            } else{
                if (gamePanel.player == 0 && gamePanel.startTurn){         //gracz odgadujacy
                    gamePanel.newTurnGuesser(gamePanel.colorOrbs, gamePanel.guesserTurn);
                    gamePanel.startTurn = false;
                } else if (gamePanel.player == 1 && gamePanel.startTurn){  //gracz szyfrujacy
                    if (gamePanel.encryptorTurn > 0) {
                        gamePanel.newTurnEncryptor(gamePanel.encryptorTurn);
                        gamePanel.startTurn = false;
                    }
                }
            }
        }
    }

}
