package com.jamiefarrelly.PayWithFireAlexa.model.incoming;

import com.jamiefarrelly.PayWithFireAlexa.model.type.BatchRequestType;
import com.jamiefarrelly.PayWithFireAlexa.model.type.OperatingCurrencyType;

public class NewBatchRequest {

    private String batchUuid;
    
    private String batchName;
    
    private String jobNumber;
    
    private BatchRequestType type;
    
    private OperatingCurrencyType currency;
    
    private String callbackUrl;

    public String getBatchUuid() {
        return batchUuid;
    }

    public void setBatchUuid(String batchUuid) {
        this.batchUuid = batchUuid;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public BatchRequestType getType() {
        return type;
    }

    public void setType(BatchRequestType type) {
        this.type = type;
    }

    public OperatingCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(OperatingCurrencyType currency) {
        this.currency = currency;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
