package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.util.AccountConstant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.Assertions;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
   // accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }
 
  /*
   * Test Case 1 - successfull money transfer
   */
  
  @Test
	public void testTransferMoney() throws Exception {
		String accountFromId = "Id-1221";
		String accountToId = "Id-3421";
		BigDecimal amount = new BigDecimal(100);		
		
		Account accountFrom = new Account(accountFromId, new BigDecimal("500"));
		Account accountTo = new Account(accountFromId, new BigDecimal("100"));
		
		MoneyTransferRequest request = new MoneyTransferRequest(accountFromId, accountToId, amount);
		
		this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountFromId\":\"Id-1221\",\"accountToId\":\"Id-3421\",\"amount\":100}"))
	      .andExpect(status().isOk())
	      .andExpect(
	        content().string("Money Transferred Successfully"));	
	     
	      
		Assertions.assertEquals(new BigDecimal("400"), accountFrom.getBalance());
		Assertions.assertEquals(new BigDecimal("200"), accountTo.getBalance());
	}
  
  /*
   * Test Case 2 - From Account Not found
   */
  
  @Test
	public void testFromAcoountNotExists() throws Exception {
		String accountFromId = "Id-XXX";
		String accountToId = "Id-3421";
		BigDecimal amount = new BigDecimal(100);		
		
		Account accountTo = new Account(accountFromId, new BigDecimal("100"));
		
		MoneyTransferRequest request = new MoneyTransferRequest(accountFromId, accountToId, amount);
		
		this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountFromId\":\"Id-XXX\",\"accountToId\":\"Id-3421\",\"amount\":100}"))
	      .andExpect(status().isBadRequest())
	      .andExpect(
	        content().string(AccountConstant.ACCOUNT_NOT_EXIST+" : " + accountFromId));     
		
	}
  
  /*
   * Test Case 2 - To Account Not found
   */
  
  @Test
	public void testToAcoountNotExists() throws Exception {
		String accountFromId = "Id-1221";
		String accountToId = "Id-YYYY";
		BigDecimal amount = new BigDecimal(100);		
		
		Account accountFrom = new Account(accountFromId, new BigDecimal("500"));
		
		MoneyTransferRequest request = new MoneyTransferRequest(accountFromId, accountToId, amount);
		
		this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountFromId\":\"Id-1221\",\"accountToId\":\"Id-YYYY\",\"amount\":100}"))
	      .andExpect(status().isBadRequest())
	      .andExpect(
	        content().string(AccountConstant.ACCOUNT_NOT_EXIST +" : " + accountToId));     
		
	}
  
  
  /*
   * Test Case 1 - OverDraft
   */
  
  @Test
	public void testOverDraft() throws Exception {
		String accountFromId = "Id-1221";
		String accountToId = "Id-3421";
		BigDecimal amount = new BigDecimal(600);		
		
		Account accountFrom = new Account(accountFromId, new BigDecimal("500"));
		Account accountTo = new Account(accountFromId, new BigDecimal("100"));
		
		MoneyTransferRequest request = new MoneyTransferRequest(accountFromId, accountToId, amount);
		
		this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountFromId\":\"Id-1221\",\"accountToId\":\"Id-3421\",\"amount\":600}"))
	      .andExpect(status().isBadRequest())
	      .andExpect(
	        content().string(AccountConstant.OVERDRAFT+" : " + accountFrom.getAccountId()));	

	}
}
