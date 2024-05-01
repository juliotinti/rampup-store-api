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
import java.util.Optional;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.Ticket;
import com.julio.rampUp.entities.dto.TicketDTO;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.repositories.TicketRepository;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketServiceTest {

    private static final String RESOURCE_NOT_FOUND_ID = "Resource not found. Id - ";
    private static final int testId = 1;

    @Mock
    private TicketRepository ticketRepo;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private TicketService serviceUnderTest;

    private Ticket ticketTest;
    private Ticket ticketTest2;
    private TicketDTO ticketDTO;
    private TicketDTO ticketDTO2;
    private Customer customerTest1;
    private com.julio.rampUp.entities.Order order1;
    private com.julio.rampUp.entities.Order order2;
    private Address address1;

    @BeforeEach
    public void setup() {

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
        ticketDTO = new TicketDTO(ticketTest);
        ticketTest2 = new Ticket(2, "I want to cancel this order cause...", order2);
        ticketDTO2 = new TicketDTO(ticketTest2);

    }

    @Test
    @DisplayName("Get all Tickets")
    @Order(0)
    public void findAll_shouldReturnAllTickets() {
        // set up
        Page<Ticket> page = new PageImpl<>(List.of(ticketTest, ticketTest2));
        when(ticketRepo.findAll(any(Pageable.class))).thenReturn(page);

        // execute
        List<Ticket> allTickets = serviceUnderTest.findAll(0);

        // assert
        assertThat(allTickets).isNotNull();
        assertThat(allTickets.size()).isEqualTo(2);
        assertThat(allTickets.get(0).getId()).isEqualTo(testId);
        assertThat(allTickets.get(0).getInstant()).isEqualTo(ticketTest.getInstant());
        assertThat(allTickets.get(0).getOrder().getId()).isEqualTo(ticketTest.getOrder().getId());
        assertThat(allTickets.get(0).getMessage()).isEqualTo(ticketTest.getMessage());

    }

    @Test
    @DisplayName("Get ticket by Id")
    @Order(1)
    public void findById_shouldReturnTicketById() {
        // set up
        Optional<Ticket> knownTicket = Optional.of(ticketTest);
        when(ticketRepo.findById(anyInt())).thenReturn(knownTicket);

        // execute
        Ticket databaseTicket = serviceUnderTest.findById(testId);

        // assert
        assertThat(databaseTicket).isNotNull();
        assertThat(databaseTicket.getId()).isEqualTo(knownTicket.get().getId());
        assertThat(databaseTicket.getInstant()).isEqualTo(ticketTest.getInstant());
        assertThat(databaseTicket.getOrder().getId()).isEqualTo(ticketTest.getOrder().getId());
        assertThat(databaseTicket.getMessage()).isEqualTo(ticketTest.getMessage());

        // verify
        verify(ticketRepo).findById(testId);

    }

    @Test
	@DisplayName("(Exception) Get ticket by Id - Id not found")
	@Order(2)
	public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() {
		//set up
		when(ticketRepo.findById(anyInt())).thenThrow(new ResourceNotFoundException(testId));
		//execute and assert
		assertThatThrownBy(() -> serviceUnderTest.findById(testId)).isInstanceOf(ResourceNotFoundException.class)
		.hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
	}

    @Test
	@DisplayName("Insert a new ticket")
	@Order(3)
	public void insert_shouldCreateNewTicket() {
		// set up
		when(ticketRepo.save(any())).thenReturn(ticketTest);
		when(orderService.findById(anyInt())).thenReturn(order1);

		// execute
		Ticket newTicket = serviceUnderTest.insert(ticketDTO, ticketDTO.getOrderId());

		// assert
		assertThat(newTicket).isNotNull();
		assertThat(newTicket.getClass()).isEqualTo(Ticket.class);
		assertThat(newTicket.getId()).isEqualTo(ticketTest.getId());
        assertThat(newTicket.getInstant()).isEqualTo(ticketTest.getInstant());
        assertThat(newTicket.getOrder().getId()).isEqualTo(ticketTest.getOrder().getId());
        assertThat(newTicket.getMessage()).isEqualTo(ticketTest.getMessage());

		// verify
		verify(ticketRepo).save(ticketTest);
	}

    @Test
    @DisplayName("Insert a new ticket within 2 hours of created order")
    @Order(5)
    public void insert_shouldCreateNewTicketAndSoftDeleteOrder() {
        // set up
        when(ticketRepo.save(any())).thenReturn(ticketTest2);
        when(orderService.findById(anyInt())).thenReturn(order2);
        doNothing().when(orderService).deleteById(anyInt());

        // execute
        Ticket newTicket = serviceUnderTest.insert(ticketDTO2, ticketDTO2.getOrderId());

        // assert
        assertThat(newTicket).isNotNull();
        assertThat(newTicket.getClass()).isEqualTo(Ticket.class);
        assertThat(newTicket.getId()).isEqualTo(ticketTest2.getId());
        assertThat(newTicket.getInstant()).isEqualTo(ticketTest2.getInstant());
        assertThat(newTicket.getOrder().getId()).isEqualTo(ticketTest2.getOrder().getId());
        assertThat(newTicket.getMessage()).isEqualTo(ticketTest2.getMessage());

        // verify
        verify(ticketRepo).save(ticketTest2);
    }

    @Test
    @DisplayName("(Exception) Insert a new ticket - Order id not found")
    @Order(5)
    public void insert_whenOrderIdNotFound_thenResourceNotFoundException() {
        //set up
        when(orderService.findById(anyInt())).thenThrow(ResourceNotFoundException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(ticketDTO, ticketDTO.getOrderId())).isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Insert a new ticket - Order id is not from this customer")
    @Order(6)
    public void insert_whenOrderIdIsNotFromTheCustomer_thenResourceNotFoundException() {
        //set up
        when(orderService.findById(anyInt())).thenReturn(order1);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(ticketDTO, 3)).isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(RESOURCE_NOT_FOUND_ID + 3);
    }

    @Test
    @DisplayName("(Exception) Insert a new ticket - Unexpected Exception")
    @Order(7)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() {
        //set up
        when(ticketRepo.save(any())).thenThrow(RuntimeException.class);
        when(orderService.findById(anyInt())).thenReturn(order1);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(ticketDTO, ticketDTO.getOrderId())).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Find all tickets by customerId")
    @Order(8)
    public void findAllTicketsByCustomerId_thenShowAllTickesForCustomer() {
        // set up
        Page<Ticket> page = new PageImpl<>(List.of(ticketTest, ticketTest2));
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 10, sort);
        when(ticketRepo.findAllTicketsByCustomerId(any(), anyInt())).thenReturn(page);

        // execute
        List<Ticket> allTicketsCustomer1 = serviceUnderTest.findAllTicketsByCustomerId(0, testId);

        // assert
        assertThat(allTicketsCustomer1).isNotNull();
        assertThat(allTicketsCustomer1.size()).isEqualTo(2);
        assertThat(allTicketsCustomer1.get(0).getId()).isEqualTo(testId);
        assertThat(allTicketsCustomer1.get(0).getInstant()).isEqualTo(ticketTest.getInstant());
        assertThat(allTicketsCustomer1.get(0).getOrder().getId()).isEqualTo(ticketTest.getOrder().getId());
        assertThat(allTicketsCustomer1.get(0).getMessage()).isEqualTo(ticketTest.getMessage());

        // verify
        verify(ticketRepo).findAllTicketsByCustomerId(pageable, testId);
    }

    @Test
    @DisplayName("Delete tikcet by Id")
    @Order(9)
    public void deleteById_shouldDeleteTicketById() {
        // set up
        when(ticketRepo.findById(anyInt())).thenReturn(Optional.of(ticketTest));
        doNothing().when(orderService).deleteById(anyInt());
        doNothing().when(ticketRepo).deleteById(anyInt());

        // execute
        serviceUnderTest.deleteById(testId);

        // verify
        verify(ticketRepo, times(1)).findById(ticketTest.getId());
        verify(ticketRepo, times(1)).deleteById(testId);
    }

    @Test
    @DisplayName("(Exception) Delete ticket by Id - Id not found")
    @Order(10)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        doThrow(EmptyResultDataAccessException.class).when(ticketRepo).findById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Delete role by Id - Unexpected Exception")
    @Order(11)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        doThrow(RuntimeException.class).when(ticketRepo).findById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(UnexpectedException.class);
    }

}
