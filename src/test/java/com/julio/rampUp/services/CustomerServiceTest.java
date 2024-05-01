package com.julio.rampUp.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.User;
import com.julio.rampUp.entities.dto.CustomerDTO;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.repositories.CustomerRepository;
import com.julio.rampUp.repositories.UserRepository;
import com.julio.rampUp.services.exceptions.CustomerAlreadyExists;
import com.julio.rampUp.services.exceptions.IdNullException;
import com.julio.rampUp.services.exceptions.NoValueForIdException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerServiceTest {

    private static final String THERE_IS_NO_USER_USING_THIS_ID = "There is no User using this id (";
    private static final String RESOURCE_NOT_FOUND_ID = "Resource not found. Id - ";
    private static final int testId = 1;
    private static final int userId = 1;

    @Mock
    private UserRepository userRepo;

    @Mock
    private CustomerRepository customerRepo;

    @InjectMocks
    private CustomerService serviceUnderTest;

    private User userTest1;
    private Customer customerTest1;
    private Customer customerTest2;
    private Optional<Customer> knownCustomer;
    private CustomerDTO customerDTO;

    @BeforeEach
    public void setup() {
        userTest1 = new User(null, "maria@gmail.com", "1234567");
        customerTest1 = new Customer(1, "João", 123454321, CustomerType.NaturalPerson, "High", "1123");
        customerTest1.setUser(userTest1);
        userTest1.setCustomer(customerTest1);

        customerTest2 = new Customer(2, "Maria", 123456789, CustomerType.NaturalPerson, "High",
                userTest1.getPassword());
        knownCustomer = Optional.of(customerTest1);

        customerDTO = new CustomerDTO(customerTest1);
        customerDTO.setUserId(userId);
    }

    @Test
    @DisplayName("Get all Customers")
    @Order(0)
    public void findAll_shouldReturnAllCustomers() {
        // set up
        Page<Customer> page = new PageImpl<>(List.of(customerTest1, customerTest2));
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 10, sort);
        when(customerRepo.findAll(pageable)).thenReturn(page);

        // execute
        List<CustomerDTO> allCustomers = serviceUnderTest.findAll(0);

        // assert
        assertThat(allCustomers).isNotNull();
        assertThat(allCustomers.size()).isEqualTo(2);
        assertThat(allCustomers.get(0).getId()).isEqualTo(customerTest1.getId());
        assertThat(allCustomers.get(0).getCustomerName()).isEqualTo(customerTest1.getCustomerName());
        assertThat(allCustomers.get(0).getDocumentNumber()).isEqualTo(customerTest1.getDocumentNumber());
        assertThat(allCustomers.get(0).getCustomerStatus()).isEqualTo(customerTest1.getCustomerStatus());
        assertThat(allCustomers.get(0).getCustomerType()).isEqualTo(customerTest1.getCustomerType());
        assertThat(allCustomers.get(0).getCreditScore()).isEqualTo(customerTest1.getCreditScore());
        assertThat(allCustomers.get(0).getPassword()).isEqualTo(customerTest1.getPassword());
        assertThat(allCustomers.get(0).getUserId()).isEqualTo(customerTest1.getUser().getId());

        // verify
        verify(customerRepo).findAll(pageable);

    }

    @Test
    @DisplayName("Get customer by Id")
    @Order(1)
    public void findById_shouldReturnCustomerById() {
        // set up

        when(customerRepo.findById(anyInt())).thenReturn(knownCustomer);

        // execute
        Customer customerDatabase = serviceUnderTest.findById(testId);

        // assert
        assertThat(customerDatabase).isNotNull();
        assertThat(customerDatabase.getId()).isEqualTo(knownCustomer.get().getId());
        assertThat(customerDatabase.getCustomerName()).isEqualTo(knownCustomer.get().getCustomerName());
        assertThat(customerDatabase.getDocumentNumber()).isEqualTo(knownCustomer.get().getDocumentNumber());
        assertThat(customerDatabase.getCustomerStatus()).isEqualTo(knownCustomer.get().getCustomerStatus());
        assertThat(customerDatabase.getCustomerType()).isEqualTo(knownCustomer.get().getCustomerType());
        assertThat(customerDatabase.getCreditScore()).isEqualTo(knownCustomer.get().getCreditScore());
        assertThat(customerDatabase.getPassword()).isEqualTo(knownCustomer.get().getPassword());
        assertThat(customerDatabase.getUser().getId()).isEqualTo(knownCustomer.get().getUser().getId());

        // verify
        verify(customerRepo).findById(anyInt());
    }

    @Test
    @DisplayName("(Exception) Get customer by Id - Id not found")
    @Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() {
        //set up
        when(customerRepo.findById(anyInt())).thenThrow(new ResourceNotFoundException(testId));
        //execute and assert
        assertThatThrownBy(() -> serviceUnderTest.findById(anyInt())).isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("Insert a new customer")
    @Order(3)
    public void insert_shouldCreateNewCustomer() {
        // set up
        User testUser = userTest1;
        testUser.setCustomer(null);
        when(userRepo.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(customerRepo.save(any())).thenReturn(customerTest1);
        when(userRepo.save(any())).thenReturn(userTest1);

        // execute
        Customer newCustomer = serviceUnderTest.insert(customerDTO);

        // assert
        assertThat(newCustomer).isNotNull();
        assertThat(newCustomer.getId()).isEqualTo(customerTest1.getId());
        assertThat(newCustomer.getCustomerName()).isEqualTo(customerTest1.getCustomerName());
        assertThat(newCustomer.getDocumentNumber()).isEqualTo(customerTest1.getDocumentNumber());
        assertThat(newCustomer.getCustomerStatus()).isEqualTo(customerTest1.getCustomerStatus());
        assertThat(newCustomer.getCustomerType()).isEqualTo(customerTest1.getCustomerType());
        assertThat(newCustomer.getCreditScore()).isEqualTo(customerTest1.getCreditScore());
        assertThat(newCustomer.getPassword()).isEqualTo(customerTest1.getPassword());
        assertThat(newCustomer.getUser().getId()).isEqualTo(customerTest1.getUser().getId());

        // verify
        verify(userRepo, times(1)).save(any());
        verify(customerRepo, times(2)).save(any());
    }

    @Test
    @DisplayName("Insert a new customer when User previouly deleted it")
    @Order(3)
    public void insert_whenCustomerWasDelete_shouldCreateNewCustomer() {
        // set up
        userTest1.getCustomer().setDeleted(true);
        when(userRepo.findById(anyInt())).thenReturn(Optional.of(userTest1));
        when(customerRepo.save(any())).thenReturn(customerTest1);
        when(userRepo.save(any())).thenReturn(userTest1);
        when(customerRepo.getReferenceById(anyInt())).thenReturn(customerTest1);

        // execute
        Customer newCustomer = serviceUnderTest.insert(customerDTO);

        // assert
        assertThat(newCustomer).isNotNull();
        assertThat(newCustomer.getId()).isEqualTo(customerTest1.getId());
        assertThat(newCustomer.getCustomerName()).isEqualTo(customerTest1.getCustomerName());
        assertThat(newCustomer.getDocumentNumber()).isEqualTo(customerTest1.getDocumentNumber());
        assertThat(newCustomer.getCustomerStatus()).isEqualTo(customerTest1.getCustomerStatus());
        assertThat(newCustomer.getCustomerType()).isEqualTo(customerTest1.getCustomerType());
        assertThat(newCustomer.getCreditScore()).isEqualTo(customerTest1.getCreditScore());
        assertThat(newCustomer.getPassword()).isEqualTo(customerTest1.getPassword());
        assertThat(newCustomer.getUser().getId()).isEqualTo(customerTest1.getUser().getId());

        // verify
        verify(userRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("(Exception) Insert a new customer - customer already exixst")
    @Order(4)
    public void insert_whenCustomerExists_shouldThrowCustomerAlreadyExists() {
        // set up
        when(userRepo.findById(anyInt())).thenReturn(Optional.of(userTest1));

        assertThatThrownBy(() -> serviceUnderTest.insert(customerDTO)).isInstanceOf(CustomerAlreadyExists.class)
        .hasMessageContaining("This user already have a customer");
    }

    @Test
    @DisplayName("(Exception) Insert a new customer - User Id not Found")
    @Order(5)
    public void insert_whenUserIdNotFound_thenThrowNoSuchElementException() {
        // set up
        when(userRepo.findById(anyInt())).thenThrow(NoSuchElementException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(customerDTO)).isInstanceOf(NoValueForIdException.class)
        .hasMessageContaining(THERE_IS_NO_USER_USING_THIS_ID + userId + ")");

    }

    @Test
    @DisplayName("(Exception) Insert a new customer - User Id null")
    @Order(5)
    public void insert_whenUserIdIsNull_thenThrowInvalidDataAccessApiUsageException() {
        // set up
        when(userRepo.findById(anyInt())).thenThrow(InvalidDataAccessApiUsageException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(customerDTO)).isInstanceOf(IdNullException.class)
                .hasMessageContaining("Id of User must not be null.");
    }

    @Test
    @DisplayName("(Exception) Insert a new customer - Unexpected Exception")
    @Order(6)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(userRepo.findById(anyInt())).thenThrow(RuntimeException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(customerDTO)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Delete customer by Id")
    @Order(7)
    public void deleteById_shouldDeleteCustomerById() {
        // set up
        doNothing().when(customerRepo).deleteById(anyInt());

        // execute
        serviceUnderTest.deleteById(testId);

        // verify
        verify(customerRepo, times(1)).deleteById(testId);
    }

    @Test
    @DisplayName("(Exception) Delete customer by Id - Id not found")
    @Order(8)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        doThrow(EmptyResultDataAccessException.class).when(customerRepo).deleteById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Delete customer by Id - Unexpected Exception")
    @Order(9)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        doThrow(RuntimeException.class).when(customerRepo).deleteById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Update customer")
    @Order(10)
    public void update_shouldUpdateCustomer() {
        // set up
        Address address1 = new Address(1, "Sete de Setembro Avenue", 554, "Center", 80230000, "Brazil",
                AddressType.HomeAddress);
        customerTest1.addAddress(address1);
        com.julio.rampUp.entities.Order orderCustomer1 = new com.julio.rampUp.entities.Order(1, Instant.now(),
                customerTest1, customerTest1.getAddresses().get(0));
        customerTest1.addOrder(orderCustomer1);
        when(customerRepo.getReferenceById(anyInt())).thenReturn(customerTest2);
        when(customerRepo.save(any())).thenReturn(customerTest1);

        // execute
        Customer updatedCustomer = serviceUnderTest.update(2, customerTest1);

        // assert
        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getId()).isEqualTo(customerTest1.getId());
        assertThat(updatedCustomer.getCustomerName()).isEqualTo(customerTest1.getCustomerName());
        assertThat(updatedCustomer.getDocumentNumber()).isEqualTo(customerTest1.getDocumentNumber());
        assertThat(updatedCustomer.getCustomerStatus()).isEqualTo(customerTest1.getCustomerStatus());
        assertThat(updatedCustomer.getCustomerType()).isEqualTo(customerTest1.getCustomerType());
        assertThat(updatedCustomer.getCreditScore()).isEqualTo(customerTest1.getCreditScore());
        assertThat(updatedCustomer.getPassword()).isEqualTo(customerTest1.getPassword());
        assertThat(updatedCustomer.getUser().getId()).isEqualTo(customerTest1.getUser().getId());
    }

    @Test
    @DisplayName("(Exception) Update customer - Id not found")
    @Order(11)
    public void update_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        when(customerRepo.getReferenceById(anyInt())).thenThrow(EntityNotFoundException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, customerTest2)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Update customer - Unexpected Exception")
    @Order(12)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(customerRepo.getReferenceById(anyInt())).thenThrow(RuntimeException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, customerTest2)).isInstanceOf(UnexpectedException.class);
    }

}
