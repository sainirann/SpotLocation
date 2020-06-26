package com.example.locatespot;

import android.location.Address;

/**
 * Class for the Retrieved Location, which is the string address
 * user entered and {@link Address}
 */
public class RetrievedLocation {
    Address address;
    String completeAddress;

    RetrievedLocation(Address address, String completeAddress) {
        this.address = address;
        this.completeAddress = completeAddress;
    }
}
