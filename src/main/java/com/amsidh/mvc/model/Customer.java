package com.amsidh.mvc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {

    private Integer id;
    private String customerFullName;
    private String emailId;
    private String mobileNumber;
    private String panCard;
    private String aadhaarNumber;

    private Address address;
}
