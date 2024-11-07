package com.store_management_system.sms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.store_management_system.sms.model.*;
import com.store_management_system.sms.repository.ProductRepository;
import com.store_management_system.sms.repository.ProductDiscountRepository;

import com.store_management_system.sms.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class ProductController {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDiscountRepository productdiscountRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/products")
    public String showProducts(Model model,@AuthenticationPrincipal UserDetails userDetails){
        User currentUser= userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            List<Product> products=productRepository.findAll();

            // List<Discount> discounts=productdiscountRepository.findByProductId();
            model.addAttribute("products",products);
            return "products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Something went wrong. Please try again later."  );
            return "products";
        }
    }


    @GetMapping("/create/product")
    public String getCreateProduct(Model model,@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser= userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Product product=new Product();
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            model.addAttribute("product",product);
            return "createProduct";
        } catch (Exception e) {
            model.addAttribute("errorMessage","Something went wrong. Please try again later."  );
            return "createProduct";
        }
    }

    @PostMapping("/create/product")
    public String postCreateProduct(@ModelAttribute Product product,Model model,@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser= userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            
            productRepository.save(product);
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Something went wrong. Please try again later."  );
            model.addAttribute("product", product);
            return "createProduct";
        }
    }

    @GetMapping("/view/product/{id}")
    public String viewProduct(@PathVariable Long id,Model model,@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser= userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            Product product=productRepository.findById(id);
            model.addAttribute("product", product);
            return "viewproduct";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to show product details. "  );
            
            return "viewproduct";
        }
    }

    @PostMapping("/update/product/{id}")
    public String updateProduct(@PathVariable Long id,Model model,@ModelAttribute Product product,@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser= userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            productRepository.save(product);
            
            return "redirect:/view/product/{id}";
        } catch (Exception e) {
            model.addAttribute("errorMessage","Failed to update product details: "  );
            model.addAttribute("product", product);
            return "viewproduct";
        }
    }

    @PostMapping("/delete/product/{id}")
    public String deleteProduct(@PathVariable Long id,Model model,@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser= userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            productRepository.deleteById(id);
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to delete product. Please try again later."  );
            return "products";
        }
    }


}
