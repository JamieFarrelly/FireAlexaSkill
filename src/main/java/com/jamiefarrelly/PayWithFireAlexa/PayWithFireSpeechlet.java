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
    
    private static final String BALANCE_CHECK_SPEECH_TEXT = "You can say ask Pay with Fire to check my balance to see what your balance is!";
    private static final String INTERNAL_TRANSFER_AMOUNT_ERROR = "Sorry, I did not hear the amount. Please say again?";
    private static final String INTERNAL_TRANSFER_ACCOUNTS_ERROR = "Sorry, I couldn't find both of those accounts. Please say again?";
    private static final String INTERNAL_TRANSFER_ACCOUNT_CURRENCIES_DO_NOT_MATCH_ERROR = "Sorry, both accounts must be the same currency";
    
    // keys to get information from the utterance
    private static final String FROM_ACCOUNT_SLOT = "FromAccount";
    private static final String TO_ACCOUNT_SLOT = "ToAccount";
    private static final String AMOUNT_SLOT = "Amount";

    // PUBLIC ---------------------------------------------------------------------------------------------
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        
        if ("PayWithFireBalanceIntent".equals(intentName)) {
            return getBalanceResponse();
        } else if ("PayWithFireInternalTransferIntent".equals(intentName)) {
            return getMakeInternalTransferResponse(intent);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getAskSpeechletResponse(BALANCE_CHECK_SPEECH_TEXT);
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
        Slot amountSlot = intent.getSlot(AMOUNT_SLOT);
        
        Double amount;
        try {
            amount = Double.parseDouble(amountSlot.getValue());
        } catch (NumberFormatException e) {
            return getAskSpeechletResponse(INTERNAL_TRANSFER_AMOUNT_ERROR);
        }
        
        // make sure the account names that the user said are actually valid account names
        // TODO: Look in to removing the need of slots for account names, you shouldn't need to update LIST_OF_ACCOUNT_NAMES.txt every time you've a new Fire account
        // https://medium.com/voiceflow/tips-and-gotchas-using-alexa-custom-slots-b88f97f26b06
        if (fromAccountSlot != null && fromAccountSlot.getValue() != null && toAccountSlot != null && toAccountSlot.getValue() != null) {
            
            String fromAccountName = fromAccountSlot.getValue();
            String toAccountName = toAccountSlot.getValue();
                            
            // Get list of fire accounts
            List<Account> fireAccounts = FIRE_API.getAccounts();
            
            // each Fire account has a unique name so using findFirst
            Account fromAccount = fireAccounts.stream().filter(acc -> acc.getName().equalsIgnoreCase(fromAccountName)).findAny().orElse(null);
            Account toAccount = fireAccounts.stream().filter(acc -> acc.getName().equalsIgnoreCase(toAccountName)).findFirst().orElse(null);
            
            // paranoid check, should never happen. if we have the account name in LIST_OF_ACCOUNT_NAMES.txt it should match one of your Fire accounts
            if (fromAccount == null || toAccount == null) {
                return getAskSpeechletResponse(INTERNAL_TRANSFER_ACCOUNTS_ERROR);
            }
            
            if (fromAccount.getCurrency().getCode() != toAccount.getCurrency().getCode()) { // we don't allow FX through batches
                return getAskSpeechletResponse(INTERNAL_TRANSFER_ACCOUNT_CURRENCIES_DO_NOT_MATCH_ERROR);
            }
            
            // FIXME: finally, perform the internal transfer
            // FIRE_API.performInternalTransfer(currency, amount, accountIdFrom, accountIdTo);
            
        } else {
            // There was no item in the intent so return the help prompt.
            return getAskSpeechletResponse(INTERNAL_TRANSFER_ACCOUNTS_ERROR);
        }
        
        return null;
    }
    
    /**
     * 
     * Creates a {@code SpeechletResponse} for the help intent.
     * 
     * @param speechText
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getAskSpeechletResponse(String speechText) {

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