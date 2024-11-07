package com.store_management_system.sms.controller;

import com.store_management_system.sms.exception.CustomServiceException;
import com.store_management_system.sms.model.*;
import com.store_management_system.sms.repository.EmployeeRepository;
import com.store_management_system.sms.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/admin/stores")
    public String listStores(Model model, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        try {
            List<Store> stores = storeService.findAllStores();
            model.addAttribute("stores", stores);
        } catch (Exception e) {
            // model.addAttribute("errorMessage", "Error fetching store data: "  );
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching store data"  );

        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        return "stores";
    }

    @GetMapping("/create/store")
    public String createStoreForm(Model model, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);

            if (!(currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")))) {
                return "error/403";
            }
            model.addAttribute("store", new Store());
            List<Employee> managers = employeeRepository.getManagersByStoreId(userService.getStoreIdByUsername(currentUser.getUsername()));
            model.addAttribute("managers", managers);
            return "createStore";
        } catch (Exception e) {
            // model.addAttribute("errorMessage", "Error: "  );
            redirectAttributes.addFlashAttribute("errorMessage", "Something went wrong."  );

            return "createStore";
        }
    }

    @PostMapping("/create/store")
    public String createStore(@ModelAttribute Store store, Model model, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
            if (!(currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")))) {
                return "error/403";
            }
            List<Employee> managers = employeeService.getAllManagers();
            model.addAttribute("managers", managers);

            if (store.getManagerId() != null) {
                Employee employee = employeeService.getEmployeeById(store.getManagerId());
                if (employee == null || employee.getDesignation() == null || !employee.getDesignation().equals("Manager")) {
                    // model.addAttribute("errorMessage", "Incorrect ManagerId");
                    redirectAttributes.addFlashAttribute("errorMessage", "Incorrect manager Id");
                    return "createStore";
                }
            }

            storeService.createStore(store);
            redirectAttributes.addFlashAttribute("successMessage", "Store created successfully.");
            return "redirect:/admin/stores";
        } catch (Exception e) {
            // model.addAttribute("errorMessage", "Creating store failed: "  );
            redirectAttributes.addFlashAttribute("errorMessage", "Creating store field "  );
            return "createStore";
        }
    }

    @GetMapping("/view/store/{id}")
    public String viewStoreDetails(Model model, @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);


            Store store = storeService.getById(id);
            if (store == null) {
                // model.addAttribute("errorMessage", "Store not found.");
                redirectAttributes.addFlashAttribute("errorMessage", "Store Not found ");
                return "viewstore";
            }
            model.addAttribute("store", store);

            List<Employee> managers = employeeRepository.getManagersByStoreId(id);
            model.addAttribute("managers", managers);
            if (!(currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) &&
                    !(currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("MANAGER")) &&
                            userService.getStoreIdByUsername(currentUser.getUsername()).equals(id))) {
                return "error/403";
            }

        } catch (Exception e) {
            // model.addAttribute("errorMessage", "Failed to fetch store details: "  );
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch store details: "  );
            return "viewstore";
        }
        return "viewstore";
    }

    @PostMapping("/update/store/{id}")
    public String updateStore(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                              @ModelAttribute Store store, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);


            if (!(currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")))) {
                return "error/403";
            }
            
            if (store.getManagerId() != null) {
                Employee employee = employeeService.getEmployeeById(store.getManagerId());
                if (employee == null || employee.getDesignation() == null || !employee.getDesignation().equals("Manager")) {
                    // model.addAttribute("errorMessage", "Incorrect ManagerId");
                    redirectAttributes.addFlashAttribute("errorMessage", "Incorrect ManagerId");
                    model.addAttribute("store", store);
                    return "viewstore";
                }
            }

            List<Employee> managers = employeeService.getAllManagers();
            model.addAttribute("managers", managers);

            storeService.updateStore(store);
            redirectAttributes.addFlashAttribute("successMessage", "Store updated successfully.");
            return "redirect:/view/store/" + id;
        } catch (Exception e) {
            // model.addAttribute("errorMessage", "Failed to update store: "  );
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update store: "  );
            model.addAttribute("store", store);
            return "viewstore";
        }
    }

    @PostMapping("/delete/store/{id}")
    public String deleteStore(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);

            if (!(currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")))) {
                return "error/403";
            }

            storeService.deleteStoreById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Store deleted successfully.");
            return "redirect:/admin/stores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete store: "  );
            model.addAttribute("errorMessage", "Failed to delete store: "  );
            return "redirect:/admin/stores";
        }
    }
}