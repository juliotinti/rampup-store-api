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
import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.ProductOffering;
import com.julio.rampUp.entities.Ticket;
import com.julio.rampUp.entities.dto.OrderDTO;
import com.julio.rampUp.entities.dto.TicketDTO;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.entities.enums.POState;
import com.julio.rampUp.mock.TokenMock;
import com.julio.rampUp.services.OrderService;
import com.julio.rampUp.services.TicketService;
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
public class OrderResourceTest {

    private static final int testId = 1;
    private static final int testPage = 0;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;
    private String accessToken;
    private TokenMock tokenMock;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private OrderService orderService;

    private Customer customerTest;
    private Address address;
    private Order orderTest1;
    private Order orderTest2;
    private OrderDTO orderDTO;
    private OrderDTO orderDTO2;
    private ProductOffering poTest;
    private Ticket ticketTest;
    private TicketDTO ticketDTO;

    @BeforeEach
    public void setup() throws Exception {
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

        orderDTO = new OrderDTO(orderTest1);
//        orderDTO.setDiscount(0.15);
//        orderDTO.setQuantity(50);
        orderDTO.setCustomerId(customerTest.getId());
        orderDTO.setDeliveryId(address.getId());
//        orderDTO.setProductOfferingId(poTest.getId());

        orderDTO2 = new OrderDTO(orderTest2);
//        orderDTO2.setDiscount(0.05);
//        orderDTO2.setQuantity(5);
        orderDTO2.setCustomerId(customerTest.getId());
        orderDTO2.setDeliveryId(address.getId());
//        orderDTO2.setProductOfferingId(poTest.getId());

        ticketTest = new Ticket(1, "I want to cancel this order cause...", orderTest1);
        ticketDTO = new TicketDTO(ticketTest);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
        tokenMock = new TokenMock();
        accessToken = tokenMock.obtainAccessToken("haaland@gmail.com", "9city9", mockMvc);

    }

    @Test
    @DisplayName("Get all Orders")
    @org.junit.jupiter.api.Order(0)
    public void findAll_shouldReturnAllOrders() throws Exception {
        // set up
        List<OrderDTO> list = List.of(orderDTO, orderDTO2);
        when(orderService.findAll(anyInt())).thenReturn(list);
        // execute and assert
//        mockMvc.perform(get("/orders/page/" + testPage).header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isOk()) //
//                .andExpect(jsonPath("$[0].orderInfo", is(orderDTO.getOrderInfo())))
//                .andExpect(jsonPath("$[1].orderInfo", is(orderDTO2.getOrderInfo())));
    }

    @Test
    @DisplayName("Get Order by Id")
    @org.junit.jupiter.api.Order(1)
    public void findById_shouldReturnOrderById() throws Exception {
        // set up
        when(orderService.findById(anyInt())).thenReturn(orderTest1);
        // execute and assert
        mockMvc.perform(get("/orders/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderTest1.getId())))
                .andExpect(jsonPath("$.customer.id", is(orderTest1.getCustomer().getId())))
                .andExpect(jsonPath("$.deliveryAddress.id", is(orderTest1.getDeliveryAddress().getId())));
    }

    @Test
    @DisplayName("(Exception) Get order by Id - Id not found")
    @org.junit.jupiter.api.Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() throws Exception {
        // set up
        int notExistedId = 5;
        when(orderService.findById(anyInt())).thenThrow(new ResourceNotFoundException(notExistedId));
        // execute and assert
        mockMvc.perform(get("/orders/" + notExistedId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Insert a new order")
    @org.junit.jupiter.api.Order(3)
    public void insert_shouldCreateNewOrder() throws Exception {
        // set up
        when(orderService.insert(any())).thenReturn(orderTest1);

        // execute and assert
        mockMvc.perform(post("/orders/").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderDTO)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(orderTest1.getId())))
                .andExpect(jsonPath("$.customer.id", is(orderTest1.getCustomer().getId())))
                .andExpect(jsonPath("$.deliveryAddress.id", is(orderTest1.getDeliveryAddress().getId())));
    }

    @Test
    @DisplayName("(Exception) Insert a new order - User Id not Found")
    @org.junit.jupiter.api.Order(4)
    public void insert_whenUserIdNotFound_thenThrowNoValueForIdException() throws Exception {
        // set up
        doThrow(NoValueForIdException.class).when(orderService).insert(any());

        // execute and assert
        mockMvc.perform(post("/orders/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(orderDTO))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new order - Customer or Address Id null")
    @org.junit.jupiter.api.Order(5)
    public void insert_whenCustomerOrAddressIdIsNull_thenThrowIdNullException() throws Exception {
        // set up
        doThrow(IdNullException.class).when(orderService).insert(any());

        // execute and assert
        mockMvc.perform(post("/orders/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(orderDTO))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new order - Unexpected Exception")
    @org.junit.jupiter.api.Order(6)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(orderService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(post("/orders/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(orderDTO))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Delete order by Id")
    @org.junit.jupiter.api.Order(7)
    public void deleteById_shouldDeleteOrderById() throws Exception {
        // set up
        doNothing().when(orderService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/orders/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("(Exception) Delete order by Id - Id not found")
    @org.junit.jupiter.api.Order(8)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(orderService).deleteById(anyInt());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(delete("/orders/" + nonExistingId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Delete order by Id - Unexpected Exception")
    @org.junit.jupiter.api.Order(9)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(orderService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/orders/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Update order")
    @org.junit.jupiter.api.Order(10)
    public void update_shouldUpdateOrder() throws Exception {
        // set up
        when(orderService.update(anyInt(), any())).thenReturn(orderTest2);

        // execute and assert
        mockMvc.perform(patch("/orders/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderTest2.getId())))
                .andExpect(jsonPath("$.customer.id", is(orderTest2.getCustomer().getId())))
                .andExpect(jsonPath("$.deliveryAddress.id", is(orderTest2.getDeliveryAddress().getId())));
    }

    @Test
    @DisplayName("(Exception) Update order - Id not found")
    @org.junit.jupiter.api.Order(11)
    public void update_whenIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(orderService).update(anyInt(), any());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(patch("/orders/" + nonExistingId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Update order - Unexpected Exception")
    @org.junit.jupiter.api.Order(12)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(orderService).update(anyInt(), any());

        // execute and assert
        mockMvc.perform(patch("/orders/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Create ticket to cancel order")
    @org.junit.jupiter.api.Order(13)
    public void createTicket_shouldCreateTickets() throws Exception {
        //set up
        when(ticketService.insert(any(), anyInt())).thenReturn(ticketTest);

        // execute and assert
        mockMvc.perform(post("/orders/" + testId + "/ticket").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(ticketDTO)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(ticketTest.getId())))
                .andExpect(jsonPath("$.message", is(ticketTest.getMessage())))
                .andExpect(jsonPath("$.order.id", is(ticketTest.getOrder().getId())));
    }

    @Test
    @DisplayName("(Exception) Create ticket to cancel order - Order id not found")
    @org.junit.jupiter.api.Order(14)
    public void createTicket_whenOrderIdNotFound_thenResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(ticketService).insert(any(), anyInt());

        // execute and assert
        mockMvc.perform(post("/orders/" + testId + "/ticket").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(ticketDTO)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("(Exception) Create ticket to cancel order - Order id is not from this customer")
    @org.junit.jupiter.api.Order(15)
    public void createTicket_whenOrderIdIsNotFromTheCustomer_thenResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(ticketService).insert(any(), anyInt());

        // execute and assert
        mockMvc.perform(post("/orders/" + testId + "/ticket").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(ticketDTO)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Create ticket to cancel order - Unexpected Exception")
    @org.junit.jupiter.api.Order(15)
    public void createTicket_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(ticketService).insert(any(), anyInt());

        // execute and assert
        mockMvc.perform(post("/orders/" + testId + "/ticket").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(ticketDTO)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
