package com.vyxentra.vehicle.enums;


public enum VehicleType {
    BIKE("Bike", 2),
    CAR("Car", 4),
    SUV("SUV", 4),
    TRUCK("Truck", 6),
    BUS("Bus", 6),
    ELECTRIC_BIKE("Electric Bike", 2),
    ELECTRIC_CAR("Electric Car", 4);

    private final String displayName;
    private final int wheelCount;

    VehicleType(String displayName, int wheelCount) {
        this.displayName = displayName;
        this.wheelCount = wheelCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWheelCount() {
        return wheelCount;
    }

    public boolean isTwoWheeler() {
        return wheelCount == 2;
    }

    public boolean isFourWheeler() {
        return wheelCount == 4;
    }

    public boolean isElectric() {
        return this == ELECTRIC_BIKE || this == ELECTRIC_CAR;
    }
}