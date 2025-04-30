package com.ai.demo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ai.demo.dto.QuestionResult;
import com.ai.demo.entity.Cart;
import com.ai.demo.entity.Course;
import com.ai.demo.entity.CourseStatus;
import com.ai.demo.entity.Question;
import com.ai.demo.entity.User;
import com.ai.demo.repo.CartRepository;
import com.ai.demo.repo.QuestionRepository;
import com.ai.demo.service.CartService;
import com.ai.demo.service.CourseService;
import com.ai.demo.service.EmailService;
import com.ai.demo.service.UserService;
//import com.stripe.Stripe;
//import com.stripe.model.PaymentIntent;
//import com.stripe.param.PaymentIntentCreateParams;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private CourseService courseService;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private CartService cartService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private CartRepository cartRepository;

	// Display the login page (GET method)
	@GetMapping("/userlogin")
	public String showLoginPage() {
		return "loginpage"; // This will load loginpage.html from templates/
	}

	// Handle user login (POST method)
	@PostMapping("/userlogin")
	public String login(@RequestParam("email") String email, @RequestParam("password") String password,
			HttpSession session) {

		// Authenticate the user from the database
		User user = userService.authenticate(email, password);

		// If user is not found, check for hardcoded admin credentials
		if (user == null) {
			String hardcodedEmail = "admin@gmail.com";
			String hardcodedPassword = "admin";

			if (hardcodedEmail.equalsIgnoreCase(email) && hardcodedPassword.equals(password)) {
				// If hardcoded admin credentials are matched, set the session and redirect to
				// admin dashboard
				session.setAttribute("username", email);
				session.setAttribute("role", "ADMIN"); // Role as ADMIN
				return "redirect:/adminDashboard"; // Redirect to admin dashboard
			} else {
				// If no matching credentials, return to login page with error
				return "redirect:/userlogin?error=true";
			}
		}

		session.setAttribute("id", user.getId());
		session.setAttribute("name", user.getName());
		session.setAttribute("email", user.getEmail());
		session.setAttribute("contact", user.getContact());
		session.setAttribute("location", user.getLocation());
		session.setAttribute("role", user.getRole());

		if ("ADMIN".equalsIgnoreCase(user.getRole())) {
			return "redirect:/adminDashboard"; // Redirect to admin dashboard
		} else if ("USER".equalsIgnoreCase(user.getRole())) {
			return "redirect:/userDashboard"; // Redirect to user dashboard
		}

		// If no matching role, return to login page with error
		return "redirect:/userlogin?error=true";
	}

	// Endpoint for user dashboard
	@GetMapping("/userDashboard")
	public String showUserDashboard(HttpSession session, Model model) {
		List<Course> courses = courseService.getAllCourses();
		model.addAttribute("courseCount", courses.size());
		
		 Long userId = (Long) session.getAttribute("id");  // Retrieve userId from session
	        if (userId != null) {
	            // Get all courses for the user
	            List<Cart> userCourses = cartService.getCoursesByUserId(userId);
	            
	            
	            // Get the count of courses with different statuses
	            long pendingCount = cartService.countCoursesByStatus(userId, CourseStatus.PENDING);
	            long acceptedCount = cartService.countCoursesByStatus(userId, CourseStatus.ACCEPTED);
	            long completedCount = cartService.countCoursesByStatus(userId, CourseStatus.COMPLETED);
	            
	            // Get the total count of courses
	            long totalCourses = cartService.countCoursesByUserId(userId);

	            // Pass the data to the view
	            model.addAttribute("userCourses", userCourses);
	            model.addAttribute("pendingCount", pendingCount);
	            model.addAttribute("acceptedCount", acceptedCount);
	            model.addAttribute("completedCount", completedCount);
	            model.addAttribute("totalCourses", totalCourses);
		
	        }
		
		
		model.addAttribute("id", session.getAttribute("id"));
		
		
		
		model.addAttribute("name", session.getAttribute("name"));
		model.addAttribute("email", session.getAttribute("email"));
		model.addAttribute("contact", session.getAttribute("contact"));
		model.addAttribute("location", session.getAttribute("location"));
		model.addAttribute("role", session.getAttribute("role"));
		return "userdashboard"; // Thymeleaf page for user dashboard
	}

	@GetMapping("/names")
	public String listCourseNames(Model model, HttpSession session) {
		// Retrieve a list of course names from the course service
		List<String> courseNames = courseService.getAllCourseNames();

		// Set the course names in the session (optional)
		session.setAttribute("courseNames", courseNames);

		// Add the list of course names to the model so it can be accessed in the view
		model.addAttribute("courseNames", courseNames);

		// Return the name of the Thymeleaf template that should be rendered
		return "coursenames"; // This should match the template file name (coursenames.html)
	}

	@GetMapping("/courses/{name}/quiz")
	public String getQuizPage(@PathVariable("name") String language, Model model, HttpSession session) {
		List<Question> questions = questionRepository.findRandomQuestionsByLanguage(language);

		// Store questions & language in session for result processing
		session.setAttribute("quizQuestions", questions);
		session.setAttribute("language", language);

		model.addAttribute("questions", questions);
		model.addAttribute("language", language);
		return "quiz";
	}

	@PostMapping("/submit-quiz")
	public String submitQuiz(@RequestParam Map<String, String> allParams, HttpSession session, Model model) {

		Long userId = (Long) session.getAttribute("id");
		if (userId == null) {
			return "redirect:/userlogin";
		}

		// Fetch complete user object from database
		User user = userService.findById(userId);
		if (user == null) {
			return "redirect:/userlogin";
		}

		// Add complete user object to model
		model.addAttribute("user", user);

		// Get quiz questions and language from session
		List<Question> originalQuestions = (List<Question>) session.getAttribute("quizQuestions");
		String language = (String) session.getAttribute("language");

		if (originalQuestions == null || language == null) {
			model.addAttribute("error", "Quiz session expired. Please start again.");
			return "error-page";
		}

		// Get the course name from the session (ensure this attribute is set earlier in
		// the session)
		String courseName = (String) session.getAttribute("language"); // Assuming "language" is the course name in
																		// session
		// Alternatively, if you have a separate attribute for course name (e.g.,
		// "courseName"):
		// String courseName = (String) session.getAttribute("courseName");

		if (courseName == null) {
			model.addAttribute("error", "Course name not found in session.");
			return "error-page";
		}

		// Initialize counters for quiz results
		int totalQuestions = originalQuestions.size();
		int attempted = 0;
		int correct = 0;
		int incorrect = 0;
		int notAttempted = totalQuestions;

		List<QuestionResult> questionResults = new ArrayList<>();

		// Process each submitted answer
		for (Question q : originalQuestions) {
			String answerKey = "q" + q.getId();
			String userAnswer = allParams.get(answerKey);
			boolean isCorrect = false;
			boolean isAttempted = false;

			if (userAnswer != null && !userAnswer.isEmpty()) {
				attempted++;
				notAttempted--;
				isAttempted = true;

				if (userAnswer.equals(q.getCorrectOption())) {
					correct++;
					isCorrect = true;
				} else {
					incorrect++;
				}
			}

			// Add result for this question
			questionResults.add(new QuestionResult(q, userAnswer, isAttempted, isCorrect));
		}

		// Calculate percentages
		double attemptedPercentage = (attempted * 100.0) / totalQuestions;
		double correctPercentage = (correct * 100.0) / totalQuestions;
		double incorrectPercentage = (incorrect * 100.0) / totalQuestions;
		double scorePercentage = attempted > 0 ? (correct * 100.0) / attempted : 0;

		// Determine user level based on score percentage
		String level;
		if (scorePercentage >= 80) {
			level = "Advanced";
		} else if (scorePercentage >= 50) {
			level = "Intermediate";
		} else {
			level = "Beginner";
		}

		// Get course recommendation based on the level
		Optional<Course> course = courseService.getCourseByNameAndLevel(courseName, level);

		// Add all the result data to the model for rendering the view
		model.addAttribute("questionResults", questionResults);
		model.addAttribute("totalQuestions", totalQuestions);
		model.addAttribute("attempted", attempted);
		model.addAttribute("correct", correct);
		model.addAttribute("incorrect", incorrect);
		model.addAttribute("notAttempted", notAttempted);
		model.addAttribute("attemptedPercentage", String.format("%.1f", attemptedPercentage));
		model.addAttribute("correctPercentage", String.format("%.1f", correctPercentage));
		model.addAttribute("incorrectPercentage", String.format("%.1f", incorrectPercentage));
		model.addAttribute("scorePercentage", String.format("%.1f", scorePercentage));
		model.addAttribute("language", language); // Passing language as course name
		model.addAttribute("level", level);
		model.addAttribute("course", course.orElse(null)); // If no course is found, pass null

		return "quiz-results";
	}

	@PostMapping("/cart/add")
	public String addToCart(@RequestParam Map<String, String> params, HttpSession session,
			RedirectAttributes redirectAttributes) {
		// Retrieve user details from the session
		Object userIdObj = session.getAttribute("id");
		String userName = (String) session.getAttribute("name");
		String userEmail = (String) session.getAttribute("email");

		System.out.println("Session userId: " + userIdObj);
		System.out.println("Session userName: " + userName);
		System.out.println("Session userEmail: " + userEmail);

		// Check if user is logged in (i.e., session attributes are present)
		if (userIdObj == null || userName == null || userEmail == null) {
			redirectAttributes.addFlashAttribute("message", "You need to be logged in to add a course to your cart.");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/login"; // Redirect to login page if user is not logged in
		}

		Long userId = (Long) userIdObj; // Convert session userId to Long
		String courseIdStr = params.get("courseId");
		String studentsCountStr = params.get("studentsCount");

		// Debugging logs
		System.out.println("Received userId: " + userId);
		System.out.println("Received courseId: " + courseIdStr);

		if (courseIdStr == null || courseIdStr.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Course ID is required.");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/userDashboard";
		}

		Long courseId;
		try {
			courseId = Long.parseLong(courseIdStr);
		} catch (NumberFormatException e) {
			redirectAttributes.addFlashAttribute("message", "Course ID must be a valid number.");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/userDashboard";
		}

		Integer studentsCount;
		try {
			studentsCount = Integer.parseInt(studentsCountStr);
		} catch (NumberFormatException e) {
			redirectAttributes.addFlashAttribute("message", "Students count must be a valid number.");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/userDashboard";
		}

		// Check for duplicates in cart (if the same user and course are already in the
		// cart)
		if (cartService.existsByUserIdAndCourseId(userId, courseId)) {
			redirectAttributes.addFlashAttribute("message", "This course is already in your cart.");
			redirectAttributes.addFlashAttribute("alertClass", "alert-warning");
			return "redirect:/userDashboard";
		}

		// Populate Cart entity
		Cart cart = new Cart();
		cart.setUserId(userId);
		cart.setUserName(userName); // Use userName from session
		cart.setUserEmail(userEmail); // Use userEmail from session
		cart.setCourseId(courseId);
		cart.setCourseName(params.get("courseName"));
		cart.setCourseCategory(params.get("courseCategory"));
		cart.setCourseDescription(params.get("courseDescription"));
		cart.setCourseLevel(params.get("courseLevel"));
		cart.setStudentsCount(studentsCount);
		cart.setCourseVideoPath(params.get("courseVideoPath"));
		cart.setCourseStatus(CourseStatus.PENDING); // Default status

		// Save to database
		cartService.addToCart(cart);

		// Success feedback
		redirectAttributes.addFlashAttribute("message", "Course added to cart successfully!");
		redirectAttributes.addFlashAttribute("alertClass", "alert-success");

		return "redirect:/userDashboard"; // Redirect to the dashboard page after adding to cart
	}

	@GetMapping("/my-courses")
	public String getUserCourses(Model model, @SessionAttribute("id") Long userId) {
		List<Cart> userCourses = cartService.findCoursesByUserId(userId); // Fetch the user's courses
		model.addAttribute("userCourses", userCourses); // Add courses to the model
		return "myCourses"; // Return the view name for displaying the courses
	}

	@PostMapping("/courses/{courseId}/complete")
	public String updateStatus(@PathVariable Long courseId, @RequestParam Long userId) {
		cartService.updateCourseStatus(userId, courseId, CourseStatus.COMPLETED);
		return "redirect:/my-courses"; // or wherever you want to go after update
	}
	
	@GetMapping("/cart")
	public String getCartItems(Model model, HttpSession session) {
	    // Retrieve userId from session
	    Long userId = (Long) session.getAttribute("id");  // Retrieve userId from session
	    
	    if (userId != null) {
	        // Get all courses for the user from the service
	        List<Cart> userCourses = cartService.getCoursesByUserId(userId);
	        
	        // Add the courses to the model
	        model.addAttribute("allCartItems", userCourses);
	    } else {
	        // If no userId is found in session, redirect to login
	        return "redirect:/userlogin"; // Redirect to login if user is not logged in
	    }

	    return "cart";  // Return the Thymeleaf template name (cart.html)
	}
	 @PostMapping("/cart/delete/{id}")
	    public String deleteCartItem(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
	        try {
	            cartService.deleteCartItem(id);
	            redirectAttributes.addFlashAttribute("message", "Course removed from cart successfully!");
	            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
	        } catch (Exception e) {
	            redirectAttributes.addFlashAttribute("message", "Error deleting course from cart.");
	            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
	        }
	        return "redirect:/cart"; // Redirect to the page displaying the cart
	    }
	
	 @GetMapping("/my-certificates")
	 public String viewMyCertificates(HttpSession session, Model model) {
	     Long userId = (Long) session.getAttribute("id");
	     
	     if (userId == null) {
	         return "redirect:/userlogin";
	     }

	     // Fetch only ACCEPTED courses for this user
	     List<Cart> acceptedCourses = cartService.findByUserIdAndCourseStatus(userId, CourseStatus.ACCEPTED);

	     model.addAttribute("acceptedCourses", acceptedCourses);
	     return "my-certificates"; // Show list of certificates
	 }

	

	
	
	
	
	
	
	
	
	
	
	
	
	
	

//	  @Value("${stripe.api.key}")
//	    private String stripeSecretKey;
//	    
//	    @Value("${stripe.public.key}")
//	    private String stripePublicKey;
//	    
//	    @PostMapping("/payment/start")
//	    public String startPayment(@RequestParam("courseId") Long courseId,
//	                             @RequestParam("amount") Long amount,
//	                             Model model) {
//	        try {
//	            // Set your secret key
//	            Stripe.apiKey = stripeSecretKey;
//
//	            // Create a PaymentIntent
//	            PaymentIntentCreateParams createParams = 
//	                    PaymentIntentCreateParams.builder()
//	                    .setAmount(amount * 100) // Stripe uses cents
//	                    .setCurrency("usd")
//	                    .putMetadata("courseId", courseId.toString())
//	                    .build();
//
//	            PaymentIntent paymentIntent = PaymentIntent.create(createParams);
//
//	            // Add necessary attributes to model
//	            model.addAttribute("clientSecret", paymentIntent.getClientSecret());
//	            model.addAttribute("stripePublicKey", stripePublicKey);
//	            model.addAttribute("amount", amount);
//	            model.addAttribute("courseId", courseId);
//	            
//	            return "payment-form"; // Return to payment form page
//	            
//	        } catch (Exception e) {
//	            model.addAttribute("error", "Payment initiation failed: " + e.getMessage());
//	            return "error";
//	        }
//	    }
//	    
//	    @PostMapping("/payment/success")
//	    public String paymentSuccess(@RequestParam("courseId") Long courseId,
//	                               HttpSession session,
//	                               Model model) {
//	        // Handle successful payment
//	        Long userId = (Long) session.getAttribute("id");
//	        // Add your logic to enroll user or grant access
//	        model.addAttribute("courseId", courseId);
//	        return "success";
//	    }

	@GetMapping("/adminDashboard")
	public String showAdminDashboard(Model model) {
		List<Course> courses = courseService.getAllCourses();
		List<User> users = userService.getAllUsers();
		List<Cart> allCartItems = cartRepository.findAll();
		
		 long pendingCount = cartService.countPendingCourses();
		    long acceptedCount = cartService.countAcceptedCourses();

		    model.addAttribute("pendingCount", pendingCount);
		    model.addAttribute("acceptedCount", acceptedCount);

		model.addAttribute("allCartItems", allCartItems);
		model.addAttribute("courseCount", courses.size());
		model.addAttribute("userCount", users.size());
		model.addAttribute("courses", courses);
		model.addAttribute("users", users);
		return "admindashboard"; // Make sure this matches your Thymeleaf template name
	}

	// Profile Page
	@GetMapping("/profile")
	public String showProfilePage(HttpSession session, Model model) {
		model.addAttribute("id", session.getAttribute("id"));
		model.addAttribute("name", session.getAttribute("name"));
		model.addAttribute("email", session.getAttribute("email"));
		model.addAttribute("contact", session.getAttribute("contact"));
		model.addAttribute("location", session.getAttribute("location"));
		model.addAttribute("role", session.getAttribute("role"));
		return "profile";
	}

	// Method to handle course deletion
	@GetMapping("/courses/delete/{id}")
	public String deleteCourse(@PathVariable("id") Long id) {
		courseService.deleteCourseById(id); // Call service to delete the course
		return "redirect:/adminDashboard"; // Redirect back to the list of courses after deletion
	}

	// Method to show the course edit form
	@GetMapping("/courses/edit/{id}")
	public String showEditCourseForm(@PathVariable("id") Long courseId, Model model) {
		Course course = courseService.getCourseById(courseId);
		model.addAttribute("course", course);
		return "editCourseForm"; // Thymeleaf page for editing the course
	}

	// Method to update the course
	@PostMapping("/courses/edit/{id}")
	public String updateCourse(@PathVariable("id") Long courseId, @ModelAttribute Course course) {
		courseService.updateCourse(courseId, course);
		return "redirect:/adminDashboard"; // Redirect back to admin dashboard after update
	}

	@PostMapping("/cart/accept/{id}")
	public String acceptRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
	    try {
	        cartService.updateCourseStatusByCartId(id, CourseStatus.ACCEPTED);

	        redirectAttributes.addFlashAttribute("message", "Request accepted successfully!");
	        redirectAttributes.addFlashAttribute("alertClass", "alert-success");
	    } catch (IllegalArgumentException e) {
	        redirectAttributes.addFlashAttribute("message", e.getMessage());
	        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
	    }

	    return "redirect:/adminDashboard";
	}

	@PostMapping("/cart/reject/{id}")
	public String rejectRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
	    try {
	        cartService.updateCourseStatusByCartId(id, CourseStatus.REJECTED);

	        redirectAttributes.addFlashAttribute("message", "Request rejected successfully!");
	        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
	    } catch (IllegalArgumentException e) {
	        redirectAttributes.addFlashAttribute("message", e.getMessage());
	        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
	    }

	    return "redirect:/adminDashboard";
	}

}
