package moe.hilaryoi.im;

import moe.hilaryoi.im.event.EventHandler;
import moe.hilaryoi.im.event.EventParcelMessageReceived;
import moe.hilaryoi.im.network.ParcelCommand;
import moe.hilaryoi.im.network.Sender;
import moe.hilaryoi.im.standard.StandardCommandListener;

import java.util.ArrayList;

public class Hangman extends StandardCommandListener {

    /*
     * EVERYTHING WORKS
     * DON'T ASK ME HOW
     * I DON'T EVEN KNOW MYSELF
     * DON'T TOUCH ANYTHING OR THINGS MIGHT BREAK
     */

    private String prefix;

    private boolean isPlaying;

    private String word;
    private char[] letters;
    private String guessedWord;
    private String initiator;
    private ArrayList<Character> guessedLetters;

    private int tries;

    public Hangman() {
        super("/hangman start <word> to start a game", "hangman");

        prefix = "[HANGMAN]  -  ";
        guessedLetters = new ArrayList<Character>();
        reset();
    }

    @Override
    public void doCommand(ParcelCommand c) {
        Sender sender = c.getSender();
        String username = sender.getUsername();

        if (c.getCommandArgs().length != 2) {
            Server.staticSend(prefix + "Type /hangman start <word> to start a game", username);
            Server.staticSend(prefix + "Current game status: " + isPlaying, username);
            return;
        }

        if (c.getCommandArgs()[0].equalsIgnoreCase("start") == false) {
            Server.staticSend(prefix + "Type /hangman start <word> to start a game", username);
            Server.staticSend(prefix + "Current game status: " + isPlaying, username);
            return;
        }

        if (isPlaying) {
            Server.staticSend("Can't start a new game when one is already in progress!", username);
            return;
        }

        reset();

        word = c.getCommandArgs()[1].toLowerCase();
        letters = word.toCharArray();
        String[] replacements = new String[letters.length];

        for (int i = 0; i < letters.length; i++) {
            replacements[i] = "_";
        }

        guessedWord = String.join("", replacements);

        initiator = username;
        isPlaying = true;

        Server.staticBroadcast("");
        Server.staticBroadcast(prefix + username + " started a Hangman game!");
        Server.staticBroadcast(prefix + "Type one letter or a whole word to guess!");
        broadcastWord();
        Server.staticBroadcast("");
    }

    private void reset() {
        isPlaying = false;
        word = null;
        guessedWord = null;
        guessedLetters.clear();
        tries = 10;
    }

    @EventHandler
    public void onMessageReceived(EventParcelMessageReceived event) {

        /*
        Check if playing
        Check if msg starts with ! (and if so, treat it like a normal msg instead of a guess)
        Check if someone is trying to guess their own word
        Check if someone inputted multiple letters / words
         */

        if (isPlaying == false) { return; }

        if (event.getParcel().getBody().startsWith("!")) {
            return;
        }

        String sender = event.getParcel().getSender().getUsername();
        String message = event.getParcel().getBody().toLowerCase();

        if (initiator.equalsIgnoreCase(sender)) {
            Server.staticSend("You can't guess your own word!", sender);
            event.setCancelled(true);
            return;
        }

        if (message.split(" ").length > 1) {
            Server.staticSend("Please only guess one letter or word", sender);
            event.setCancelled(true);
            return;
        }

        // ======= End of checks ======

        char[] guess = message.toCharArray();

        boolean guessedCorrectly = false;

        // Guessing a word

        if (guess.length > 1) {

            if (message.equalsIgnoreCase(word)) {
                guessedCorrectly = true;
                doGameOver(true);
            }

            else {
                tries = tries - 1;

                Server.staticBroadcast("");
                Server.staticBroadcast(prefix + sender + " guessed the word wrong!");

                if (tries == 0) {
                    doGameOver(false);
                    return;
                }

                else {
                    Server.staticBroadcast(prefix + "Tries left: " + tries);
                    Server.staticBroadcast("");
                    broadcastWord();
                    Server.staticBroadcast("");
                }
            }

        }

        // Guessing a single letter

        else if (guess.length == 1) {

            char guessLetter = guess[0];

            if (checkIfAlreadyGuessed(guessLetter)) {
                Server.staticSend("You already guessed this letter!", sender);
                event.setCancelled(true);
                return;
            }

            for (int i = 0; i < letters.length; i++) {
                if (letters[i] == guessLetter) {
                    guessedWord = editGuessedWord(i, guessLetter);
                    guessedCorrectly = true;
                }
            }

            guessedLetters.add(guessLetter);

            if (guessedCorrectly) {

                Server.staticBroadcast("");
                Server.staticBroadcast(prefix + sender + " guessed the letter '" + guessLetter + "' correctly!");

                if (wordIsGuessed()) {
                    doGameOver(true);
                }

                else {
                    Server.staticBroadcast(prefix + "Tries left: " + tries);
                    Server.staticBroadcast("");
                    broadcastWord();
                    Server.staticBroadcast("");
                }
            }

            else {

                tries = tries - 1;

                Server.staticBroadcast("");
                Server.staticBroadcast(prefix + sender + " guessed the letter '" + guessLetter + "' WRONGLY!");

                if (tries == 0) {
                    doGameOver(false);
                    return;
                }

                else {

                    Server.staticBroadcast(prefix + "Tries left: " + tries);
                    Server.staticBroadcast("");
                    broadcastWord();
                    Server.staticBroadcast("");

                }

            }

        }


    }

    private void doGameOver(boolean didSomeoneWin) {
        String didGuess = "";

        if (didSomeoneWin) {
            didGuess = "The word '" + word + "' has been guessed CORRECTLY!";
        }

        else {
            didGuess = "The word'" + word + "' was NOT guessed correcty :( " + initiator + " wins.";
        }

        Server.staticBroadcast("");
        Server.staticBroadcast(prefix + "GAME OVER!");
        Server.staticBroadcast(prefix + didGuess);
        Server.staticBroadcast("");
        Server.staticBroadcast(prefix + "Type /hangman start <word> if you want to start a new game");

        reset();
    }

    private boolean wordIsGuessed() {
        if (guessedWord.equalsIgnoreCase(word)) {
            return true;
        }

        return false;
    }

    private void broadcastWord() {
        Server.staticBroadcast(prefix + "WORD: " + getGuessedWordReadable() + "   (" + letters.length + " letters)");
        Server.staticBroadcast(prefix + "Guessed letters: " + String.join(", ", guessedLetters + "."));
    }

    private String editGuessedWord(int index, char letter) {
        char[] editedGuessedWord = guessedWord.toCharArray();
        editedGuessedWord[index] = letter;
        return new String(editedGuessedWord);
    }

    /**
     * Just adds a space inbetween every letter or every underscore to make it more readable
     * Aka it converts _____ to _ _ _ _ _
     *
     * @return guessedWord with spaces inbetween every letter
     */

    private String getGuessedWordReadable() {

        String[] replacements = new String[letters.length];
        char[] guessedWordLetters = guessedWord.toCharArray();

        for (int i = 0; i < letters.length; i++) {
            replacements[i] = guessedWordLetters[i] + " ";
        }

        return String.join("", replacements);
    }

    private boolean checkIfAlreadyGuessed(char letter) {
        if (guessedLetters.contains(letter)) {
            return true;
        }

        return false;
    }

}
