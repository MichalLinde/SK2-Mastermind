package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Objects;

public class GamePanel extends JLayeredPane implements ActionListener {
    //PRZYCISK KONCZACY TURE
    JButton endTurnButon;
    //TABLICA PRZECHOWUJACA PRZYCISKI DO UKLADANIA SZYFROW DLA GRACZA ODGADUJACEGO
    JButton[][] colorOrbs;
    //TABLICA PRZECHOWUJACA PANELE DO USTAWIANIA WARTOSCI PODPOWIEDZI
    JSpinner[][] clues;
    //TABLICA PRZECHOWUJACA PRZYCISKI DO ULOZENIA SZYFRU DLA GRACZA SZYFRUJACEGO
    JButton[] codeOrbs;
    //LISTA PRZECHOWUJACA KOLORY DO UKLADANIA SZYFROW
    ArrayList<Color> colors;
    //ZMIENNA OKRESLAJACA NUMER TURY GRACZA ODGADUJACEGO
    int guesserTurn = 0;
    //ZMIENNA OKRESLAJACA NUMER TURY GRACZA SZYFRUJACEGO
    int encryptorTurn = 0;
    //ZMIENNA OKRELSJACA RODZAJ GRACZA
    int player;
    //ZMIENNA OKRESLAJACA STAN GRY
    //0- GRA TRWA, 1- KONIEC GRY;
    int gameState = 0;
    //ZMIENNA PRZECHOWUJACA KOMBINACJE SZYFRU DO ODGADNIECIA;
    String cipher;
    //ZMIENNA PRZECHOWUJACA PODPOWIEDZI W FORMIE STRINGA
    String cluesFromMessage;
    //ZMIENNA PRZECHOWUJACA OSTATNI SZYFR GRACZA ODGADUJACEGO
    String guessFromMessage;
    //ZMIENNA PRZECHOWUJACA WIADOMOSC ODEBRANA
    String messageRecieved;
    //ZMIENNA PRZECHOWUJACA WIADOMOSC WYSYLANA
    String messageSent;
    //ZMIENNA PRZECHOWUJACA INFORMACJE O ODEBRANIU WIADOMOSCI OD PRZECIWNIKA
    boolean canPlay = false;

    boolean startTurn = false;

    Connection connection;

    //KONSTRUKTOR KLASY Z ARGUMENETEM OKRESLAJACYM RODZAJ GRACZA
    //gameMode: 0 - guesser, 1 - encryptor
    public GamePanel(int gameMode, Connection connection){
        this.player = gameMode;
        this.connection = connection;

        if (player == 1){
            canPlay = true;
        }


        //  UTWORZENIE PRZYCISKU KONCZACEGO TURE
        endTurnButon = new JButton("End Turn");
        endTurnButon.setBounds(380, 350, 100,30);
        endTurnButon.addActionListener(this);
        this.add(endTurnButon);
        if (player == 0){
            endTurnButon.setEnabled(false);
        }




        //  UTWORZENIE LISTY KOLOROW DO UKLADANIA KOMBINACJI
        colors = new ArrayList<Color>(){
            {
                add(Color.RED);
                add(Color.GREEN);
                add(Color.YELLOW);
                add(Color.PINK);
                add(Color.BLUE);
                add(Color.ORANGE);
            }
        };

        //  PRZYGOTOWANIE PRZYCISKOW DO UKLADANIA SZYFROW DLA GRACZA ODGADUJACEGO
        colorOrbs = new JButton[8][4];
        for (int i =0; i < 8; i++){
            for (int j = 0; j < 4; j++){
                colorOrbs[i][j] = new JButton();
                colorOrbs[i][j].setBounds(50 + j*70, 650 - 70*i, 60, 60);
                colorOrbs[i][j].setBackground(Color.WHITE);
                colorOrbs[i][j].addActionListener(this);
                colorOrbs[i][j].setEnabled(false);
                this.add(colorOrbs[i][j]);
            }
        }

        //  PRZYGOTOWANIE POL NA PODPOWIEDZI
        clues = new JSpinner[8][2];
        for (int i = 0; i < 8; i++){
            SpinnerModel value = new SpinnerNumberModel(0,0,4,1);
            clues[i][0] = new JSpinner(value);
            clues[i][0].setBounds(10, 650 - 70*i, 35,60 );
            if (player == 0 || encryptorTurn == 0){
                clues[i][0].setEnabled(false);
            }
            this.add(clues[i][0]);
            SpinnerModel value2 = new SpinnerNumberModel(0,0,4,1);
            clues[i][1] = new JSpinner(value2);
            clues[i][1].setBounds(325, 650 - 70*i, 35, 60);
            if (player == 0 || encryptorTurn == 0){
                clues[i][1].setEnabled(false);
            }
            this.add(clues[i][1]);
        }

        //  PRZYGOTOWANIE PRZYCISKOW DO ULOZENIA SZYFRU PRZEZ GRACZA SZYFRUJACEGO
        codeOrbs = new JButton[4];
        for (int i = 0; i < 4; i++) {
            codeOrbs[i] = new JButton();
            codeOrbs[i].setBounds(50 + i * 70, 50, 60, 60);
            codeOrbs[i].setBackground(Color.RED);
            codeOrbs[i].addActionListener(this);
            if (player != 1) {
                codeOrbs[i].setVisible(false);
                codeOrbs[i].setEnabled(false);
            }
            this.add(codeOrbs[i]);
        }

    }


    ///////////////////////////////////////////////////////////
    //  FUNKCJE I PROCEDURY UNIWERSALNE DLA OBU GRACZY   //////
    ///////////////////////////////////////////////////////////

    //  PROCEDURA BLOKUJACA PRZYCISKI NIEODPOWIEDNIE DLA DANEGO GRACZA LUB DANEJ TURY
    public void blockOrbs(JButton[][] orbs, int currentRow){
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 4; j++){
                if (i != currentRow){
                    orbs[i][j].setEnabled(false);
                }
            }
        }
    }

    //  PROCEDURA ZMIENIAJACA KOLOR PRZYCIKU PO JEGO KLIKNIECIU
    public void changeColor(JButton button){
        Color tempColor = button.getBackground();
        if (tempColor == Color.WHITE){
            button.setBackground(colors.get(0));
        } else{
            int tempIndex = colors.indexOf(tempColor);
            if (tempIndex != 5){
                button.setBackground(colors.get(tempIndex + 1));
            } else{
                button.setBackground(colors.get(0));
            }
        }
    }

    //  FUNKCJA ZWRACAJACA STAN GRY
    public int checkGameState(){
        if (guesserTurn >= 7){
            endTurnButon.setEnabled(false);
            setGameOver(cipher);
            for (JButton c: codeOrbs){
                c.setVisible(true);
            }
            return 1;
        } else {
            if (guesserTurnResult(guesserTurn).equals(cipher)){
                endTurnButon.setEnabled(false);
                blockOrbs(colorOrbs, -1);
                setGameOver(cipher);
                for (JButton c: codeOrbs){
                    c.setVisible(true);
                }
                return 1;
            } else{
                return 0;
            }
        }
    }

    public void setGameOver(String colorCombination){
        String[] colorsOrder = colorCombination.split("");
        Color tempColor;
        for (int j = 0; j < 4; j ++) {
            if (Objects.equals(colorsOrder[j], "r")) {
                tempColor = Color.RED;
            } else if (Objects.equals(colorsOrder[j], "b")) {
                tempColor = Color.BLUE;
            } else if (Objects.equals(colorsOrder[j], "y")) {
                tempColor = Color.YELLOW;
            } else if (Objects.equals(colorsOrder[j], "g")) {
                tempColor = Color.GREEN;
            } else if (Objects.equals(colorsOrder[j], "o")) {
                tempColor = Color.ORANGE;
            } else if (Objects.equals(colorsOrder[j], "p")) {
                tempColor = Color.PINK;
            } else {
                tempColor = Color.WHITE;
            }
            codeOrbs[j].setBackground(tempColor);
        }

        }

    public void disableAll(){
        if (gameState != 0){
            blockClues();
            endTurnButon.setEnabled(false);
        }
    }



    ////////////////////////////////////////////////////////
    //  FUNKCJE I PROCEDURY DLA GRACZA SZYFRUJACEGO   //////
    ////////////////////////////////////////////////////////

    //PROCEDURA WYKONUJACA POLECENIA ZWIAZANE Z ROZPOCZECIEM NOWEJ TURY POPRZEZ GRACZA SZYFRUJACEGO
    public void newTurnEncryptor(int turn){
        endTurnButon.setEnabled(true);
        for (int i = 0; i < 8; i++){
            if (i != turn -1){
                clues[i][0].setEnabled(false);
                clues[i][1].setEnabled(false);
            } else{
                clues[i][0].setEnabled(true);
                clues[i][1].setEnabled(true);
            }
        }
        if (encryptorTurn > 0) {
            //System.out.println(encryptorTurn);
            decodeMessageEncryptor(messageRecieved);
            setColors(guessFromMessage, encryptorTurn - 1);
            disableAll();
        }
    }

    //  FUNKCJA SPRAWDZAJACA CZY W UTWORZONYM SZYFRZE NIE MA BAZOWEGO KOLORU (BIALY), ZWRACA FALSE JESLI OWY WYSTEPUJE
    public boolean codeCheck(JButton[] code){
        for (int i = 0; i < 4; i++){
            if (code[i].getBackground() == Color.WHITE){
                return false;
            }
        }
        return true;
    }

    //  FUNKCJA ZWRACAJACA PODPOWIEDZI UTWORZONE W DANEJ TURZE W FORMIE STRINGA
    public String cluesForGuesser(int turnNumber){
        String cluesNumbers = "";
        if (turnNumber > 0) {
            cluesNumbers += clues[turnNumber - 1][0].getValue().toString();
            cluesNumbers += clues[turnNumber - 1][1].getValue().toString();
        }else {
            cluesNumbers += "00";
        }

        return cluesNumbers;
    }

    //  PROCEDURA USTAWIAJACA KOMBINACJE KOLOROW UTWORZONA PRZEZ GRACZA ODGADUJACEGO Z WYKORZYSTANIEM STRINGA SKLADAJACEGO SIE Z ZAKODOWANYCH WCZESNIEJ KOLOROW
    public void setColors(String colorCombination, int turn){
        String[] colorsOrder = colorCombination.split("");
        Color tempColor;
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 4; j ++){
                if (i == turn){
                    if (Objects.equals(colorsOrder[j], "r")){
                        tempColor = Color.RED;
                    } else if (Objects.equals(colorsOrder[j], "b")){
                        tempColor = Color.BLUE;
                    } else if (Objects.equals(colorsOrder[j], "y")){
                        tempColor = Color.YELLOW;
                    } else if (Objects.equals(colorsOrder[j], "g")){
                        tempColor = Color.GREEN;
                    } else if (Objects.equals(colorsOrder[j], "o")){
                        tempColor = Color.ORANGE;
                    } else if (Objects.equals(colorsOrder[j], "p")){
                        tempColor = Color.PINK;
                    } else{
                        tempColor = Color.WHITE;
                    }
                    colorOrbs[i][j].setBackground(tempColor);
                }
            }
        }
    }

    public void blockClues(){
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 2; j++){
                clues[i][j].setEnabled(false);
            }
        }
    }

    //  PROCEDURA WYKONUJACA POLECENIA ZWIAZANE Z ZAKONCZENIEM TURY PRZEZ GRACZA SZYFRUJACEGO
    public void encryptorEndTurn(){
        if (encryptorTurn == 0){
            if (!codeCheck(codeOrbs)){
                JOptionPane.showMessageDialog(this, "You must not leave white tile!");
            } else{
                for (int i = 0; i < 4; i++){
                    codeOrbs[i].setEnabled(false);
                }
                messageSent = createMessageEncryptor();
                //System.out.println(messageSent);
            }
        } else{

            //System.out.println(cluesForGuesser(encryptorTurn));
            //System.out.println(createMessageEncryptor());
            messageSent = createMessageEncryptor();
            //System.out.println(messageSent);
        }
        canPlay = false;
        blockClues();
        endTurnButon.setEnabled(false);
        //System.out.println(encryptorTurn);
    }

    //   FUNKCJA ZWRACAJACA STRINGA ZLOZONEGO Z PIERWSZYCH LITER KOLOROW ULOZONYCH PRZEZ GRACZA SZYFRUJACEGO
    public String encryptorCodeIntoString(){
        String code = "";
        for (int i = 0; i < 4; i ++){
            Color tempColor = codeOrbs[i].getBackground();
            if (tempColor == Color.BLUE){
                code += "b";
            } else if (tempColor == Color.GREEN){
                code += "g";
            } else if (tempColor == Color.ORANGE){
                code += "o";
            } else if (tempColor == Color.RED){
                code += "r";
            } else if (tempColor == Color.PINK){
                code += "p";
            } else if (tempColor == Color.YELLOW) {
                code += "y";
            }
        }
        return code;
    }

    //FUNCKJA TWORZACA WIADOMOSC DO WYSLANIA PO TURZE GRACZA SZYFRUJACAEGO
    public String createMessageEncryptor(){
        String message = "";
        message += "1";
        message += encryptorCodeIntoString();
        message += cluesForGuesser(encryptorTurn);
        return message;
    }

    //PROCEDURA DO ODSZYFROWANIA WIADOMOSCI OD GRACZA ODGADUJACEGO
    public void decodeMessageEncryptor(String message){
        String[] messageList = message.split("");
        String[] codeFromMessage = new String[4];
        for (int i = 2; i < 6; i++){
            codeFromMessage[i-2] = messageList[i];
        }
        guessFromMessage = String.join("", codeFromMessage);
        //System.out.println(guessFromMessage);
        gameState = Integer.parseInt(messageList[1]);
    }


    ////////////////////////////////////////////////////////
    //  FUNKCJE I PROCEDURY DLA GRACZA ODGADUJACEGO   //////
    ////////////////////////////////////////////////////////


    //PROCEDURA WYKONUJACA POLECENIA ZWIAZANE Z ROZPOCZECIEM NOWEJ TURY POPRZEZ GRACZA ODGADUJACEGO
    public void newTurnGuesser(JButton[][] orbs, int turn){
        endTurnButon.setEnabled(true);
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 4; j++){
                if (i != turn){
                    orbs[i][j].setEnabled(false);
                } else{
                    orbs[i][j].setEnabled(true);
                }
            }
        }
        if (gameState != 0){
            blockOrbs(colorOrbs, -1);
            endTurnButon.setEnabled(false);
        }
        decodeMessageGuesser(messageRecieved);
        setClues(cluesFromMessage);

    }

    //   FUNKCJA ZWRACAJACA STRINGA ZLOZONEGO Z PIERWSZYCH LITER KOLOROW ULOZONYCH PRZEZ GRACZA ZGADUJACEGO W DANEJ TURZE
    public String guesserTurnResult(int turnNumber){
        String result = "";
        for (int i = 0; i < 4; i ++){
            Color tempColor = colorOrbs[turnNumber][i].getBackground();
            if (tempColor == Color.BLUE){
                result += "b";
            } else if (tempColor == Color.GREEN){
                result += "g";
            } else if (tempColor == Color.ORANGE){
                result += "o";
            } else if (tempColor == Color.RED){
                result += "r";
            } else if (tempColor == Color.PINK){
                result += "p";
            } else if (tempColor == Color.YELLOW){
                result += "y";
            } else{
                result += "w";
            }
        }
        return result;
    }

    //  PROCEDURA USTAWIAJACA PODPOWIEDZI UTWORZONE PRZEZ GRACZA SZYFRUJACEGO Z WYKORZYSTANIEM STRINGA PRZECHOWUJACEGO PODPOWIEDZI
    public void setClues(String new_Clues){
        String[] newClues = new_Clues.split("");
        int a = Integer.parseInt(newClues[0]);
        int b = Integer.parseInt(newClues[1]);
        if (guesserTurn > 0) {
            clues[guesserTurn - 1][0].setValue(Integer.valueOf(a));
            clues[guesserTurn - 1][1].setValue(Integer.valueOf(b));
        }
    }

    //  PROCEDURA WYKONUJACA POLECENIA ZWIAZANE Z ZAKONCZENIEM TURY PRZEZ GRACZA ODGADUJACEGO
    public void guesserEndTurn(){
        blockOrbs(colorOrbs, -1);
        endTurnButon.setEnabled(false);
        String result = guesserTurnResult(guesserTurn);
        //System.out.println(result);
        newTurnGuesser(colorOrbs, guesserTurn + 1);
        gameState = checkGameState();
        if (gameState == 0){
            System.out.println("game in progress");
        } else if (gameState == 1) {
            System.out.println("game over");
        }
        messageSent = createMessageGuesser();
        //System.out.println(messageSent);
        guesserTurn += 1;
        canPlay = false;

    }

    //FUNKCJA ZWRACAJACA WIADOMOSC DO WYSLANIA PO ZAKONCZENIU TURY GRACZA ODGADUJACEGO
    public String createMessageGuesser(){
        String message ="";
        message += "0";
        message += Integer.toString(gameState);
        message += guesserTurnResult(guesserTurn);
        message += "0";
        return message;
    }

    //PROCEDURA DO ODSZYFROWANIA WIADOMOSCI OD GRACZA SZYFRUJACEGO
    public void decodeMessageGuesser(String message){
        if (message != null) {
            String[] messageList = message.split("");
            String[] codeMessage = new String[4];
            String[] cluesMessage = new String[2];


            for (int i = 1; i < 5; i++) {
                codeMessage[i - 1] = messageList[i];
            }
            cipher = String.join("", codeMessage);
            //System.out.println(cipher);

            cluesMessage[0] = messageList[5];
            cluesMessage[1] = messageList[6];

            cluesFromMessage = String.join("", cluesMessage);
            //System.out.println(cluesFromMessage);
        }
    }




    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == endTurnButon){
            if (player == 0 && canPlay) {
                guesserEndTurn();
                connection.sendMessage(messageSent);
            }
            else if (player == 1 && canPlay){
                encryptorEndTurn();
                connection.sendMessage(messageSent);
            }
        }

        // OKRESLENIE REAKCJI NA PRZYCISNIECIE PRZYCISKU ZWIAZANEGO Z UKLADANIEM SZYFROW
        else if (src == colorOrbs[0][0]){
            changeColor(colorOrbs[0][0]);
        }
        else if (src == colorOrbs[0][1]){
            changeColor(colorOrbs[0][1]);
        }
        else if (src == colorOrbs[0][2]){
            changeColor(colorOrbs[0][2]);
        }
        else if (src == colorOrbs[0][3]){
            changeColor(colorOrbs[0][3]);
        }
        else if (src == colorOrbs[1][0]){
            changeColor(colorOrbs[1][0]);
        }
        else if (src == colorOrbs[1][1]){
            changeColor(colorOrbs[1][1]);
        }
        else if (src == colorOrbs[1][2]){
            changeColor(colorOrbs[1][2]);
        }
        else if (src == colorOrbs[1][3]){
            changeColor(colorOrbs[1][3]);
        }
        else if (src == colorOrbs[2][0]){
            changeColor(colorOrbs[2][0]);
        }
        else if (src == colorOrbs[2][1]){
            changeColor(colorOrbs[2][1]);
        }
        else if (src == colorOrbs[2][2]){
            changeColor(colorOrbs[2][2]);
        }
        else if (src == colorOrbs[2][3]){
            changeColor(colorOrbs[2][3]);
        }
        else if (src == colorOrbs[3][0]){
            changeColor(colorOrbs[3][0]);
        }
        else if (src == colorOrbs[3][1]){
            changeColor(colorOrbs[3][1]);
        }
        else if (src == colorOrbs[3][2]){
            changeColor(colorOrbs[3][2]);
        }
        else if (src == colorOrbs[3][3]){
            changeColor(colorOrbs[3][3]);
        }
        else if (src == colorOrbs[4][0]){
            changeColor(colorOrbs[4][0]);
        }
        else if (src == colorOrbs[4][1]){
            changeColor(colorOrbs[4][1]);
        }
        else if (src == colorOrbs[4][2]){
            changeColor(colorOrbs[4][2]);
        }
        else if (src == colorOrbs[4][3]){
            changeColor(colorOrbs[4][3]);
        }
        else if (src == colorOrbs[5][0]){
            changeColor(colorOrbs[5][0]);
        }
        else if (src == colorOrbs[5][1]){
            changeColor(colorOrbs[5][1]);
        }
        else if (src == colorOrbs[5][2]){
            changeColor(colorOrbs[5][2]);
        }
        else if (src == colorOrbs[5][3]){
            changeColor(colorOrbs[5][3]);
        }
        else if (src == colorOrbs[6][0]){
            changeColor(colorOrbs[6][0]);
        }
        else if (src == colorOrbs[6][1]){
            changeColor(colorOrbs[6][1]);
        }
        else if (src == colorOrbs[6][2]){
            changeColor(colorOrbs[6][2]);
        }
        else if (src == colorOrbs[6][3]){
            changeColor(colorOrbs[6][3]);
        }
        else if (src == colorOrbs[7][0]){
            changeColor(colorOrbs[7][0]);
        }
        else if (src == colorOrbs[7][1]){
            changeColor(colorOrbs[7][1]);
        }
        else if (src == colorOrbs[7][2]){
            changeColor(colorOrbs[7][2]);
        }
        else if (src == colorOrbs[7][3]){
            changeColor(colorOrbs[7][3]);
        }
        else if (src == codeOrbs[0]){
            changeColor(codeOrbs[0]);
        }
        else if (src == codeOrbs[1]){
            changeColor(codeOrbs[1]);
        }
        else if (src == codeOrbs[2]){
            changeColor(codeOrbs[2]);
        }
        else if (src == codeOrbs[3]){
            changeColor(codeOrbs[3]);
        }

    }
}
