package com.example.guitartrainer;

public class EarTrainingCardStats {
    private String cardUniqueId;
    private int successPerc;


    private EarTrainingGuessFunctionLevel.LevelType levelType;

    public EarTrainingCardStats(String cardUniqueId, int successPerc,
                                EarTrainingGuessFunctionLevel.LevelType levelType) {
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

    public EarTrainingGuessFunctionLevel.LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(EarTrainingGuessFunctionLevel.LevelType levelType) {
        this.levelType = levelType;
    }
}
