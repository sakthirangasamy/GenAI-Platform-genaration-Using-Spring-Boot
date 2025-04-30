package com.ai.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.demo.entity.Cart;
import com.ai.demo.entity.CourseStatus;
import com.ai.demo.repo.CartRepository;

@Service
public class CartService {

	@Autowired
	private CartRepository cartRepository;

	public void addToCart(Cart cart) {
		cartRepository.save(cart);
	}

	public List<Cart> findCoursesByUserId(Long userId) {
		return cartRepository.findByUserId(userId);
	}

	public boolean existsByUserIdAndCourseId(Long userId, Long courseId) {
		return cartRepository.existsByUserIdAndCourseId(userId, courseId);
	}

	public void updateCourseStatus(Long userId, Long courseId, CourseStatus status) {
	    // Log incoming request for debugging
	    System.out.println("Updating course status for userId=" + userId + ", courseId=" + courseId);

	    Optional<Cart> cartItem = cartRepository.findByUserIdAndCourseId(userId, courseId);

	    if (cartItem.isPresent()) {
	        Cart cart = cartItem.get();
	        System.out.println("Before update: " + cart.getCourseStatus());
	        cart.setCourseStatus(status);
	        cartRepository.save(cart);
	        System.out.println("After update: " + cart.getCourseStatus());
	    } else {
	        throw new IllegalArgumentException(
	            String.format("Course with ID %d not found in user %d's cart", courseId, userId)
	        );
	    }
	}


	public Cart getCartById(Long id) {
		return cartRepository.findById(id).orElseThrow(() -> new RuntimeException("Cart not found"));
	}

	public Cart saveCart(Cart cart) {
		return cartRepository.save(cart);
	}

	public void updateCourseStatusByCartId(Long cartId, CourseStatus status) {
		Optional<Cart> cartItem = cartRepository.findById(cartId);
		if (cartItem.isPresent()) {
			Cart cart = cartItem.get();
			System.out.println("Before update: " + cart.getCourseStatus());
			cart.setCourseStatus(status);
			cartRepository.save(cart);
			System.out.println("After update: " + cart.getCourseStatus());
		} else {
			throw new IllegalArgumentException("Cart item not found with id: " + cartId);
		}
	}

	public long countPendingCourses() {
		return cartRepository.countByCourseStatus(CourseStatus.PENDING);
	}

	public long countAcceptedCourses() {
		return cartRepository.countByCourseStatus(CourseStatus.ACCEPTED);
	}
	
	  // Fetch all the courses for a particular user
    public List<Cart> getCoursesByUserId(Long userId) {
        return cartRepository.findByUserId(userId);  // This will return all courses added to the cart by the user
    }

    // Count the total number of courses by userId and course status
    public long countCoursesByStatus(Long userId, CourseStatus status) {
        return cartRepository.countByUserIdAndCourseStatus(userId, status);  // Count courses by userId and course status
    }

    // Count the total number of courses for a user
    public long countCoursesByUserId(Long userId) {
        return cartRepository.countByUserId(userId);  // This will return the total number of courses for a user
    }
    
    public List<Cart> findAllCartItems() {
        return cartRepository.findAll();
    }

    // Delete cart item by ID
    public void deleteCartItem(Long id) {
        cartRepository.deleteById(id);
    }
    
    public List<Cart> findByUserIdAndCourseStatus(Long userId, CourseStatus status) {
        return cartRepository.findByUserIdAndCourseStatus(userId, status);
    }
}
