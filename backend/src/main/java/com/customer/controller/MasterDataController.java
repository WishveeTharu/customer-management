package com.customer.controller;

import com.customer.model.City;
import com.customer.model.Country;
import com.customer.repository.CityRepository;
import com.customer.repository.CountryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterDataController {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    @GetMapping("/countries")
    public ResponseEntity<List<CountryDto>> getCountries() {
        List<CountryDto> list = countryRepository.findAll().stream()
            .map(c -> new CountryDto(c.getId(), c.getName(), c.getCode()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CityDto>> getCities(
            @RequestParam(required = false) Long countryId) {
        List<City> cities = countryId != null
            ? cityRepository.findByCountryId(countryId)
            : cityRepository.findAll();
        List<CityDto> list = cities.stream()
            .map(c -> new CityDto(c.getId(), c.getName()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Data
    static class CountryDto {
        private final Long id;
        private final String name;
        private final String code;
    }

    @Data
    static class CityDto {
        private final Long id;
        private final String name;
    }
}