package com.jamiefarrelly.PayWithFireAlexa;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.jamiefarrelly.PayWithFireAlexa.model.outgoing.Account;

public class PayWithFireAPITest {

    /**
     * Before running this, make sure you've changed the API application details in PayWithFireAPI
     * 
     * Pretty much just a check to make sure your API application details are correct before deploying JAR to Lambda
     */
    @Test
    public void getAccountsTest() {
        
        List<Account> accounts = PayWithFireAPI.getAccounts();
        
        // not checking account names or balances - these could change
        Assert.assertNotNull(accounts);
    }
}
