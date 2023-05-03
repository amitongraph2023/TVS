package com.tokens.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Location {

//	Station_name
//	Merchant_ID
//	Terminal_id
//	GPS_coordinate
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer internalMerchantId;
	
	@Column(name="merchant_id")
	private Integer merchantId;
	
	@Column(name="merchant_name")
	private String merchantName;
	
	@OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
	private List<Pos> pos;

	public Integer getInternalMerchantId() {
		return internalMerchantId;
	}

	public void setInternalMerchantId(Integer internalMerchantId) {
		this.internalMerchantId = internalMerchantId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public Integer getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Integer merchantId) {
		this.merchantId = merchantId;
	}

	public List<Pos> getPos() {
		return pos;
	}

	public void setPos(List<Pos> pos) {
		this.pos = pos;
	}
	
}
