package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }
  
  @Test
  void tarnsferMoney() {
    MoneyTransferRequest request = new MoneyTransferRequest("Id-123","id-246",new BigDecimal("200"));
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);
    
    Account account1 = new Account("Id-246");
    account.setBalance(new BigDecimal(2000));
    this.accountsService.createAccount(account1);    
    
    String res =  this.accountsService.transferMoney(request);

    assertEquals(res,"Money Transferred Successfully");
  }
  

}
