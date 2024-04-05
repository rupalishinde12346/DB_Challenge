package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.OverDraftException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import com.dws.challenge.util.AccountConstant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {
	
	private final EmailNotificationService emailNotificationService;
	
	@Autowired
	  public AccountsRepositoryInMemory(EmailNotificationService emailNotificationService) {	   
	    this.emailNotificationService = emailNotificationService;
	  } 

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

	@Override
	public String transferMoney(MoneyTransferRequest moneyTransferRequest) {
		// TODO Auto-generated method stub
		
		Account accountFrom=accounts.get(moneyTransferRequest.getAccountFromId());	
		if(accountFrom == null)
		{
			throw new AccountNotExistException(AccountConstant.ACCOUNT_NOT_EXIST+" : "+ moneyTransferRequest.getAccountFromId() , AccountConstant.ACCOUNT_ERROR_CODE );
		}
	
		Account accountTo=accounts.get(moneyTransferRequest.getAccountToId());
		if(accountTo == null)
		{
			throw new AccountNotExistException(AccountConstant.ACCOUNT_NOT_EXIST + " : " + moneyTransferRequest.getAccountToId() , AccountConstant.ACCOUNT_ERROR_CODE);
		}
		
		if(accountFrom.getBalance().compareTo(moneyTransferRequest.getAmount()) < 0) {
			throw new OverDraftException(AccountConstant.OVERDRAFT+" : " + accountFrom.getAccountId() , AccountConstant.Overdraft_ERROR_CODE);
		}
		
		accountFrom.setBalance(accountFrom.getBalance().subtract(moneyTransferRequest.getAmount()));
		accountTo.setBalance(accountTo.getBalance().add(moneyTransferRequest.getAmount()));
		
		// send notification		
		emailNotificationService.notifyAboutTransfer(accountFrom, "Your account is debited with "+ moneyTransferRequest.getAmount()+" and is tarnsferred successfully to recepient :"+ accountTo );
		emailNotificationService.notifyAboutTransfer(accountTo, "Your account is credited successfully with "+ moneyTransferRequest.getAmount()+" by "+ accountFrom );
		
		return "Money Transferred Successfully";
	}

}
