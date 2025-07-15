package entity;

import javax.persistence.Embeddable;

@Embeddable
public class BankInfo {
    private String bankName;
    private String accountNumber;

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}