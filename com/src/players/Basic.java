package com.src.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.src.common.Constants;
import com.src.goods.GoodsType;

public class Basic extends Player {

    final void setGoodsHashMaps(final HashMap<Integer, Integer> legalGoods,
                                final HashMap<Integer, Integer> illegalGoods) {

        // split goods in two subcategories for every player: legal goods and illegal goods
        for (int card : this.getCards()) {
            if (factory.getGoodsById(card).getType() == GoodsType.Legal) {

                if (legalGoods.containsKey(card)) {
                    legalGoods.put(card, legalGoods.get(card) + 1);
                } else {
                    legalGoods.put(card, 1);
                }
            } else {

                if (illegalGoods.containsKey(card)) {
                    illegalGoods.put(card, illegalGoods.get(card) + 1);
                } else {
                    illegalGoods.put(card, 1);
                }
            }
        }
    }

    final void putLegalGoods(final HashMap<Integer, Integer> legalGoods) {
        // base strategy for putting legal goods into bag
        int maxQuant = 0;
        int freqCardIndex = -1;
        int valueOfFreqCard = 0;

        // search for the most frequent legal good
        for (Map.Entry<Integer, Integer> it : legalGoods.entrySet()) {
            int cardId = (Integer) ((Map.Entry) it).getKey();
            int freq = (Integer) ((Map.Entry) it).getValue();

            if (freq > maxQuant) {
                maxQuant = freq;
                freqCardIndex = cardId;
                valueOfFreqCard = factory.getGoodsById(cardId).getProfit();

            } else if (freq == maxQuant && valueOfFreqCard
                    < factory.getGoodsById(cardId).getProfit()) {
                freqCardIndex = cardId;
                valueOfFreqCard = factory.getGoodsById(cardId).getProfit();

            } else if (freq == maxQuant && valueOfFreqCard
                    == factory.getGoodsById(cardId).getProfit() && freqCardIndex < cardId) {
                freqCardIndex = cardId;
            }
        }

        // check not to overflow the bag, then add the good
        if (maxQuant <= Constants.MAX_ITEMS) {
            this.getBag().put(freqCardIndex, maxQuant);
        } else {
            this.getBag().put(freqCardIndex, Constants.MAX_ITEMS);
        }

        // declare good
        this.setDeclaredGood(freqCardIndex);
    }
    final void putIllegalGood(final HashMap<Integer, Integer> illegalGoods) {
        // base strategy for putting illegal good into bag

        // check not to create dues
        if (this.getMoney() < Constants.ILLEGAL_GOOD_PENALTY) {
            this.setDeclaredGood(-1);
            return;
        }

        int bestCardIndex = -1;
        int valueOfBestCard = 0;

        // search for the most profitable illegal good
        for (Map.Entry<Integer, Integer> it : illegalGoods.entrySet()) {
            int cardId = (Integer) ((Map.Entry) it).getKey();
            int profit = factory.getGoodsById(cardId).getProfit();

            if (valueOfBestCard < profit || (valueOfBestCard == profit
                    && bestCardIndex < cardId)) {
                bestCardIndex = cardId;
                valueOfBestCard = profit;
            }
        }

        // remove illegal item from illegalGoods map
        if (illegalGoods.get(bestCardIndex) == 1) {
            illegalGoods.remove(bestCardIndex);
        } else {
            illegalGoods.put(bestCardIndex, illegalGoods.get(bestCardIndex) - 1);
        }

        // add the good
        if (this.getBag().containsKey(bestCardIndex)) {
            this.getBag().put(bestCardIndex, this.getBag().get(bestCardIndex) + 1);
        } else {
            this.getBag().put(bestCardIndex, 1);
        }
    }

    private void makeBag(final HashMap<Integer, Integer> legalGoods,
                         final HashMap<Integer, Integer> illegalGoods) {

        // apply base strategy for merchant
        if (!legalGoods.isEmpty()) {
            this.putLegalGoods(legalGoods);
        } else {
            this.putIllegalGood(illegalGoods);
            this.setDeclaredGood(0);
        }
    }

    @Override
    /**
     * main method for the mercahnt
     */
    public void merchantBehaviour(final int round) {
        this.setBag(new HashMap<>());
        this.setBribe(0);

        HashMap<Integer, Integer> legalGoods = new HashMap<>();
        HashMap<Integer, Integer> illegalGoods = new HashMap<>();
        this.setGoodsHashMaps(legalGoods, illegalGoods);

        this.makeBag(legalGoods, illegalGoods);
    }

    final void letMerchantGo(final Player player) {

        // let the merchant put all of his goods in the shop
        for (Map.Entry<Integer, Integer> it : player.getBag().entrySet()) {
            int cardId = (Integer) ((Map.Entry) it).getKey();
            int freq = (Integer) ((Map.Entry) it).getValue();

            if (player.getShop().containsKey(cardId)) {
                player.getShop().put(cardId, player.getShop().get(cardId) + freq);
            } else {
                player.getShop().put(cardId, freq);
            }
        }
    }

    private void letAllMerchantsGo(final ArrayList<Player> players, final Player player) {
        int index = players.indexOf(player);

        // iterate through all remaining merchants and let them go
        for (int i = index; i < players.size(); i++) {

            Player currentPlayer = players.get(i);
            this.letMerchantGo(player);
            // restore bribe to the merchant
            currentPlayer.setMoney(currentPlayer.getMoney() + currentPlayer.getBribe());
        }
    }

    final void checkOneItemBag(final Player player, final ArrayList<Integer> deck) {
        Map.Entry pair = player.getBag().entrySet().iterator().next();
        int cardId = (Integer) pair.getKey();
        int freq = (Integer) pair.getValue();

        // check if good is correct declared
        if (cardId == player.getDeclaredGood()) {

            // let the sheriff pay the penalty to the merchant
            int penalty = factory.getGoodsById(cardId).getPenalty();
            this.setMoney(this.getMoney() - freq * penalty);
            player.setMoney(player.getMoney() + freq * penalty);

            // add the good to the merchant's shop
            if (player.getShop().containsKey(cardId)) {
                player.getShop().put(cardId, player.getShop().get(cardId) + freq);
            } else {
                player.getShop().put(cardId, freq);
            }

        } else {

            // let the merchant play the penalty to the sheriff
            int penalty = factory.getGoodsById(cardId).getPenalty();
            this.setMoney(this.getMoney() + freq * penalty);
            player.setMoney(player.getMoney() - freq * penalty);

            // add card to the bottom of the deck
            deck.add(cardId);
        }
    }

    final void checkMoreItemsBag(final Player player, final ArrayList<Integer> deck) {
        int totalPenalty = 0;

        // iterate through merchant bag
        for (Map.Entry<Integer, Integer> it : player.getBag().entrySet()) {
            int cardId = (Integer) ((Map.Entry) it).getKey();
            int freq = (Integer) ((Map.Entry) it).getValue();

            // check if good is correct declared
            if (player.getDeclaredGood() != cardId) {
                // calculate the penalty
                totalPenalty += factory.getGoodsById(cardId).getPenalty() * freq;

                // add card to the bottom of the deck
                deck.add(cardId);
            } else {

                // add the good to the shop
                if (player.getShop().containsKey(cardId)) {
                    player.getShop().put(cardId, player.getShop().get(cardId) + freq);
                } else {
                    player.getShop().put(cardId, freq);
                }
            }
        }

        // let the merchant pay the total penalty to the sheriff
        player.setMoney(player.getMoney() - totalPenalty);
        this.setMoney(this.getMoney() + totalPenalty);
    }
    @Override
    /**
     * main method for the sheriff
     */
    public void sheriffBehaviour(final ArrayList<Player> players, final ArrayList<Integer> deck) {
        for (Player player : players) {

            // check not to check himself
            if (this == player) {
                continue;
            }

            // check if sheriff has money enough to do the job
            if (this.getMoney() < Constants.MAX_ITEMS * Constants.LEGAL_GOOD_PENALTY) {
                this.letAllMerchantsGo(players, player);
                return;
            }

            // check if player has at least one good
            if (player.getDeclaredGood() == -1) {
                continue;
            }
            player.setMoney(player.getMoney() + player.getBribe());

            // check the merchant
            if (player.getBag().size() > 1) {
                this.checkMoreItemsBag(player, deck);
            } else if (player.getBag().size() == 1) {
                this.checkOneItemBag(player, deck);
            }
        }
    }
    @Override
    /**
     * method for printing the ranking
     */
    public String toString() {
        return this.getId() + " BASIC " + this.getProfit();
    }
}
