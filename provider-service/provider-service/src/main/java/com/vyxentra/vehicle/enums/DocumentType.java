package com.vyxentra.vehicle.enums;


public enum DocumentType {
    GST_CERTIFICATE("GST Certificate", true),
    PAN_CARD("PAN Card", true),
    BUSINESS_REGISTRATION("Business Registration", true),
    ADDRESS_PROOF("Address Proof", true),
    IDENTITY_PROOF("Identity Proof", true),
    INSURANCE_CERTIFICATE("Insurance Certificate", false),
    LICENSE("Trade License", true),
    BANK_STATEMENT("Bank Statement", false),
    PHOTOGRAPH("Photograph", false),
    CERTIFICATION("Service Certification", false);

    private final String displayName;
    private final boolean mandatory;

    DocumentType(String displayName, boolean mandatory) {
        this.displayName = displayName;
        this.mandatory = mandatory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isMandatory() {
        return mandatory;
    }
}
