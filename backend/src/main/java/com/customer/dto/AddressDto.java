package com.customer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AddressDto {

    @Data @NoArgsConstructor
    public static class Request {
        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;

        private String addressLine2;

        @NotNull(message = "City is required")
        private Long cityId;

        @NotNull(message = "Country is required")
        private Long countryId;
    }

    @Data @NoArgsConstructor
    public static class Response {
        private Long id;
        private String addressLine1;
        private String addressLine2;
        private String cityName;
        private String countryName;
        private Long cityId;
        private Long countryId;
    }
}