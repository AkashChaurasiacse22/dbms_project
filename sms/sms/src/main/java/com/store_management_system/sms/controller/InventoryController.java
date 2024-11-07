package com.store_management_system.sms.controller;

import org.springframework.stereotype.Controller;
import com.store_management_system.sms.model.*;
import com.store_management_system.sms.repository.*;
import com.store_management_system.sms.service.StoreService;
import com.store_management_system.sms.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class InventoryController {
    @Autowired
    private UserService userService;
    @Autowired
    private InventoryRepository inventoryRepository;

    

    @Autowired
    private StoreService storeService;

    // Display the inventory management page
    @GetMapping("/inventory")
    public String showInventoryManagement(Model model,@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            if(currentUser.getRoleId().equals(1L)){
                List<Store> stores = storeService.findAllStores();
                model.addAttribute("stores", stores);
            }else{
                Long storeId=userService.getStoreIdByUsername(currentUser.getUsername());
                List<Store> stores = storeService.findById(storeId);
                model.addAttribute("stores", stores);
                return "storeInventory";
            }
                 
        // model.addAttribute("inventory", inventory);
        return "inventorys"; 
        } catch (Exception e) {
           model.addAttribute("errorMessage", "failed to fetch inventory");
        //    model.addAttribute("inventory", new Inventory());
        return "inventorys"; 
        }
        
    }
    @GetMapping("/storeinventory/{storeId}")
    public String showStoreInventory(Model model,@PathVariable Long storeId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            List<Store> stores = storeService.findById(storeId);
            model.addAttribute("stores", stores);
            return "storeInventory";
        }
        catch (Exception e) {
            model.addAttribute("errorMessage","Failed to fetch store inventory");
            return "inventorys"; 
        }
        
    }
    // Create a new inventory
    @PostMapping("/create/inventory")
    public String createInventory(@RequestParam("quantity") Long quantity,@RequestParam("storeId") Long storeId,@RequestParam("productId") Long productId ,Model model,@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            Inventory inventory=new Inventory();  
            inventory.setQuantity(quantity);
            inventory.setProductId(productId);
            inventory.setStoreId(storeId);
            inventoryRepository.save(inventory);
            return "redirect:/storeinventory/" + storeId;
        } catch (Exception e) {
            model.addAttribute("errorMessage","Failed to create inventory");
            return "redirect:/storeinventory/" + storeId;
        }
         
    }

    // Update an existing inventory
    @PostMapping("/update/inventory/{productId}/{storeId}")
    public String updateInventory(@PathVariable("productId") Integer productId,@PathVariable("storeId") Integer storeId, @RequestParam("quantity") Long quantity,Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            Inventory inventory=new Inventory();
            inventory=inventoryRepository.findByProductAndStoreId((long)productId,(long)storeId).get(0);
            inventory.setQuantity(quantity);
            // inventory.setId((long)id); // Set the ID to ensure it updates the correct record
        inventoryRepository.update(inventory);
        return "redirect:/storeinventory/" + storeId; 
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error Updating the inventory");
            return "redirect:/storeinventory/" + storeId;
        }
        
    }

    // Delete an inventory
    @PostMapping("/delete/inventory/{productId}/{storeId}")
    public String deleteInventory(@PathVariable("productId") Integer productId,@PathVariable("storeId") Integer storeId,Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            inventoryRepository.deleteById((long)productId,(long)storeId);
            return "redirect:/storeinventory/" + storeId;
        } catch (Exception e) {
            model.addAttribute("errorMessage","failed to delete inventory");
            return "redirect:/storeinventory/" + storeId;
        }
        
        
        
    }
}
