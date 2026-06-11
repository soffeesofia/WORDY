import keyboard
import PlayerClass
from WordyOperations import *
import sys
import threading
import time

# Global variable to store the username
client_username = ""
client = WordyOperations()


def main_menu():
    print("Welcome " + client_username + "!")
    print("Menu:")
    print("1. Start Game")
    print("2. Display Top 5 Players and Words")
    print("3. Logout and Quit")
    print()


def login_menu():
    print("===---WORDY---===")
    print("1. Login")
    print("2. Exit Game")
    print("===---====---===")
    print()


def logout():
    print("Logging out")
    try:
        client.logout(client_username)
    except Exception as e:
        print("An error occurred during logout:", str(e))
        sys.exit()

    main()


def leaderboard():
    try:
        top_players = client.topPlayers()
        top_words = client.topWords()

        print("Top 5 Players:")
        i = 0
        for player in top_players:
            i += 1
            print(f"{i}. {player}")

        print("Top 5 Words")
        j = 0
        for word in top_words:
            j += 1
            print(f"{j}. {word.wordSub} by {word.username}")
    except Exception as e:
        print("An error occurred while displaying top players and words:", str(e))

    print("From Leaderboard")
    main_menu()
    get_user_choice()


def declare_winner():
    print("--- GAME OVER ---")
    print("\nPlayer ", client.getGameWin(client_username), " has won the game!")
    print("\nReturning to the Main Menu...")
    time.sleep(3)
    main_menu()
    get_user_choice()


def check_winner():
    if not client.getWinState(client_username):
        print("Initializing New Round...")
    else:
        print("Winner Found!")
        declare_winner()


def declare_round():
    print("Getting Round Winner...")
    try:
        print("Player", client.getRoundWin(client_username), "has won the round!")
    except wordyGame.noWinner as nw:
        print("No winner for this round.")
    except wordyGame.drawWinners as dw:
        print("No winner for this round. ", dw.message)

    print("--- ROUND END ---")
    time.sleep(3)
    keyboard.press_and_release("enter")
    check_winner()



def start_round():
    print("You have 10 seconds to form a word. Good Luck!")
    print("Your letters are: ", client.receiveLetters(client_username))

    # Event to signal the completion of the timer thread
    timer_completed = threading.Event()

    # stop_flag = False

    def timer():
        client.startGameTime(10, client_username)
        while True:
            elapsed = client.getGameTime(client_username)
            if elapsed == 0:  # or stop_flag:
                break
            time.sleep(1)

        timer_completed.set()
        print("\nTimer ended")
        declare_round()
        print("Loading...")
        client.signalRoundEnd(client_username)
        start_round()

    def user_input():
        while timer_thread.is_alive():
            entered = input("Enter Word: ")
            if len(entered.strip()) == 0:
                print("Empty word. Please enter a valid word.")
                continue

            try:
                client.submitWord(entered, client_username)
                print("Valid word!")
            except wordyGame.invalidLetters as il:
                print(il.message)
            except wordyGame.invalidWord as iw:
                print(iw.message)
            except wordyGame.noSubmittedWord as nsw:
                if timer_completed.is_set():
                    break
                else:
                    print(nsw.message)

            # if len(entered) == 0:
            #     keyboard.press_and_release("enter")

    # Start the timer method as a thread
    timer_thread = threading.Thread(target=timer)
    timer_thread.start()

    # Accept user input
    input_thread = threading.Thread(target=user_input)
    input_thread.start()

    # Wait for the timer thread to finish
    timer_completed.wait()
    # stop_flag = True
    # Wait for the input thread to finish
    input_thread.join()

    print("----")
    check_winner()



def start_game():
    try:
        client.newGame(client_username)
        is_game_not_ongoing = False
        while client.getGameState(client_username):
            is_game_not_ongoing = client.getGameState(client_username)

        print("Game Found! Initializing...")
        time.sleep(3)
        start_round()

    except wordyGame.noOtherPlayersAvailable as e:
        print("Game Error: ", e.message)
        print("From start_game() NOPA E")
        print()
    except Exception as e:
        print("Program Error: ", e)
        print("From start_game() PE E")
        print()

    main_menu()
    get_user_choice()


def get_user_choice():
    while True:
        choice = input("Enter your choice (1-3): ")
        if choice.isdigit() and 1 <= int(choice) <= 3:
            if choice == "1":
                print("Waiting for other players to join...")
                start_game()
            elif choice == "2":
                leaderboard()
            elif choice == "3":
                logout()
            break
        else:
            print("Invalid user input. Please enter a number between 1 and 3.")


def login_user():
    # Declare the global_username as global
    global client_username
    print("Enter Username: ")
    username_input = input()
    print("Enter Password: ")
    password_input = input()
    player = PlayerClass.Player(username_input, password_input)
    is_logged = True
    try:
        is_logged = client.verifyLogin(player)
    except (wordyGame.invalidCredentials, wordyGame.invalidUser, wordyGame.existingSession) as e:
        print("Error during login:", e.message)
        is_logged = False
    except Exception as e:
        print("An error occurred during login:", str(e))
        is_logged = False

    if is_logged:
        client_username = username_input
        main_menu()
        get_user_choice()
    else:
        print("Login failed. Please try again.")
        login_user()


def main():
    login_menu()
    while True:
        choice = input("Enter your choice (1-2): ")
        if choice.isdigit() and 1 <= int(choice) <= 2:
            if choice == "1":
                try:
                    login_user()
                except Exception as e:
                    print("An error occurred during login:", str(e))

                break
            elif choice == "2":
                print("Exiting the game.")
                sys.exit()
        else:
            print("Invalid user_input. Please enter a number between 1 and 2.")
        print()


if __name__ == "__main__":
    main()
