package com.example.usecase17;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public final class PCData {

    public static final List<CPU> ALL_CPUS = Arrays.asList(
            new CPU("cpu1", "Intel Core i9-14900K", new BigDecimal("589"), "LGA1700", 125, 100),
            new CPU("cpu2", "Intel Core i7-14700K", new BigDecimal("419"), "LGA1700", 125, 85),
            new CPU("cpu3", "Intel Core i5-14600K", new BigDecimal("319"), "LGA1700", 125, 70),
            new CPU("cpu4", "AMD Ryzen 9 7950X", new BigDecimal("549"), "AM5", 170, 98),
            new CPU("cpu5", "AMD Ryzen 7 7700X", new BigDecimal("349"), "AM5", 105, 80),
            new CPU("cpu6", "AMD Ryzen 5 7600X", new BigDecimal("229"), "AM5", 105, 65),
            new CPU("cpu0", "None", new BigDecimal("0"), "None", 0, 0));

    public static final List<Motherboard> ALL_MOTHERBOARDS = Arrays.asList(
            new Motherboard("mb1", "ASUS ROG Z790", new BigDecimal("389"), "LGA1700", "DDR5",
                    7200, 128, "ATX", 4, 6),
            new Motherboard("mb2", "MSI MPG Z790", new BigDecimal("299"), "LGA1700", "DDR5", 6400,
                    128, "ATX", 3, 6),
            new Motherboard("mb3", "ASUS TUF B760", new BigDecimal("189"), "LGA1700", "DDR5",
                    5600, 128, "ATX", 2, 4),
            new Motherboard("mb4", "ASUS ROG X670E", new BigDecimal("429"), "AM5", "DDR5", 6400,
                    128, "ATX", 4, 6),
            new Motherboard("mb5", "MSI MPG X670", new BigDecimal("319"), "AM5", "DDR5", 6000,
                    128, "ATX", 3, 6),
            new Motherboard("mb6", "ASUS TUF B650", new BigDecimal("199"), "AM5", "DDR5", 5600,
                    128, "ATX", 2, 4),
            new Motherboard("mb0", "None", new BigDecimal("0"), "None", "None", 0, 0, "None", 0,
                    0));

    public static final List<RAM> ALL_RAM = Arrays.asList(
            new RAM("ram1", "32GB DDR5-6000 (2x16GB)", new BigDecimal("129"), "DDR5", 6000, 32),
            new RAM("ram2", "32GB DDR5-5600 (2x16GB)", new BigDecimal("99"), "DDR5", 5600, 32),
            new RAM("ram3", "64GB DDR5-6000 (2x32GB)", new BigDecimal("219"), "DDR5", 6000, 64),
            new RAM("ram4", "16GB DDR5-5600 (2x8GB)", new BigDecimal("59"), "DDR5", 5600, 16),
            new RAM("ram5", "32GB DDR4-3200 (2x16GB)", new BigDecimal("79"), "DDR4", 3200, 32),
            new RAM("ram6", "16GB DDR4-3200 (2x8GB)", new BigDecimal("45"), "DDR4", 3200, 16),
            new RAM("ram0", "None", new BigDecimal("0"), "None", 0, 0));

    public static final List<GPU> ALL_GPUS = Arrays.asList(
            new GPU("gpu1", "NVIDIA RTX 4090", new BigDecimal("1599"), 450, 336, 100),
            new GPU("gpu2", "NVIDIA RTX 4080", new BigDecimal("1199"), 320, 310, 88),
            new GPU("gpu3", "NVIDIA RTX 4070 Ti", new BigDecimal("799"), 285, 285, 75),
            new GPU("gpu4", "AMD RX 7900 XTX", new BigDecimal("999"), 355, 320, 85),
            new GPU("gpu5", "AMD RX 7900 XT", new BigDecimal("849"), 315, 305, 78),
            new GPU("gpu6", "NVIDIA RTX 4060 Ti", new BigDecimal("499"), 160, 244, 55),
            new GPU("gpu0", "None", new BigDecimal("0"), 0, 0, 0));

    public static final List<Storage> ALL_STORAGE = Arrays.asList(
            new Storage("stor1", "2TB Samsung 990 Pro (NVMe)", new BigDecimal("189"), "M.2 NVMe",
                    2000, 7450),
            new Storage("stor2", "1TB Samsung 990 Pro (NVMe)", new BigDecimal("109"), "M.2 NVMe",
                    1000, 7450),
            new Storage("stor3", "2TB Samsung 870 EVO (SATA SSD)", new BigDecimal("149"),
                    "2.5\" SATA", 2000, 560),
            new Storage("stor4", "1TB Samsung 870 EVO (SATA SSD)", new BigDecimal("89"),
                    "2.5\" SATA", 1000, 560),
            new Storage("stor5", "4TB Seagate Barracuda (HDD)", new BigDecimal("89"),
                    "3.5\" SATA", 4000, 190),
            new Storage("stor6", "2TB WD Blue (HDD)", new BigDecimal("54"), "3.5\" SATA", 2000,
                    180),
            new Storage("stor7", "None", new BigDecimal("0"), "None", 0, 0));

    public static final List<PSU> ALL_PSUS = Arrays.asList(
            new PSU("psu1", "1000W Corsair RM1000x (80+ Gold)", new BigDecimal("199"), 1000,
                    "ATX", "80+ Gold"),
            new PSU("psu2", "850W Corsair RM850x (80+ Gold)", new BigDecimal("149"), 850, "ATX",
                    "80+ Gold"),
            new PSU("psu3", "750W Corsair RM750x (80+ Gold)", new BigDecimal("119"), 750, "ATX",
                    "80+ Gold"),
            new PSU("psu4", "650W Corsair RM650x (80+ Gold)", new BigDecimal("99"), 650, "ATX",
                    "80+ Gold"),
            new PSU("psu5", "550W Corsair RM550x (80+ Gold)", new BigDecimal("79"), 550, "ATX",
                    "80+ Gold"),
            new PSU("psu0", "None", new BigDecimal("0"), 0, "None", "None"));

    public static final List<Case> ALL_CASES = Arrays.asList(
            new Case("case1", "NZXT H7 Flow (ATX)", new BigDecimal("129"), "ATX", 400, 185,
                    "ATX"),
            new Case("case2", "Fractal Design North (ATX)", new BigDecimal("139"), "ATX", 360,
                    170, "ATX"),
            new Case("case3", "Lian Li O11 Dynamic (ATX)", new BigDecimal("149"), "ATX", 420, 167,
                    "ATX"),
            new Case("case4", "Corsair 4000D Airflow (ATX)", new BigDecimal("104"), "ATX", 360,
                    170, "ATX"),
            new Case("case0", "None", new BigDecimal("0"), "None", 0, 0, "None"));

    public static final List<Cooler> ALL_COOLERS = Arrays.asList(
            new Cooler("cool1", "Noctua NH-D15 (Air)", new BigDecimal("109"), "Air", 165, 180,
                    "LGA1700", "AM5"),
            new Cooler("cool2", "be quiet! Dark Rock Pro 4 (Air)", new BigDecimal("89"), "Air",
                    162, 250, "LGA1700", "AM5"),
            new Cooler("cool3", "Arctic Liquid Freezer II 360 (AIO)", new BigDecimal("149"),
                    "AIO", 60, 350, "LGA1700", "AM5"),
            new Cooler("cool4", "NZXT Kraken X63 (AIO)", new BigDecimal("139"), "AIO", 58, 280,
                    "LGA1700", "AM5"),
            new Cooler("cool5", "Cooler Master Hyper 212 (Air)", new BigDecimal("49"), "Air", 158,
                    150, "LGA1700", "AM5"),
            new Cooler("cool0", "None", new BigDecimal("0"), "None", 0, 0, "None", "None"));
}

