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
import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.OrderItem;
import com.julio.rampUp.entities.ProductOffering;
import com.julio.rampUp.entities.dto.OrderDTO;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.entities.enums.POState;
import com.julio.rampUp.repositories.AddressRepository;
import com.julio.rampUp.repositories.CustomerRepository;
import com.julio.rampUp.repositories.OrderItemRepository;
import com.julio.rampUp.repositories.OrderRepository;
import com.julio.rampUp.repositories.ProductOfferingRepository;
import com.julio.rampUp.services.exceptions.IdNullException;
import com.julio.rampUp.services.exceptions.NoValueForIdException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderServiceTest {

    private static final String THERE_IS_NO_CUSTOMER_USING_THIS_ID = "There is no Customer using this id (";
    private static final String THERE_IS_NO_ADDRESS_USING_THIS_ID = "Or, there is no Address using this id (";
    private static final String RESOURCE_NOT_FOUND_ID = "Resource not found. Id - ";
    private static final int testId = 1;
    private static final int customerId = 1;
    private static final int addressId = 1;

    @Mock
    private AddressRepository addressRepo;

    @Mock
    private AddressService addressService;

    @Mock
    private CustomerRepository customerRepo;

    @Mock
    private ProductOfferingRepository productRepo;

    @Mock
    private OrderItemRepository orderItemRepo;

    @Mock
    private OrderRepository orderRepo;

    @InjectMocks
    private OrderService serviceUnderTest;

    private Customer customerTest;
    private Address address;
    private Order orderTest1;
    private Order orderTest2;
    private OrderDTO orderDTO;
    private ProductOffering poTest;
    private OrderItem orderItem;
    private Optional<Customer> opCustomer;
    private Optional<Address> opAddress;
    private Optional<ProductOffering> opProduct;

    @BeforeEach
    public void setup() {
        customerTest = new Customer(1, "João", 123454321, CustomerType.NaturalPerson, "High", "1123");
        address = new Address(1, "Sete de Setembro Avenue", 554, "Center", 80230000, "Brazil", AddressType.HomeAddress);
        customerTest.addAddress(address);
        address.setCustomer(customerTest);

        orderTest1 = new Order(1, Instant.parse("2022-06-20T19:53:07Z"), customerTest,
                customerTest.getAddresses().get(0));
        orderTest2 = new Order(2, Instant.now(), customerTest, customerTest.getAddresses().get(0));

        customerTest.addOrder(orderTest1);
        customerTest.addOrder(orderTest2);

        poTest = new ProductOffering(1, "namePO1", 50000.0, true, POState.Active);

        orderItem = new OrderItem(orderTest1, poTest, 0.1, 5);

        orderDTO = new OrderDTO(orderTest1);
//        orderDTO.setDiscount(0.15);
//        orderDTO.setQuantity(50);
        orderDTO.setCustomerId(customerTest.getId());
        orderDTO.setDeliveryId(address.getId());
//        orderDTO.setProductOfferingId(poTest.getId());

        opCustomer = Optional.of(customerTest);
        opAddress = Optional.of(address);
        opProduct = Optional.of(poTest);

    }

    @Test
    @DisplayName("Get all Orders")
    @org.junit.jupiter.api.Order(0)
    public void findAll_shouldReturnAllOrders() {
        // set up
        Page<Order> page = new PageImpl<>(List.of(orderTest1, orderTest2));
        when(orderRepo.findAll(any(Pageable.class))).thenReturn(page);

        // execute
        List<OrderDTO> allOrders = serviceUnderTest.findAll(0);

        // assert
        assertThat(allOrders).isNotNull();
        assertThat(allOrders.size()).isEqualTo(2);
        assertThat(allOrders.get(0).getId()).isEqualTo(orderTest1.getId());
        assertThat(allOrders.get(0).getCustomerId()).isEqualTo(orderTest1.getCustomer().getId());
        assertThat(allOrders.get(0).getDeliveryId()).isEqualTo(orderTest1.getDeliveryAddress().getId());
        assertThat(allOrders.get(1).getId()).isEqualTo(orderTest2.getId());
        assertThat(allOrders.get(1).getCustomerId()).isEqualTo(orderTest2.getCustomer().getId());
        assertThat(allOrders.get(1).getDeliveryId()).isEqualTo(orderTest2.getDeliveryAddress().getId());

    }

    @Test
    @DisplayName("Get Order by Id")
    @org.junit.jupiter.api.Order(1)
    public void findById_shouldReturnOrderById() {
        // set up
        Optional<Order> knownOrder = Optional.of(orderTest1);
        when(orderRepo.findById(anyInt())).thenReturn(knownOrder);

        // execute
        Order databaseOrder = serviceUnderTest.findById(testId);

        // assert
        assertThat(databaseOrder).isNotNull();
        assertThat(databaseOrder.getId()).isEqualTo(knownOrder.get().getId());
        assertThat(databaseOrder.getInstant()).isEqualTo(knownOrder.get().getInstant());
        assertThat(databaseOrder.getCustomer().getId()).isEqualTo(knownOrder.get().getCustomer().getId());
        assertThat(databaseOrder.getDeliveryAddress().getId()).isEqualTo(knownOrder.get().getDeliveryAddress().getId());

        // verify
        verify(orderRepo).findById(testId);

    }

    @Test
    @DisplayName("(Exception) Get order by Id - Id not found")
    @org.junit.jupiter.api.Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() {
        //set up
        when(orderRepo.findById(anyInt())).thenThrow(new ResourceNotFoundException(testId));
        //execute and assert
        assertThatThrownBy(() -> serviceUnderTest.findById(anyInt())).isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("Insert a new order")
    @org.junit.jupiter.api.Order(3)
    public void insert_shouldCreateNewOrder() {
        // set up
        when(customerRepo.findById(anyInt())).thenReturn(opCustomer);
        when(addressRepo.findById(anyInt())).thenReturn(opAddress);
        when(orderRepo.save(any())).thenReturn(orderTest1);
        when(customerRepo.save(any())).thenReturn(customerTest);

        // execute
        Order newOrder = serviceUnderTest.insert(orderDTO);

        // assert
        assertThat(newOrder).isNotNull();
        assertThat(newOrder.getId()).isEqualTo(orderTest1.getId());


        //verify
        verify(customerRepo,times(1)).save(any());
        verify(orderRepo,times(2)).save(any());
    }

    @Test
    @DisplayName("(Exception) Insert a new order - User Id not Found")
    @org.junit.jupiter.api.Order(4)
    public void insert_whenUserIdNotFound_thenThrowNoSuchElementException() {
        // set up
        when(customerRepo.findById(anyInt())).thenThrow(NoSuchElementException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(orderDTO)).isInstanceOf(NoValueForIdException.class)
                .hasMessageContaining(THERE_IS_NO_CUSTOMER_USING_THIS_ID + customerId + "). "
                        + THERE_IS_NO_ADDRESS_USING_THIS_ID + addressId + ")");
    }

    @Test
    @DisplayName("(Exception) Insert a new order - Customer or Address Id null")
    @org.junit.jupiter.api.Order(5)
    public void insert_whenCustomerOrAddressIdIsNull_thenThrowInvalidDataAccessApiUsageException() {
        // set up
        when(customerRepo.findById(anyInt())).thenThrow(InvalidDataAccessApiUsageException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(orderDTO)).isInstanceOf(IdNullException.class)
                .hasMessageContaining("Id of Customer or Address must not be null.");
    }

    @Test
    @DisplayName("(Exception) Insert a new order - Unexpected Exception")
    @org.junit.jupiter.api.Order(6)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(customerRepo.findById(anyInt())).thenThrow(RuntimeException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(orderDTO)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Delete order by Id")
    @org.junit.jupiter.api.Order(7)
    public void deleteById_shouldDeleteOrderById() {
        // set up
        doNothing().when(orderRepo).deleteById(anyInt());

        // execute
        serviceUnderTest.deleteById(testId);

        // verify
        verify(orderRepo, times(1)).deleteById(testId);
    }

    @Test
    @DisplayName("(Exception) Delete order by Id - Id not found")
    @org.junit.jupiter.api.Order(8)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        doThrow(EmptyResultDataAccessException.class).when(orderRepo).deleteById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Delete order by Id - Unexpected Exception")
    @org.junit.jupiter.api.Order(9)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        doThrow(RuntimeException.class).when(orderRepo).deleteById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Update order")
    @org.junit.jupiter.api.Order(10)
    public void update_shouldUpdateOrder() {
        // set up
        when(orderRepo.getReferenceById(anyInt())).thenReturn(orderTest1);
        when(orderRepo.save(any())).thenReturn(orderTest2);
        when(addressService.update(anyInt(), anyInt(), any())).thenReturn(orderTest2.getDeliveryAddress());

        // execute
        Order updatedOrder = serviceUnderTest.update(testId, orderTest2);

        // assert
        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getId()).isEqualTo(orderTest2.getId());
        assertThat(updatedOrder.getInstant()).isEqualTo(orderTest2.getInstant());
        assertThat(updatedOrder.getCustomer().getId()).isEqualTo(orderTest2.getCustomer().getId());
        assertThat(updatedOrder.getDeliveryAddress().getId()).isEqualTo(orderTest2.getDeliveryAddress().getId());

    }

    @Test
    @DisplayName("(Exception) Update order - Id not found")
    @org.junit.jupiter.api.Order(11)
    public void update_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        when(orderRepo.getReferenceById(anyInt())).thenThrow(EntityNotFoundException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, orderTest2)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Update order - Unexpected Exception")
    @org.junit.jupiter.api.Order(12)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(orderRepo.getReferenceById(anyInt())).thenThrow(RuntimeException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, orderTest2)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Find all orders of an user")
    @org.junit.jupiter.api.Order(13)
    public void findAllOrdersByUserId_shouldReturnAllOrdersOffTheUser() {
        // set up
        Page<Order> page = new PageImpl<>(List.of(orderTest1, orderTest2));
        when(orderRepo.findAllOrdersByCustomerId(any(), anyInt())).thenReturn(page);

        // execute
        List<Order> allOrdersCustomerTest = serviceUnderTest.findAllOrdersByCustomerId(0, 1);

        // assert
        assertThat(allOrdersCustomerTest).isNotNull();
        assertThat(allOrdersCustomerTest.size()).isEqualTo(2);

        // verify
        verify(orderRepo).findAllOrdersByCustomerId(any(), anyInt());

    }

}
