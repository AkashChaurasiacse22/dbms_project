package com.store_management_system.sms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import com.store_management_system.sms.model.*;
import com.store_management_system.sms.repository.CustomerRepository;
import com.store_management_system.sms.repository.InventoryRepository;
import com.store_management_system.sms.repository.OrderRepository;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import com.store_management_system.sms.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class OrderController {
    @Autowired
    private UserService userService;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/order/{productId}/{storeId}")
    public String createOrder(@PathVariable Integer productId,@PathVariable Integer storeId, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        
        try {
            Order order = new Order();
            order.setEmployeeId(currentUser.getEmployeeId());
            order.setProductId((long)productId);
            order.setStoreId((long)storeId);
            // System.err.println(order.getProductId()+"aaaaa"+order.getStoreId()+"aaaaa"+order.getEmployeeId());
            order.setOdate(LocalDate.now());
            order.setPrice(inventoryRepository.getPriceByProductId((long)productId));
    
            // Fetch all customer details and add to model
            List<Customer> customers = customerRepository.findAll();
            model.addAttribute("customers", customers);
    
            model.addAttribute("order", order);
            return "order";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Something went wrong. Please try again later: "  );
            model.addAttribute("currentUser", currentUser);
            return "order";
        }
    }

    @PostMapping("/order/create")
    public String postCreateOrder(Model model,@AuthenticationPrincipal UserDetails userDetails,@ModelAttribute Order order) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        
        try {
            model.addAttribute("currentUser", currentUser);
            Long quantity=order.getQuantity();
            Long customerId=order.getCustomerId();
            // Long inventoryId=order.getInventoryId();
            // System.err.println(order.getProductId()+"bbbbb"+order.getStoreId()+"bbbbbb"+order.getEmployeeId());
            List<Inventory> inventories=inventoryRepository.findByProductAndStoreId(order.getProductId(),order.getStoreId());
            if(inventories.size()==0){
                model.addAttribute("errorMessage", "Incorrect details. ");
                
            model.addAttribute("order", order);
            model.addAttribute("currentUser", currentUser);
           return "order";
            }
            Inventory inventory=inventories.get(0);
            Long qmax=inventory.getQuantity();

            if(customerRepository.findById(customerId)==null || quantity==null || quantity>qmax){
                
                model.addAttribute("errorMessage", "Incorrect Details. ");
                
            model.addAttribute("order", order);
            model.addAttribute("currentUser", currentUser);
           return "order";
            }
            inventory.setQuantity(qmax-quantity);
            inventoryRepository.update(inventory);
            orderRepository.save(order);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Incorrect Details.." );
            model.addAttribute("order", order);
            model.addAttribute("currentUser", currentUser);
           return "order";
        }
        
        return "redirect:/customers";
    }
    
    @PostMapping("/delete/order/{orderId}/{customerId}")
    public String deleteOrder(Model model,@PathVariable Long orderId, @PathVariable Long customerId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Order order=orderRepository.findById(orderId);
            Inventory inventory=inventoryRepository.findByProductAndStoreId(order.getProductId(),order.getStoreId()).get(0);
            Customer customer=customerRepository.findById(customerId);
            Double customerAccount=customer.getAccount();
            
            
            customer.setAccount(customerAccount-order.getPrice()*order.getQuantity()+order.getPayment());
            Long q=inventory.getQuantity();
            inventory.setQuantity(q+order.getQuantity());
            customerRepository.save(customer);
            inventoryRepository.update(inventory);
            orderRepository.deleteById(orderId);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to delete." );
            return "error";
        }
        return "redirect:/customer/orders/" + customerId;
    }
    
}
