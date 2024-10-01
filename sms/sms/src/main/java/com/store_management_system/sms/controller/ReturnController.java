package com.store_management_system.sms.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.store_management_system.sms.model.*;
import com.store_management_system.sms.model.Customer;
import com.store_management_system.sms.model.Inventory;
import com.store_management_system.sms.model.Return;
import com.store_management_system.sms.repository.CustomerRepository;
import com.store_management_system.sms.repository.InventoryRepository;
import com.store_management_system.sms.repository.OrderRepository;
import com.store_management_system.sms.repository.ReturnRepository;

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

    @GetMapping("return/create/{id}")
    public String createReturn(Model model,@PathVariable Long id){
        try {
           Return return1 =new Return();
           return1.setOrderId(id);
           LocalDate currdate=LocalDate.now();
           return1.setRdate(currdate);
            model.addAttribute("return1", return1);
            return "createReturn";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to get create return statement page."+e.getMessage());
            return "redirect:/customers";
        }
        
    }
    @PostMapping("return/create")
    public String postCreateReturn(Model model,@ModelAttribute Return return1){
        try {
            
            Long q=return1.getQuantity();
            Double newprice=return1.getPrice();
            Order order=orderRepository.findById(return1.getOrderId());
            if(order.getQuantity()<q){
                model.addAttribute("errorMessage", "incorrect quantity..");
                model.addAttribute("return1", return1);
            return "createReturn";
            }
            Double oldprice=order.getPrice();
            Inventory inventory=inventoryRepository.findById(order.getInventoryId());
            Customer customer=customerRepository.findById(order.getCustomerId());
            inventory.setQuantity(inventory.getQuantity()+q);
            inventoryRepository.save(inventory);
            customer.setAccount(customer.getAccount()-(oldprice-newprice)*q);
            customerRepository.save(customer);

            returnRepository.save(return1);
            return "redirect:/customers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to create return statement "+e.getMessage());
            model.addAttribute("return1", return1);
            return "createReturn";
        }
    }

    @GetMapping("return/view/{id}")
    public String viewReturn(Model model,@PathVariable Long id){
        try {
            Return return1=returnRepository.findById(id);
            model.addAttribute("return1", return1);

            return "viewreturn";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to get  return statement page."+e.getMessage());
            return "redirect:/customers";
        }
    }

    @PostMapping("update/return/{id}")
    public String updateReturn(Model model,@PathVariable Long id,@ModelAttribute Return return1){
        try {
            Return returnold=returnRepository.findById(return1.getId());
            Long qold=returnold.getQuantity();
            Double oldprice=returnold.getPrice();
            Long qnew=return1.getQuantity();
            Double newprice=return1.getPrice();
            Order order=orderRepository.findById(return1.getOrderId());
            if(order.getQuantity()<qnew){
                model.addAttribute("errorMessage", "incorrect quantity..");
                model.addAttribute("return1", return1);
            return "createReturn";
            }
            Inventory inventory=inventoryRepository.findById(order.getInventoryId());
            Customer customer=customerRepository.findById(order.getCustomerId());
            inventory.setQuantity(inventory.getQuantity()-qold+qnew);
            inventoryRepository.save(inventory);
            customer.setAccount(customer.getAccount()+(oldprice*qold)-(newprice*qnew));
            customerRepository.save(customer);

            returnRepository.save(return1);
            return "redirect:/customers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to update return statement page."+e.getMessage());
            model.addAttribute("return1",return1);
            return "viewreturn";
        }
    }

    @PostMapping("/delete/return/{id}")
    public String deleteReturn(Model model,@PathVariable Long id) {
        try {
            Return return1=returnRepository.findById(id);
            Order order=orderRepository.findById(return1.getOrderId());
            Inventory inventory=inventoryRepository.findById(order.getInventoryId());
            Customer customer=customerRepository.findById(order.getCustomerId());
            inventory.setQuantity(inventory.getQuantity()-return1.getQuantity());
            inventoryRepository.save(inventory);
            customer.setAccount(customer.getAccount()+(return1.getPrice()*return1.getQuantity()));
            customerRepository.save(customer);

            returnRepository.deleteById(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to delete."+e.getMessage());
        
        }
        
        return "redirect:/customers";
    }
}
