package com.julio.rampUp.resources;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.Ticket;
import com.julio.rampUp.entities.User;
import com.julio.rampUp.entities.dto.AddressDTO;
import com.julio.rampUp.entities.dto.CustomerDTO;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.mock.TokenMock;
import com.julio.rampUp.services.AddressService;
import com.julio.rampUp.services.CustomerService;
import com.julio.rampUp.services.OrderService;
import com.julio.rampUp.services.TicketService;
import com.julio.rampUp.services.exceptions.AddressException;
import com.julio.rampUp.services.exceptions.CustomerAlreadyExists;
import com.julio.rampUp.services.exceptions.IdNullException;
import com.julio.rampUp.services.exceptions.NoValueForIdException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerResourceTest {

    private static final int testId = 1;
    private static final int testPage = 0;
    private static final int testAddressId = 1;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;
    private String accessToken;
    private TokenMock tokenMock;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private AddressService addressService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private TicketService ticketService;

    private User userTest1;
    private User userTest2;
    private Customer customerTest1;
    private Customer customerTest2;
    private CustomerDTO customerDTO;
    private CustomerDTO customerDTO2;
    private CustomerDTO customerDTO3;
    private Address addressTest1;
    private Address addressTest2;
    private AddressDTO addressDTO;
    private com.julio.rampUp.entities.Order orderTest1;
    private com.julio.rampUp.entities.Order orderTest2;
    private Ticket ticketTest1;
    private Ticket ticketTest2;

    @BeforeEach
    public void setup() throws Exception {
        userTest1 = new User(1, "maria@gmail.com", "1234567");
        userTest2 = new User(2, "joao@gmail.com", "1234567");
        customerTest1 = new Customer(1, "Maria", 123454321, CustomerType.NaturalPerson, "High", "1123");
        customerTest1.setUser(userTest1);
        userTest1.setCustomer(customerTest1);

        customerTest2 = new Customer(2, "Joao", 123456789, CustomerType.NaturalPerson, "High", userTest1.getPassword());
        customerTest2.setUser(userTest2);
        userTest2.setCustomer(customerTest2);

        customerDTO = new CustomerDTO(customerTest1);
        customerDTO.setUserId(userTest1.getId());
        customerDTO2 = new CustomerDTO(customerTest2);
        customerDTO2.setUserId(userTest2.getId());

        customerDTO3 = new CustomerDTO(customerTest2);

        addressTest1 = new Address(1, "Sete de Setembro Avenue", 554, "Center", 80230000, "Brazil",
                AddressType.HomeAddress);
        addressTest2 = new Address(2, "Professora Dona Lili Street", 125, "Center", 37130000, "Brazil",
                AddressType.ShippingAddress);
        addressTest1.setCustomer(customerTest1);
        addressTest2.setCustomer(customerTest1);
        customerTest1.addAddress(addressTest1);
        customerTest1.addAddress(addressTest2);
        addressDTO = new AddressDTO(addressTest1);

        orderTest1 = new com.julio.rampUp.entities.Order(1, Instant.parse("2022-06-20T19:53:07Z"), customerTest1,
                customerTest1.getAddresses().get(0));
        orderTest2 = new com.julio.rampUp.entities.Order(2, Instant.now(), customerTest1,
                customerTest1.getAddresses().get(0));

        ticketTest1 = new Ticket(1, "I want to cancel this order cause...", orderTest1);
        ticketTest2 = new Ticket(2, "I want to cancel this order cause...", orderTest2);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
        tokenMock = new TokenMock();
        accessToken = tokenMock.obtainAccessToken("haaland@gmail.com", "9city9", mockMvc);

    }

    @Test
    @DisplayName("Get all Customers")
    @Order(0)
    public void findAll_shouldReturnAllCustomers() throws Exception {
        // set up
        List<CustomerDTO> list = List.of(customerDTO, customerDTO2);
        when(customerService.findAll(anyInt())).thenReturn(list);
        // execute and assert
        mockMvc.perform(get("/customers/page/" + testPage).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()) //
                .andExpect(jsonPath("$[0].customerName", is("Maria")))
                .andExpect(jsonPath("$[0].documentNumber", is(customerDTO.getDocumentNumber())))
                .andExpect(jsonPath("$[0].customerStatus", is("Active Customer")))
                .andExpect(jsonPath("$[1].customerName", is("Joao")))
                .andExpect(jsonPath("$[1].documentNumber", is(customerDTO2.getDocumentNumber())))
                .andExpect(jsonPath("$[1].customerStatus", is("Active Customer")));

    }

    @Test
    @DisplayName("Get customer by Id")
    @Order(1)
    public void findById_shouldReturnCustomerById() throws Exception {
        // set up
        when(customerService.findById(anyInt())).thenReturn(customerTest1);
        // execute and assert
        mockMvc.perform(get("/customers/home/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerTest1.getId())))
                .andExpect(jsonPath("$.customerName", is("Maria")))
                .andExpect(jsonPath("$.documentNumber", is(customerTest1.getDocumentNumber())))
                .andExpect(jsonPath("$.customerStatus", is("Active Customer")))
                .andExpect(jsonPath("$.creditScore", is(customerTest1.getCreditScore())))
                .andExpect(jsonPath("$.user.id", is(customerTest1.getUser().getId())));
    }

    @Test
    @DisplayName("(Exception) Get customer by Id - Id not found")
    @Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() throws Exception {
        // set up
        int notExistedId = 5;
        when(customerService.findById(anyInt())).thenThrow(new ResourceNotFoundException(notExistedId));
        // execute and assert
        mockMvc.perform(get("/customers/home/" + notExistedId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Insert a new customer")
    @Order(3)
    public void insert_shouldCreateNewCustomer() throws Exception {
        // set up
        when(customerService.insert(any())).thenReturn(customerTest1);

        // execute and assert
        mockMvc.perform(post("/customers/").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(customerDTO)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(customerTest1.getId())))
                .andExpect(jsonPath("$.customerName", is("Maria")))
                .andExpect(jsonPath("$.documentNumber", is(customerTest1.getDocumentNumber())))
                .andExpect(jsonPath("$.customerStatus", is("Active Customer")))
                .andExpect(jsonPath("$.creditScore", is(customerTest1.getCreditScore())))
                .andExpect(jsonPath("$.user.id", is(customerTest1.getUser().getId())));
    }

    @Test
    @DisplayName("Insert a new customer when User previouly deleted it")
    @Order(4)
    public void insert_whenCustomerWasDelete_shouldCreateNewCustomer() throws Exception {
        // set up
        userTest1.getCustomer().setDeleted(true);
        when(customerService.insert(any())).thenReturn(customerTest1);

        // execute and assert
        mockMvc.perform(post("/customers/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(customerDTO))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(customerTest1.getId())))
                .andExpect(jsonPath("$.customerName", is("Maria")))
                .andExpect(jsonPath("$.documentNumber", is(customerTest1.getDocumentNumber())))
                .andExpect(jsonPath("$.customerStatus", is("Active Customer")))
                .andExpect(jsonPath("$.creditScore", is(customerTest1.getCreditScore())))
                .andExpect(jsonPath("$.user.id", is(customerTest1.getUser().getId())));

        userTest1.getCustomer().setDeleted(false);
    }

    @Test
    @DisplayName("(Exception) Insert a new customer - Customer already exixst")
    @Order(5)
    public void insert_whenCustomerExists_shouldThrowCustomerAlreadyExists() throws Exception {
        // set up
        doThrow(CustomerAlreadyExists.class).when(customerService).insert(any());

        // execute and assert
        mockMvc.perform(post("/customers/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(customerDTO2))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new customer - User Id not Found")
    @Order(6)
    public void insert_whenUserIdNotFound_thenThrowNoValueForIdException() throws Exception {
        // set up
        doThrow(NoValueForIdException.class).when(customerService).insert(any());
        int nonExistingId = 5;
        customerDTO3.setUserId(nonExistingId);

        // execute and assert
        mockMvc.perform(post("/customers/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(customerDTO3))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new customer - User Id null")
    @Order(7)
    public void insert_whenUserIdIsNull_thenThrowIdNullException() throws Exception {
        // set up
        doThrow(IdNullException.class).when(customerService).insert(any());
        customerDTO3.setUserId(null);

        // execute and assert
        mockMvc.perform(post("/customers/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(customerDTO3))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new customer - Unexpected Exception")
    @Order(8)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(customerService).insert(any());

        // execute and assert
        mockMvc.perform(post("/customers/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(customerDTO3))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Delete customer by Id")
    @Order(9)
    public void deleteById_shouldDeleteCustomerById() throws Exception {
        // set up
        doNothing().when(customerService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/customers/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("(Exception) Delete customer by Id - Id not found")
    @Order(10)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(customerService).deleteById(anyInt());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(delete("/customers/" + nonExistingId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Delete customer by Id - Unexpected Exception")
    @Order(11)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(customerService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/customers/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Update customer")
    @Order(12)
    public void update_shouldUpdateCustomer() throws Exception {
        // set up
        when(customerService.update(anyInt(), any())).thenReturn(customerTest2);

        // execute and assert
        mockMvc.perform(patch("/customers/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(customerTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerTest2.getId())))
                .andExpect(jsonPath("$.customerName", is("Joao")))
                .andExpect(jsonPath("$.documentNumber", is(customerTest2.getDocumentNumber())))
                .andExpect(jsonPath("$.customerStatus", is("Active Customer")))
                .andExpect(jsonPath("$.creditScore", is(customerTest2.getCreditScore())))
                .andExpect(jsonPath("$.user.id", is(customerTest2.getUser().getId())));
    }

    @Test
    @DisplayName("(Exception) Update customer - Id not found")
    @Order(13)
    public void update_whenIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(customerService).update(anyInt(), any());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(patch("/customers/" + nonExistingId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(customerTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Update customer - Unexpected Exception")
    @Order(14)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(customerService).update(anyInt(), any());

        // execute and assert
        mockMvc.perform(patch("/customers/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(customerTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Find all customer's addresses")
    @Order(15)
    public void findAllAddressesByCustomerId_thenReturnAllCustomerAddresses() throws Exception {
        // set up
        List<Address> customerTest1Addresses = List.of(addressTest1, addressTest2);
        when(addressService.findAllAddressesByCustomerId(anyInt(), anyInt())).thenReturn(customerTest1Addresses);

        // execute and assert
        mockMvc.perform(get("/customers/" + testId + "/addresses/page/" + testPage).header("Authorization",
                "Bearer " + accessToken)).andExpect(status().isOk()) //
                .andExpect(jsonPath("$[0].id", is(addressTest1.getId())))
                .andExpect(jsonPath("$[0].street", is(addressTest1.getStreet())))
                .andExpect(jsonPath("$[0].houseNumber", is(addressTest1.getHouseNumber())))
                .andExpect(jsonPath("$[0].neighborhood", is(addressTest1.getNeighborhood())))
                .andExpect(jsonPath("$[0].zipCode", is(addressTest1.getZipCode())))
                .andExpect(jsonPath("$[0].country", is(addressTest1.getCountry())))
                .andExpect(jsonPath("$[1].id", is(addressTest2.getId())))
                .andExpect(jsonPath("$[1].street", is(addressTest2.getStreet())))
                .andExpect(jsonPath("$[1].houseNumber", is(addressTest2.getHouseNumber())))
                .andExpect(jsonPath("$[1].neighborhood", is(addressTest2.getNeighborhood())))
                .andExpect(jsonPath("$[1].zipCode", is(addressTest2.getZipCode())))
                .andExpect(jsonPath("$[1].country", is(addressTest2.getCountry())));
    }

    @Test
    @DisplayName("Delete customer address by Id")
    @Order(16)
    public void deleteAddressById_shouldDeleteCustomerAddressById() throws Exception {
        // set up
        doNothing().when(addressService).deleteById(anyInt(), anyInt());

        // execute and assert
        mockMvc.perform(delete("/customers/" + testId + "/addresses/" + testAddressId).header("Authorization",
                "Bearer " + accessToken)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("(Exception) Delete customer address by Id - Customer dont have this address")
    @Order(17)
    public void deleteAddressById_whenAddressIsNotFromTheCustomer_shouldThrowAddressException() throws Exception {
        // set up
        doThrow(AddressException.class).when(addressService).deleteById(anyInt(), anyInt());
        int anotherCustomerAddressId = 5;
        // execute and assert
        mockMvc.perform(delete("/customers/" + testId + "/addresses/" + anotherCustomerAddressId)
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Delete customer address by Id - Unexpected Exception")
    @Order(18)
    public void deleteAddressById_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(addressService).deleteById(anyInt(), anyInt());
        // execute and assert
        mockMvc.perform(delete("/customers/" + testId + "/addresses/" + testAddressId).header("Authorization",
                "Bearer " + accessToken)).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Insert a customer's address")
    @Order(19)
    public void insertAddress_thenCreateAddressForCustomer() throws Exception {
        // set up
        when(addressService.insert(anyInt(), any())).thenReturn(addressTest1);

        // execute and assert
        mockMvc.perform(post("/customers/" + testId + "/addresses/").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(addressDTO)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated()) //
                .andExpect(jsonPath("$.id", is(addressTest1.getId())))
                .andExpect(jsonPath("$.street", is(addressTest1.getStreet())))
                .andExpect(jsonPath("$.houseNumber", is(addressTest1.getHouseNumber())))
                .andExpect(jsonPath("$.neighborhood", is(addressTest1.getNeighborhood())))
                .andExpect(jsonPath("$.zipCode", is(addressTest1.getZipCode())))
                .andExpect(jsonPath("$.country", is(addressTest1.getCountry())));

    }

    @Test
    @DisplayName("(Exception) Insert a customer's address - CustomerId is not correct")
    @Order(20)
    public void insertAddress_whenAddressIsNotFromTheCustomer_thenThrowAddressException() throws Exception {
        // set up
        addressDTO.setCustomerId(customerTest2.getId());
        doThrow(AddressException.class).when(addressService).insert(anyInt(), any());

        // execute and assert
        mockMvc.perform(post("/customers/" + testId + "/addresses/").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(addressDTO)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
        addressDTO.setCustomerId(customerTest1.getId());
    }

    @Test
    @DisplayName("(Exception) Insert a customer's address - Unexpected Exception")
    @Order(21)
    public void insertAddress_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(addressService).insert(anyInt(), any());

        // execute and assert
        mockMvc.perform(post("/customers/" + testId + "/addresses/").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(addressDTO)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Update customer's address")
    @Order(22)
    public void updateAddress_shouldUpdateCustomerAddress() throws Exception {
        // set up
        when(addressService.update(anyInt(), anyInt(), any())).thenReturn(addressTest2);

        // execute and assert
        mockMvc.perform(patch("/customers/" + testId + "/addresses/" + testAddressId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(addressTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street", is(addressTest2.getStreet())))
                .andExpect(jsonPath("$.houseNumber", is(addressTest2.getHouseNumber())))
                .andExpect(jsonPath("$.neighborhood", is(addressTest2.getNeighborhood())))
                .andExpect(jsonPath("$.zipCode", is(addressTest2.getZipCode())))
                .andExpect(jsonPath("$.country", is(addressTest2.getCountry())));
    }

    @Test
    @DisplayName("(Exception) Update customer's address - Customer dont have this address")
    @Order(23)
    public void updateAddress_whenAddressIsNotFromTheCustomer_shouldThrowAddressException() throws Exception {
        // set up
        doThrow(AddressException.class).when(addressService).update(anyInt(), anyInt(), any());
        addressTest2.setCustomer(customerTest2);

        // execute and assert
        mockMvc.perform(
                patch("/customers/" + testId + "/addresses/" + testAddressId).contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(addressTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());

        addressTest2.setCustomer(customerTest1);
    }

    @Test
    @DisplayName("(Exception) Update customer's address - Unexpected Exception")
    @Order(24)
    public void updateAddress_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(addressService).update(anyInt(), anyInt(), any());

        // execute and assert
        mockMvc.perform(
                patch("/customers/" + testId + "/addresses/" + testAddressId).contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(addressTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Find all customer's orders")
    @Order(25)
    public void findAllOrdersByCustomerId_thenReturnAllCustomerOrders() throws Exception {
        // set up
        List<com.julio.rampUp.entities.Order> customerTest1Orders = List.of(orderTest1, orderTest2);
        when(orderService.findAllOrdersByCustomerId(anyInt(), anyInt())).thenReturn(customerTest1Orders);

        // execute and assert
        mockMvc.perform(get("/customers/" + testId + "/orders/page/" + testPage).header("Authorization",
                "Bearer " + accessToken)).andExpect(status().isOk()) //
                .andExpect(jsonPath("$[0].id", is(orderTest1.getId())))
                .andExpect(jsonPath("$[0].customer.id", is(orderTest1.getCustomer().getId())))
                .andExpect(jsonPath("$[0].deliveryAddress.id", is(orderTest1.getDeliveryAddress().getId())))
                .andExpect(jsonPath("$[1].id", is(orderTest2.getId())))
                .andExpect(jsonPath("$[1].customer.id", is(orderTest2.getCustomer().getId())))
                .andExpect(jsonPath("$[1].deliveryAddress.id", is(orderTest2.getDeliveryAddress().getId())));
    }

    @Test
    @DisplayName("Find all customer's tickets")
    @Order(26)
    public void findAllTicketsByCustomerId_thenReturnAllCustomerTickets() throws Exception {
        // set up
        List<Ticket> customerTest1Tickets = List.of(ticketTest1, ticketTest2);
        when(ticketService.findAllTicketsByCustomerId(anyInt(), anyInt())).thenReturn(customerTest1Tickets);

        // execute and assert
        mockMvc.perform(get("/customers/" + testId + "/tickets/page/" + testPage).header("Authorization",
                "Bearer " + accessToken)).andExpect(status().isOk()) //
                .andExpect(jsonPath("$[0].id", is(ticketTest1.getId())))
                .andExpect(jsonPath("$[0].message", is(ticketTest1.getMessage())))
                .andExpect(jsonPath("$[0].order.id", is(ticketTest1.getOrder().getId())))
                .andExpect(jsonPath("$[1].id", is(ticketTest2.getId())))
                .andExpect(jsonPath("$[1].message", is(ticketTest2.getMessage())))
                .andExpect(jsonPath("$[1].order.id", is(ticketTest2.getOrder().getId())));
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
