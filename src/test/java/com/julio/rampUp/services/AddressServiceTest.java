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
import com.julio.rampUp.entities.dto.AddressDTO;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.repositories.AddressRepository;
import com.julio.rampUp.repositories.CustomerRepository;
import com.julio.rampUp.services.exceptions.AddressException;
import com.julio.rampUp.services.exceptions.IdNullException;
import com.julio.rampUp.services.exceptions.NoValueForIdException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AddressServiceTest {

    private static final String THIS_ADDRESS_DON_T_EXIST_FOR_THIS_CUSTOMER = "This address don't exist for this customer";
    private static final String THERE_IS_NO_CUSTOMER_USING_THIS_ID = "There is no Customer using this id (";
    private static final String RESOURCE_NOT_FOUND_ID = "Resource not found. Id - ";
    private static final int testId = 1;
    private static final int customerId = 1;
    private static final int userId = 1;

    @Mock
    private AddressRepository addressRepo;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AddressService serviceUnderTest;

    private Address addressTest1;
    private Address addressTest2;
    private Optional<Customer> knownCustomer;
    private AddressDTO addressDTO;
    private User userTest;

    @BeforeEach
    public void setup() {
        addressTest1 = new Address(1, "Sete de Setembro Avenue", 554, "Center", 80230000, "Brazil",
                AddressType.HomeAddress);
        addressTest2 = new Address(2, "Professora Dona Lili Street", 125, "Center", 37130000, "Brazil",
                AddressType.ShippingAddress);
        userTest = new User(userId, "maria@gmail.com", "1234567");

        knownCustomer = Optional
                .of(new Customer(customerId, "Maria", 123456789, CustomerType.NaturalPerson, "High", "123"));
        knownCustomer.get().addAddress(addressTest1);
        knownCustomer.get().setUser(userTest);

        addressDTO = new AddressDTO(addressTest1);
        addressDTO.setCustomerId(customerId);

        addressTest1.setCustomer(knownCustomer.get());
    }

    @Test
    @DisplayName("Get all Addresses")
    @Order(0)
    public void findAll_shouldReturnAllAddresses() {
        // set up
        Page<Address> page = new PageImpl<>(List.of(addressTest1, addressTest2));
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 10, sort);
        when(addressRepo.findAllByDeleted(pageable, false)).thenReturn(page);

        // execute
        List<AddressDTO> allAddresses = serviceUnderTest.findAll(0);

        // assert
        assertThat(allAddresses).isNotNull();
        assertThat(allAddresses.size()).isEqualTo(2);
        assertThat(allAddresses.get(0).getId()).isEqualTo(addressTest1.getId());
        assertThat(allAddresses.get(0).getStreet()).isEqualTo(addressTest1.getStreet());
        assertThat(allAddresses.get(0).getHouseNumber()).isEqualTo(addressTest1.getHouseNumber());
        assertThat(allAddresses.get(0).getNeighborhood()).isEqualTo(addressTest1.getNeighborhood());
        assertThat(allAddresses.get(0).getZipCode()).isEqualTo(addressTest1.getZipCode());
        assertThat(allAddresses.get(0).getCountry()).isEqualTo(addressTest1.getCountry());
        assertThat(allAddresses.get(0).getAddressType()).isEqualTo(addressTest1.getAddressType());

        // verify
        verify(addressRepo).findAllByDeleted(pageable, false);
    }

    @Test
    @DisplayName("Get address by Id")
    @Order(1)
    public void findById_shouldReturnAddressById() {
        // set up
        Optional<Address> knownAddress = Optional.of(addressTest1);
        when(addressRepo.findById(anyInt())).thenReturn(knownAddress);

        // execute
        Address databaseAddress = serviceUnderTest.findById(testId);

        // assert
        assertThat(databaseAddress).isNotNull();
        assertThat(databaseAddress.getId()).isEqualTo(knownAddress.get().getId());
        assertThat(databaseAddress.getStreet()).isEqualTo(knownAddress.get().getStreet());
        assertThat(databaseAddress.getHouseNumber()).isEqualTo(knownAddress.get().getHouseNumber());
        assertThat(databaseAddress.getNeighborhood()).isEqualTo(knownAddress.get().getNeighborhood());
        assertThat(databaseAddress.getZipCode()).isEqualTo(knownAddress.get().getZipCode());
        assertThat(databaseAddress.getCountry()).isEqualTo(knownAddress.get().getCountry());
        assertThat(databaseAddress.getAddressType()).isEqualTo(knownAddress.get().getAddressType());

        // verify
        verify(addressRepo).findById(testId);

    }

    @Test
    @DisplayName("(Exception) Get address by Id - Id not found")
    @Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() {
        // set up
        Optional<Address> knownAddress = Optional.of(addressTest1);
        knownAddress.get().setDeleted(true);
        when(addressRepo.findById(anyInt())).thenReturn(knownAddress);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.findById(testId)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Get address by Id - Address deleted")
    @Order(3)
    public void findById_whenAddressWasDeleted_ShouldThrowResourceNotFoundException() {
        //set up
        when(addressRepo.findById(anyInt())).thenThrow(new ResourceNotFoundException(testId));
        //execute and assert
        assertThatThrownBy(() -> serviceUnderTest.findById(anyInt())).isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("Insert a new address")
    @Order(4)
    public void insert_shouldCreateNewAddress() {
        // set up
        when(customerRepository.findById(anyInt())).thenReturn(knownCustomer);
        when(addressRepo.save(any())).thenReturn(addressTest1);
        when(customerRepository.save(any())).thenReturn(knownCustomer.get());

        // execute
        Address newAddress = serviceUnderTest.insert(customerId, addressDTO);

        // assert
        assertThat(newAddress).isNotNull();
        assertThat(newAddress.getId()).isEqualTo(addressTest1.getId());
        assertThat(newAddress.getStreet()).isEqualTo(addressTest1.getStreet());
        assertThat(newAddress.getHouseNumber()).isEqualTo(addressTest1.getHouseNumber());
        assertThat(newAddress.getNeighborhood()).isEqualTo(addressTest1.getNeighborhood());
        assertThat(newAddress.getZipCode()).isEqualTo(addressTest1.getZipCode());
        assertThat(newAddress.getCountry()).isEqualTo(addressTest1.getCountry());
        assertThat(newAddress.getAddressType()).isEqualTo(addressTest1.getAddressType());

        assertThat(newAddress.getCustomer().getId()).isEqualTo(knownCustomer.get().getId());
        assertThat(knownCustomer.get().getAddresses().stream().anyMatch(el -> el.getId() == newAddress.getId())).isTrue();

        //verify
        verify(customerRepository,times(1)).save(any());
        verify(addressRepo,times(2)).save(any());
    }

    @Test
    @DisplayName("(Exception) Insert a new address - CustomerId is not correct")
    @Order(5)
    public void insert_whenAddressIsNotFromTheCustomer_thenThrowAddressException() {
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(0, addressDTO)).isInstanceOf(AddressException.class)
                .hasMessageContaining("CustomerId of address is not the id of the customer");
    }

    @Test
    @DisplayName("(Exception) Insert a new address - Customer Id not Found")
    @Order(6)
    public void insert_whenCustomerIdNotFound_thenThrowNoSuchElementException() {
        // set up
        when(customerRepository.findById(anyInt())).thenThrow(NoSuchElementException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(customerId, addressDTO)).isInstanceOf(NoValueForIdException.class)
        .hasMessageContaining(THERE_IS_NO_CUSTOMER_USING_THIS_ID + customerId + ")");
    }

    @Test
    @DisplayName("(Exception) Insert a new address - Customer Id null")
    @Order(7)
    public void insert_whenCustomerIdIsNull_thenThrowInvalidDataAccessApiUsageException() {
        // set up
        when(customerRepository.findById(anyInt())).thenThrow(InvalidDataAccessApiUsageException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(null, addressDTO)).isInstanceOf(IdNullException.class)
                .hasMessageContaining("Id of Customer must not be null.");
    }

    @Test
    @DisplayName("(Exception) Insert a new address - Unexpected Exception")
    @Order(8)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(customerRepository.findById(anyInt())).thenReturn(knownCustomer);
        when(addressRepo.save(any())).thenThrow(RuntimeException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(customerId, addressDTO)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Delete address by Id")
    @Order(9)
    public void deleteById_shouldDeleteAddressById() {
        // set up
        doNothing().when(addressRepo).deleteById(anyInt());
        when(customerRepository.findById(anyInt())).thenReturn(knownCustomer);

        // execute
        serviceUnderTest.deleteById(customerId, testId);

        // verify
        verify(addressRepo, times(1)).deleteById(testId);
    }

    @Test
    @DisplayName("(Exception) Delete address by Id - Customer dont have this address")
    @Order(10)
    public void deleteById_whenAddressIsNotFromTheCustomer_shouldThrowAddressException() {
        // set up
        when(customerRepository.findById(anyInt())).thenReturn(knownCustomer);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(customerId, 5)).isInstanceOf(AddressException.class)
                .hasMessageContaining(THIS_ADDRESS_DON_T_EXIST_FOR_THIS_CUSTOMER);
    }

    @Test
    @DisplayName("(Exception) Delete address by Id - Id not found")
    @Order(11)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        when(customerRepository.findById(anyInt())).thenReturn(knownCustomer);
        doThrow(EmptyResultDataAccessException.class).when(addressRepo).deleteById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(2, testId))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Delete address by Id - Unexpected Exception")
    @Order(12)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(customerRepository.findById(anyInt())).thenThrow(RuntimeException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(customerId, testId)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Update Address")
    @Order(13)
    public void update_shouldUpdateAddress() {
        // set up
        when(addressRepo.getReferenceById(anyInt())).thenReturn(addressTest1);
        when(addressRepo.save(any())).thenReturn(addressTest2);

        // execute
        Address updatedAddress = serviceUnderTest.update(customerId, testId, addressTest2);

        // assert
        assertThat(updatedAddress).isNotNull();
        assertThat(updatedAddress.getId()).isEqualTo(addressTest2.getId());
        assertThat(updatedAddress.getStreet()).isEqualTo(addressTest2.getStreet());
        assertThat(updatedAddress.getHouseNumber()).isEqualTo(addressTest2.getHouseNumber());
        assertThat(updatedAddress.getNeighborhood()).isEqualTo(addressTest2.getNeighborhood());
        assertThat(updatedAddress.getZipCode()).isEqualTo(addressTest2.getZipCode());
        assertThat(updatedAddress.getCountry()).isEqualTo(addressTest2.getCountry());
        assertThat(updatedAddress.getAddressType()).isEqualTo(addressTest2.getAddressType());
    }

    @Test
    @DisplayName("(Exception) Update Address - Customer dont have this address")
    @Order(14)
    public void update_whenAddressIsNotFromTheCustomer_shouldThrowAddressException() {
        //set up
        when(addressRepo.getReferenceById(anyInt())).thenReturn(addressTest1);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(2, testId, addressTest2)).isInstanceOf(AddressException.class)
                .hasMessageContaining(THIS_ADDRESS_DON_T_EXIST_FOR_THIS_CUSTOMER);
    }

    @Test
    @DisplayName("(Exception) Update Address - Id not found")
    @Order(15)
    public void update_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        when(addressRepo.getReferenceById(anyInt())).thenThrow(EntityNotFoundException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(2, testId, addressTest2)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Update Address - Unexpected Exception")
    @Order(16)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(addressRepo.getReferenceById(anyInt())).thenThrow(RuntimeException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(customerId, testId, addressTest2)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Get all addresses by user id")
    @Order(17)
    public void findAllAddressesByUserId_thenReturnAddressesByUserId() {
        // set up
        Page<Address> page = new PageImpl<>(List.of(addressTest1));
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 10, sort);
        int userId = 1;
        when(addressRepo.findAllAddressesByCustomerId(pageable, userId)).thenReturn(page);

        // execute
        List<Address> allUserAdressess = serviceUnderTest.findAllAddressesByCustomerId(0, userId);

        // assert
        assertThat(allUserAdressess).isNotNull();
        assertThat(allUserAdressess.size()).isEqualTo(1);

        // verify
        verify(addressRepo, times(1)).findAllAddressesByCustomerId(any(), anyInt());
    }

}
