package com.julio.rampUp.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.ItemsSum;
import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.OrderItem;
import com.julio.rampUp.entities.ProductOffering;
import com.julio.rampUp.entities.dto.OrderDTO;
import com.julio.rampUp.entities.dto.OrderItemDTO;
import com.julio.rampUp.repositories.AddressRepository;
import com.julio.rampUp.repositories.CustomerRepository;
import com.julio.rampUp.repositories.OrderItemRepository;
import com.julio.rampUp.repositories.OrderRepository;
import com.julio.rampUp.repositories.ProductOfferingRepository;
import com.julio.rampUp.services.exceptions.IdNullException;
import com.julio.rampUp.services.exceptions.NoValueForIdException;
import com.julio.rampUp.services.exceptions.NotForSaleException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ProductOfferingRepository productOfferingRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public List<OrderDTO> findAll(int page) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, 6, sort);
        return orderToDTO(repository.findAll(pageable).toList());
    }

    private List<OrderDTO> orderToDTO(List<Order> orders) {
        List<OrderDTO> ordersDTO = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO(order);
            orderDTO.setCustomerId(order.getCustomer().getId());
            orderDTO.setDeliveryId(order.getDeliveryAddress().getId());
            ordersDTO.add(orderDTO);
        }
        return ordersDTO;
    }

    public Order findById(Integer id) {
        Optional<Order> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public List<Order> findAllOrdersByCustomerId(int page, Integer id) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 2, sort);
        return repository.findAllOrdersByCustomerId(pageable, id).toList();
    }

    public Order insert(OrderDTO orderDTO) {
        try {
            Optional<Customer> customerDB = customerRepository.findById(orderDTO.getCustomerId());
            Customer customer = customerDB.get();

            Optional<Address> addressDB = addressRepository.findById(orderDTO.getDeliveryId());
            Address address = addressDB.get();

            Order order = builder(orderDTO, customer, address);
            repository.save(order);
            customer.addOrder(order);
            customerRepository.save(customer);

            for (OrderItemDTO product : orderDTO.getOrderItemDTO()) {
                Optional<ProductOffering> productDB = productOfferingRepository.findById(product.getProductId());
                ProductOffering productOffering = productDB.get();
                if (!productOffering.getSellIndicator()) {
                    throw new NotForSaleException(productOffering.getId());
                }
                OrderItem orderItem = new OrderItem(order, productOffering, product.getDiscount(),
                        product.getQuantity());
                orderItemRepository.save(orderItem);

            }

            return repository.save(order);
        } catch (NoSuchElementException e) {
            throw new NoValueForIdException("Customer", "Address", orderDTO.getCustomerId(), orderDTO.getDeliveryId());
        } catch (InvalidDataAccessApiUsageException e) {
            throw new IdNullException("Customer or Address");
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

    public Order update(Integer id, Order newOrder) {
        try {
            Order orderToUpdate = repository.getReferenceById(id);
            updateData(orderToUpdate, newOrder);
            return repository.save(orderToUpdate);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public List<Integer> ordersInfo() {
        List<Integer> orderInfo = new ArrayList<>();
        orderInfo.add(repository.quantityOfOrders());
        orderInfo.add(repository.quantityOfCancelledOrders());
        return orderInfo;
    }

    public List<ItemsSum> quantitySoldItems() {
        List<Object[]> soldItemObject = orderItemRepository.quantitySoldItems();
        List<ItemsSum> soldItems = new ArrayList<>();
        if (soldItemObject != null && !soldItemObject.isEmpty()) {
            for (Object[] soldItem : soldItemObject) {
                soldItems.add(new ItemsSum(Integer.parseInt(soldItem[0].toString()),
                        Integer.parseInt(soldItem[2].toString()), soldItem[1].toString()));
            }
        }
        List<Object[]> notSoldItemObject = orderItemRepository
                .quantityNotSoldItems(soldItems.stream().map(item -> item.getProd_id()).collect(Collectors.toList()));
        if (notSoldItemObject != null && !notSoldItemObject.isEmpty()) {
            for (Object[] notSoldItem : notSoldItemObject) {
                soldItems.add(new ItemsSum(Integer.parseInt(notSoldItem[0].toString()),
                        Integer.parseInt(notSoldItem[2].toString()), notSoldItem[1].toString()));
            }
        }
        return soldItems;
    }

    private void updateData(Order orderToUpdate, Order newOrder) {
        if (newOrder.getDeliveryAddress().getId() == orderToUpdate.getDeliveryAddress().getId()) {
            for (Address address : orderToUpdate.getCustomer().getAddresses()) {
                if (address.getId() == newOrder.getDeliveryAddress().getId()) {
                    address = newOrder.getDeliveryAddress();
                    addressService.update(newOrder.getCustomer().getId(), newOrder.getDeliveryAddress().getId(),
                            address);
                }
            }
            orderToUpdate.setDeliveryAddress(newOrder.getDeliveryAddress());
        }
    }

    private Order builder(OrderDTO orderDTO, Customer customer, Address address) {
        Order order = new Order(orderDTO.getId(), Instant.now(), customer, address);
        return order;
    }

}
