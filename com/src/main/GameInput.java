package com.src.main;

import java.util.List;

class GameInput {
    private final List<Integer> mAssetOrder;
    private final List<String> mPlayersOrder;
    private int mRounds;

    GameInput(final int rounds, final List<Integer> assets, final List<String> players) {
        mAssetOrder = assets;
        mPlayersOrder = players;
        mRounds = rounds;
    }

    final List<Integer> getAssetIds() {
        return mAssetOrder;
    }

    final List<String> getPlayerNames() {
        return mPlayersOrder;
    }

    final int getRounds() {
        return mRounds;
    }

}
