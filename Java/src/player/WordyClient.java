package player;

import constants.Constants;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import player.services.AuthenticationService;
import wordyGame.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class WordyClient extends Application {
    @FXML
    private Button startButton, closeButton, startGameButton,
            a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, enterButton;
    @FXML
    private PasswordField passwordLogin;
    @FXML
    private TextField usernameLogin;
    @FXML
    private Label globalUsername, answer, invalid;
    @FXML
    public AnchorPane HomePage, GamePage;
    @FXML
    private Pane buttonsPane;
    private String globalUserStr = null;
    private double xOffset = 0;
    private double yOffset = 0;
    static WordyInt impl;
    private AuthenticationService _authService = AuthenticationService.getInstance();


    @FXML
    void startPressed(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerLogInPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setTitle("Start Page");
        stage.setScene(scene);

        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
    }

    @FXML
    void closePressed(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    void loginPressed(ActionEvent event) throws IOException {
        String username = usernameLogin.getText();
        String password = passwordLogin.getText();
        Player p = new Player(username, password);
        boolean isLoggedIn = false;
        try {
            boolean val = impl.verifyLogin(p);
            isLoggedIn = val;
        } catch (invalidUser ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Failed");
            alert.setContentText("User not found.");
            alert.showAndWait();
        } catch (existingSession ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Existing Session");
            alert.setContentText("User is already logged in from another device ");
            alert.showAndWait();
        } catch (invalidCredentials ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Failed");
            alert.setContentText("Invalid credentials.");
            alert.showAndWait();
        }
        setGlobalUsername(username);
        if (!isLoggedIn) {
            usernameLogin.clear();
            passwordLogin.clear();
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerHomePage.fxml"));
            Parent root = loader.load();
            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setTitle("WORDY");
            _authService.setPlayer(p);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            stage.setOnCloseRequest(close -> {
                impl.logout(globalUsername.getText());
            });
            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });

            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
            Label usrLbl = (Label) root.lookup("#globalUsername");
            usrLbl.setText(globalUsername.getText());
        }

    }

    @FXML
    void startGamePressed(ActionEvent event) throws IOException {
        setGlobalUsername(globalUsername.getText());
        startGameButton.setDisable(true);
        Node node = (Node) event.getSource();
        Stage stageGame = (Stage) node.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerWaitingPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stageGame.setScene(scene);
        stageGame.show();
        waitingPage(stageGame);
    }

    void waitingPage(Stage stageGame) throws IOException {
        boolean noGame = false;
        try {
            impl.newGame(globalUsername.getText());
        } catch (noOtherPlayersAvailable ex) {
            noGame = true;
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Game Error");
            alert.setContentText("No players are available. Please try again later.");
            alert.showAndWait();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerHomePage.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stageGame.setScene(scene);
            Label usrLbl = (Label) root.lookup("#globalUsername");
            usrLbl.setText(getGlobalUsername());
            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });

            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stageGame.setX(event.getScreenX() - xOffset);
                    stageGame.setY(event.getScreenY() - yOffset);
                }
            });

            stageGame.show();


        }
        if (!noGame) {
            boolean isNotOngoing = false;
            while (impl.getGameState(globalUsername.getText())){
                isNotOngoing = impl.getGameState(globalUsername.getText());
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerMatchFound.fxml"));
            Parent root = loader.load();
            Stage stageMatch = stageGame;
            stageMatch.setTitle("WORDY");
            stageMatch.setScene(new Scene(root));
            stageMatch.show();

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> {
                try {
                    openGameplayPage(stageMatch);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);

                }
            });
            delay.play();
        }
    }
    void openGameplayPage(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerGamePlay.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Wordy | GAME START");
        stage.setScene(scene);

        char[] letters = impl.receiveLetters(globalUsername.getText());

        a = (Button) scene.lookup("#a");
        b = (Button) scene.lookup("#b");
        c = (Button) scene.lookup("#c");
        d = (Button) scene.lookup("#d");
        e = (Button) scene.lookup("#e");
        f = (Button) scene.lookup("#f");
        g = (Button) scene.lookup("#g");
        h = (Button) scene.lookup("#h");
        i = (Button) scene.lookup("#i");
        j = (Button) scene.lookup("#j");
        k = (Button) scene.lookup("#k");
        l = (Button) scene.lookup("#l");
        m = (Button) scene.lookup("#m");
        n = (Button) scene.lookup("#n");
        o = (Button) scene.lookup("#o");
        p = (Button) scene.lookup("#p");
        q = (Button) scene.lookup("#q");

        a.setText(String.valueOf(letters[0]));
        b.setText(String.valueOf(letters[1]));
        c.setText(String.valueOf(letters[2]));
        d.setText(String.valueOf(letters[3]));
        e.setText(String.valueOf(letters[4]));
        f.setText(String.valueOf(letters[5]));
        g.setText(String.valueOf(letters[6]));
        h.setText(String.valueOf(letters[7]));
        i.setText(String.valueOf(letters[8]));
        j.setText(String.valueOf(letters[9]));
        k.setText(String.valueOf(letters[10]));
        l.setText(String.valueOf(letters[11]));
        m.setText(String.valueOf(letters[12]));
        n.setText(String.valueOf(letters[13]));
        o.setText(String.valueOf(letters[14]));
        p.setText(String.valueOf(letters[15]));
        q.setText(String.valueOf(letters[16]));
        stage.show();

        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        impl.startGameTime(10, globalUsername.getText());
        Label timerLbl = (Label) root.lookup("#countdown");
        Timer gameplayTimer = new Timer();
        gameplayTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (impl.getGameTime(globalUsername.getText()) > 0) {
                    Platform.runLater(() -> timerLbl.setText(String.valueOf(impl.getGameTime(globalUsername.getText()))));
                } else {
                    gameplayTimer.cancel();
                    Platform.runLater(() -> {
                        try {
                            getRoundWinner(stage);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }
            }
        }, 1000, 1000);
    }

    void getRoundWinner(Stage stage) throws IOException {
        setGlobalUsername(globalUsername.getText());
        FXMLLoader loaderRound = new FXMLLoader(getClass().getResource("gui/PlayerWinLose.fxml"));
        Parent rootRound = loaderRound.load();
        Scene sceneRound = new Scene(rootRound);
        String roundWinner = null;
        try {
            roundWinner = impl.getRoundWin(getGlobalUsername());
        } catch (noWinner ex) {
            Label roundWnr = (Label) rootRound.lookup("#wlButton");
            roundWnr.setText("No Winner for this round!");
        } catch (drawWinners ex) {
            Label roundWnr = (Label) rootRound.lookup("#wlButton");
            roundWnr.setText("It's a draw!");
        }
        if(roundWinner != null){
            Label roundWnr = (Label) rootRound.lookup("#wlButton");
            roundWnr.setText("Player " +roundWinner+ " has won the round!");
            stage.setScene(sceneRound);
            stage.show();
        }
        stage.setScene(sceneRound);
        stage.show();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }


        if (!impl.getWinState(getGlobalUsername())) {
            try {
                impl.signalRoundEnd(globalUsername.getText());
                openGameplayPage(stage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try {
                openResult(stage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }


    }


    void openResult(Stage stage) throws IOException {
        String winner = impl.getGameWin(globalUsername.getText());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerResult.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        Label wlLbl = (Label) root.lookup("#RWLButton");
        Label winnerName = (Label) root.lookup("#playerWinner");
        if (!winner.equals(globalUsername.getText())){
            wlLbl.setText("LOST!");
        } else {
            wlLbl.setText("WON!");
        }
        winnerName.setText("Player " + winner + " has won the game!");
        stage.show();
    }
    @FXML
    void logoutPressed(ActionEvent event) throws IOException {
        Player player = _authService.getPlayer();
        if(player == null) {
            return;
        }
        impl.logout(player.username);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerStartPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setTitle("LoggedIn Page");
        stage.setScene(scene);
    }

    @FXML
    void quitGamePressed(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerHomePage.fxml"));
        Parent root = loader.load();
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setTitle("WORDY");
        Player p = _authService.getPlayer();
        String user = p.username;
        globalUsername = (Label) root.lookup("#globalUsername");
        globalUsername.setText(globalUserStr);
        globalUsername.setText(user);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void leaderboardBackBtn(ActionEvent event) throws IOException {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }


    @FXML
    void leaderboardBtn(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/leaderboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Leaderboard Page");
        stage.setScene(scene);
        stage.show();

        String[] topPlayers = impl.topPlayers();

        Word[] topWords = impl.topWords();

        TableView<String> leaderBoardView = (TableView<String>) root.lookup("#leaderBoardView");
        TableColumn<String, Integer> rankingColumn = (TableColumn<String, Integer>) leaderBoardView.getColumns().get(0);
        TableColumn<String, String> playerColumn = (TableColumn<String, String>) leaderBoardView.getColumns().get(1);

        TableView<String> wordBoardView = (TableView<String>) root.lookup("#leaderBoardViewone");
        TableColumn<String, String> wordColumn = (TableColumn<String, String>) wordBoardView.getColumns().get(0);
        TableColumn<String, String> playerWordColumn = (TableColumn<String, String>) wordBoardView.getColumns().get(1);

        leaderBoardView.getItems().clear();
        wordBoardView.getItems().clear();

        for (int i = 0; i < Math.min(topPlayers.length, 5); i++) {
            String player = topPlayers[i];
            int ranking = i + 1;
            leaderBoardView.getItems().add(ranking + ". " + player);
        }

        rankingColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(Integer.valueOf(data.getValue().split("\\.")[0])));
        rankingColumn.setEditable(false);

        playerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().split("\\.")[1]));
        playerColumn.setEditable(false);

        for (int i = 0; i < Math.min(topWords.length, 5); i++) {
            Word word = topWords[i];
            String player = word.username;
            String longestWord = word.wordSub;
            wordBoardView.getItems().add(longestWord + " - " + player);
        }

        wordColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().split(" - ")[0]));
        wordColumn.setEditable(false);

        playerWordColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().split(" - ")[1]));
        playerWordColumn.setEditable(false);
    }
    StringBuilder concatenatedText;
    @FXML
    void randomLetters(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();
        //if button is x then it will reset the buttons and the entered text on the label
        if (buttonText.equals("CLR")) {
            answer.setText("");
            invalid.setText("");
            concatenatedText = null;
            for (Node node : buttonsPane.getChildren()) {
                if (node instanceof Button) {
                    Button button = (Button) node;
                    button.setDisable(false);
                }
            }
            //else, button should be added in the pane and deactivated
        } else {
            if (concatenatedText == null) {
                concatenatedText = new StringBuilder();
            }
            concatenatedText.append(buttonText);
            answer.setText(concatenatedText.toString());
            for (Node node : buttonsPane.getChildren()) {
                if (node instanceof Button) {
                    clickedButton.setDisable(true);
                }
            }
        }
    }

    @FXML
    void enterPressed(ActionEvent event) {
        try {
            String enteredWord = answer.getText();
            Player player = _authService.getPlayer();
            String username = player.username;
            invalid = (Label) enterButton.getScene().lookup("#invalid");
            impl.submitWord(enteredWord, username);
            invalid.setText("GOOD JOB!");
        } catch (invalidWord e) {
            invalid.setText("INVALID WORD!");
        } catch (noSubmittedWord e) {
            invalid.setText("SUBMIT A WORD!");
        } catch (invalidLetters e) {
            invalid.setText("INVALID LETTERS!");
        }
    }

    @FXML
    void RcontinuePressed(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerHomePage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Node node = (Node) event.getSource();
        Label usrLbl = (Label) root.lookup("#globalUsername");
        usrLbl.setText(getGlobalUsername());
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setScene(scene);
        stage.show();

    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/PlayerStartPage.fxml"));
        Parent root = loader.load();
        stage.initStyle(StageStyle.UNDECORATED);

        root.setOnMousePressed((EventHandler<MouseEvent>) event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        Scene scene = new Scene(root);
        stage.setTitle("Wordy");
        stage.setScene(scene);

        startButton = (Button) scene.lookup("#startButton");
        startButton.setOnMouseEntered(event -> {
            startButton.setCursor(Cursor.HAND);
        });

        closeButton = (Button) scene.lookup("#closeButton");
        closeButton.setOnMouseEntered(event -> {
            closeButton.setCursor(Cursor.HAND);
        });

        stage.show();
    }
    public String getGlobalUsername() {
        return this.globalUserStr;
    }

    public void setGlobalUsername(String user) {
        this.globalUserStr = user;
    }

    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args,null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Constants.NAME_SERVICE_OBJECT_NAME);

            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            String name = "WordyApp";
            impl = WordyIntHelper.narrow(ncRef.resolve_str(name));
        } catch (Exception e) {
            System.out.println("Wordy has encountered an error! Please restart the program.");
        }
        launch(args);
    }


}
