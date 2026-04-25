package com.customer.repository;

import com.customer.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByNicNumber(String nicNumber);

    boolean existsByNicNumberAndIdNot(String nicNumber, Long id);

    Optional<Customer> findByNicNumber(String nicNumber);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.nicNumber LIKE CONCAT('%', :search, '%'))")
    Page<Customer> findAllSummary(@Param("search") String search, Pageable pageable);

    @EntityGraph(attributePaths = {"mobileNumbers", "addresses", "addresses.city",
                                   "addresses.country", "familyMembers"})
    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT c FROM Customer c WHERE c.nicNumber IN :nics")
    List<Customer> findAllByNicNumberIn(@Param("nics") List<String> nics);
}