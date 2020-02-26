package com.src.main;

import com.src.common.PlayersComparator;
import com.src.goods.GoodsFactory;
import com.src.goods.Goods;
import com.src.goods.LegalGoods;
import com.src.goods.IllegalGoods;
import com.src.goods.GoodsType;
import com.src.players.Player;
import com.src.players.PlayersFactory;
import com.src.players.PlayerStrategy;

import java.util.ArrayList;
import java.util.Map;

final class GameLogic {
    private ArrayList<Player> players;
    private PlayersFactory playersFactory;

    GameLogic() {
        this.players = new ArrayList<>();
        this.playersFactory = PlayersFactory.getInstance();
    }

    private void generatePlayers(final GameInput gameInput) {
        // create the players based on input Strings
        for (String name : gameInput.getPlayerNames()) {
            switch (name) {
                case "basic":
                    this.players.add(playersFactory.createPlayer(PlayerStrategy.Basic));
                    break;
                case "greedy":
                    this.players.add(playersFactory.createPlayer(PlayerStrategy.Greedy));
                    break;
                case "bribed":
                    this.players.add(playersFactory.createPlayer(PlayerStrategy.Bribe));
                    break;
                default:
                    break;
            }
        }

        // set the players' ids
        for (int i = 0; i < this.players.size(); i++) {
            this.players.get(i).setId(i);
        }
    }

    private void runRounds(final GameInput gameInput) {
        for (int round = 0; round < gameInput.getRounds(); round++) {
            int sheriffIndex = 0;

            // play the semirounds
            for (int i = 0; i < this.players.size(); i++) {
                // set sheriff role
                this.players.get(sheriffIndex).setRole("Sheriff");
                Player sheriff = this.players.get(sheriffIndex);

                // let merchants make the bag
                for (Player player : this.players) {
                    if (player.getRole().equals("Merchant")) {
                        player.drawCards((ArrayList<Integer>) gameInput.getAssetIds());
                        player.merchantBehaviour(round);
                    }
                }

                // check merchants
                sheriff.sheriffBehaviour(this.players,
                        (ArrayList<Integer>) gameInput.getAssetIds());

                // change the sheriff
                this.players.get(sheriffIndex).setRole("Merchant");
                sheriffIndex++;
            }
        }
    }

    private void calculateProfit(final GoodsFactory factory) {
        for (Player player : this.players) {

            // get the profit of each good brought to the shop
            for (Map.Entry<Integer, Integer> it : player.getShop().entrySet()) {
                int cardId = (Integer) ((Map.Entry) it).getKey();
                int profit = factory.getGoodsById(cardId).getProfit();
                int freq = (Integer) ((Map.Entry) it).getValue();
                player.setProfit(player.getProfit() + profit * freq);
            }

            // add remaining money to profit
            player.setProfit(player.getProfit() + player.getMoney());
        }
    }

    private void calculateIllegalBonus(final Goods good, final int goodId) {
        IllegalGoods illegalGood = (IllegalGoods) good;

        for (Player player : this.players) {

            // check if illegal good is players's shop
            if (player.getShop().containsKey(goodId)) {
                int quant = player.getShop().get(goodId);

                // iterate through illegalBonus map
                for (Map.Entry<Goods, Integer> it : illegalGood.getIllegalBonus().entrySet()) {
                    Goods legalGood = (Goods) ((Map.Entry) it).getKey();
                    int multiCoef = (Integer) ((Map.Entry) it).getValue();

                    // for each illegal good add to shop associated legal goods
                    if (player.getShop().containsKey(legalGood.getId())) {
                        player.getShop().put(legalGood.getId(),
                                player.getShop().get(legalGood.getId()) + quant * multiCoef);
                    } else {
                        player.getShop().put(legalGood.getId(), quant * multiCoef);
                    }
                }
            }
        }
    }

    private void calculateKingQueenBonus(final Goods good, final int goodId) {
        LegalGoods legalGood = (LegalGoods) good;
        Player king = null;
        Player queen = null;
        // initialise the two max quantities of goods
        int maxQuant1 = 0;
        int maxQuant2 = 0;

        for (Player player : players) {
            if (player.getShop().get(goodId) != null) {

                if (player.getShop().get(goodId) > maxQuant2) {
                    // set king and queen
                    maxQuant1 = maxQuant2;
                    maxQuant2 = player.getShop().get(goodId);
                    queen = king;
                    king = player;
                } else if (player.getShop().get(goodId) > maxQuant1) {
                    // set queen
                    maxQuant1 = player.getShop().get(goodId);
                    queen = player;
                }
            }
        }

        // give king and queen bonus
        if (king != null) {
            king.setProfit(king.getProfit() + legalGood.getKingBonus());
        }
        if (queen != null) {
            queen.setProfit(queen.getProfit() + legalGood.getQueenBonus());
        }
    }

    private void calculateBonus(final GoodsFactory factory) {

        // iterate through all goods to give illegalGoods bonus
        for (Map.Entry<Integer, Goods> it : factory.getAllGoods().entrySet()) {
            int goodId = (Integer) ((Map.Entry) it).getKey();
            Goods good = (Goods) ((Map.Entry) it).getValue();
            if (good.getType() == GoodsType.Illegal) {
                this.calculateIllegalBonus(good, goodId);
            }
        }

        // iterate through all goods to give king and queen bonus
        for (Map.Entry<Integer, Goods> it : factory.getAllGoods().entrySet()) {
            int goodId = (Integer) ((Map.Entry) it).getKey();
            Goods good = (Goods) ((Map.Entry) it).getValue();
            if (good.getType() == GoodsType.Legal) {
                this.calculateKingQueenBonus(good, goodId);
            }
        }
    }

    private void printRanking() {
        // sort and print the players
        this.players.sort(new PlayersComparator());

        for (Player player : this.players) {
            System.out.println(player);
        }
    }

    void resolveGame(final GameInput gameInput, final GoodsFactory factory) {
        this.generatePlayers(gameInput);

        this.runRounds(gameInput);

        this.calculateBonus(factory);
        this.calculateProfit(factory);

        this.printRanking();
    }
}
