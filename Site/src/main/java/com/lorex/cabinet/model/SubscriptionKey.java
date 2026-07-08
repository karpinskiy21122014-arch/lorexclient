package com.lorex.cabinet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subscription_keys")
public class SubscriptionKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyCode;
    private int days;
    private boolean used;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKeyCode() { return keyCode; }
    public void setKeyCode(String keyCode) { this.keyCode = keyCode; }
    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}