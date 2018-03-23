package com.jamiefarrelly.PayWithFireAlexa.model.outgoing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * API returns more info but this is all we need here
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

    private String name;

    private CurrencyTypeDescription currency;

    private Long balance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CurrencyTypeDescription getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyTypeDescription currency) {
        this.currency = currency;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}
