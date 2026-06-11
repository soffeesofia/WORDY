package objects;

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable {
    String gameID;
    List<String> playerList;
    char[] randomLetters;
    int seconds;
    List<String> winSeq;
    Map<String, String> submittedWords;
    String winner;

    public Game() {
        this.gameID = UUID.randomUUID().toString();
        this.playerList = new ArrayList<>();
        this.seconds = 0;
        this.randomLetters = new char[17];
        this.winSeq = new ArrayList<>();
        this.submittedWords = new HashMap<>();
        this.winner = "";
    }

    public String getGameID() {
        return gameID;
    }
    public void setGameID(String gameID) {
        this.gameID = gameID;
    }
    public List<String> getPlayerList() {
        return playerList;
    }
    public void setPlayerList(List<String> playerList) {
        this.playerList = playerList;
    }
    public List<String> getWinSeq() {
        return winSeq;
    }
    public void setWinSeq(List<String> winSeq) {
        this.winSeq = winSeq;
    }
    public String getWinner() {
        return winner;
    }
    public void setWinner(String winner) {
        this.winner = winner;
    }
    public Map<String, String> getSubmittedWords() {
        return submittedWords;
    }
    public void setSubmittedWords(Map<String, String> submittedWords) {
        this.submittedWords = submittedWords;
    }
    public char[] getRandomLetters() {
        return randomLetters;
    }
    public void setRandomLetters(char[] randomLetters) {
        this.randomLetters = randomLetters;
    }
    public int getSeconds() {
        return seconds;
    }
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
