package com.store_management_system.sms.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.store_management_system.sms.model.*;
import com.store_management_system.sms.repository.CustomerRepository;
import com.store_management_system.sms.repository.InventoryRepository;
import com.store_management_system.sms.repository.OrderRepository;
import com.store_management_system.sms.repository.ReturnRepository;
import com.store_management_system.sms.service.UserService;

@Controller
public class ReturnController {
    @Autowired
    private ReturnRepository returnRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserService userService;

    @GetMapping("return/create/{id}")
    public String createReturn(Model model,@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
           Return return1 =new Return();
           return1.setOrderId(id);
           LocalDate currdate=LocalDate.now();
           return1.setRdate(currdate);
            model.addAttribute("return1", return1);
            return "createReturn";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to get create return statement page." );
            Order order=orderRepository.findById(id);
            return "redirect:/customer/orders/" + order.getCustomerId();
        }
        
    }
    @PostMapping("return/create")
    public String postCreateReturn(Model model,@ModelAttribute Return return1, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            
            Long q=return1.getQuantity();
            Double newprice=return1.getPrice();
            Order order=orderRepository.findById(return1.getOrderId());
            if(order.getQuantity()<q){
                model.addAttribute("errorMessage", "Incorrect quantity..");
                model.addAttribute("return1", return1);
            return "createReturn";
            }
            Double oldprice=order.getPrice();
            Inventory inventory=inventoryRepository.findByProductAndStoreId(order.getProductId(),order.getStoreId()).get(0);
            Customer customer=customerRepository.findById(order.getCustomerId());
            inventory.setQuantity(inventory.getQuantity()+q);
            inventoryRepository.update(inventory);
            customer.setAccount(customer.getAccount()-(oldprice)*q+newprice);
            customerRepository.save(customer);

            returnRepository.save(return1);
            return "redirect:/customer/orders/" + order.getCustomerId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to create return statement " );
            model.addAttribute("return1", return1);
            return "createReturn";
        }
    }

    @GetMapping("return/view/{id}")
    public String viewReturn(Model model,@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Return return1=returnRepository.findByOrderId(id);
            model.addAttribute("return1", return1);

            return "viewreturn";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to get return statement page." );
            Order order=orderRepository.findById(id);
            return "redirect:/customer/orders/" + order.getCustomerId();
        }
    }

    @PostMapping("update/return/{id}")
    public String updateReturn(Model model,@PathVariable Long id,@ModelAttribute Return return1, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Return returnold=returnRepository.findByOrderId(return1.getOrderId());
            Long qold=returnold.getQuantity();
            Double oldprice=returnold.getPrice();
            Long qnew=return1.getQuantity();
            Double newprice=return1.getPrice();
            Order order=orderRepository.findById(return1.getOrderId());
            Double orderprice=order.getPrice();
            if(order.getQuantity()<qnew){
                model.addAttribute("errorMessage", "Incorrect quantity..");
                model.addAttribute("return1", return1);
            return "createReturn";
            }
            Inventory inventory=inventoryRepository.findByProductAndStoreId(order.getProductId(),order.getStoreId()).get(0);
            Customer customer=customerRepository.findById(order.getCustomerId());
            inventory.setQuantity(inventory.getQuantity()-qold+qnew);
            inventoryRepository.update(inventory);
            customer.setAccount(customer.getAccount()+(orderprice*qold)-(orderprice*qnew)+(newprice-oldprice));
            customerRepository.save(customer);

            returnRepository.save(return1);
            return "redirect:/customer/orders/" + order.getCustomerId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to update return statement page." );
            model.addAttribute("return1",return1);
            return "viewreturn";
        }
    }

    @PostMapping("/delete/return/{id}")
    public String deleteReturn(Model model,@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Return return1=returnRepository.findByOrderId(id);
            Order order=orderRepository.findById(return1.getOrderId());
            Inventory inventory=inventoryRepository.findByProductAndStoreId(order.getProductId(),order.getStoreId()).get(0);
            Customer customer=customerRepository.findById(order.getCustomerId());
            inventory.setQuantity(inventory.getQuantity()-return1.getQuantity());
            inventoryRepository.update(inventory);
            customer.setAccount(customer.getAccount()+(-return1.getPrice()+order.getPrice()*return1.getQuantity()));
            customerRepository.save(customer);

            returnRepository.deleteByOrderId(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to delete." );
        
        }
        
        Order order=orderRepository.findById(id);
        return "redirect:/customer/orders/" + order.getCustomerId();
    }
}