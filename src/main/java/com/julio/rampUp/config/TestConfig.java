package com.julio.rampUp.config;

import java.time.Instant;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.OrderItem;
import com.julio.rampUp.entities.ProductOffering;
import com.julio.rampUp.entities.Role;
import com.julio.rampUp.entities.Ticket;
import com.julio.rampUp.entities.User;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.entities.enums.Authorities;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.entities.enums.POState;
import com.julio.rampUp.repositories.AddressRepository;
import com.julio.rampUp.repositories.CustomerRepository;
import com.julio.rampUp.repositories.OrderItemRepository;
import com.julio.rampUp.repositories.OrderRepository;
import com.julio.rampUp.repositories.ProductOfferingRepository;
import com.julio.rampUp.repositories.RoleRepository;
import com.julio.rampUp.repositories.TicketRepository;
import com.julio.rampUp.repositories.UserRepository;

@Configuration
@Profile("test")
public class TestConfig implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductOfferingRepository productOfferingRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public void run(String... args) throws Exception {

        ProductOffering poTest1 = new ProductOffering(null, "namePO1", 50000.0, true, POState.Active);
        ProductOffering poTest2 = new ProductOffering(null, "namePO2", 150000.0, true, POState.Active);
        ProductOffering poTest3 = new ProductOffering(null, "namePO3", 20000.0, true, POState.Technical);
        ProductOffering poTest4 = new ProductOffering(null, "namePO4", 30000.0, false, POState.Active);
        ProductOffering poTest5 = new ProductOffering(null, "namePO5", 32220.0, true, POState.Definition);
        ProductOffering poTest6 = new ProductOffering(null, "namePO6", 300.0, true, POState.Active);
        ProductOffering poTest7 = new ProductOffering(null, "namePO7", 33000.0, true, POState.Definition);
        ProductOffering poTest8 = new ProductOffering(null, "namePO8", 32000.0, true, POState.Active);
        ProductOffering poTest9 = new ProductOffering(null, "namePO9", 34000.0, true, POState.Definition);
        productOfferingRepository.saveAll(
                Arrays.asList(poTest1, poTest2, poTest3, poTest4, poTest5, poTest6, poTest7, poTest8, poTest9));

        Role roleOperator = new Role(null, Authorities.Operator);
        Role roleAdmin = new Role(null, Authorities.Admin);
        roleRepository.saveAll(Arrays.asList(roleOperator, roleAdmin));

        User userTest1 = new User(null, "maria@gmail.com",
                "$2a$10$fN9VsZ8Uo9LpiDNcKq8SAuW7vpGafpMxlrS1/uto8pc5a8LlycPDS"); // 1234567
        User userTest2 = new User(null, "joao@gmail.com",
                "$2a$10$DbMAUZolPgDwqC0.HOKRhepWzbasKLJqpCf2srFhLhnKjF2IyLFjG"); // 12345
        User userTest3 = new User(null, "cassandra@gmail.com",
                "$2a$10$S.6Logj2d3.GJXSlRnFeTuvfEat9ImcHp0RxTIkxUcdKFpubV3kiu"); // xdfeeef
        User userTest4 = new User(null, "rodrigo@gmail.com",
                "$2a$10$pRXzNAHPQx/CDY6Bpse17.qRIL4rry064QtX2gW1NRhEGKmHg3OHG"); // 123rod123
        User userTest5 = new User(null, "haaland@gmail.com",
                "$2a$10$zPMVimQpVsd0UEZsV5iLTutyhVu6mI5kqQGD8aW6TGBj9Ai9aH.h2"); // 9city9
        User userTest6 = new User(null, "teste123@gmail.com",
                "$2a$10$zPMVimQpVsd0UEZsV5iLTutyhVu6mI5kqQGD8aW6TGBj9Ai9aH.h2"); // 9city9

        userTest1.addRole(roleOperator);
        userTest2.addRole(roleOperator);
        userTest3.addRole(roleOperator);
        userTest4.addRole(roleOperator);
        userTest5.addRole(roleAdmin);
        userTest6.addRole(roleOperator);

        userRepository.saveAll(Arrays.asList(userTest1, userTest2, userTest3, userTest4, userTest5, userTest6));

        Customer customerTest1 = new Customer(null, "Maria", 123456789, CustomerType.NaturalPerson, "High",
                userTest1.getPassword());
        Customer customerTest2 = new Customer(null, "João", 123454321, CustomerType.NaturalPerson, "High",
                userTest2.getPassword());
        Customer customerTest3 = new Customer(null, "Cassandra", 101010101, CustomerType.LegalPerson, "High",
                userTest3.getPassword());
        Customer customerTest4 = new Customer(null, "Rodrigo", 123123123, CustomerType.Technical, "High",
                userTest4.getPassword());
        Customer customerTest5 = new Customer(null, "Haalandinho", 123123123, CustomerType.Technical, "High",
                userTest4.getPassword());
        Customer customerTest6 = new Customer(null, "Julio Tinti", 123123123, CustomerType.Technical, "High",
                userTest4.getPassword());
        customerRepository.saveAll(Arrays.asList(customerTest1, customerTest2, customerTest3, customerTest4,
                customerTest5, customerTest6));

        userTest1.setCustomer(customerTest1);
        userTest2.setCustomer(customerTest2);
        userTest3.setCustomer(customerTest3);
        userTest4.setCustomer(customerTest4);
        userTest5.setCustomer(customerTest5);
        userTest6.setCustomer(customerTest6);
        userRepository.saveAll(Arrays.asList(userTest1, userTest2, userTest3, userTest4, userTest5, userTest6));

        customerTest1.setUser(userTest1);
        customerTest2.setUser(userTest2);
        customerTest3.setUser(userTest3);
        customerTest4.setUser(userTest4);
        customerTest5.setUser(userTest5);
        customerTest6.setUser(userTest6);
        customerRepository.saveAll(Arrays.asList(customerTest1, customerTest2, customerTest3, customerTest4,
                customerTest5, customerTest6));

        Address homeAddressCustomer1 = new Address(null, "Sete de Setembro Avenue", 554, "Center", 80230000, "Brazil",
                AddressType.HomeAddress);
        Address shippingAddressCustomer1 = new Address(null, "Professora Dona Lili Street", 125, "Center", 37130000,
                "Brazil", AddressType.ShippingAddress);
        shippingAddressCustomer1.setDeleted(true);
        Address homeAddressCustomer2 = new Address(null, "Iguaçu Avenue", 1111, "Rebouças", 82220000, "Brazil",
                AddressType.HomeAddress);
        Address homeAddressCustomer3 = new Address(null, "Silva Jardim Avenue", 125, "Center", 80204000, "Brazil",
                AddressType.ShippingAddress);
        Address homeAddressCustomer4 = new Address(null, "Bacacheri Street", 1233, "Center", 12220000, "Brazil",
                AddressType.HomeAddress);
        addressRepository.saveAll(Arrays.asList(homeAddressCustomer1, shippingAddressCustomer1, homeAddressCustomer2,
                homeAddressCustomer3, homeAddressCustomer4));

        homeAddressCustomer1.setCustomer(customerTest1);
        shippingAddressCustomer1.setCustomer(customerTest1);
        homeAddressCustomer2.setCustomer(customerTest2);
        homeAddressCustomer3.setCustomer(customerTest3);
        homeAddressCustomer4.setCustomer(customerTest4);
        addressRepository.saveAll(Arrays.asList(homeAddressCustomer1, shippingAddressCustomer1, homeAddressCustomer2,
                homeAddressCustomer3, homeAddressCustomer4));

        customerTest1.addAddress(homeAddressCustomer1);
        customerTest1.addAddress(shippingAddressCustomer1);
        customerTest2.addAddress(homeAddressCustomer2);
        customerTest3.addAddress(homeAddressCustomer3);
        customerTest4.addAddress(homeAddressCustomer4);
        customerRepository.saveAll(Arrays.asList(customerTest1, customerTest2, customerTest3, customerTest4));

        Order orderCustomer1 = new Order(null, Instant.parse("2022-06-20T19:53:07Z"), customerTest1,
                customerTest1.getAddresses().get(0));
        Order orderCustomer2 = new Order(null, Instant.parse("2023-06-20T19:53:07Z"), customerTest2,
                customerTest2.getAddresses().get(0));
        Order order2Customer2 = new Order(null, Instant.parse("2023-07-03T21:52:22Z"), customerTest2,
                customerTest2.getAddresses().get(0));
        Order order3Customer2 = new Order(null, Instant.now(), customerTest2, customerTest2.getAddresses().get(0));
        Order orderCustomer3 = new Order(null, Instant.now(), customerTest3, customerTest3.getAddresses().get(0));
        Order orderCustomer4 = new Order(null, Instant.now(), customerTest4, customerTest4.getAddresses().get(0));
        orderRepository.saveAll(Arrays.asList(orderCustomer1, orderCustomer2, order2Customer2, order3Customer2,
                orderCustomer3, orderCustomer4));

        customerTest1.addOrder(orderCustomer1);
        customerTest2.addOrder(orderCustomer2);
        customerTest2.addOrder(order2Customer2);
        customerTest2.addOrder(order3Customer2);
        customerTest3.addOrder(orderCustomer3);
        customerTest4.addOrder(orderCustomer4);
        customerRepository.saveAll(Arrays.asList(customerTest1, customerTest2, customerTest3, customerTest4));

        OrderItem orderItemCustomer1 = new OrderItem(orderCustomer1, poTest5, 0.1, 5);
        OrderItem orderItemCustomer2 = new OrderItem(orderCustomer2, poTest1, 0.15, 10);
        OrderItem orderItem2Customer2 = new OrderItem(orderCustomer2, poTest2, 0.15, 10);
        OrderItem orderItem3Customer2 = new OrderItem(orderCustomer2, poTest3, 0.15, 10);
        OrderItem orderItemCustomer3 = new OrderItem(orderCustomer3, poTest3, 0.08, 1);
        OrderItem orderItemCustomer4 = new OrderItem(orderCustomer4, poTest2, 0.12, 2);
        orderItemRepository.saveAll(Arrays.asList(orderItemCustomer1, orderItemCustomer2, orderItem2Customer2,
                orderItem3Customer2, orderItemCustomer3, orderItemCustomer4));

        Ticket ticketTest = new Ticket(null, "I want to cancel this order cause...", orderCustomer1);
        Ticket ticketTest2 = new Ticket(null, "I want to cancel this order cause...", orderCustomer2);
        Ticket ticketTest1 = new Ticket(null, "I want to cancel this order cause...", orderCustomer3);
        ticketTest1.setSolved(true);
        ticketRepository.saveAll(Arrays.asList(ticketTest, ticketTest1, ticketTest2));

    }

};