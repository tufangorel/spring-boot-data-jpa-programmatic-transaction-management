package com.company.customerinfo.mapper;


import com.company.customerinfo.dto.CustomerDTO;
import com.company.customerinfo.model.Customer;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDTO toCustomerDTO(Customer customer);

    Customer customerDTOToCustomer(CustomerDTO customerDTO);

    Iterable<CustomerDTO> customersToCustomerAllDtos(Iterable<Customer> customers);
}
