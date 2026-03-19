package com.example.usecase17;

import java.math.BigDecimal;

public class RAM extends Component {
    private String type;
    private int speed;
    private int capacity;

    public RAM(String id, String name, BigDecimal price, String type, int speed,
            int capacity) {
        super(id, name, price);
        this.type = type;
        this.speed = speed;
        this.capacity = capacity;
    }

    public String getType() {
        return type;
    }

    public int getSpeed() {
        return speed;
    }

    public int getCapacity() {
        return capacity;
    }
}

