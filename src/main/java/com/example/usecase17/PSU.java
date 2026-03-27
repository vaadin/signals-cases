package com.example.usecase17;

import java.math.BigDecimal;

public class PSU extends Component {
    private int wattage;
    private String formFactor;
    private String efficiency;

    public PSU(String id, String name, BigDecimal price, int wattage,
            String formFactor, String efficiency) {
        super(id, name, price);
        this.wattage = wattage;
        this.formFactor = formFactor;
        this.efficiency = efficiency;
    }

    public int getWattage() {
        return wattage;
    }

    public String getFormFactor() {
        return formFactor;
    }

    public String getEfficiency() {
        return efficiency;
    }
}
