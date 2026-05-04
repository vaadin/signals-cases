package com.example.usecase17;

import java.math.BigDecimal;

public class GPU extends Component {
    private int powerConsumption;
    private int lengthMm;
    private int score;

    public GPU(String id, String name, BigDecimal price, int powerConsumption,
            int lengthMm, int score) {
        super(id, name, price);
        this.powerConsumption = powerConsumption;
        this.lengthMm = lengthMm;
        this.score = score;
    }

    public int getPowerConsumption() {
        return powerConsumption;
    }

    public int getLengthMm() {
        return lengthMm;
    }

    public int getScore() {
        return score;
    }
}
