package com.src.main;

import com.src.goods.GoodsFactory;

public final class Main {
    private Main() { }

    public static void main(final String[] args) {
        GameInputLoader gameInputLoader = new GameInputLoader(args[0], args[1]);
        GameInput gameInput = gameInputLoader.load();

        GoodsFactory factory = GoodsFactory.getInstance();

        GameLogic gameLogic = new GameLogic();
        gameLogic.resolveGame(gameInput, factory);
    }
}
