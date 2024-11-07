package com.store_management_system.sms.controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import com.store_management_system.sms.repository.CustomerRepository;
import com.store_management_system.sms.repository.CustomerPaymentRepository;
import com.store_management_system.sms.repository.OrderRepository;

import com.store_management_system.sms.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.store_management_system.sms.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class CustomerController {
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    UserService userService;

    @Autowired
    CustomerPaymentRepository customerPaymentRepository;
    @Autowired
    OrderRepository orderRepository;

    @GetMapping("/customers")
    public String getCustomers(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            List<Customer> customers=customerRepository.findAll();
            model.addAttribute("customers",customers);
            model.addAttribute("count", 5);
            
            return "customers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error fetching all customers..." );
            model.addAttribute("count", 5);
            
            return "customers";
        }
    }
    

    @GetMapping("/create/customer")
    public String getCreateCustomer(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Customer customer=new Customer();
            customer.setAccount((double)0);
            if(customer.getEmails()==null){
                customer.setEmails(new ArrayList<>());
            }
            for (int i = customer.getEmails().size(); i < 5; i++) {
                customer.getEmails().add(new CustomerMail());
            }
            model.addAttribute("customer",customer);
            return "createCustomer";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create Customers. " );
            return "createCustomer";
        }
        
    }
    @PostMapping("/create/customer")
    public String postCreateCustomer(@ModelAttribute Customer customer,Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            if(customer.getEmails()!=null){
                customer.getEmails().removeIf(email -> email.getCustomerEmail() == null || email.getCustomerEmail().isEmpty());
            }
            Long phone_no = customer.getPhoneNo();
            if(phone_no!=null && customerRepository.findByPhoneNo(phone_no)!=null){
                model.addAttribute("errorMessage", "Duplicate phone number");
                model.addAttribute("customer", customer);
                return "createCustomer";
            }
            customerRepository.save(customer);
            return "redirect:/customers";
        } catch (Exception e) {
            // model.addAttribute("errorMessage", "Failed to create Customers. " );
            model.addAttribute("errorMessage", "Failed to create Customers. "+"Duplicate value of phone number or email");
            return "createCustomer";
        }
    }

    @GetMapping("/view/customer/{id}")
    public String getViewCustomer(@PathVariable Long id,Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Customer customer = customerRepository.findById(id);
                if (customer == null) {
                    model.addAttribute("errorMessage", "This customer does not exist...");
                    model.addAttribute("customer", customer);
                    return "viewcustomer";
                }
                if(customer.getEmails()==null){
                    customer.setEmails(new ArrayList<>());
                }
                for (int i = customer.getEmails().size(); i < 5; i++) {
                    customer.getEmails().add(new CustomerMail());
                }
            model.addAttribute("customer", customer);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to fetch customer details...");
            
            return "viewcustomer";
        }
        return "viewcustomer";
    }

    @PostMapping("/update/customer/{id}")
    public String postUpdateCustomer(Model model,@ModelAttribute Customer customer,@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

            if(currentUser.getRoleId().equals(  1L) || currentUser.getRoleId().equals(2L)){
                if(customer.getEmails()!=null){
                    customer.getEmails().removeIf(email -> email.getCustomerEmail() == null || email.getCustomerEmail().isEmpty());
                }
                Long phone_no = customer.getPhoneNo();
                if(phone_no!=null && customerRepository.findByPhoneNo(phone_no)!=null){
                    model.addAttribute("errorMessage", "Duplicate phone number");
                    model.addAttribute("customer", customer);
                    return "createCustomer";
                }
                customerRepository.save(customer);
            }else{
                return "error/403";
            }
            
            return "redirect:/view/customer/{id}";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update customer details.. " );
            model.addAttribute("customer", customer);
            return "viewcustomer";
        }
        
    }

    @PostMapping("/delete/customer/{id}")
    public String deleteCustomer(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails,Model model) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

    try {
        if (!(currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("EMPLOYEE")))  ) {
            customerRepository.deleteById(id);
            return "redirect:/customers";
        } else { 
            return "error/403";
        }
        
        }catch(Exception e){
            model.addAttribute("errorMessage","Failed to delete the customer." );
            return "redirect:/customers";
        }
    
    }

    @PostMapping("/customer/{id}/pay")
    public String postCustomerPayment(@PathVariable Long id, @RequestParam Double paymentAmount, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

        try {
        // Fetch the customer by ID
            Customer customer = customerRepository.findById(id);
            if (customer == null) {
                model.addAttribute("errorMessage", "Customer not found");
                return "redirect:/customers"; // Redirect back to customers list with an error
            }

        // Record the payment in the customer_payment table
            CustomerPayment customerPayment = new CustomerPayment();
            customerPayment.setCustomerId(id); // Associate payment with customer
            customerPayment.setPaymentAmount(paymentAmount); // Set payment amount
            customerPayment.setPaymentDate(LocalDate.now()); // Set current date as payment date

        // Save payment record in customer_payment table
            customerPaymentRepository.save(customerPayment);

        // Update the customer's account balance
            Double currentBalance = customer.getAccount();
            Double updatedBalance = currentBalance - paymentAmount;
            customer.setAccount(updatedBalance);

        // Save the updated customer account balance in the database
            customerRepository.updateCustomerAccount(customer.getId(), updatedBalance);

            // Redirect to the main customers list page (or wherever you want)
            return "redirect:/customers"; // Redirect back to the customers list
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to process payment: "  );
            return "redirect:/customers"; // Redirect back to customers list with an error
        }
    }
    @GetMapping("/customer/payment/{id}")
    public String viewPaymentHistory(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        // Fetch the customer by ID
        try {
            Customer customer = customerRepository.findById(id);
            if (customer == null) {
                model.addAttribute("errorMessage", "Customer not found");
                return "redirect:/customers";
            }
            List<CustomerPayment> payments = customerPaymentRepository.findPaymentsByCustomerId(id);
            model.addAttribute("customer", customer);
            model.addAttribute("payments", payments);

        // Return the payment-history Thymeleaf view
            return "customerPayment"; 
        }
        catch (Exception e)
        {
            model.addAttribute("errorMessage", "Failed to show payment details: "  );
            return "redirect:/customers"; 
        }
    }
    @GetMapping("/customer/orders/{id}")
    public String viewSupplierBuy(@PathVariable Long id, Model model,  @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Customer customer = customerRepository.findById(id);
            if (customer == null) {
                model.addAttribute("errorMessage", "customer not found");
                return "redirect:/customers";
            }
            model.addAttribute("customer", customer);
            List<Order> order = orderRepository.findByCustomerId(id);
            model.addAttribute("customerorders", order);
            return "customerOrders";
        }
        catch (Exception e)
        {
            model.addAttribute("errorMessage", "Failed to show customer orders: "  );
            return "redirect:/customers"; 
        }
    }
    // @GetMapping("/search/customer/{phoneNo}")
    // public String getMethodName(@PathVariable String firstName) {
    //     return new String();
    // }
    
}
