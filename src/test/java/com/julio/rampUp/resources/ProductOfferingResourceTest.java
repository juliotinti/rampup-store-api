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

import com.julio.rampUp.entities.ProductOffering;
import com.julio.rampUp.entities.enums.POState;
import com.julio.rampUp.mock.TokenMock;
import com.julio.rampUp.services.ProductOfferingService;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductOfferingResourceTest {

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
    private ProductOfferingService poService;

    private ProductOffering poTest1;
    private ProductOffering poTest2;

    @BeforeEach
    public void setup() throws Exception {
        poTest1 = new ProductOffering(1, "namePO1", 50000.0, true, POState.Active);
        poTest2 = new ProductOffering(2, "namePO2", 150000.0, true, POState.Active);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
        tokenMock = new TokenMock();
        accessToken = tokenMock.obtainAccessToken("haaland@gmail.com", "9city9", mockMvc);

    }

    @Test
    @DisplayName("Get all product offerings")
    @Order(0)
    public void findAll_shouldReturnAllProductOfferings() throws Exception {
        // set up
        List<ProductOffering> list = List.of(poTest1, poTest2);
        when(poService.findAll(anyInt())).thenReturn(list);
        // execute and assert
        mockMvc.perform(get("/productOfferings/page/" + testPage).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()) //
                .andExpect(jsonPath("$[0].id", is(poTest1.getId())))
                .andExpect(jsonPath("$[0].productName", is("namePO1")))
                .andExpect(jsonPath("$[0].unitPrice", is(poTest1.getUnitPrice())))
                .andExpect(jsonPath("$[1].id", is(poTest2.getId())))
                .andExpect(jsonPath("$[1].productName", is("namePO2")))
                .andExpect(jsonPath("$[1].unitPrice", is(poTest2.getUnitPrice())));
    }

    @Test
    @DisplayName("Get product offerings by Id")
    @Order(1)
    public void findById_shouldReturnProductOfferingById() throws Exception {
        // set up
        when(poService.findById(anyInt())).thenReturn(poTest1);
        // execute and assert
        mockMvc.perform(get("/productOfferings/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(poTest1.getId())))
                .andExpect(jsonPath("$.productName", is("namePO1")))
                .andExpect(jsonPath("$.unitPrice", is(poTest1.getUnitPrice())));
    }

    @Test
    @DisplayName("(Exception) Get product offering by Id - Id not found")
    @Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() throws Exception {
        // set up
        int notExistedId = 5;
        when(poService.findById(anyInt())).thenThrow(new ResourceNotFoundException(notExistedId));
        // execute and assert
        mockMvc.perform(get("/productOfferings/" + notExistedId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Insert a new product offering")
    @Order(3)
    public void insert_shouldCreateNewProductOffering() throws Exception {
        // set up
        when(poService.insert(any())).thenReturn(poTest1);

        // execute and assert
        mockMvc.perform(post("/productOfferings").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(poTest1)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(poTest1.getId())))
                .andExpect(jsonPath("$.productName", is("namePO1")))
                .andExpect(jsonPath("$.unitPrice", is(poTest1.getUnitPrice())));
    }

    @Test
    @DisplayName("(Exception) Insert a new product offering - Unexpected Exception")
    @Order(4)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(poService).insert(any());

        // execute and assert
        mockMvc.perform(post("/productOfferings/").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(poTest1)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Delete product offering by Id")
    @Order(5)
    public void deleteById_shouldDeleteProductOfferingById() throws Exception {
        // set up
        doNothing().when(poService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/productOfferings/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("(Exception) Delete product offering by Id - Id not found")
    @Order(6)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(poService).deleteById(anyInt());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(delete("/productOfferings/" + nonExistingId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Delete product offering by Id - Unexpected Exception")
    @Order(7)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(poService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/productOfferings/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Update product offering")
    @Order(8)
    public void update_shouldUpdateProductOffering() throws Exception {
        // set up
        when(poService.update(anyInt(), any())).thenReturn(poTest2);

        // execute and assert
        mockMvc.perform(patch("/productOfferings/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(poTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(poTest2.getId())))
                .andExpect(jsonPath("$.productName", is("namePO2")))
                .andExpect(jsonPath("$.unitPrice", is(poTest2.getUnitPrice())));
    }

    @Test
    @DisplayName("(Exception) Update product offering - Id not found")
    @Order(9)
    public void update_whenIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(poService).update(anyInt(), any());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(patch("/productOfferings/" + nonExistingId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(poTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Update product offering - Unexpected Exception")
    @Order(10)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(poService).update(anyInt(), any());

        // execute and assert
        mockMvc.perform(patch("/productOfferings/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(poTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
