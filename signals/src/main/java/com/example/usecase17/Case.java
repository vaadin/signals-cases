package com.example.usecase17;

import java.math.BigDecimal;

public class Case extends Component {
    private String formFactor;
    private int gpuClearanceMm;
    private int cpuCoolerClearanceMm;
    private String psuFormFactor;

    public Case(String id, String name, BigDecimal price, String formFactor,
            int gpuClearanceMm, int cpuCoolerClearanceMm,
            String psuFormFactor) {
        super(id, name, price);
        this.formFactor = formFactor;
        this.gpuClearanceMm = gpuClearanceMm;
        this.cpuCoolerClearanceMm = cpuCoolerClearanceMm;
        this.psuFormFactor = psuFormFactor;
    }

    public String getFormFactor() {
        return formFactor;
    }

    public int getGpuClearanceMm() {
        return gpuClearanceMm;
    }

    public int getCpuCoolerClearanceMm() {
        return cpuCoolerClearanceMm;
    }

    public String getPsuFormFactor() {
        return psuFormFactor;
    }
}
