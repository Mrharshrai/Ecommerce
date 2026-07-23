package com.shop.userservice.user_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "driver_profile")
public class DriverProfile {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String vehicleNumber;

    // 🔧 Constructors
    public DriverProfile() {}

    public DriverProfile(User user, String phoneNumber, String vehicleNumber) {
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.vehicleNumber = vehicleNumber;
    }

    // ✅ Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
}
