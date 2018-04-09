package com.jamiefarrelly.PayWithFireAlexa;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.jamiefarrelly.PayWithFireAlexa.model.outgoing.Account;

/**
 * 
 * At the moment there's no standard way of testing Alexa skills in Java. The Alexa Simulator is the recommended way of testing your entire skill
 * which is in the Alexa developer console.
 *
 */
public class PayWithFireAPITest {
    
    private static final PayWithFireAPI FIRE_API = new PayWithFireAPI();
    
    /**
     * 
     * Before running this, make sure you've changed the API application details in PayWithFireAPI
     * Pretty much just a check to make sure your API application details are correct before deploying JAR to Lambda
     * 
     */
    @Test
    public void getAccountsTest() {
        
        List<Account> accounts = FIRE_API.getAccounts();
        
        // not checking account names or balances - these could change
        Assert.assertNotNull(accounts);
    }
}
