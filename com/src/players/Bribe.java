package com.src.players;

import com.src.common.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Bribe extends Basic {
    private int totalItems;
    private int totalPenalty;

    private void countIllegalItem(final HashMap<Integer, Integer> illegalGoods) {
        // put illegal item in bag and increase penalty
        this.putIllegalGood(illegalGoods);
        this.totalItems++;
        this.totalPenalty += Constants.ILLEGAL_GOOD_PENALTY;
    }

    private void putIllegalGoods(final HashMap<Integer, Integer> illegalGoods) {
        // put the first two illegal goods in bag
        if (!illegalGoods.isEmpty()) {
            this.countIllegalItem(illegalGoods);
        }
        if (this.getMoney() > 2 * Constants.ILLEGAL_GOOD_PENALTY) {
            if (!illegalGoods.isEmpty()) {
                this.countIllegalItem(illegalGoods);
            }
        }

        // if he has money enough put more illegal goods
        while (!illegalGoods.isEmpty() && this.totalPenalty < this.getMoney()
                - Constants.ILLEGAL_GOOD_PENALTY && this.totalItems < Constants.MAX_ITEMS) {
            this.countIllegalItem(illegalGoods);
        }

        // set the bribe based on number of illegal items in bag
        if (this.totalItems == 1 || this.totalItems == 2) {
            this.setBribe(Constants.DEFAULT_BRIBE);
        } else if (this.totalItems > 2) {
            this.setBribe(Constants.SPECIAL_BRIBE);
        }
    }

    private void putLegalGood(final HashMap<Integer, Integer> legalGoods) {
        int bestCardId = -1;
        int valueOfBestCard = 0;

        // search for the most profitable legal good
        for (Map.Entry<Integer, Integer> it : legalGoods.entrySet()) {
            int cardId = (Integer) ((Map.Entry) it).getKey();
            int profit = factory.getGoodsById(cardId).getProfit();

            if (valueOfBestCard < profit || (valueOfBestCard == profit && bestCardId < cardId)) {
                bestCardId = cardId;
                valueOfBestCard = profit;
            }
        }

        // put found item in bag
        if (this.getBag().containsKey(bestCardId)) {
            this.getBag().put(bestCardId, this.getBag().get(bestCardId) + 1);
        } else {
            this.getBag().put(bestCardId, 1);
        }

        // remove item from legal goods map
        if (legalGoods.get(bestCardId) == 1) {
            legalGoods.remove(bestCardId);
        } else {
            legalGoods.put(bestCardId, legalGoods.get(bestCardId) - 1);
        }
    }

    private void completeWithLegalGoods(final HashMap<Integer, Integer> legalGoods) {
        // bribed strategy for putting legal goods in bag
        while (!legalGoods.isEmpty() && this.totalItems < Constants.MAX_ITEMS
                && this.totalPenalty < this.getMoney() - Constants.LEGAL_GOOD_PENALTY) {

            this.putLegalGood(legalGoods);
            this.totalItems++;
            this.totalPenalty += Constants.LEGAL_GOOD_PENALTY;
        }
    }

    @Override
    public final void merchantBehaviour(final int round) {
        this.setBag(new HashMap<>());
        this.setBribe(0);

        // split goods
        HashMap<Integer, Integer> legalGoods = new HashMap<>();
        HashMap<Integer, Integer> illegalGoods = new HashMap<>();
        this.setGoodsHashMaps(legalGoods, illegalGoods);

        this.totalItems = 0;
        this.totalPenalty = 0;
        if (!illegalGoods.isEmpty() && this.getMoney() > Constants.DEFAULT_BRIBE) {
            // apply bribed strategy
            this.putIllegalGoods(illegalGoods);
            this.completeWithLegalGoods(legalGoods);
            this.setDeclaredGood(0);

        } else {
            // apply base strategy
            if (!legalGoods.isEmpty()) {
                this.putLegalGoods(legalGoods);
            } else {
                this.putIllegalGood(illegalGoods);
                this.setDeclaredGood(0);
            }
        }

        // load bribe from merchant money
        this.setMoney(this.getMoney() - this.getBribe());
    }

    private void checkPlayer(final Player player, final ArrayList<Integer> deck) {
        // check if sheriff has money enough
        if (this.getMoney() >= Constants.MAX_ITEMS * Constants.LEGAL_GOOD_PENALTY) {
            if (player.getDeclaredGood() != -1) {

                // restore merchant's bribe
                player.setMoney(player.getMoney() + player.getBribe());

                if (player.getBag().size() > 1) {
                    this.checkMoreItemsBag(player, deck);
                } else if (player.getBag().size() == 1) {
                    this.checkOneItemBag(player, deck);
                }
            }
        } else {
            // play base strategy
            this.letMerchantGo(player);
            player.setMoney(player.getMoney() + player.getBribe());
        }
    }

    private void play2PlayersSheriff(final ArrayList<Player> players,
                                     final int index, final ArrayList<Integer> deck) {
        // for 2 players inputs
        Player otherPlayer;
        if (index == 0) {
            otherPlayer = players.get(1);
        } else {
            otherPlayer = players.get(0);
        }
        this.checkPlayer(otherPlayer, deck);
    }
    private void playMorePlayersSheriff(final ArrayList<Player> players,
                                        final int index, final ArrayList<Integer> deck) {
        // for more than 2 players inputs
        Player leftPlayer;
        Player rightPlayer;

        // find adjacent players
        if (index == 0) {
            leftPlayer = players.get(players.size() - 1);
            rightPlayer = players.get(index + 1);
        } else if (index == players.size() - 1) {
            leftPlayer = players.get(index - 1);
            rightPlayer = players.get(0);
        } else {
            leftPlayer = players.get(index - 1);
            rightPlayer = players.get(index + 1);
        }

        // check adjacent players
        this.checkPlayer(leftPlayer, deck);
        this.checkPlayer(rightPlayer, deck);

        // let other players go, but take the bribe if exist
        for (Player player : players) {
            if (player == this || player == leftPlayer || player == rightPlayer) {
                continue;
            }
            this.letMerchantGo(player);
            this.setMoney(this.getMoney() + player.getBribe());
        }
    }

    @Override
    public final void sheriffBehaviour(final ArrayList<Player> players,
                                       final ArrayList<Integer> deck) {
        int index = players.indexOf(this);
        if (players.size() == 2) {
            this.play2PlayersSheriff(players, index, deck);
        } else {
            this.playMorePlayersSheriff(players, index, deck);
        }
    }

    @Override
    public final String toString() {
        return this.getId() + " BRIBED " + this.getProfit();
    }
}
