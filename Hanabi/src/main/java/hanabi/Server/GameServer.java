package hanabi.Server;

import hanabi.Model.Board;
import hanabi.Model.Deck;
import hanabi.Model.MoveType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;



public class GameServer {
    private static Board BOARD;
    private int NUMBEROFPLAYERS = 7;
    private ServerSocket serverSocket;
    private AtomicInteger numOfPlayers;
    private static int PORT = 9999;
    private static ArrayList<ServerSideConnection> players;
    public GameServer(){
        System.out.println("[server started]");
        numOfPlayers= new AtomicInteger(0);
        try{
            serverSocket = new ServerSocket(PORT);
        }catch(IOException ex){
            ex.printStackTrace();
        }
        players = new ArrayList<>();
    }
    public void acceptConnections(){
        try{
            System.out.println("[waiting for connections]");
            while(numOfPlayers.get() < NUMBEROFPLAYERS){
                Socket client = serverSocket.accept();
                ServerSideConnection ssc = new ServerSideConnection(client,players, numOfPlayers.incrementAndGet());
                players.add(ssc);
                System.out.println("[new thread created]");
                Thread t = new Thread(ssc);
                t.start();
                System.out.println("[ready for another connection]");
                if(BOARD != null) NUMBEROFPLAYERS = BOARD.getPlayerAmount();
            }
            System.out.println("All players are in");
            sendToAll(BOARD);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
    private class ServerSideConnection implements Runnable{

        private Socket socket;
        private ArrayList<ServerSideConnection> connections;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private int playerID;
        private String playerName;

        public ServerSideConnection(Socket s, ArrayList<ServerSideConnection> playrs, int playerID){
            socket = s;
            connections = playrs;
            this.playerID = playerID;
            try {
                out = new ObjectOutputStream(s.getOutputStream());
                out.flush();
                in = new ObjectInputStream(s.getInputStream());
                out.writeInt(playerID);                out.flush();

                if(playerID == 1){
                    BOARD = (Board)in.readObject();
                }
                else{
                    this.playerName = (String) in.readObject();
                    BOARD.getPlayers().get(playerID-1).changeName(playerName);

                }
                System.out.println(this.playerName + " " + this.playerID);
                System.out.println(BOARD.toString());
            }catch(IOException | ClassNotFoundException ex){
                ex.printStackTrace();
            }
            System.out.println("done");
        }
        @Override
        public void run() {
            try {
                //sendToAll("Waiting for players");

                while (numOfPlayers.get() < NUMBEROFPLAYERS) {
                    Thread.onSpinWait();
                }
                while(true){
                    BOARD = (Board) in.readObject();
                    sendToAll(BOARD);
                    MoveType moveType = (MoveType) in.readObject();
                    sendToAll(moveType);
                }

            } catch (IOException | ClassNotFoundException ignored) {
                System.out.println("[Connection terminated]");
            }

        }

    }
    public void sendToAll(Object o) throws IOException {
        for(ServerSideConnection ssc : players){
            ssc.out.writeObject(o);
            ssc.out.flush();
        }
    }
    public static void main(String[] args){
        GameServer gs = new GameServer();
        gs.acceptConnections();
    }
}
