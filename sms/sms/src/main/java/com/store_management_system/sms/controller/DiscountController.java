package com.store_management_system.sms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.store_management_system.sms.repository.DiscountRepository;
import com.store_management_system.sms.repository.ProductRepository;
import com.store_management_system.sms.repository.ProductDiscountRepository;
import com.store_management_system.sms.service.UserService;
import com.store_management_system.sms.model.*;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class DiscountController {
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDiscountRepository productdiscountRepository;

    @GetMapping("/discounts")
    public String getDiscounts(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            List<Discount> discounts=discountRepository.findAll();
            model.addAttribute("discounts", discounts);
        
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to show discounts: "  );
        }
        return "discount";
    }

    @GetMapping("/create/discount")
    public String getCreateDiscount(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            List<Product> products=productRepository.findAll();

            // List<Discount> discounts=productdiscountRepository.findByProductId();
            model.addAttribute("products",products);
            Discount discount=new Discount();
            if(discount.getProductIds()==null){
                discount.setProductIds(new ArrayList<>());
            }
            for (int i = discount.getProductIds().size(); i < 10; i++) {
                discount.getProductIds().add(null);
            }
            model.addAttribute("discount", discount);
            return "createDiscount";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Something went wrong. Please try again later: "  );
        
            return "redirect:/discounts";
        }
    }

    @PostMapping("/create/discount")
    public String postCreateDiscount(Model model,@ModelAttribute Discount discount, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            if(discount.getProductIds()!=null){
                discount.getProductIds().removeIf(productId -> productId == null );
            }
            if(discount.getProductIds()==null){
                discount.setProductIds(new ArrayList<>());
            }
            discountRepository.save(discount);
            return "redirect:/discounts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create discount: "  );
            model.addAttribute("discount", discount);
            List<Product> products=productRepository.findAll();

            // List<Discount> discounts=productdiscountRepository.findByProductId();
            model.addAttribute("products",products);
            return "createDiscount";
        }
        
    }
    
    @GetMapping("/view/discount/{id}")
    public String viewDiscount(Model model,@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            List<Product> products=productRepository.findAll();

            // List<Discount> discounts=productdiscountRepository.findByProductId();
            model.addAttribute("products",products);
            Discount discount=discountRepository.findById(id);
            if(discount.getProductIds()==null){
                discount.setProductIds(new ArrayList<>());
            }
            for (int i = discount.getProductIds().size(); i < 10; i++) {
                discount.getProductIds().add(null);
            }
            model.addAttribute("discount", discount);
            return "viewdiscount";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to show discount details: "  );
        
            return "redirect:/discounts";
        }
    }
    
    @PostMapping("/update/discount/{id}")
    public String updateDiscount(Model model,@PathVariable Long id,@ModelAttribute Discount discount, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            if(discount.getProductIds()!=null){
                discount.getProductIds().removeIf(productId -> productId == null );
            }
            if(discount.getProductIds()==null){
                discount.setProductIds(new ArrayList<>());
            }
            discountRepository.save(discount);

            return "redirect:/discounts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Something Went wrong. Try agin later."  );
            model.addAttribute("discount", discount);
            List<Product> products=productRepository.findAll();

            // List<Discount> discounts=productdiscountRepository.findByProductId();
            model.addAttribute("products",products);
            return "viewdiscount";
        }
    }
    
    @PostMapping("/delete/discount/{id}")
    public String postMethodName(Model model,@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            discountRepository.delete(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage","Failed to delete this discount"  );

        }
        return "redirect:/discounts";
    }
    @GetMapping("/discount/product/{id}")
    public String productDiscount(Model model,@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            List<Product> products=productdiscountRepository.findProductByDiscountID(id);
            Discount discount=discountRepository.findById(id);
            model.addAttribute("discount",discount);
            model.addAttribute("products",products);
            return "productdiscount";
        }
        catch (Exception e) {
            model.addAttribute("errorMessage","Failed to get the product with this discount"  );
            return "redirect:/discounts";
        } 
    }
    
}
