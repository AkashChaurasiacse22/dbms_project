package com.store_management_system.sms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.store_management_system.sms.model.*;
import com.store_management_system.sms.repository.EmployeeRepository;
import com.store_management_system.sms.service.*;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.ArrayList;
@Controller
public class EmployeeController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private StoreService storeService;


    @GetMapping("/view/employee/{id}")
public String viewEmployeeDetails(Model model, @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
    try {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

        Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                model.addAttribute("errorMessage", "This employee does not exist...");
                model.addAttribute("employee", employee);
                model.addAttribute("currentUser", currentUser);
                return "viewemployee";
            }
        if ((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")) )|| 
            ((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("MANAGER"))) && employeeService.checkBelongToSameStoreById(id, currentUser.getEmployeeId()))) {
            
            
            model.addAttribute("employee", employee);
            model.addAttribute("currentUser", currentUser);
        } else {
            return "error/403"; 
        }
        
    } catch (Exception e) {
        model.addAttribute("errorMessage", "Failed to fetch employee details...");
        
        return "viewemployee";
    }
    return "viewemployee";
}

@PostMapping("/update/employee/{id}")
public String updateEmployee(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, 
                             @ModelAttribute Employee currentEmployee, @ModelAttribute Employee employee, 
                             Model model) {
    try {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

        if ((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) || 
            ((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("MANAGER"))) && employeeService.checkBelongToSameStoreById(id, currentUser.getEmployeeId()))) {
                Long supervisor=employee.getSupervisor();
                if(supervisor!=null){
                    Employee sup=employeeService.getEmployeeById(supervisor);
                if(sup==null){
                    model.addAttribute("errorMessage", "Incorrect supevisor..");
                    model.addAttribute("employee", employee);
                    return "createEmployee";
                }
                }
                Long phone_no = employee.getPhoneNo();
                if(phone_no!=null && (employeeRepository.findByPhoneNo(phone_no)!=null)){
                    model.addAttribute("errorMessage", "Duplicate phone number");
                    model.addAttribute("employee", employee);
                    return "createEmployee";

                }
                employeeService.saveEmployee(employee);
        } else {
            return "error/403"; 
        }
        
        
        return "redirect:/view/employee/" + id;
    } catch (Exception e) {
        model.addAttribute("errorMessage", "Failed to update employee. Please try again.");
        model.addAttribute("employee", employee);
        return "viewemployee"; 
    }
}

@PostMapping("/delete/employee/{id}")
public String deleteEmployee(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,Model model) {
    User currentUser = userService.getUserByUsername(userDetails.getUsername());
    model.addAttribute("currentUser", currentUser);

    try {
        if ((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) || 
            ((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("MANAGER"))) && employeeService.checkBelongToSameStoreById(id, currentUser.getEmployeeId()))) {
                employeeService.deleteEmployeeById(id);
        } else {
            return "error/403"; 
        }
        if((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")))){
            return"redirect:/admin/stores";
        }
        else{
            return"redirect:/employees";
        }
        
    } catch (Exception e) {
        model.addAttribute("errorMessage", "Failed to update employee. Please try again.");
        if((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")))){
            return"redirect:/admin/stores";
        }
        else{
            return"redirect:/employees";
        }
        
    }
}

@PostMapping("/create/employee")
public String createEmployee(@AuthenticationPrincipal UserDetails userDetails,@ModelAttribute("employee") Employee employee, Model model) {
    try {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

        if ((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) || 
            ((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("MANAGER"))) && employeeService.checkBelongToSameStoreByStoreId(employee.getStoreId(), currentUser.getEmployeeId()))) {
                Long supervisor=employee.getSupervisor();
                if(supervisor!=null){
                    Employee sup=employeeService.getEmployeeById(supervisor);
                if(sup==null){
                    model.addAttribute("errorMessage", "Incorrect supevisor..");
                    model.addAttribute("employee", employee);
                    return "createEmployee";
                }
                }
                    
                Long phone_no = employee.getPhoneNo();
                if(phone_no!=null && (employeeRepository.findByPhoneNo(phone_no)!=null)){
                    model.addAttribute("errorMessage", "Duplicate phone number");
                    model.addAttribute("employee", employee);
                    return "createEmployee";

                }
                employeeService.saveEmployee(employee);
        } else  {
            return "error/403"; 
        }
        if(currentUser.getRoleId().equals(1)){
            return "redirect:/admin/stores";
        }

        return "redirect:/employees"; 
    } catch (Exception e) {
        model.addAttribute("errorMessage", "Something went wrong. Please try again later: "  );
        model.addAttribute("employee", employee);
        return "createEmployee"; 
    }
}

@GetMapping("/create/employee/store/{storeId}")
public String showCreateEmployeeForStore(@AuthenticationPrincipal UserDetails userDetails,@PathVariable Long storeId, Model model) {
    try {
        Store store = storeService.getById(storeId); 
    if (store == null) {
        model.addAttribute("errorMessage", "Invalid store ID.");
        return "redirect:/home"; 
    }
    User currentUser = userService.getUserByUsername(userDetails.getUsername());
    model.addAttribute("currentUser", currentUser);

        if ((currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("EMPLOYEE"))) ) {
            return "error/403";
        }
    Employee employee = new Employee();
    employee.setStoreId(storeId); 
    model.addAttribute("employee", employee);
    return "createEmployee"; 
    } catch (Exception e) {
        model.addAttribute("errorMessage", "Error occurred while creating the employee.");
        return "createEmployee";
    }
    
}



    @GetMapping("/employees")
    public String listEmployees(Model model,@ AuthenticationPrincipal UserDetails userDetails) {
        try{
            User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

            List<Employee>employees=new ArrayList<>();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("currentUserStoreId", userService.getStoreIdByUsername(currentUser.getUsername()));
            if (currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN") )) {
                employees = employeeService.getAllEmployees();
            }else if(currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("MANAGER")) ){
                employees=employeeService.getEmployeesWithSameStoreById(currentUser.getEmployeeId());
            }      
            else{
                return "error/403"; 
            }
            model.addAttribute("employees", employees);
            return "employees";
        }catch(Exception e){
            model.addAttribute("errorMessage", "Something went wrong. Try again later."  );
            return "employees";
        }
         
    }

}
