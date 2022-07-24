package com.amsidh.mvc.service;

import com.amsidh.mvc.model.Customer;

import java.util.List;

public interface CustomerService {
    Customer saveCustomer(Customer customer);

    Customer findCustomer(Integer id);

    Customer updateCustomer(Integer id, Customer updateCustomer);

    void deleteCustomer(Integer id);

    List<Customer> allCustomer();
}
