package com.jamiefarrelly.PayWithFireAlexa;

import java.util.List;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.jamiefarrelly.PayWithFireAlexa.model.outgoing.Account;

/**
 * 
 * Based on https://github.com/amzn/alexa-skills-kit-java
 */
public class PayWithFireSpeechlet implements Speechlet {
    
    private static final PayWithFireAPI FIRE_API = new PayWithFireAPI();
    
    // keys to get information from the utterance
    private static final String FROM_ACCOUNT_SLOT = "FromAccount";
    private static final String TO_ACCOUNT_SLOT = "ToAccount";

    // PUBLIC ---------------------------------------------------------------------------------------------
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        
        if ("PayWithFireBalanceIntent".equals(intentName)) {
            return getBalanceResponse();
        } else if ("PayWithFireInternalTransferIntent".equals(intentName)) {
            return getMakeInternalTransferResponse(intent);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        
        return getWelcomeResponse();
    }

    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        // TODO Auto-generated method stub
        
    }

    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        // TODO Auto-generated method stub
        
    }  
    
    // PRIVATE ---------------------------------------------------------------------------------------------
    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        
        String speechText = "Welcome to the Pay with Fire, you can ask for your balance by saying check my balance";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("PayWithFire");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
    
    /**
     * Creates a {@code SpeechletResponse} for the Pay with Fire intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getBalanceResponse() {
        
        List<Account> fireAccounts = FIRE_API.getAccounts();
        
        String speechText = "";
        double amount;
        String currencySymbol;
        
        for (int i = 0; i < fireAccounts.size(); i++) {
            
            Account fireAccount = fireAccounts.get(i);
            
            amount = fireAccount.getBalance() / 100.0;
            currencySymbol = fireAccount.getCurrency().getCode().getCurrencyCode() == "EUR" ? "€" : "£"; // just two currencies at the moment
            
            speechText = speechText + " " + fireAccount.getName() + " has " + currencySymbol + amount
                         + (i == (fireAccounts.size() - 1) ? "." : ","); // last account - full stop, we're finished.
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("PayWithFire");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }
    
    /**
     * WIP 
     */
    private SpeechletResponse getMakeInternalTransferResponse(Intent intent) {
        
        Slot fromAccountSlot = intent.getSlot(FROM_ACCOUNT_SLOT);
        Slot toAccountSlot = intent.getSlot(TO_ACCOUNT_SLOT);
        
        // make sure the account names that the user said are actually valid account names
        // TODO: Look in to removing the need of slots for account names, you shouldn't need to update LIST_OF_ACCOUNT_NAMES.txt every time you've a new Fire account
        if (fromAccountSlot != null && fromAccountSlot.getValue() != null && toAccountSlot != null && toAccountSlot.getValue() != null) {
            
            String fromAccount = fromAccountSlot.getValue();
            String toAccount = toAccountSlot.getValue();
                            
            // Get list of fire accounts
            List<Account> fireAccounts = FIRE_API.getAccounts();
            
            boolean fromAccountExists = fireAccounts.stream().anyMatch(acc -> acc.getName().equalsIgnoreCase(fromAccount));
            boolean toAccountExists = fireAccounts.stream().anyMatch(acc -> acc.getName().equalsIgnoreCase(toAccount));
            
            // paranoid check, should never happen. if we have the account name in LIST_OF_ACCOUNT_NAMES.txt it should match one of your Fire accounts
            if (fromAccountExists == false || toAccountExists == false) {
                return getHelpResponse(); // TODO: have 2 help responses for the two different intents
            }
            
            // FIXME: finally, perform the internal transfer
            // FIRE_API.performInternalTransfer(currency, amount, accountIdFrom, accountIdTo);
            
        } else {
            // There was no item in the intent so return the help prompt.
            return getHelpResponse(); // TODO: have 2 help responses for the two different intents
        }
        
        return null;
    }
    
    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        
        String speechText = "You can say ask Pay with Fire to check my balance to see what your balance is!";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("PayWithFire");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

}