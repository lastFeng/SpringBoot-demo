package com.example.mongodgdemo;

import com.example.mongodgdemo.domain.Customer;
import com.example.mongodgdemo.repository.CustomerRepository;
import com.example.mongodgdemo.service.CustomerService;
import com.sun.corba.se.spi.orbutil.threadpool.NoSuchThreadPoolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

//@EnableMongoRepositories(basePackages = "com.example.mongodgdemo.repository")
@SpringBootApplication
public class MongodbDemoApplication implements CommandLineRunner {

    @Autowired
    private CustomerService repository;

    public static void main(String[] args) {
        SpringApplication.run(MongodbDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        repository.deleteAll();

        // save a couple of customers
        repository.save(new Customer("Alice", "Smith"));
        repository.save(new Customer("Tom", "Smith"));

        System.out.println();
        // fetch all customers
        System.out.println("Customers found with findAll(): ");
        System.out.println("--------------------------------");
        for (Customer customer : repository.findAll()) {
            System.out.println(customer);
        }

        System.out.println();
        // fetch an individual customer
        System.out.println("Customer found with findByFirstName('Alice')");
        System.out.println("--------------------------------------------");
        System.out.println(repository.findByFirstName("Alice"));

        System.out.println();
        // fetch an individual customer
        System.out.println("Customer found with findByFLastName('Smith')");
        System.out.println("--------------------------------------------");
        for (Customer customer: repository.findByLastName("Smith")) {
            System.out.println(customer);
        }

    }
}
