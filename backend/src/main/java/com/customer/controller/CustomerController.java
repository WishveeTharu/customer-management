package com.customer.controller;

import com.customer.dto.CustomerDto;
import com.customer.dto.PageResponse;
import com.customer.service.BulkCustomerService;
import com.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final BulkCustomerService bulkCustomerService;

    @PostMapping
    public ResponseEntity<CustomerDto.Response> create(
            @Valid @RequestBody CustomerDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDto.Request request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CustomerDto.Summary>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(customerService.getAll(search, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // @PostMapping("/bulk-upload")
    // public ResponseEntity<CustomerDto.BulkResult> bulkUpload(
    //         @RequestParam("file") MultipartFile file) throws IOException {
    //     if (file.isEmpty()) {
    //         return ResponseEntity.badRequest().build();
    //     }
    //     return ResponseEntity.ok(bulkCustomerService.processBulkUpload(file));
    // }

    @PostMapping("/bulk-upload")
    public ResponseEntity<CustomerDto.BulkResult> bulkUpload(
            @RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bulkCustomerService.processBulkUpload(file));
    }
}