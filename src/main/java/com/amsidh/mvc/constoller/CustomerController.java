package com.amsidh.mvc.constoller;

import com.amsidh.mvc.model.Customer;
import com.amsidh.mvc.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Data
@Slf4j
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @GetMapping("/{id}")
    public Customer getCustomer(@PathVariable Integer id) {
        Customer customer = customerService.findCustomer(id);
        log.info("Response return {}", objectMapper.writeValueAsString(customer), kv("BackendSystemName", "MS-Service"));
        return customer;
    }

    @SneakyThrows
    @PostMapping
    public Customer saveCustomer(@RequestBody Customer customer) {
        log.info("Request received {}", objectMapper.writeValueAsString(customer));
        return customerService.saveCustomer(customer);
    }

    @SneakyThrows
    @GetMapping
    public List<Customer> getAllCustomers() {
        List<Customer> customers = customerService.allCustomer();
        log.info("Request received {}", objectMapper.writeValueAsString(customers));
        return customers;
    }

    @DeleteMapping("/{id}")
    public String deleteCustomer(@PathVariable Integer id) {
        customerService.deleteCustomer(id);
        log.info("Customer delete successfully");
        return "Customer delete successfully";
    }

    @SneakyThrows
    @PutMapping("/{id}")
    public Customer updateCustomer(@PathVariable Integer id, @RequestBody Customer customer) {
        log.info("Request to update customer with id {} and request {}", id, objectMapper.writeValueAsString(customer));
        return customerService.updateCustomer(id, customer);
    }
}
