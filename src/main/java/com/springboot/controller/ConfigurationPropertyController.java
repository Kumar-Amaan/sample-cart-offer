package com.springboot.controller;

import com.springboot.property.ComplexProperty;
import com.springboot.property.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/confProperty")
public class ConfigurationPropertyController {

	@Autowired
	private ComplexProperty complexProperty;

	@GetMapping
	public List<Property> fetchConfigurationProperties() {
		return complexProperty.getProperty();
	}
}