package com.ai.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.demo.entity.Cart;
import com.ai.demo.entity.CourseStatus;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

	boolean existsByUserIdAndCourseId(Long userId, Long courseId);

	List<Cart> findByUserId(Long userId);

	Optional<Cart> findByUserIdAndCourseId(Long userId, Long courseId);

	long countByCourseStatus(CourseStatus status);
	


    // Count the total number of courses by userId
    long countByUserId(Long userId);

    // Count the total number of courses by userId and courseStatus
    long countByUserIdAndCourseStatus(Long userId, CourseStatus status);
    
    List<Cart> findByUserIdAndCourseStatus(Long userId, CourseStatus status);

    

}
