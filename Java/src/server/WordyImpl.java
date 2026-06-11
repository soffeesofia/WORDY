package server;

import objects.Game;
import wordyGame.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WordyImpl extends WordyIntPOA {
    private static final String VOWELS = "AEIOU";
    private static final String CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ";
    List<String> onlinePlayers = new ArrayList<>();
    List<String> waitingPlayers = new ArrayList<>();
    List<Game> ongoingGames = new ArrayList<>();
    List<Game> inWaitGames = new ArrayList<>();
    DatabaseConnection databaseConnection = new DatabaseConnection();
    private static final ReentrantLock roundLock = new ReentrantLock();
    private static final ReentrantLock endLock = new ReentrantLock();
    private static final ReentrantLock winLock = new ReentrantLock();

    /**
     * SERVER
     */


    /**
     * Stores the valid, submitted word in the SQL database
     * @param word submitted by the player
     * @param username of the player
     */
    public void storeWord(String word, String username) {
        try {
            Connection c = databaseConnection.getConnection();
            String query = "insert into words (username, words) values (?,?)";
            PreparedStatement ps = c.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setString(1, username);
            ps.setString(2, word);
            ps.executeUpdate();
            ps.close();
            c.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes game's random letters for the first round and add the game object into the list of ongoing games
     * @param game object which contains the char array of random letters
     */
    public void gamePrep (Game game){
        synchronized (ongoingGames) {
            ongoingGames.add(game);
        }
        System.out.println("New game: " + game.getGameID());
        System.out.println("With players: ");
        char i = 'a';
        for (String u : game.getPlayerList()){
            System.out.println(i + ". " + u);
            i++;
        }
        char[] lettersToSend = generateLetters();
        game.setRandomLetters(lettersToSend);
    }

    /**
     * Iterates through the list of ongoing games and finds the game object associated with the player
     * @param username of the player
     * @return game object of the player
     */
    public Game getGame (String username){
        Game game = null;
        for (Game g: ongoingGames){
            for (String u: g.getPlayerList()){
                if (u.equals(username)){
                    game = g;
                    break;
                }
            }
        }
        if (game != null){
            return game;
        } else {
            return null;
        }

    }

    /**
     * Generates a list of 17 random letters
     * @return char array of random letters
     */
    public char[] generateLetters() {
//        Random rand = new Random();
//        char[] letters = new char[17];
//        int vowelCount = 0;
//
//        // Generate 5 to 7 vowels
//        int numVowels = rand.nextInt(3) + 5;
//        for (int i = 0; i < numVowels; i++) {
//            int index = rand.nextInt(VOWELS.length());
//            letters[i] = VOWELS.charAt(index);
//            vowelCount++;
//        }
//
//        // Generate remaining consonants
//        for (int i = numVowels; i < 17; i++) {
//            int index = rand.nextInt(CONSONANTS.length());
//            letters[i] = CONSONANTS.charAt(index);
//        }
//
//        // Shuffle letters randomly
//        for (int i = 0; i < letters.length; i++) {
//            int index = rand.nextInt(letters.length);
//            char temp = letters[i];
//            letters[i] = letters[index];
//            letters[index] = temp;
//        }
        char[] letters = { 'W', 'R', 'S', 'I', 'T', 'C', 'O', 'U', 'S', 'N', 'A', 'B', 'L', 'E', 'D', 'F', 'G'};
        return letters;
    }

    /**
     * Determines if the submitted word is valid or not
     * @param enteredWord from the player
     * @return true if the word is in the list defined under file 'words.txt'; false if the word has less than 5 letters
     * or is not listed under 'words.txt'
     */
    public boolean verifyWord(String enteredWord){
        if (enteredWord.length() < 5){
            return false;
        } else {
            String currentLine;
            try (FileReader fileReader = new FileReader("words.txt");
                 BufferedReader bufferedReader = new BufferedReader(fileReader)){
                while ((currentLine = bufferedReader.readLine()) != null){
                    if (enteredWord.equalsIgnoreCase(currentLine)){
                        return true;
                    }
                }
                return false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Determines if the submitted word contains a letter that is not part of the generated array of random letters
     * @param word submitted by the player
     * @param username of the player
     * @return true if the word has no extra letters; false if otherwise
     */
    public boolean hasLetters (String word, String username){
        Game game =  getGame(username);
        char[] letters = game.getRandomLetters();
        char[] entered = word.toCharArray();
        for(char e : entered){
            boolean found = false;
            for (char l : letters){
                if (Character.toUpperCase(e) == Character.toUpperCase(l)){
                    found = true;
                    break;
                }
            }
            if (!found){
                return false;
            }
        }
        return true;

    }

    /**
     * Increments the number of wins of a player in the SQL database
     * @param username of the player
     */
    public void incrementGameWin(String username) {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            Connection connection = databaseConnection.getConnection();
            String query = "UPDATE accounts SET wins = wins + 1 WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }

    /**
     * CLIENT
     */


    /**
     * Verifies the login credentials of the connecting client
     * @param player object that defines the client's username and password
     * @return true if the credentials exist within the SQL database
     * @throws invalidCredentials if the given credentials is erroneous
     * @throws invalidUser if the given credentials do not exist in the SQL database
     * @throws existingSession of the connecting client is logged in from another device
     */
    @Override
    public boolean verifyLogin(Player player) throws invalidCredentials, invalidUser, existingSession {
        try{
            boolean foundUsername = false;
            Connection c = databaseConnection.getConnection();
            ArrayList<Player> players = new ArrayList<>();
            String query = "SELECT * FROM accounts ORDER BY username";
            Statement s = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = s.executeQuery(query);

            while(rs.next()){
                Player p = new Player(rs.getString(1), rs.getString(2));
                players.add(p);
            }
            rs.close();
            for (String u :onlinePlayers){
                if (u.equals(player.username)){
                    throw new existingSession("You are already logged in from another console");
                }
            }
            for (Player p : players){
                if (p.username.equals(player.username)){
                    foundUsername = true;
                    if (!(p.password.equals(player.password))){
                        throw new invalidCredentials("Invalid Credentials");
                    }
                    break;
                }
            }

            if (!foundUsername){
                throw new invalidUser("User not Found.");
            }
            onlinePlayers.add(player.username);
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Creates a new game object if there are no existing games or adds a player into a waiting list
     * @param username of the player
     * @throws noOtherPlayersAvailable if there are no other connecting players
     */
    @Override
    public void newGame(String username) throws noOtherPlayersAvailable {
        if (inWaitGames.size() == 0 ){
            Game g = new Game();
            g.getPlayerList().add(username);
            inWaitGames.add(g);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                long elapsedTime = 0;
                while (elapsedTime < 10000) {
                    synchronized (g) {
                        g.getPlayerList().addAll(waitingPlayers);
                        synchronized (waitingPlayers) {
                             waitingPlayers.removeAll(g.getPlayerList());
                        }
                    }
                    elapsedTime = System.currentTimeMillis() - startTime;
                }
            });

            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                System.out.println("Executor was interrupted: " + e.getMessage());
            }
            inWaitGames.remove(g);
            if (g.getPlayerList().size() < 2){
                throw new noOtherPlayersAvailable("No other players available.");
            } else {
                gamePrep(g);
            }
        } else {
            waitingPlayers.add(username);
        }

    }

    /**
     * Retrieves the game's set of generated letters
     * @param username of the player
     * @return char array of random letters
     */
    @Override
    public char[] receiveLetters(String username) {
        Game game = getGame(username);
        return game.getRandomLetters();
    }

    /**
     * Determine the validity of a player's submitted word
     * @param word submitted by the player
     * @param username of the player
     * @throws invalidWord if the word has less than letters or if it is not found in the defined list of words
     * @throws noSubmittedWord if the player does not submit a word
     * @throws invalidLetters if player submits a word with letters that are not part of the set of random letters
     */
    @Override
    public void submitWord(String word, String username) throws invalidWord, noSubmittedWord, invalidLetters {
        if (word == null || word.isEmpty()) {
            throw new noSubmittedWord("No Submitted Word");
        } else if (!hasLetters(word, username)) {
            throw new invalidLetters("Invalid letters");
        } else if (!verifyWord(word)) {
            throw new invalidWord("Invalid word");
        } else if (verifyWord(word)){
            Game g = getGame(username);
            g.getSubmittedWords().put(username, word.toLowerCase());
            storeWord(word.toLowerCase(), username);
        }

    }

    /**
     * Returns the current value of a game's timer
     * @param username of the player
     * @return seconds left on the game's timer
     */
    @Override
    public int getGameTime(String username){
        Game game = getGame(username);
        return game.getSeconds();
    }

    /**
     * Creates a countdown for the game's timer
     * @param secs suration of the timer
     * @param username of the player
     */
    @Override
    public void startGameTime(int secs, String username) {
        Game game = getGame(username);
        Thread countdownThread = new Thread(() -> {
            for (int i = secs; i >= 0; i--) {
                game.setSeconds(i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        countdownThread.start();
    }

    /**
     * Determines if a game is ongoing
     * @param username of the player
     * @return true if the game is not ongoing; false if the game is ongoing
     */
    @Override
    public boolean getGameState(String username) {
        Game game = getGame(username);
        if (game == null){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines of the game has a winner
     * @param username of the player
     * @return tru of the game has a winner; false if otherwise
     */
    @Override
    public boolean getWinState(String username) {
        Game game = getGame(username);
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < game.getWinSeq().size(); i++) {
            String winner = game.getWinSeq().get(i);
            if (counts.containsKey(winner)) {
                counts.put(winner, counts.get(winner) + 1);
            } else {
                counts.put(winner, 1);
            }
        }
        for (Map.Entry<String, Integer> winCount : counts.entrySet()) {
            int count = winCount.getValue() / game.getPlayerList().size();
            if (count >= 3 && !winCount.getKey().equals("null")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the winner of the current round through iterating the game's list of submitted words
     * @param username of the player
     * @return the username of the player with the longest submitted word
     * @throws noWinner if all players have not submitted a word
     * @throws drawWinners if two or more players submitted the longest word
     */
    @Override
    public String getRoundWin(String username) throws noWinner, drawWinners {
        String roundWinner = null;
        Game game = getGame(username);
        List<String> roundWinners = new ArrayList<>();

        roundLock.lock();
        try {
            if (!game.getSubmittedWords().isEmpty()) {
                Map<String, Integer> userLengths = new HashMap<>();
                for (Map.Entry<String, String> wordUser : game.getSubmittedWords().entrySet()) {
                    String word = wordUser.getValue();
                    int wordLength = word.length();
                    userLengths.put(wordUser.getKey(), wordLength);
                }
                int max = Integer.MIN_VALUE;
                for (Map.Entry<String, Integer> userToLength : userLengths.entrySet()) {
                    if (userToLength.getValue() > max) {
                        roundWinners.clear();
                        roundWinners.add(userToLength.getKey());
                        max = userToLength.getValue();
                    } else if (userToLength.getValue() == max) {
                        roundWinners.add(userToLength.getKey());
                    }
                }
                if (roundWinners.size() > 1) {
                    game.getWinSeq().add("null");
                    throw new drawWinners("Draw detected");
                } else {
                    roundWinner = roundWinners.get(0);
                    game.getWinSeq().add(roundWinner);
                }
            } else {
                game.getWinSeq().add("null");
                throw new noWinner("No winners detected");
            }
        } finally {
            roundLock.unlock();
        }

        return roundWinner;
    }

    /**
     * Resets a game's list of submitted words and generates a new set of random letters
     * @param username of the player
     */
    @Override
    public void signalRoundEnd(String username) {
        endLock.lock();
        try{
            Game game = getGame(username);
            Map<String, String> submittedWords = game.getSubmittedWords();
            submittedWords.clear();
            game.setSubmittedWords(submittedWords);
            char[] clearLetters = game.getRandomLetters();
            Arrays.fill(clearLetters, '\u0000');
            game.setRandomLetters(clearLetters);
            char[] newLetters = generateLetters();
            game.setRandomLetters(newLetters);
        }finally {
            endLock.unlock();
        }
    }


    /**
     * Calculates the overall winner of the game
     * @param username of the player
     * @return username of the game winner
     */
    @Override
    public String getGameWin(String username) {
        String gameWinner = null;
        Game game = getGame(username);
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < game.getWinSeq().size(); i++) {
            String winner = game.getWinSeq().get(i);
            if (counts.containsKey(winner)) {
                counts.put(winner, counts.get(winner) + 1);
            } else {
                counts.put(winner, 1);
            }
        }
        for (Map.Entry<String, Integer> winCount : counts.entrySet()) {
            int count = winCount.getValue() / game.getPlayerList().size();
            if (count >= 3 && !winCount.getKey().equals("null")) {
                gameWinner = winCount.getKey();
            }
        }
        if (gameWinner.equals(username)){
            incrementGameWin(gameWinner);
        }
        return gameWinner;
    }

    /**
     * Removes player from the list of online players
     * @param username of the player
     */
    @Override
    public void logout(String username) {
        onlinePlayers.remove(username);
        System.out.println("User " + username + " has logged out");
    }

    /**
     * Retrieves a list of the players with the most wins
     * @return string array of the top 5 players
     */
    @Override
    public String[] topPlayers() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        Connection c = databaseConnection.getConnection();
        String query = "SELECT username FROM accounts ORDER BY wins DESC LIMIT 5";
        Statement statement = null;
        try {
            statement = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = statement.executeQuery(query);

            List<String> topPlayersList = new ArrayList<>();
            while (rs.next()) {
                String username = rs.getString("username");
                topPlayersList.add(username);
            }
            rs.close();
            statement.close();
            return topPlayersList.toArray(new String[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a list of the longest submitted words
     * @return array of Word objects of the top 5 longest words
     */
    @Override
    public Word[] topWords() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        Connection c = databaseConnection.getConnection();
        String query = "SELECT DISTINCT words, username FROM words ORDER BY LENGTH(words) DESC LIMIT 5";
        Statement statement = null;
        try {
            statement = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = statement.executeQuery(query);
            List<Word> wordList = new ArrayList<>();
            while (rs.next()) {
                String word = rs.getString("words");
                String username = rs.getString("username");
                Word wordObj = new Word(username, word);
                wordList.add(wordObj);
            }
            rs.close();
            statement.close();
            return wordList.toArray(new Word[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
