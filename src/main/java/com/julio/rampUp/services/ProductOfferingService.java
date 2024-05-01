package com.julio.rampUp.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.julio.rampUp.entities.ProductOffering;
import com.julio.rampUp.repositories.ProductOfferingRepository;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@Service
public class ProductOfferingService {

    @Autowired
    private ProductOfferingRepository repository;

    public List<ProductOffering> findAll(int page) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 6, sort);
        return repository.findAll(pageable).toList();
    }

    public ProductOffering findById(Integer id) {
        Optional<ProductOffering> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public ProductOffering insert(ProductOffering productOffering) {
        try {
            return repository.save(productOffering);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public void deleteById(Integer id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public ProductOffering update(Integer id, ProductOffering newProductOffering) {
        try {
            ProductOffering updatedProductOffering = repository.getReferenceById(id);
            updateData(updatedProductOffering, newProductOffering);
            return repository.save(updatedProductOffering);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public Integer productToSale() {
        List<Object[]> count = repository.productToSale();
        if (count != null && !count.isEmpty()) {
            return Integer.parseInt(count.get(0)[0].toString());
        }
        return 0;
    }

    public int prodQuantity() {
        return (int) repository.count();
    }

    private void updateData(ProductOffering updatedProductOffering, ProductOffering newProductOffering) {
        updatedProductOffering.setProductName(newProductOffering.getProductName());
        updatedProductOffering.setUnitPrice(newProductOffering.getUnitPrice());
        updatedProductOffering.setSellIndicator(newProductOffering.getSellIndicator());
        updatedProductOffering.setState(newProductOffering.getState());
    }

}
