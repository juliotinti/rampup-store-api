package com.julio.rampUp.resources;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.Ticket;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.mock.TokenMock;
import com.julio.rampUp.services.OrderService;
import com.julio.rampUp.services.TicketService;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketResourceTest {

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

    private Ticket ticketTest;
    private Ticket ticketTest2;
    private Customer customerTest1;
    private com.julio.rampUp.entities.Order order1;
    private com.julio.rampUp.entities.Order order2;
    private Address address1;

    @BeforeEach
    public void setup() throws Exception {
        customerTest1 = new Customer(1, "Maria", 123456789, CustomerType.NaturalPerson, "High", "123");
        address1 = new Address(1, "Sete de Setembro Avenue", 554, "Center", 80230000, "Brazil",
                AddressType.HomeAddress);
        customerTest1.addAddress(address1);
        order1 = new com.julio.rampUp.entities.Order(1, Instant.parse("2022-06-20T19:53:07Z"), customerTest1,
                customerTest1.getAddresses().get(0));
        order2 = new com.julio.rampUp.entities.Order(2, Instant.now(), customerTest1,
                customerTest1.getAddresses().get(0));
        customerTest1.addOrder(order1);
        customerTest1.addOrder(order2);

        ticketTest = new Ticket(1, "I want to cancel this order cause...", order1);
        ticketTest2 = new Ticket(2, "I want to cancel this order cause...", order2);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
        tokenMock = new TokenMock();
        accessToken = tokenMock.obtainAccessToken("haaland@gmail.com", "9city9", mockMvc);

    }

    @Test
    @DisplayName("Get all Tickets")
    @Order(0)
    public void findAll_shouldReturnAllTickets() throws Exception {
        // set up
        List<Ticket> list = List.of(ticketTest, ticketTest2);
        when(ticketService.findAll(anyInt())).thenReturn(list);
        // execute and assert
        mockMvc.perform(get("/tickets/page/" + testPage).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()) //
                .andExpect(jsonPath("$[0].id", is(ticketTest.getId())))
                .andExpect(jsonPath("$[0].message", is(ticketTest.getMessage())))
                .andExpect(jsonPath("$[0].order.id", is(ticketTest.getOrder().getId())))
                .andExpect(jsonPath("$[1].id", is(ticketTest2.getId())))
                .andExpect(jsonPath("$[1].message", is(ticketTest2.getMessage())))
                .andExpect(jsonPath("$[1].order.id", is(ticketTest2.getOrder().getId())));
    }

    @Test
    @DisplayName("Get ticket by Id")
    @Order(1)
    public void findById_shouldReturnTicketById() throws Exception {
        // set up
        when(ticketService.findById(anyInt())).thenReturn(ticketTest);
        // execute and assert
        mockMvc.perform(get("/tickets/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ticketTest.getId())))
                .andExpect(jsonPath("$.message", is(ticketTest.getMessage())))
                .andExpect(jsonPath("$.order.id", is(ticketTest.getOrder().getId())));
    }

    @Test
    @DisplayName("(Exception) Get ticket by Id - Id not found")
    @Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() throws Exception {
        // set up
        int notExistedId = 5;
        when(ticketService.findById(anyInt())).thenThrow(new ResourceNotFoundException(notExistedId));
        // execute and assert
        mockMvc.perform(get("/tickets/" + notExistedId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete ticket by Id")
    @Order(3)
    public void deleteById_shouldDeleteTicketById() throws Exception {
        // set up
        doNothing().when(ticketService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/tickets/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("(Exception) Delete ticket by Id - Id not found")
    @Order(4)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(ticketService).deleteById(anyInt());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(delete("/tickets/" + nonExistingId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Delete role by Id - Unexpected Exception")
    @Order(5)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(ticketService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/tickets/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

}
