package com.example.guitartrainer.fretboardVisualization;

import com.example.guitartrainer.earTraining.GuessFunctionLevel;

public class CardStats {
    private String cardUniqueId;
    private int successSeconds;
    private PlayFunctionsLevel.LevelType levelType;

    public CardStats(String cardUniqueId, int successSeconds,
                     PlayFunctionsLevel.LevelType levelType) {
        this.cardUniqueId = cardUniqueId;
        this.successSeconds = successSeconds;
        this.levelType = levelType;
    }


    public String getCardUniqueId() {
        return cardUniqueId;
    }

    public void setCardUniqueId(String cardUniqueId) {
        this.cardUniqueId = cardUniqueId;
    }

    public int getSuccessSeconds() {
        return successSeconds;
    }

    public void setSuccessSeconds(int successSeconds) {
        this.successSeconds = successSeconds;
    }

    public PlayFunctionsLevel.LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(PlayFunctionsLevel.LevelType levelType) {
        this.levelType = levelType;
    }
}
