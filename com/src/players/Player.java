package com.src.players;

import com.src.common.Constants;
import com.src.goods.GoodsFactory;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Player {
    private int id;
    private int money;

    private ArrayList<Integer> cards;

    private boolean role; // 0 --- merchant; 1 --- sheriff
    private int declaredGood;
    private int bribe;
    private int profit;

    private HashMap<Integer, Integer> bag;
    private HashMap<Integer, Integer> shop;

    protected static GoodsFactory factory;

    Player() {
        this.money = Constants.INITIAL_MONEY;
        this.profit = 0;
        this.shop = new HashMap<>();
        factory = GoodsFactory.getInstance();
    }

    // getters and setters for player's fields
    final int getId() {
        return this.id;
    }
    public final void setId(final int id) {
        this.id = id;
    }

    final ArrayList<Integer> getCards() {
        return this.cards;
    }

    final HashMap<Integer, Integer> getBag() {
        return this.bag;
    }
    final void setBag(final HashMap<Integer, Integer> bag) {
        this.bag = bag;
    }

    public final HashMap<Integer, Integer> getShop() {
        return this.shop;
    }

    public final int getMoney() {
        return this.money;
    }
    final void setMoney(final int money) {
        this.money = money;
    }

    public final int getProfit() {
        return this.profit;
    }
    public final void setProfit(final int profit) {
        this.profit = profit;
    }

    final int getDeclaredGood() {
        return this.declaredGood;
    }
    final void setDeclaredGood(final int declaredGood) {
        this.declaredGood = declaredGood;
    }

    final int getBribe() {
        return this.bribe;
    }
    final void setBribe(final int bribe) {
        this.bribe = bribe;
    }

    public final String getRole() {
        if (this.role) {
            return "Sheriff";
        }
        return "Merchant";
    }
    public final void setRole(final String role) {
        if (role.equals("Merchant")) {
            this.role = false;
        } else if (role.equals("Sheriff")) {
            this.role = true;
        }
    }

    public final void drawCards(final ArrayList<Integer> deck) {
        // replace old cards with new cards
        this.cards = new ArrayList<>();

        for (int i = 0; i < Constants.CARDS_NUMBER; i++) {
            this.cards.add(deck.get(0));
            deck.remove(0);
        }
    }

    // the 2 main methods based on player's role during semiround
    public abstract void merchantBehaviour(int round);
    public abstract void sheriffBehaviour(ArrayList<Player> players, ArrayList<Integer> deck);
}
