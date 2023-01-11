package fr.univcotedazur.simpletcfs.order.repositories;

import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import fr.univcotedazur.simpletcfs.order.entities.Order;
import fr.univcotedazur.simpletcfs.order.entities.OrderStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.customer = :customer and o.status = :status")
    public List<Order> findOrdersForCustomerWithStatus(
            @Param("customer") Customer customer,
            @Param("status") OrderStatus state,
            Sort sort);

}
