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
import com.jamiefarrelly.PayWithFireAlexa.model.type.OperatingCurrencyType;

/**
 * Two main flows at the moment:
 *
 * - "Ask Fire what my balance is"
 * - "Ask Fire to move 10 euro from AccountA to AccountB"
 * 
 * Based on https://github.com/amzn/alexa-skills-kit-java
 */
public class PayWithFireSpeechlet implements Speechlet {
    
    private static final PayWithFireAPI FIRE_API = new PayWithFireAPI();
    
    private static final String WELCOME_AND_HELP_SPEECH_TEXT = "Welcome to the Pay with Fire, you can ask for your balance or moving money between your Fire accounts";
    private static final String INTERNAL_TRANSFER_AMOUNT_ERROR = "Sorry, I did not hear the amount. Please say again?";
    private static final String INTERNAL_TRANSFER_ACCOUNT_CURRENCY_ERROR = "Sorry, I did not hear the currency. Please say again?";
    private static final String INTERNAL_TRANSFER_ACCOUNTS_ERROR = "Sorry, I couldn't find both of those accounts. Please say again?";
    private static final String INTERNAL_TRANSFER_ACCOUNT_CURRENCIES_DO_NOT_MATCH_ERROR = "Sorry, both accounts must be the same currency";
    
    // keys to get information from the utterance
    private static final String FROM_ACCOUNT_SLOT = "FromAccount";
    private static final String TO_ACCOUNT_SLOT = "ToAccount";
    private static final String AMOUNT_SLOT = "Amount";
    private static final String CURRENCY_SLOT = "Currency";

    // PUBLIC ---------------------------------------------------------------------------------------------
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        
        if ("PayWithFireBalanceIntent".equals(intentName)) {
            return getBalanceResponse();
        } else if ("PayWithFireInternalTransferIntent".equals(intentName)) {
            return getMakeInternalTransferResponse(intent);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getAskSpeechletResponse(WELCOME_AND_HELP_SPEECH_TEXT);
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

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("PayWithFire");
        card.setContent(WELCOME_AND_HELP_SPEECH_TEXT);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(WELCOME_AND_HELP_SPEECH_TEXT);

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
     * 
     * Proof of concept to move money around by using Alexa. There's lots of room for improvement to make the code cleaner (having 4 slots doesn't help!) along with
     * adding more functionality.
     * 
     * Moves money between one of your Fire accounts to another Fire account. There's a number of checks that we must do including:
     * 
     *   - Makes sure that the amount is actually a number (Amazon doesn't have the concept of AMAZON.MONEY for slots yet). They recommended to me that
     *     I should parse {Euros} point/dot {Cents} as two separate slots but to keep things simple we'll only deal with whole numbers.
     *   
     *   - Makes sure the user said 2 accounts that they have as Fire accounts. It doesn't have to be a match with LIST_OF_ACCOUNT_NAMES.txt though.
     *     If someone says an account name that isn't there, it'll still work. The file is just to help Amazon match accounts if someone says a slight
     *     variation of an account name. See https://medium.com/voiceflow/tips-and-gotchas-using-alexa-custom-slots-b88f97f26b06
     * 
     * @param intent
     */
    private SpeechletResponse getMakeInternalTransferResponse(Intent intent) {
        
        Slot fromAccountSlot = intent.getSlot(FROM_ACCOUNT_SLOT);
        Slot toAccountSlot = intent.getSlot(TO_ACCOUNT_SLOT);
        Slot amountSlot = intent.getSlot(AMOUNT_SLOT);
        Slot currencySlot = intent.getSlot(CURRENCY_SLOT);
        
        Integer amount;

        // slots can be missing or provided with an empty value
        if (amountSlot != null && amountSlot.getValue() != null) {
            
            try {
                amount = Integer.parseInt(amountSlot.getValue());
            } catch (NumberFormatException e) {
                return getAskSpeechletResponse(INTERNAL_TRANSFER_AMOUNT_ERROR);
            }
        } else {
            return getAskSpeechletResponse(INTERNAL_TRANSFER_AMOUNT_ERROR);
        }

        // slots can be missing or provided with an empty value
        if (fromAccountSlot != null && fromAccountSlot.getValue() != null && toAccountSlot != null && toAccountSlot.getValue() != null) {
            
            String fromAccountName = fromAccountSlot.getValue();
            String toAccountName = toAccountSlot.getValue();
                            
            // Get list of fire accounts
            List<Account> fireAccounts = FIRE_API.getAccounts();
            
            // each Fire account has a unique name so using findAny
            Account fromAccount = fireAccounts.stream().filter(acc -> acc.getName().equalsIgnoreCase(fromAccountName)).findAny().orElse(null);
            Account toAccount = fireAccounts.stream().filter(acc -> acc.getName().equalsIgnoreCase(toAccountName)).findAny().orElse(null);
            
            // we couldn't find at least one of the account names the user said
            if (fromAccount == null || toAccount == null) {
                return getAskSpeechletResponse(INTERNAL_TRANSFER_ACCOUNTS_ERROR);
            }
            
            if (fromAccount.getCurrency().getCode() != toAccount.getCurrency().getCode()) { // we don't allow FX through batches
                return getAskSpeechletResponse(INTERNAL_TRANSFER_ACCOUNT_CURRENCIES_DO_NOT_MATCH_ERROR);
            }
            
            String currency = currencySlot.getValue(); // TODO: null check
            
            OperatingCurrencyType currencyType = null;
            
            // these checks cover both if the person says "euro" or "euros" for example. likewise for "pound" and "pounds"
            if (currency.contains("euro")) {
                currencyType = OperatingCurrencyType.EUR;
            } else if (currency.contains("pound")) {
                currencyType = OperatingCurrencyType.GBP;
            } else {
                return getAskSpeechletResponse(INTERNAL_TRANSFER_ACCOUNT_CURRENCY_ERROR);
            }
            
            // finally, perform the internal transfer
            FIRE_API.performInternalTransfer(currencyType, amount, fromAccount.getIcan(), toAccount.getIcan());
            
            String speechText = "Money has been transfered from " + fromAccountName + " to " + toAccountName;
            
            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("PayWithFire");
            card.setContent(speechText);

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);

            return SpeechletResponse.newTellResponse(speech, card);
            
        } else {
            // There was no item in the intent so return the help prompt.
            return getAskSpeechletResponse(INTERNAL_TRANSFER_ACCOUNTS_ERROR);
        }
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