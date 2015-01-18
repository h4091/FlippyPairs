package org.faudroids.distributedmemory.core;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Simon Hofmann on 13.01.15.
 */
public class GameManager {

    private ArrayList<Card> cardStack = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private int openPairs;

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    private int currentPlayer;
    private int winner;
    private Random rand;

    private enum returnValue {
        MATCH, MISS
    }

    public enum gameEvents {
        FINISH, RUN, MATCH, SWITCH, ERROR
    }

    //Set up a new game with given amount of pairs
    public GameManager(int pairsCount, int playerCount)
    {
        Log.d("Init", "Pairs: " + pairsCount);
        this.rand = new Random();
        for(int id = 0; id < pairsCount*2; id+=2) {
            int randomValue = rand.nextInt(pairsCount);
            this.cardStack.add(new Card(id, randomValue));
            Log.d("Init", "Value " + id + " : " + this.cardStack.get(id).getValue());
            this.cardStack.add(new Card(id + 1, randomValue));
            Log.d("Init", "Value " + (id + 1) + " : " + this.cardStack.get(id + 1).getValue());
        }

        for(int id = 0; id < playerCount; ++id) {
            this.players.add(new Player(id, "asdf"+id));
        }

        this.currentPlayer = 0;
        this.openPairs = pairsCount;
    }

    public returnValue matchPairs(int first, int second) throws IndexOutOfBoundsException {
        try {
            if (!this.cardStack.get(first).isClosed() && !this.cardStack.get(second).isClosed()) {
                if (this.cardStack.get(first).getValue() == this.cardStack.get(second).getValue()) {
                    this.cardStack.get(first).setClosed(true);
                    this.cardStack.get(second).setClosed(true);
                    --this.openPairs;
                    return returnValue.MATCH;
                } else {
                    return returnValue.MISS;
                }
            } else {
                return returnValue.MISS;
            }
        } catch (IndexOutOfBoundsException e)  {
            throw e;
        }
    }

    public gameEvents run(int id1, int id2) {
        try {
            if (this.openPairs - 1 > 0) {
                if (matchPairs(id1, id2) == returnValue.MATCH) {
                    this.players.get(this.currentPlayer).setPoints(this.players.get(this.currentPlayer)
                            .getPoints() + 1);
                    Log.i("run", "Points: " + this.players.get(this.currentPlayer).getPoints());
                    Log.d("run", "open pairs " + this.openPairs);
                    return gameEvents.MATCH;
                } else if (matchPairs(id1, id2) == returnValue.MISS) {
                    this.currentPlayer = getNextPlayer();
                    Log.i("run", "Switch player: " + this.players.get(this.currentPlayer).getName
                            ());
                    return gameEvents.SWITCH;
                }
            } else {
                if (matchPairs(id1, id2) == returnValue.MATCH) {
                    this.players.get(this.currentPlayer).setPoints(this.players.get(this.currentPlayer)
                            .getPoints() + 1);
                    Log.i("run", "Winner: " + Collections.max(this.players).getId());
                    this.winner = Collections.max(this.players).getId();
                    return gameEvents.FINISH;
                } else {
                    this.currentPlayer = getNextPlayer();
                    return gameEvents.SWITCH;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return gameEvents.ERROR;
        }
        return gameEvents.RUN;
    }

    private int getNextPlayer() {
        ++currentPlayer;
        return currentPlayer%this.players.size();
    }

    public int getWinnerId() {
        return winner;
    }

    public int getCardValue(int id) {
        return this.cardStack.get(id).getValue();
    }

    public String getPlayerName(int id) {
        return this.players.get(id).getName();
    }

    public int getPlayerPoints(int id) {
        return this.players.get(id).getPoints();
    }
}
