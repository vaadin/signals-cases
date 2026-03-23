package com.example.usecase17;

import java.math.BigDecimal;

public class CPU extends Component {
    private String socket;
    private int tdp;
    private int score;

    public CPU(String id, String name, BigDecimal price, String socket, int tdp,
            int score) {
        super(id, name, price);
        this.socket = socket;
        this.tdp = tdp;
        this.score = score;
    }

    public String getSocket() {
        return socket;
    }

    public int getTdp() {
        return tdp;
    }

    public int getScore() {
        return score;
    }
}

