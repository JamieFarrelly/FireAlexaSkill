package com.jamiefarrelly.FireAlexa;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.jamiefarrelly.FireAlexa.model.incoming.ApiAccessToken;
import com.jamiefarrelly.FireAlexa.model.incoming.NewApiAccessTokenRequest;
import com.jamiefarrelly.FireAlexa.model.incoming.NewBatchRequest;
import com.jamiefarrelly.FireAlexa.model.incoming.NewBatchRequestItemInternalTransfer;
import com.jamiefarrelly.FireAlexa.model.outgoing.Account;
import com.jamiefarrelly.FireAlexa.model.outgoing.Accounts;
import com.jamiefarrelly.FireAlexa.model.outgoing.NewBatchRequestResponse;
import com.jamiefarrelly.FireAlexa.model.type.BatchRequestType;
import com.jamiefarrelly.FireAlexa.model.type.OperatingCurrencyType;

public class FireAPI {

    private static RestTemplate restTemplate = new RestTemplate();
    
    private static final String CREATE_ACCESS_TOKEN_URL = "https://api.fire.com/business/v1/apps/accesstokens";
    private static final String GET_ACCOUNT_DETAILS_URL = "https://api.fire.com/business/v1/accounts";
    
    /*
     * To move money around in Fire, you must do the following:
     *  - Create a batch request
     *  - Add items to that batch request
     *  - Submit that batch request to finally move the money around
     *  
     *  To keep things simple, we'll do all of this at once when someone asks Alexa to "move €x from AccountA to AccountB".
     *  We're only dealing with moving money from one of your own Fire accounts to another one of your Fire accounts.
     */
    private static final String CREATE_BATCH_REQUEST_URL = "https://api.fire.com/business/v1/batches";
    private static final String ADD_INTERNAL_TRANSFER_ITEM_TO_BATCH_REQUEST_URL = "https://api.fire.com/business/v1/batches/{uuid}/internaltransfers";
    private static final String SUBMIT_BATCH_REQUEST_URL = "https://api.fire.com/business/v1/batches/{uuid}";
    
    // for now this is hard coded - don't want to store these details for multiple users for example (security wise)
    // IMPORTANT - never commit these details to the likes of Github
    private static final String CLIENT_KEY = "CLIENT_KEY_FROM_FIRE";
    private static final String CLIENT_ID = "CLIENT_ID_FROM_FIRE";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN_FROM_FIRE";
    
    private static final String BEARER_TXT = "Bearer ";
    private static final String AUTH_HEADER_TXT = "Authorization";
    
    /**
     * Gets a list of your Fire accounts
     * 
     * @return List<Account>
     */
    public List<Account> getAccounts() {
        
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        
        // first, call over to the API to get an access token
        ApiAccessToken token = getApiAuthToken();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTH_HEADER_TXT, BEARER_TXT + token.getAccessToken());
        
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        ResponseEntity<Accounts> accountsResponse = restTemplate.exchange(GET_ACCOUNT_DETAILS_URL, HttpMethod.GET, entity, Accounts.class);
        List<Account> accountList = accountsResponse.getBody().getAccounts();
        return accountList;
    }
    
    /**
     * Transfer money between one fire account to another fire account. Handles the three API calls that are needed to do this.
     * 
     * @param currency
     * @param amountSpoken - if the user says 10 euro; we need to pass 1000 to the API for example so we need to do some changes to what's passed in.
     * @param accountIdFrom
     * @param accountIdTo
     */
    public void performInternalTransfer(OperatingCurrencyType currency, Integer amountSpoken, Integer accountIdFrom, Integer accountIdTo) {
        
        long amount = (long) amountSpoken * 100; // if the amount spoken was 10, we'll pass the amount down to the API as 1000 (1000 cent or pennies)
        
        // create a batch
        NewBatchRequestResponse newBatchResponse = createBatchRequest(currency, BatchRequestType.INTERNAL_TRANSFER);
        
        String batchUuid = newBatchResponse.getBatchUuid();
        
        // add an item to the batch
        addInternalTransferToBatch(batchUuid, accountIdFrom, accountIdTo, amount);
        
        // submit the batch
        submitBatchRequest(batchUuid);
    }

    // PRIVATE ------------------------------------------------------------------------------------------------------------------------------
    /**
     * Calls over to get an auth token so we can call other endpoints like the account details endpoint
     * 
     * Take a look at https://fire.com/docs/ for more info
     * 
     * @return ApiAccessToken
     */
    private ApiAccessToken getApiAuthToken() {
        
        NewApiAccessTokenRequest newApiAccessTokenRequest = new NewApiAccessTokenRequest();
        
        Long now = System.currentTimeMillis();
        
        // 3 pieces of info you get from business.fire.com
        newApiAccessTokenRequest.setClientId(CLIENT_ID);
        newApiAccessTokenRequest.setRefreshToken(REFRESH_TOKEN);
        newApiAccessTokenRequest.setClientSecret(DigestUtils.sha256Hex(now + CLIENT_KEY)); // as per docs https://fire.com/docs/ 
        
        newApiAccessTokenRequest.setNonce(now);
        
        ApiAccessToken token = restTemplate.postForObject(CREATE_ACCESS_TOKEN_URL, newApiAccessTokenRequest, ApiAccessToken.class);
        return token;
    }
    
    /**
     * Create a new batch request
     * 
     * @param currency
     * @param batchType
     * @return NewBatchRequestResponse
     */
    private NewBatchRequestResponse createBatchRequest(OperatingCurrencyType currency, BatchRequestType batchType) {
        
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        
        // first, call over to the API to get an access token
        ApiAccessToken token = getApiAuthToken();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTH_HEADER_TXT, BEARER_TXT + token.getAccessToken());
        
        NewBatchRequest newBatchRequest = new NewBatchRequest(); // we'll leave out all the optional fields like batch names and so on
        newBatchRequest.setCurrency(currency);
        newBatchRequest.setType(batchType);
        
        ResponseEntity<NewBatchRequestResponse> response = 
                        restTemplate.exchange(CREATE_BATCH_REQUEST_URL, HttpMethod.POST, new HttpEntity<NewBatchRequest>(newBatchRequest, headers), NewBatchRequestResponse.class);
        
        return response.getBody();
    }
    
    /**
     * Adds an internal transfer item to a batch request
     * 
     * @param batchRequestUuid - this must be from an internal batch request, there's a few different types of batch requests (they can not be mixed)
     * @param accountIdFrom - the id from the Account you want to send money from (it's the ican that's returned in the {@link #getAccounts()} endpoint)
     * @param accountIdTo - the id from the Account you want to send money to
     * @param amount - 100 for example is €1 or £1 depending on the currency of the batch that was created in the first place
     */
    private void addInternalTransferToBatch(String batchRequestUuid, Integer accountIdFrom, Integer accountIdTo, Long amount) {
        
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        
        // first, call over to the API to get an access token
        ApiAccessToken token = getApiAuthToken();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTH_HEADER_TXT, BEARER_TXT + token.getAccessToken());
        
        NewBatchRequestItemInternalTransfer internalTransferItem = new NewBatchRequestItemInternalTransfer(); // we'll leave out all the optional fields
        internalTransferItem.setAmount(amount);
        internalTransferItem.setIcanFrom(accountIdFrom);
        internalTransferItem.setIcanTo(accountIdTo);

        restTemplate.exchange(ADD_INTERNAL_TRANSFER_ITEM_TO_BATCH_REQUEST_URL, HttpMethod.POST, new HttpEntity<NewBatchRequestItemInternalTransfer>(internalTransferItem, headers), 
                                                                                                                                  NewBatchRequestResponse.class, batchRequestUuid);
    }
    
    /**
     * Submits a batch request. This endpoint is common between all types of batch requests so in the future it can be used for things other than
     * internal transfers, such as bank transfers.
     * 
     * @param batchRequestUuid
     */
    private void submitBatchRequest(String batchRequestUuid) {
        
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        
        // first, call over to the API to get an access token
        ApiAccessToken token = getApiAuthToken();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTH_HEADER_TXT, BEARER_TXT + token.getAccessToken());
        
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        restTemplate.exchange(SUBMIT_BATCH_REQUEST_URL, HttpMethod.PUT, entity, Void.class, batchRequestUuid);
    }
}
