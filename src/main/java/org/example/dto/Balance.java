package org.example.dto;

public class Balance {
    private String userId;
    private double balance;

    public Balance() {
    }

    public Balance(String userId, double balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Balance{userId='" + userId + "', balance=" + balance + '}';
    }
}
