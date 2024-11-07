package com.store_management_system.sms.controller;

import java.time.LocalDate;
import java.util.List;

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
import com.store_management_system.sms.repository.FeedbackRepository;
import com.store_management_system.sms.repository.OrderRepository;
import com.store_management_system.sms.repository.CustomerRepository;
import com.store_management_system.sms.service.UserService;

@Controller
public class FeedbackController {
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    UserService userService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    CustomerRepository customerRepository;

    @GetMapping("/feedback/create/{orderId}")
    public String createFeedback(Model model,@PathVariable Long orderId, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Feedback feedback =new Feedback();
            feedback.setOrderId(orderId);
            LocalDate currdate=LocalDate.now();
            feedback.setFdate(currdate);
            model.addAttribute("feedback", feedback);
            return "createFeedback";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to get create feedback page." );
            Order order=orderRepository.findById(orderId);
            return "redirect:/customer/orders/" + order.getCustomerId();
        }
        
    }
    @PostMapping("/feedback/create")
    public String postCreateFeedback(Model model,@ModelAttribute Feedback feedback, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            feedbackRepository.save(feedback);
            Order order=orderRepository.findById(feedback.getOrderId());
            return "redirect:/customer/orders/" + order.getCustomerId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to create Feedback " );
            model.addAttribute("feedback", feedback);
            return "createFeedback";
        }
    }

    @GetMapping("/feedback/view/{orderId}")
    public String viewFeedback(Model model,@PathVariable Long orderId, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Feedback feedback=feedbackRepository.findByOrderId(orderId);
            model.addAttribute("feedback", feedback);
            return "viewfeedback";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to get feedback page." );
            Order order=orderRepository.findById(orderId);
            return "redirect:/customer/orders/" + order.getCustomerId();
        }
    }

    @PostMapping("/update/feedback/{id}")
    public String updateFeedback(Model model,@PathVariable Long id,@ModelAttribute Feedback feedback, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            Order order=orderRepository.findById(id);
            feedbackRepository.save(feedback);
            return "redirect:/customer/orders/" + order.getCustomerId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to update feedback page." );
            model.addAttribute("feedback",feedback);
            return "viewfeedback";
        }
    }

    @PostMapping("/delete/feedback/{id}")
    public String deleteFeedback(Model model,@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        try {
            feedbackRepository.deleteByOrderId(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to delete." );
        }
        Order order=orderRepository.findById(id);
        return "redirect:/customer/orders/" + order.getCustomerId();
    }
}
