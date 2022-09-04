package com.example.guitartrainer.earTraining;

public class CardStats {
    private String cardUniqueId;
    private int successPerc;
    private GuessFunctionLevel.LevelType levelType;

    public CardStats(String cardUniqueId, int successPerc,
                     GuessFunctionLevel.LevelType levelType) {
        this.cardUniqueId = cardUniqueId;
        this.successPerc = successPerc;
        this.levelType = levelType;
    }


    public String getCardUniqueId() {
        return cardUniqueId;
    }

    public void setCardUniqueId(String cardUniqueId) {
        this.cardUniqueId = cardUniqueId;
    }

    public int getSuccessPerc() {
        return successPerc;
    }

    public void setSuccessPerc(int successPerc) {
        this.successPerc = successPerc;
    }

    public GuessFunctionLevel.LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(GuessFunctionLevel.LevelType levelType) {
        this.levelType = levelType;
    }
}
