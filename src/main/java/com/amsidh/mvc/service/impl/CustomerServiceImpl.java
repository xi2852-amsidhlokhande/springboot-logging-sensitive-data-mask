package com.amsidh.mvc.service.impl;

import com.amsidh.mvc.model.Address;
import com.amsidh.mvc.model.Customer;
import com.amsidh.mvc.service.CustomerService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;


@Data
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final List<Customer> customers = new ArrayList<>();

    static {

        customers.add(Customer.builder().id(1).name("Amsidh").emailId("amsidhlokhande@gmail.com").mobileNumber("8108551845").panCard("ABCDE3245J").aadhaarCard("549824674826").address(Address.builder().city("Pune").state("MH").pinCode(412105L).build()).build());
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        customers.add(customer);
        return customer;
    }

    @Override
    public Customer findCustomer(Integer id) {
        return customers.stream().filter(customer -> Objects.equals(customer.getId(), id)).findFirst().orElseThrow(() -> new RuntimeException("Customer not found with id " + id));
    }

    @Override
    public Customer updateCustomer(Integer id, Customer updateCustomer) {
        return customers.stream().filter(customer -> Objects.equals(customer.getId(), id)).findFirst().map(customer -> {
            ofNullable(updateCustomer.getName()).ifPresent(customer::setName);
            ofNullable(updateCustomer.getEmailId()).ifPresent(customer::setEmailId);
            ofNullable(updateCustomer.getAadhaarCard()).ifPresent(customer::setAadhaarCard);
            ofNullable(updateCustomer.getPanCard()).ifPresent(customer::setPanCard);
            ofNullable(updateCustomer.getMobileNumber()).ifPresent(customer::setMobileNumber);
            ofNullable(updateCustomer.getAddress()).ifPresent(updateAddress -> {
                Address address = customer.getAddress();
                ofNullable(updateAddress.getCity()).ifPresent(address::setCity);
                ofNullable(updateAddress.getState()).ifPresent(address::setState);
                ofNullable(updateAddress.getPinCode()).ifPresent(address::setPinCode);
                customer.setAddress(address);
            });
            return customer;
        }).orElseThrow(() -> new RuntimeException("Customer not found with id " + id));
    }

    @Override
    public void deleteCustomer(Integer id) {
        customers.removeIf(customer -> Objects.equals(customer.getId(), id));
    }

    @Override
    public List<Customer> allCustomer() {
        return customers;
    }
}
