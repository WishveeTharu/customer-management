package com.customer;

import com.customer.dto.CustomerDto;
import com.customer.dto.PageResponse;
import com.customer.model.Customer;
import com.customer.repository.CityRepository;
import com.customer.repository.CountryRepository;
import com.customer.repository.CustomerRepository;
import com.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerDto.Request validRequest;
    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        validRequest = new CustomerDto.Request();
        validRequest.setName("Tharu Perera");
        validRequest.setDateOfBirth(LocalDate.of(1998, 5, 15));
        validRequest.setNicNumber("199812345678");

        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setName("Tharu Perera");
        sampleCustomer.setDateOfBirth(LocalDate.of(1998, 5, 15));
        sampleCustomer.setNicNumber("199812345678");
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Test
    void createCustomer_success() {
        when(customerRepository.existsByNicNumber(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        CustomerDto.Response response = customerService.create(validRequest);

        assertNotNull(response);
        assertEquals("Tharu Perera", response.getName());
        assertEquals("199812345678", response.getNicNumber());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_duplicateNic_throwsConflict() {
        when(customerRepository.existsByNicNumber(anyString())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> customerService.create(validRequest));

        assertEquals(409, ex.getStatus().value());
        verify(customerRepository, never()).save(any());
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────

    @Test
    void getById_success() {
        when(customerRepository.findByIdWithDetails(1L))
            .thenReturn(Optional.of(sampleCustomer));

        CustomerDto.Response response = customerService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Tharu Perera", response.getName());
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(customerRepository.findByIdWithDetails(99L))
            .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> customerService.getById(99L));

        assertEquals(404, ex.getStatus().value());
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsPagedResults() {
        when(customerRepository.findAllSummary(isNull(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(sampleCustomer)));

        PageResponse<CustomerDto.Summary> result =
            customerService.getAll(null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAll_withSearch_returnsFilteredResults() {
        when(customerRepository.findAllSummary(eq("Tharu"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(sampleCustomer)));

        PageResponse<CustomerDto.Summary> result =
            customerService.getAll("Tharu", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Tharu Perera", result.getContent().get(0).getName());
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Test
    void updateCustomer_success() {
        when(customerRepository.findByIdWithDetails(1L))
            .thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.existsByNicNumberAndIdNot(anyString(), anyLong()))
            .thenReturn(false);
        when(customerRepository.save(any(Customer.class)))
            .thenReturn(sampleCustomer);

        validRequest.setName("Tharu Updated");
        CustomerDto.Response response = customerService.update(1L, validRequest);

        assertNotNull(response);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_notFound_throwsNotFound() {
        when(customerRepository.findByIdWithDetails(99L))
            .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> customerService.update(99L, validRequest));

        assertEquals(404, ex.getStatus().value());
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    void deleteCustomer_success() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        assertDoesNotThrow(() -> customerService.delete(1L));
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCustomer_notFound_throwsNotFound() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> customerService.delete(99L));

        assertEquals(404, ex.getStatus().value());
    }
}