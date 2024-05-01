package com.julio.rampUp.resources;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.julio.rampUp.entities.dto.AddressDTO;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.mock.TokenMock;
import com.julio.rampUp.services.AddressService;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AddressResourceTest {

    private static final int testId = 1;
    private static final int testPage = 0;
    private static final int customerId = 1;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;
    private String accessToken;
    private TokenMock tokenMock;

    @MockBean
    private AddressService addressService;

    private Address addressTest1;
    private Address addressTest2;
    private AddressDTO addressDTO;

    @BeforeEach
    public void setup() throws Exception {
        addressTest1 = new Address(1, "Sete de Setembro Avenue", 554, "Center", 80230000, "Brazil",
                AddressType.HomeAddress);
        addressTest2 = new Address(2, "Professora Dona Lili Street", 125, "Center", 37130000, "Brazil",
                AddressType.ShippingAddress);
        addressTest2.setDeleted(true);

        addressDTO = new AddressDTO(addressTest1);
        addressDTO.setCustomerId(customerId);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
        tokenMock = new TokenMock();
        accessToken = tokenMock.obtainAccessToken("haaland@gmail.com", "9city9", mockMvc);

    }

    @Test
    @DisplayName("Get all addresses")
    @Order(0)
    public void findAll_shouldReturnAllAddress() throws Exception {
        // set up
        List<AddressDTO> list = List.of(addressDTO); // addressTest2 is deleted
        when(addressService.findAll(anyInt())).thenReturn(list);
        // execute and assert
        mockMvc.perform(get("/addresses/page/" + testPage).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()) //
                .andExpect(jsonPath("$[0].street", is(addressDTO.getStreet())))
                .andExpect(jsonPath("$[0].houseNumber", is(addressDTO.getHouseNumber())))
                .andExpect(jsonPath("$[0].neighborhood", is(addressDTO.getNeighborhood())))
                .andExpect(jsonPath("$[0].zipCode", is(addressDTO.getZipCode())))
                .andExpect(jsonPath("$[0].country", is(addressDTO.getCountry())));
    }

    @Test
    @DisplayName("Get address by Id")
    @Order(1)
    public void findById_shouldReturnAddressById() throws Exception {
        // set up
        when(addressService.findById(anyInt())).thenReturn(addressTest1);
        // execute and assert
        mockMvc.perform(get("/addresses/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(addressTest1.getId())))
                .andExpect(jsonPath("$.street", is(addressTest1.getStreet())))
                .andExpect(jsonPath("$.houseNumber", is(addressTest1.getHouseNumber())))
                .andExpect(jsonPath("$.neighborhood", is(addressTest1.getNeighborhood())))
                .andExpect(jsonPath("$.zipCode", is(addressTest1.getZipCode())))
                .andExpect(jsonPath("$.country", is(addressTest1.getCountry())));
    }

    @Test
    @DisplayName("(Exception) Get address by Id - Id not found")
    @Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() throws Exception {
        // set up
        int notExistedId = 5;
        when(addressService.findById(anyInt())).thenThrow(new ResourceNotFoundException(notExistedId));
        // execute and assert
        mockMvc.perform(get("/addresses/" + notExistedId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("(Exception) Get address by Id - Address deleted")
    @Order(3)
    public void findById_whenAddressWasDeleted_ShouldThrowResourceNotFoundException() throws Exception {
        // set up
        int deletedAddressId = 2;
        when(addressService.findById(anyInt())).thenThrow(new ResourceNotFoundException(deletedAddressId));
        // execute and assert
        mockMvc.perform(get("/addresses/" + deletedAddressId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

    }

}
