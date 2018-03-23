package com.jamiefarrelly.PayWithFireAlexa.model.type;

public enum CurrencyType {

    EUR("EUR","Euro"),
    GBP("GBP","Pound Sterling");
    
    private String currencyCode;
    
    private String currencyName;
    
    private CurrencyType(String code, String name) {
        this.currencyCode = code;
        this.currencyName = name;
    }

    public String value() {
        return name();
    }

    public static CurrencyType fromValue(String v) {
        return valueOf(v);
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }
}
