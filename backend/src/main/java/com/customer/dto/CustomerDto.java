package com.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerDto {

    @Data @NoArgsConstructor
    public static class Request {
        @NotBlank(message = "Name is required")
        private String name;

        @NotNull(message = "Date of birth is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateOfBirth;

        @NotBlank(message = "NIC number is required")
        private String nicNumber;

        private List<String> mobileNumbers = new ArrayList<>();

        @Valid
        private List<AddressDto.Request> addresses = new ArrayList<>();

        private List<Long> familyMemberIds = new ArrayList<>();
    }

    @Data @NoArgsConstructor
    public static class Response {
        private Long id;
        private String name;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateOfBirth;

        private String nicNumber;
        private List<String> mobileNumbers = new ArrayList<>();
        private List<AddressDto.Response> addresses = new ArrayList<>();
        private List<FamilyMemberDto> familyMembers = new ArrayList<>();

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
    }

    @Data @NoArgsConstructor
    public static class Summary {
        private Long id;
        private String name;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateOfBirth;

        private String nicNumber;
        private String primaryMobile;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor
    public static class FamilyMemberDto {
        private Long id;
        private String name;
        private String nicNumber;
    }

    @Data @NoArgsConstructor
    public static class BulkResult {
        private int totalRows;
        private int successCount;
        private int errorCount;
        private List<String> errors = new ArrayList<>();
    }
}