package com.src.players;

import com.src.common.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Greedy extends Basic {
    private void playEvenRound(final HashMap<Integer, Integer> illegalGoods,
                               final boolean illegalCardPut) {

        // greedy strategy for even rounds
        if ((!illegalCardPut && this.getMoney() < Constants.ILLEGAL_GOOD_PENALTY)
                || (illegalCardPut && this.getMoney() < 2 * Constants.ILLEGAL_GOOD_PENALTY)) {
            return;
        }

        int bestCardIndex = -1;
        int valueOfBestCard = 0;

        // search for the most profitable illegal good
        for (Map.Entry<Integer, Integer> it : illegalGoods.entrySet()) {
            int cardId = (Integer) ((Map.Entry) it).getKey();
            if (valueOfBestCard < factory.getGoodsById(cardId).getProfit()) {
                bestCardIndex = cardId;
                valueOfBestCard = factory.getGoodsById(cardId).getProfit();
            }
        }

        // good not found
        if (bestCardIndex == -1) {
            return;
        }

        // put the good into the bag
        if (this.getBag().containsKey(bestCardIndex)) {
            this.getBag().put(bestCardIndex, this.getBag().get(bestCardIndex) + 1);
        } else {
            this.getBag().put(bestCardIndex, 1);
        }
    }

    @Override
    public void merchantBehaviour(final int round) {
        boolean illegalCardPut = false;
        this.setBag(new HashMap<>());
        this.setBribe(0);

        // split goods
        HashMap<Integer, Integer> legalGoods = new HashMap<>();
        HashMap<Integer, Integer> illegalGoods = new HashMap<>();
        this.setGoodsHashMaps(legalGoods, illegalGoods);

        if (!legalGoods.isEmpty()) {
            this.putLegalGoods(legalGoods);
        } else {
            this.putIllegalGood(illegalGoods);
            this.setDeclaredGood(0);
            illegalCardPut = true;
        }

        // play even round greedy strategy
        if (round % 2 == 1 && !illegalGoods.isEmpty()) {
            this.playEvenRound(illegalGoods, illegalCardPut);
        }
    }

    @Override
    public void sheriffBehaviour(final ArrayList<Player> players,
                                 final ArrayList<Integer> deck) {
        for (Player player : players) {

            // check not to check himself
            if (this == player) {
                continue;
            }

            // check if sheriff has money enough to do the job
            if (this.getMoney() < Constants.LEGAL_GOOD_PENALTY  * Constants.MAX_ITEMS) {
                this.setMoney(this.getMoney() + player.getBribe());
                continue;
            }

            // check if player has at least one good
            if (player.getDeclaredGood() == -1) {
                continue;
            }

            // take the bribe and let merchant go
            if (player.getBribe() > 0) {
                this.letMerchantGo(player);
                this.setMoney(this.getMoney() + player.getBribe());
                continue;
            }
            player.setMoney(player.getMoney() + player.getBribe());

            // check merchant
            if (player.getBag().size() > 1) {
                this.checkMoreItemsBag(player, deck);
            } else if (player.getBag().size() == 1) {
                this.checkOneItemBag(player, deck);
            }
        }
    }

    @Override
    public String toString() {
        return this.getId() + " GREEDY " + this.getProfit();
    }
}
