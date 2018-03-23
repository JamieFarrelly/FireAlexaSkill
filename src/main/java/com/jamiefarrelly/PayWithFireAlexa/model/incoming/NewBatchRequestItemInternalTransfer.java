package com.jamiefarrelly.PayWithFireAlexa.model.incoming;

public class NewBatchRequestItemInternalTransfer {

    private String batchItemUuid;
    
    private Integer icanFrom;

    private Integer icanTo;
    
    private Long amount; 

    protected String ref;

    public String getBatchItemUuid() {
        return batchItemUuid;
    }

    public void setBatchItemUuid(String batchItemUuid) {
        this.batchItemUuid = batchItemUuid;
    }

    public Integer getIcanFrom() {
        return icanFrom;
    }

    public void setIcanFrom(Integer icanFrom) {
        this.icanFrom = icanFrom;
    }

    public Integer getIcanTo() {
        return icanTo;
    }

    public void setIcanTo(Integer icanTo) {
        this.icanTo = icanTo;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
