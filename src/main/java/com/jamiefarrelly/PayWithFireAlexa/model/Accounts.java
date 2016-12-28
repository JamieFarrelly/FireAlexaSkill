package com.jamiefarrelly.PayWithFireAlexa.model;

import java.util.ArrayList;
import java.util.List;

public class Accounts {
    
    protected List<Account> accounts;

    public List<Account> getAccounts() {
        
        if (accounts == null) {
            accounts = new ArrayList<Account>();
        }
        
        return this.accounts;
    }

}
