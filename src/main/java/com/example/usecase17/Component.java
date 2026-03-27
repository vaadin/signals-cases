package com.example.usecase17;

import java.math.BigDecimal;

public class Component {
    protected String id;
    protected String name;
    protected BigDecimal price;

    public Component(String id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name;
    }
}
