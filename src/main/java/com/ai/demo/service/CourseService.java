package com.ai.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.demo.entity.Course;
import com.ai.demo.repo.CourseRepository;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    // Save a new course
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }


    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
 // Method to get a course by ID
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public void deleteCourseById(Long id) {
        // Check if the course exists
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id); // Delete course by ID
        } else {
            throw new IllegalArgumentException("Course not found with id: " + id);
        }
    }

    // Method to update a course
    public void updateCourse(Long courseId, Course courseDetails) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setName(courseDetails.getName());
        course.setCategory(courseDetails.getCategory());
        course.setLevel(courseDetails.getLevel());
        course.setDescription(courseDetails.getDescription());
        course.setStudentsCount(courseDetails.getStudentsCount());
        courseRepository.save(course);
    }

    public List<String> getAllCourseNames() {
        return courseRepository.findAllCourseNames();
    }
    
    public Optional<Course> getCourseByNameAndLevel(String name, String level) {
        return courseRepository.findByNameAndLevel(name, level);
    }
    
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }
}
