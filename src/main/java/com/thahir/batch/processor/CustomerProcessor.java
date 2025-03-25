package com.thahir.batch.processor;


import com.thahir.batch.model.Customer;
import com.thahir.batch.model.FormattedCustomer;
import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer, FormattedCustomer> {

    @Override
    public FormattedCustomer process(Customer customer) {
        // Skip processing for specific records if needed
        if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
            return null; // Returning null causes this item to be skipped
        }

        // Create formatted output object
        FormattedCustomer formattedCustomer = new FormattedCustomer();
        
        // Format ID - ensure it fits in 10 characters
        formattedCustomer.setId(String.format("%010d", customer.getId()));
        
        // Format names - capitalize first letter, lowercase rest, truncate if needed
        formattedCustomer.setFirstName(formatName(customer.getFirstName(), 20));
        formattedCustomer.setLastName(formatName(customer.getLastName(), 20));
        
        // Format email - convert to lowercase, truncate if needed
        String email = customer.getEmail();
        if (email != null) {
            email = email.toLowerCase().trim();
            if (email.length() > 30) {
                email = email.substring(0, 30);
            }
        }
        formattedCustomer.setEmail(email);
        
        // Format phone - standardize format, remove non-numeric characters
        String phone = customer.getPhoneNumber();
        if (phone != null) {
            phone = phone.replaceAll("[^0-9]", "");
            if (phone.length() > 15) {
                phone = phone.substring(0, 15);
            }
        }
        formattedCustomer.setPhoneNumber(phone);
        
        return formattedCustomer;
    }
    
    private String formatName(String name, int maxLength) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        
        name = name.trim();
        // Capitalize first letter, lowercase the rest
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        
        // Truncate if longer than maxLength
        if (name.length() > maxLength) {
            name = name.substring(0, maxLength);
        }
        
        return name;
    }
}