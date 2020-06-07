package hanabi.Controller;

import hanabi.Server.ClientSideConnection;
import hanabi.Server.GameServer;
import javafx.application.Application;
import javafx.stage.Stage;

import static javafx.application.Platform.exit;

public class HanabiMain extends Application {
    public static GameInformation gameInformation = new GameInformation();
    public static ClientSideConnection csc;
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //new StartServer().start();
        boolean end;
        do {
            end=true;
            MultiplayerModeWindow.run();
            switch (MultiplayerModeWindow.action) {
                case 0: //forced exit
                    exit();

                case 1: //create a game
                    GameCreationWindow.run();
                    if(GameCreationWindow.end)
                        StartGame.run(stage);
                    else
                        end=false;
                    break;

                case 2: //join game
                    GameJoiningWindow.run();
                    if(GameJoiningWindow.end)
                        StartGame.run(stage);
                    else
                        end=false;
                    break;

                case 3: // exit
                    exit();

            }
        }while(!end);
    }

}