package com.example.usecase17;

import java.math.BigDecimal;

public class Cooler extends Component {
    private String type;
    private int heightMm;
    private int maxTdp;
    private String[] compatibleSockets;

    public Cooler(String id, String name, BigDecimal price, String type,
            int heightMm, int maxTdp, String... compatibleSockets) {
        super(id, name, price);
        this.type = type;
        this.heightMm = heightMm;
        this.maxTdp = maxTdp;
        this.compatibleSockets = compatibleSockets;
    }

    public String getType() {
        return type;
    }

    public int getHeightMm() {
        return heightMm;
    }

    public int getMaxTdp() {
        return maxTdp;
    }

    public String[] getCompatibleSockets() {
        return compatibleSockets;
    }
}
