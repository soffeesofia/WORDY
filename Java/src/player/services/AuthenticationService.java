package player.services;

import wordyGame.Player;

/**
 * This singleton is used to store the current player.
 */
public class AuthenticationService {
    /// Generate Singleton
    private static AuthenticationService instance = null;
    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    private Player _player;

    public Player getPlayer() {
        return _player;
    }

    public void setPlayer(Player player) {
        _player = player;
    }
}

