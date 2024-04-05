package com.dws.challenge.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MoneyTransferRequest {
	@NotNull
	@NotEmpty
	private final String accountFromId;
	
	  @NotNull
	  @NotEmpty
	  private final String accountToId;
	  
	  @NotNull
	  @Min(value = 0, message = "Transfer Amount can not be less than zero.")
	  private final BigDecimal amount;
	  
	

	  @JsonCreator
	  public MoneyTransferRequest(@JsonProperty("accountFromId") String accountFromId,@JsonProperty("accountToId") String accountToId,
	    @JsonProperty("amount") BigDecimal amount) {
	    this.accountFromId = accountFromId;
	    this.accountToId = accountToId;
	    this.amount = amount;
	  }

	public String getAccountFromId() {
		return accountFromId;
	}

	public String getAccountToId() {
		return accountToId;
	}

	public BigDecimal getAmount() {
		return amount;
	}		
	  
	  

}
