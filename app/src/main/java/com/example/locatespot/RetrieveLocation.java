package com.example.locatespot;

import android.location.Address;

public class RetrieveLocation {
    Address address;
    String completeAddress;
    public RetrieveLocation(Address address, String completeAddress) {
        this.address = address;
        this.completeAddress = completeAddress;
    }
}
