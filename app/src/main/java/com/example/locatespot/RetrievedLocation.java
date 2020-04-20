package com.example.locatespot;

import android.location.Address;

public class RetrievedLocation {
    Address address;
    String completeAddress;

    RetrievedLocation(Address address, String completeAddress) {
        this.address = address;
        this.completeAddress = completeAddress;
    }
}
