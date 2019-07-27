package com.isaac.games;

public class ServerSidePlayer {

    private int score;
    private String playerName;
    private int bid;
    private int tricks;

    public ServerSidePlayer() {
        score = 0;
        playerName = "";
        bid = 0;
        tricks = 0;
    }

    public void setTricks(boolean addOrClear) {
        if (addOrClear) {
            tricks += 1;
        } else {
            tricks = 0;
        }
    }

    public int getTricks() {
        return tricks;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int value) {
        bid = value;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String value) {
        playerName = value;
    }

    public void addScore(int delta) {
        score += delta;
    }

    public int getScore() {
        return score;
    }

}