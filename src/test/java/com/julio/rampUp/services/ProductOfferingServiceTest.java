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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.julio.rampUp.entities.ProductOffering;
import com.julio.rampUp.entities.enums.POState;
import com.julio.rampUp.repositories.ProductOfferingRepository;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductOfferingServiceTest {

    private static final String RESOURCE_NOT_FOUND_ID = "Resource not found. Id - ";
    private static final int testId = 1;

    @Mock
    private ProductOfferingRepository productRepo;

    @InjectMocks
    private ProductOfferingService serviceUnderTest;

    private ProductOffering poTest1;
    private ProductOffering poTest2;

    @BeforeEach
    public void setup() {
        poTest1 = new ProductOffering(1, "namePO1", 50000.0, true, POState.Active);
        poTest2 = new ProductOffering(2, "namePO2", 150000.0, true, POState.Active);
    }

    @Test
    @DisplayName("Get all product offerings")
    @Order(0)
    public void findAll_shouldReturnAllProductOfferings() {
        // set up
        Page<ProductOffering> page = new PageImpl<>(List.of(poTest1, poTest2));
        when(productRepo.findAll(any(Pageable.class))).thenReturn(page);

        // execute
        List<ProductOffering> allProducts = serviceUnderTest.findAll(0);

        // assert
        assertThat(allProducts).isNotNull();
        assertThat(allProducts.size()).isEqualTo(2);
        assertThat(allProducts.get(0).getId()).isEqualTo(poTest1.getId());
        assertThat(allProducts.get(0).getProductName()).isEqualTo(poTest1.getProductName());
        assertThat(allProducts.get(0).getUnitPrice()).isEqualTo(poTest1.getUnitPrice());
        assertThat(allProducts.get(0).getSellIndicator()).isEqualTo(poTest1.getSellIndicator());
        assertThat(allProducts.get(0).getState()).isEqualTo(poTest1.getState());

    }

    @Test
    @DisplayName("Get product offerings by Id")
    @Order(1)
    public void findById_shouldReturnProductOfferingById() {
        // set up
        Optional<ProductOffering> knownPO = Optional.of(poTest1);
        when(productRepo.findById(anyInt())).thenReturn(knownPO);

        // execute
        ProductOffering poDatabase = serviceUnderTest.findById(testId);

        // assert
        assertThat(poDatabase).isNotNull();
        assertThat(poDatabase.getId()).isEqualTo(knownPO.get().getId());
        assertThat(poDatabase.getProductName()).isEqualTo(knownPO.get().getProductName());
        assertThat(poDatabase.getUnitPrice()).isEqualTo(knownPO.get().getUnitPrice());
        assertThat(poDatabase.getSellIndicator()).isEqualTo(knownPO.get().getSellIndicator());
        assertThat(poDatabase.getState()).isEqualTo(knownPO.get().getState());

        // verify
        verify(productRepo).findById(anyInt());

    }

    @Test
    @DisplayName("(Exception) Get product offering by Id - Id not found")
    @Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() {
        //set up
        when(productRepo.findById(anyInt())).thenThrow(new ResourceNotFoundException(testId));
        //execute and assert
        assertThatThrownBy(() -> serviceUnderTest.findById(anyInt())).isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("Insert a new product offering")
    @Order(3)
    public void insert_shouldCreateNewProductOffering() {
        // set up
        when(productRepo.save(any())).thenReturn(poTest1);

        // execute
        ProductOffering newProductOfferring = serviceUnderTest.insert(poTest1);

        // assert
        assertThat(newProductOfferring).isNotNull();
        assertThat(newProductOfferring.getClass()).isEqualTo(ProductOffering.class);
        assertThat(newProductOfferring.getId()).isEqualTo(poTest1.getId());
        assertThat(newProductOfferring.getProductName()).isEqualTo(poTest1.getProductName());
        assertThat(newProductOfferring.getUnitPrice()).isEqualTo(poTest1.getUnitPrice());
        assertThat(newProductOfferring.getSellIndicator()).isEqualTo(poTest1.getSellIndicator());
        assertThat(newProductOfferring.getState()).isEqualTo(poTest1.getState());

        // verify
        verify(productRepo).save(poTest1);
    }

    @Test
    @DisplayName("(Exception) Insert a new product offering - Unexpected Exception")
    @Order(4)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(productRepo.save(any())).thenThrow(RuntimeException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(poTest1)).isInstanceOf(UnexpectedException.class);

    }

    @Test
    @DisplayName("Delete product offering by Id")
    @Order(5)
    public void deleteById_shouldDeleteProductOfferingById() {
        // set up
        doNothing().when(productRepo).deleteById(anyInt());

        // execute
        serviceUnderTest.deleteById(testId);

        // verify
        verify(productRepo, times(1)).deleteById(testId);
    }

    @Test
    @DisplayName("(Exception) Delete product offering by Id - Id not found")
    @Order(6)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        doThrow(EmptyResultDataAccessException.class).when(productRepo).deleteById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Delete product offering by Id - Unexpected Exception")
    @Order(7)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        doThrow(RuntimeException.class).when(productRepo).deleteById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Update product offering")
    @Order(8)
    public void update_shouldUpdateProductOffering() {
        // set up
        ProductOffering newProductOffering = new ProductOffering(null, "New PO1 Name", 150000.0, null, POState.Active);
        when(productRepo.getReferenceById(anyInt())).thenReturn(poTest1);
        when(productRepo.save(any())).thenReturn(newProductOffering);

        // execute
        ProductOffering updatedProductOffering = serviceUnderTest.update(testId, newProductOffering);

        // assert
        assertThat(updatedProductOffering).isNotNull();
        assertThat(updatedProductOffering.getId()).isEqualTo(newProductOffering.getId());
        assertThat(updatedProductOffering.getProductName()).isEqualTo(newProductOffering.getProductName());
        assertThat(updatedProductOffering.getUnitPrice()).isEqualTo(newProductOffering.getUnitPrice());
        assertThat(updatedProductOffering.getSellIndicator()).isEqualTo(newProductOffering.getSellIndicator());
        assertThat(updatedProductOffering.getState()).isEqualTo(newProductOffering.getState());
    }

    @Test
    @DisplayName("(Exception) Update product offering - Id not found")
    @Order(9)
    public void update_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        ProductOffering newProductOffering = new ProductOffering(null, "New PO1 Name", 150000.0, true, POState.Active);
        when(productRepo.getReferenceById(anyInt())).thenThrow(EntityNotFoundException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, newProductOffering))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Update product offering - Unexpected Exception")
    @Order(10)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        ProductOffering newProductOffering = new ProductOffering(null, "New PO1 Name", 150000.0, true, POState.Active);
        when(productRepo.getReferenceById(anyInt())).thenThrow(RuntimeException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, newProductOffering))
                .isInstanceOf(UnexpectedException.class);
    }

}
