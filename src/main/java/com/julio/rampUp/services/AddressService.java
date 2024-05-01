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
import com.julio.rampUp.entities.dto.AddressDTO;
import com.julio.rampUp.repositories.AddressRepository;
import com.julio.rampUp.repositories.CustomerRepository;
import com.julio.rampUp.services.exceptions.AddressException;
import com.julio.rampUp.services.exceptions.IdNullException;
import com.julio.rampUp.services.exceptions.NoValueForIdException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@Service
public class AddressService {

    @Autowired
    private AddressRepository repository;

    @Autowired
    private CustomerRepository customerRepository;

    public List<AddressDTO> findAll(int page) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 10, sort);
        return addressToDTO(repository.findAllByDeleted(pageable, false).toList());
    }

    private List<AddressDTO> addressToDTO(List<Address> addresses) {
        List<AddressDTO> addressesDTO = new ArrayList<>();
        for (Address address : addresses) {
            addressesDTO.add(new AddressDTO(address));
        }
        return addressesDTO;
    }

    public Address findById(Integer id) {
        Optional<Address> obj = repository.findById(id);
        if (obj.get().getDeleted() == true)
            throw new ResourceNotFoundException(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public List<Address> findAllAddressesByCustomerId(int page, Integer id) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 10, sort);
        return repository.findAllAddressesByCustomerId(pageable, id).toList();
    }

    public Address insert(Integer customerId, AddressDTO addressDTO) {
        try {
            Optional<Customer> customerDB = customerRepository.findById(addressDTO.getCustomerId());
            if (addressDTO.getCustomerId() != customerId)
                throw new AddressException();
            Customer customer = customerDB.get();

            Address address = builder(addressDTO);

            address.setCustomer(customer);
            repository.save(address);
            customer.addAddress(address);
            customerRepository.save(customer);

            return repository.save(address);
        } catch (AddressException e) {
            throw new AddressException("CustomerId of address is not the id of the customer");
        } catch (NoSuchElementException e) {
            throw new NoValueForIdException("Customer", addressDTO.getCustomerId());
        } catch (InvalidDataAccessApiUsageException e) {
            throw new IdNullException("Customer");
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public void deleteById(Integer customerId, Integer id) {
        try {
            Optional<Customer> customerDB = customerRepository.findById(customerId);
            if (customerDB.get().getAddresses().stream().anyMatch(el -> el.getId() == id))
                repository.deleteById(id);
            else
                throw new AddressException();
        } catch (AddressException e) {
            throw new AddressException();
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public Address update(Integer customerId, Integer id, Address newAddress) {
        try {
            Address updatedAddress = repository.getReferenceById(id);
            if (updatedAddress.getCustomer().getId() != customerId)
                throw new AddressException();
            updateData(updatedAddress, newAddress);
            return repository.save(updatedAddress);
        } catch (AddressException e) {
            throw new AddressException();
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    private void updateData(Address updatedAddress, Address newAddress) {
        updatedAddress.setStreet(newAddress.getStreet());
        updatedAddress.setHouseNumber(newAddress.getHouseNumber());
        updatedAddress.setNeighborhood(newAddress.getNeighborhood());
        updatedAddress.setZipCode(newAddress.getZipCode());
        updatedAddress.setCountry(newAddress.getCountry());
        updatedAddress.setAddressType(newAddress.getAddressType());
    }

    private Address builder(AddressDTO addressDTO) {
        Address address = new Address(addressDTO.getId(), addressDTO.getStreet(), addressDTO.getHouseNumber(),
                addressDTO.getNeighborhood(), addressDTO.getZipCode(), addressDTO.getCountry(),
                addressDTO.getAddressType());
        return address;
    }

}
