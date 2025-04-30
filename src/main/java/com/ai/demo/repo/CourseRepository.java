package com.ai.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ai.demo.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
	@Query("SELECT DISTINCT c.name FROM Course c")
	List<String> findAllCourseNames();

	
	  Optional<Course> findByNameAndLevel(String name, String level);
}
