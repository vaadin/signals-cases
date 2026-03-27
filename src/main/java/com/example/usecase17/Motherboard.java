package com.example.usecase17;

import java.math.BigDecimal;

public class Motherboard extends Component {
    private String socket;
    private String ramType;
    private int maxRamSpeed;
    private int maxRamCapacity;
    private String formFactor;
    private int m2Slots;
    private int sataSlots;

    public Motherboard(String id, String name, BigDecimal price, String socket,
            String ramType, int maxRamSpeed, int maxRamCapacity,
            String formFactor, int m2Slots, int sataSlots) {
        super(id, name, price);
        this.socket = socket;
        this.ramType = ramType;
        this.maxRamSpeed = maxRamSpeed;
        this.maxRamCapacity = maxRamCapacity;
        this.formFactor = formFactor;
        this.m2Slots = m2Slots;
        this.sataSlots = sataSlots;
    }

    public String getSocket() {
        return socket;
    }

    public String getRamType() {
        return ramType;
    }

    public int getMaxRamSpeed() {
        return maxRamSpeed;
    }

    public int getMaxRamCapacity() {
        return maxRamCapacity;
    }

    public String getFormFactor() {
        return formFactor;
    }

    public int getM2Slots() {
        return m2Slots;
    }

    public int getSataSlots() {
        return sataSlots;
    }
}
