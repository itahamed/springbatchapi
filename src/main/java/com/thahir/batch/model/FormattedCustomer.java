package com.thahir.batch.model;

import lombok.Getter;
import lombok.Setter;
// Output model - formatted for fixed-length output
@Getter
@Setter
public class FormattedCustomer {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

}