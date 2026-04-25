package com.customer.service;

import com.customer.dto.AddressDto;
import com.customer.dto.CustomerDto;
import com.customer.dto.PageResponse;
import com.customer.model.*;
import com.customer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    @Transactional
    public CustomerDto.Response create(CustomerDto.Request req) {
        if (customerRepository.existsByNicNumber(req.getNicNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Customer with NIC " + req.getNicNumber() + " already exists");
        }
        Customer customer = buildCustomer(new Customer(), req);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDto.Response update(Long id, CustomerDto.Request req) {
        Customer customer = customerRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        if (customerRepository.existsByNicNumberAndIdNot(req.getNicNumber(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "NIC " + req.getNicNumber() + " is already used by another customer");
        }

        customer.getMobileNumbers().clear();
        customer.getAddresses().clear();
        customer.getFamilyMembers().clear();
        buildCustomer(customer, req);
        return toResponse(customerRepository.save(customer));
    }

    // @Transactional(readOnly = true)
    // public CustomerDto.Response getById(Long id) {
    //     Customer customer = customerRepository.findByIdWithDetails(id)
    //         .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    //     return toResponse(customer);
    // }

    @Transactional(readOnly = true)
    public CustomerDto.Response getById(Long id) {
        Customer customer = customerRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerDto.Summary> getAll(String search, int page, int size) {
        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<Customer> pageResult = customerRepository.findAllSummary(
            searchParam,
            PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"))
        );
        List<CustomerDto.Summary> summaries = pageResult.getContent().stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
        return new PageResponse<>(summaries, page, size, pageResult.getTotalElements());
    }

    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        customerRepository.deleteById(id);
    }

    private Customer buildCustomer(Customer customer, CustomerDto.Request req) {
        customer.setName(req.getName());
        customer.setDateOfBirth(req.getDateOfBirth());
        customer.setNicNumber(req.getNicNumber());

        if (req.getMobileNumbers() != null) {
            req.getMobileNumbers().forEach(mobile -> {
                CustomerMobile cm = new CustomerMobile();
                cm.setCustomer(customer);
                cm.setMobile(mobile);
                customer.getMobileNumbers().add(cm);
            });
        }

        if (req.getAddresses() != null) {
            req.getAddresses().forEach(addrReq -> {
                City city = cityRepository.findById(addrReq.getCityId())
                    .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "City not found: " + addrReq.getCityId()));
                Country country = countryRepository.findById(addrReq.getCountryId())
                    .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Country not found: " + addrReq.getCountryId()));
                CustomerAddress addr = new CustomerAddress();
                addr.setCustomer(customer);
                addr.setAddressLine1(addrReq.getAddressLine1());
                addr.setAddressLine2(addrReq.getAddressLine2());
                addr.setCity(city);
                addr.setCountry(country);
                customer.getAddresses().add(addr);
            });
        }

        if (req.getFamilyMemberIds() != null && !req.getFamilyMemberIds().isEmpty()) {
            List<Customer> members = customerRepository.findAllById(req.getFamilyMemberIds());
            customer.getFamilyMembers().addAll(members);
        }

        return customer;
    }

    public CustomerDto.Response toResponse(Customer c) {
        CustomerDto.Response res = new CustomerDto.Response();
        res.setId(c.getId());
        res.setName(c.getName());
        res.setDateOfBirth(c.getDateOfBirth());
        res.setNicNumber(c.getNicNumber());
        res.setCreatedAt(c.getCreatedAt());
        res.setUpdatedAt(c.getUpdatedAt());
        res.setMobileNumbers(c.getMobileNumbers().stream()
            .map(CustomerMobile::getMobile).collect(Collectors.toList()));
        res.setAddresses(c.getAddresses().stream()
            .map(this::toAddressResponse).collect(Collectors.toList()));
        res.setFamilyMembers(c.getFamilyMembers().stream()
            .map(fm -> {
                CustomerDto.FamilyMemberDto dto = new CustomerDto.FamilyMemberDto();
                dto.setId(fm.getId());
                dto.setName(fm.getName());
                dto.setNicNumber(fm.getNicNumber());
                return dto;
            }).collect(Collectors.toList()));
        return res;
    }

    // private AddressDto.Response toAddressResponse(CustomerAddress addr) {
    //     AddressDto.Response res = new AddressDto.Response();
    //     res.setId(addr.getId());
    //     res.setAddressLine1(addr.getAddressLine1());
    //     res.setAddressLine2(addr.getAddressLine2());
    //     res.setCityId(addr.getCity().getId());
    //     res.setCityName(addr.getCity().getName());
    //     res.setCountryId(addr.getCountry().getId());
    //     res.setCountryName(addr.getCountry().getName());
    //     return res;
    // }

    private AddressDto.Response toAddressResponse(CustomerAddress addr) {
    AddressDto.Response res = new AddressDto.Response();
    res.setId(addr.getId());
    res.setAddressLine1(addr.getAddressLine1());
    res.setAddressLine2(addr.getAddressLine2());
    try {
        res.setCityId(addr.getCity().getId());
        res.setCityName(addr.getCity().getName());
        res.setCountryId(addr.getCountry().getId());
        res.setCountryName(addr.getCountry().getName());
    } catch (Exception e) {
        // lazy load issue fallback
    }
    return res;
}

    public CustomerDto.Summary toSummary(Customer c) {
        CustomerDto.Summary s = new CustomerDto.Summary();
        s.setId(c.getId());
        s.setName(c.getName());
        s.setDateOfBirth(c.getDateOfBirth());
        s.setNicNumber(c.getNicNumber());
        s.setCreatedAt(c.getCreatedAt());
        if (!c.getMobileNumbers().isEmpty()) {
            s.setPrimaryMobile(c.getMobileNumbers().iterator().next().getMobile());
        }
        return s;
    }

    public Map<String, Customer> findByNicsAsMap(List<String> nics) {
        return customerRepository.findAllByNicNumberIn(nics).stream()
            .collect(Collectors.toMap(Customer::getNicNumber, Function.identity()));
    }
}