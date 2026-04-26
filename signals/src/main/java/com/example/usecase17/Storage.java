package com.example.usecase17;

import java.math.BigDecimal;

public class Storage extends Component {
    private String type;
    private int capacityGB;
    private int speedMBps;

    public Storage(String id, String name, BigDecimal price, String type,
            int capacityGB, int speedMBps) {
        super(id, name, price);
        this.type = type;
        this.capacityGB = capacityGB;
        this.speedMBps = speedMBps;
    }

    public String getType() {
        return type;
    }

    public int getCapacityGB() {
        return capacityGB;
    }

    public int getSpeedMBps() {
        return speedMBps;
    }
}
