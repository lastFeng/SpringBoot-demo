package com.example.mongodgdemo.service;

import com.example.mongodgdemo.domain.Customer;
import com.example.mongodgdemo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author guowf
 * @mail: guowf_buaa@163.com
 * @date created in 2020/2/27 10:42
 * @description:
 */
@Service
public class CustomerService {
    @Autowired
    private CustomerRepository repository;

    public void deleteAll() {
        repository.deleteAll();
    }

    public void save(Customer customer) {
        repository.save(customer);
    }

    public List<Customer> findAll() {
        return repository.findAll();
    }

    public Customer findByFirstName(String firstName) {
        return repository.findByFirstName(firstName);
    }

    public List<Customer> findByLastName(String lastName) {
        return repository.findByLastName(lastName);
    }
}
