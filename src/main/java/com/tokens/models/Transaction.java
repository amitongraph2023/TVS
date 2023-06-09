package com.tokens.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.ConstructorResult;
import javax.persistence.ColumnResult;


@Entity
@Table
@SqlResultSetMapping(
	    name = "CustomerDtoMapping",
	    classes = @ConstructorResult(
	        targetClass = CustomerDto.class,
	        columns = {
	            @ColumnResult(name = "customer_id", type = String.class),
	            @ColumnResult(name = "total_amount", type = Double.class)
	        }
	    )
	)
@SqlResultSetMapping(
	    name = "LocationDtoMapping",
	    classes = @ConstructorResult(
	        targetClass = LocationDto.class,
	        columns = {
	            @ColumnResult(name = "merchant_id", type = Integer.class),
	            @ColumnResult(name = "total_amount", type = Double.class)
	        }
	    )
	)
public class Transaction {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer internalTransactionId;
	
	@Column(name="transaction_id")
	private String transactionId;
	
	@Column(name="token")
	private String token;
	
	@Column(name="customer_id")
	private String customerId;
	
	@Column(name="amount")
	private Double amount;

	@Column(name="created_date")
	private String createdDate;
	
	@Column(name="merchant_id")
	private Integer merchantId;
	
	@Column(name="pos_id")
	private Integer posId;
	
	@Column(name="card_number")
	private String cardNumber;
	
	@Column(name="source_ip")
	private String sourceIp;
	
	//optional
	@Column(name="gps_location")
    private String gpsLocation;
	
	@Column(name="status")
	private String status;
	
	@Column(name="lastUpdated")
	private String lastUpdated;
	
	@Column(name="system_id")
	private String systemId;
	
	public Transaction() {}
	
	public Transaction(String transactionId, String token, String customerId,
			Double amount, String createdDate, Integer merchantId, Integer posId, String cardNumber, String sourceIp,
			String gpsLocation, String status,String systemId) {
		
		super();
		this.transactionId = transactionId;
		this.token = token;
		this.customerId = customerId;
		this.amount = amount;
		this.createdDate = createdDate;
		this.merchantId = merchantId;
		this.posId = posId;
		this.cardNumber = cardNumber;
		this.sourceIp = sourceIp;
		this.gpsLocation = gpsLocation;
		this.status = status;
		this.systemId = systemId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}


	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getGpsLocation() {
		return gpsLocation;
	}

	public void setGpsLocation(String gpsLocation) {
		this.gpsLocation = gpsLocation;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUserId() {
		return customerId;
	}

	public void setUserId(String userId) {
		this.customerId = userId;
	}

	public Integer getLocationId() {
		return merchantId;
	}

	public void setLocationId(Integer locationId) {
		this.merchantId = locationId;
	}

	public Integer getPosId() {
		return posId;
	}

	public void setPosId(Integer posId) {
		this.posId = posId;
	}

	public String getSourceIp() {
		return sourceIp;
	}

	public void setSourceIp(String sourceIp) {
		this.sourceIp = sourceIp;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getInternalTransactionId() {
		return internalTransactionId;
	}

	public void setInternalTransactionId(Integer internalTransactionId) {
		this.internalTransactionId = internalTransactionId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Integer getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Integer merchantId) {
		this.merchantId = merchantId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public String getSystemId() {
		return systemId;
	}
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}
