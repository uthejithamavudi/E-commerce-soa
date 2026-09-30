package com.marketgrid.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Page<Order> findByCustomerUserId(String customerUserId, Pageable pageable);

    @Query("{ 'subOrders.vendorId': ?0 }")
    List<Order> findOrdersContainingVendor(String vendorId);
}
