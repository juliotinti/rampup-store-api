package com.julio.rampUp.services;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.User;
import com.julio.rampUp.entities.dto.CustomerDTO;
import com.julio.rampUp.repositories.CustomerRepository;
import com.julio.rampUp.repositories.UserRepository;
import com.julio.rampUp.services.exceptions.CustomerAlreadyExists;
import com.julio.rampUp.services.exceptions.IdNullException;
import com.julio.rampUp.services.exceptions.NoValueForIdException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private UserRepository userRepository;

    public List<CustomerDTO> findAll(int page) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 10, sort);
        return customerToDTO(repository.findAll(pageable).toList());
    }

    private List<CustomerDTO> customerToDTO(List<Customer> customers) {
        List<CustomerDTO> customersDTO = new ArrayList<>();
        for (Customer customer : customers) {
            customersDTO.add(new CustomerDTO(customer));
        }
        return customersDTO;
    }

    public Customer findById(Integer id) {
        Optional<Customer> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public Customer insert(CustomerDTO customerDTO) {
        try {
            Optional<User> userDB = userRepository.findById(customerDTO.getUserId());
            User user = userDB.get();
            Customer customer = builder(customerDTO);
            if (user.getCustomer() != null) { // customer already exists
                if (user.getCustomer().getDeleted() == false)
                    throw new CustomerAlreadyExists();
                else { // but was deleted
                    customer.setDeleted(false);
                    customer = update(user.getCustomer().getId(), customer);
                }
            }

            user.setCustomer(customer);
            customer.setUser(user);
            repository.save(customer);
            userRepository.save(user);

            return repository.save(customer);
        } catch (NoSuchElementException e) {
            throw new NoValueForIdException("User", customerDTO.getUserId());
        } catch (InvalidDataAccessApiUsageException e) {
            throw new IdNullException("User");
        } catch (CustomerAlreadyExists e) {
            throw new CustomerAlreadyExists();
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }

    }

    public void deleteById(Integer id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public Customer update(Integer id, Customer newCustomer) {
        try {
            Customer updatedCustomer = repository.getReferenceById(id);
            updateData(updatedCustomer, newCustomer);
            return repository.save(updatedCustomer);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    private void updateData(Customer updatedCustomer, Customer newCustomer) {
        if (newCustomer.getCustomerName() != null)
            updatedCustomer.setCustomerName(newCustomer.getCustomerName());
        if (newCustomer.getDocumentNumber() != null)
            updatedCustomer.setDocumentNumber(newCustomer.getDocumentNumber());
        if (newCustomer.getCustomerStatus() != null)
            updatedCustomer.setCustomerStatus(newCustomer.getCustomerStatus());
        if (newCustomer.getCustomerType() != null)
            updatedCustomer.setCustomerType(newCustomer.getCustomerType());
        if (newCustomer.getCreditScore() != null)
            updatedCustomer.setCreditScore(newCustomer.getCreditScore());
        if (newCustomer.getPassword() != null)
            updatedCustomer.setPassword(newCustomer.getPassword());
        if (newCustomer.getUser() != null)
            updatedCustomer.setUser(newCustomer.getUser());
        for (Address customerAddress : newCustomer.getAddresses()) {
            updatedCustomer.addAddress(customerAddress);
        }
        for (Order customerOrder : newCustomer.getOrders()) {
            updatedCustomer.addOrder(customerOrder);
        }
    }

    private Customer builder(CustomerDTO customerDTO) {
        Customer customer = new Customer(customerDTO.getId(), customerDTO.getCustomerName(),
                customerDTO.getDocumentNumber(), customerDTO.getCustomerType(), customerDTO.getCreditScore(),
                customerDTO.getPassword());
        return customer;
    }

}
