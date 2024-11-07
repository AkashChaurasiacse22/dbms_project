package com.store_management_system.sms.controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import com.store_management_system.sms.repository.SupplierPaymentRepository;
import com.store_management_system.sms.repository.SupplierRepository;
import com.store_management_system.sms.repository.BuyRepository;
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
public class SupplierController {
    @Autowired
    SupplierRepository supplierRepository;
    @Autowired
    SupplierPaymentRepository supplierPaymentRepository;
    @Autowired
    BuyRepository buyRepository;
    @Autowired
    UserService userService;

    @GetMapping("/suppliers")
    public String getSuppliers(Model model,@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            List<Supplier> suppliers=supplierRepository.findAll();
            model.addAttribute("suppliers",suppliers);
            return "suppliers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "failed to fetch suppliers");
            return "suppliers";
        }
    }
    

    @GetMapping("/create/supplier")
    public String getCreateSupplier(Model model,@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }

            Supplier supplier=new Supplier();
            supplier.setAccount((double)0);
            if(supplier.getEmails()==null){
                supplier.setEmails(new ArrayList<>());
            }
            for (int i = supplier.getEmails().size(); i < 5; i++) {
                supplier.getEmails().add(new SupplierMail());
            }
            model.addAttribute("supplier",supplier);
            return "createSupplier";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "something went wrong");
            return "createSupplier";
        }
        
    }
    @PostMapping("/create/supplier")
    public String postCreateSupplier(@ModelAttribute Supplier supplier,Model model,@AuthenticationPrincipal UserDetails userDetails) {
        try {
            
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }

            supplier.getEmails().removeIf(email -> email.getSupplierEmail() == null || email.getSupplierEmail().isEmpty());

            Long phone_no = supplier.getPhoneNo();
            if(phone_no!=null && supplierRepository.findByPhoneNo(phone_no)!=null){
                model.addAttribute("errorMessage", "Duplicate phone number");
                model.addAttribute("supplier", supplier);
                return "createSupplier";
            }

            supplierRepository.save(supplier);
            return "redirect:/suppliers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "failed to create supplier");
            return "createSupplier";
        }
    }

    @GetMapping("/view/supplier/{id}")
    public String getViewSupplier(@PathVariable Long id,Model model,@AuthenticationPrincipal UserDetails userDetails ) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
            if(currentUser.getRoleId().equals(3L)){
                return "error/403";
            }
            
            Supplier supplier = supplierRepository.findById(id);
                if (supplier == null) {
                    model.addAttribute("errorMessage", "This supplier does not exist...");
                    model.addAttribute("supplier", supplier);
                    return "viewsupplier";
                }
                if(supplier.getEmails()==null){
                    supplier.setEmails(new ArrayList<>());
                }
                for (int i = supplier.getEmails().size(); i < 5; i++) {
                    supplier.getEmails().add(new SupplierMail());
                }
            model.addAttribute("supplier", supplier);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to fetch supplier details...");
            
            return "viewsupplier";
        }
        return "viewsupplier";
    }

    @PostMapping("/update/supplier/{id}")
    public String postUpdateSupplier(Model model,@ModelAttribute Supplier supplier,@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
            if(currentUser.getRoleId().equals(  1L) || currentUser.getRoleId().equals(2L)){
                supplier.getEmails().removeIf(email -> email.getSupplierEmail() == null || email.getSupplierEmail().isEmpty());
                Long phone_no = supplier.getPhoneNo();
                if(phone_no!=null && supplierRepository.findByPhoneNo(phone_no)!=null){
                    model.addAttribute("errorMessage", "Duplicate phone number");
                    model.addAttribute("supplier", supplier);
                    return "createSupplier";
                }
                supplierRepository.save(supplier);
            }else{
                return "error/403";
            }
            
            return "redirect:/view/supplier/{id}";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update supplier details.." );
            model.addAttribute("supplier", supplier);
            return "viewsupplier";
        }
        
    }

    @PostMapping("/delete/supplier/{id}")
    public String deleteSupplier(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails,Model model) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
    try {
        if (!(currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("EMPLOYEE")))  ) {
                supplierRepository.deleteById(id);
                return "redirect:/suppliers";
        } else {
            return "error/403"; 
        }
        
        }catch(Exception e){
            model.addAttribute("errorMessage","Failed to delete the supplier." );
            return "redirect:/suppliers";
        }
    
    }
    @PostMapping("/supplier/{id}/pay")
    public String postSupplierPayment(@PathVariable Long id, @RequestParam Double paymentAmount, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

        try {
        // Fetch the supplier by ID
            Supplier supplier = supplierRepository.findById(id);
            if (supplier == null) {
                model.addAttribute("errorMessage", "Supplier not found");
                return "redirect:/suppliers"; // Redirect back to suppliers list with an error
            }

        // Record the payment in the supplier_payment table
            SupplierPayment supplierPayment = new SupplierPayment();
            supplierPayment.setSupplierId(id); // Associate payment with supplier
            supplierPayment.setPaymentAmount(paymentAmount); // Set payment amount
            supplierPayment.setPaymentDate(LocalDate.now()); // Set current date as payment date

        // Save payment record in supplier_payment table
            supplierPaymentRepository.save(supplierPayment);

        // Update the supplier's account balance
            Double currentBalance = supplier.getAccount();
            Double updatedBalance = currentBalance - paymentAmount;
            supplier.setAccount(updatedBalance);

        // Save the updated supplier account balance in the database
            supplierRepository.updateSupplierAccount(supplier.getId(), updatedBalance);

            // Redirect to the mainvsuppliers list page (or wherever you want)
            return "redirect:/suppliers"; // Redirect back to the suppliers list
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to process payment: "  );
            return "redirect:/suppliers"; // Redirect back to suppliers list with an error
        }
    }
    @GetMapping("/supplier/payment/{id}")
    public String viewPaymentHistory(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        // Fetch the supplier by ID
        try {
            Supplier supplier = supplierRepository.findById(id);
            if (supplier == null) {
                model.addAttribute("error", "Supplier not found");
                return "redirect:/suppliers";
            }
            List<SupplierPayment> payments = supplierPaymentRepository.findPaymentsBySupplierId(id);
            model.addAttribute("supplier", supplier);
            model.addAttribute("payments", payments);

        // Return the payment-history Thymeleaf view
            return "supplierPayment"; 
        }
        catch (Exception e)
        {
            model.addAttribute("errorMessage", "Failed to show payment details: "  );
            return "redirect:/suppliers"; 
        }
    }

    @GetMapping("/supplier/buys/{id}")
    public String viewSupplierBuy(@PathVariable Long id, Model model,  @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Supplier supplier = supplierRepository.findById(id);
            if (supplier == null) {
                model.addAttribute("error", "Supplier not found");
                return "redirect:/suppliers";
            }
            model.addAttribute("supplier", supplier);
            List<Buy> buy = buyRepository.findBySupplierId(id);
            model.addAttribute("supplierbuys", buy);
            return "supplierBuys";
        }
        catch (Exception e)
        {
            model.addAttribute("errorMessage", "Failed to show supplier buys: "  );
            return "redirect:/suppliers"; 
        }
    }
    // @GetMapping("/search/supplier/{phoneNo}")
    // public String getMethodName(@PathVariable String firstName) {
    //     return new String();
    // }
    

    
}
