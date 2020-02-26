package com.src.players;

public final class PlayersFactory {
    private static PlayersFactory instance = new PlayersFactory();

    private PlayersFactory() { }

    public static PlayersFactory getInstance() {
        return instance;
    }

    public Player createPlayer(final PlayerStrategy strategy) {
        // crate player based on given strategy
        switch (strategy) {
            case Basic:
                return new Basic();
            case Greedy:
                return new Greedy();
            case Bribe:
                return new Bribe();
            default:
                return null;
        }
    }
}
