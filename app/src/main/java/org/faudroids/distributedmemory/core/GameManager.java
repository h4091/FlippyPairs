package org.faudroids.distributedmemory.core;

import android.util.Log;
import android.widget.Toast;

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
    private int currentPlayer;
    private Random rand;

    //Set up a new game with given amount of pairs
    public GameManager(int pairsCount, int playerCount)
    {
        this.rand = new Random();
        for(int id = 0; id < pairsCount*2; id+=2) {
            int randomValue = rand.nextInt(pairsCount);
            this.cardStack.add(new Card(id, randomValue));
            Log.d("Init", "Value " + id + " : " + this.cardStack.get(id).getValue());
            this.cardStack.add(new Card(id + 1, randomValue));
            Log.d("Init", "Value " + (id+1) + " : " + this.cardStack.get(id+1).getValue());
        }
        for(int id = 0; id < playerCount; ++id) {
            this.players.add(new Player(id, "asdf"));
        }

        this.currentPlayer = 0;
        this.openPairs = pairsCount;
    }

    //TODO: Range checks!
    public boolean matchPairs(int first, int second) {
        if (!this.cardStack.get(first).isClosed() && !this.cardStack.get(second).isClosed()) {
            if (this.cardStack.get(first).getValue() == this.cardStack.get(second).getValue()) {
                this.cardStack.get(first).setClosed(true);
                this.cardStack.get(second).setClosed(true);
                --this.openPairs;
                return true;
            } else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public int run(int id1, int id2) {
        Log.i("run", "Player " + this.currentPlayer);
        if(this.openPairs-1>0) {
            if(matchPairs(id1, id2)) {
                this.players.get(this.currentPlayer).setPoints(this.players.get(this.currentPlayer)
                        .getPoints()+1);
                Log.i("run", "Points: " + this.players.get(this.currentPlayer).getPoints());
                return -1;
            } else {
                this.currentPlayer = getNextPlayer();
                return -1;
            }
        } else {
            if(matchPairs(id1, id2)) {
                this.players.get(this.currentPlayer).setPoints(this.players.get(this.currentPlayer)
                        .getPoints() + 1);
            }
            Log.i("run", "Winner: " + Collections.max(this.players).getId());
            return Collections.max(this.players).getId();
        }
    }

    private int getNextPlayer() {
        ++currentPlayer;
        return currentPlayer%this.players.size();
    }

    public String getPlayerName(int id) {
        return this.players.get(id).getName();
    }
}
