import sys
from omniORB import CORBA
import wordyGame


class WordyOperations:
    def __init__(self, username=None):
        self.username = username
        self.orb = CORBA.ORB_init(sys.argv, CORBA.ORB_ID)
        obj_ref = self.orb.string_to_object("corbaname::localhost:9999#WordyApp")
        self.server_obj = obj_ref._narrow(wordyGame.WordyInt)

    def verifyLogin(self, p):
        return self.server_obj.verifyLogin(p)
    def newGame(self, username):
        self.server_obj.newGame(username)
    def receiveLetters(self, username):
        return self.server_obj.receiveLetters(username)
    def submitWord(self, word, username):
        self.server_obj.submitWord(word, username)
    def getGameTime(self, username):
        return self.server_obj.getGameTime(username)
    def startGameTime(self, seconds, username):
        self.server_obj.startGameTime(seconds, username)
    def getGameState(self, username):
        return self.server_obj.getGameState(username)
    def getWinState(self, username):
        return self.server_obj.getWinState(username)
    def getRoundWin(self, username):
        return self.server_obj.getRoundWin(username)
    def signalRoundEnd(self, username):
        return self.server_obj.signalRoundEnd(username)
    def getGameWin(self, username):
        return self.server_obj.getGameWin(username)
    def logout(self, username):
        self.server_obj.logout(username)
    def topPlayers(self):
        return self.server_obj.topPlayers()
    def topWords(self):
        return self.server_obj.topWords()
