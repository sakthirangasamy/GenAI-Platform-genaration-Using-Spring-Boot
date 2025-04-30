package com.ai.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmailNotification(Long userId, Long courseId, String action) {
        // Fetch user details and course details based on the userId and courseId
        // You should have a User and Course service that can retrieve the user and course details from the database.
        
        String userEmail = getUserEmail(userId);  // Implement this method to get the user's email
        String courseName = getCourseName(courseId);  // Implement this method to get the course name
        
        String subject = "Course Status Update";
        String messageBody = "";
        
        if ("accepted".equals(action)) {
            messageBody = "Dear User, your course '" + courseName + "' has been accepted!";
        } else if ("rejected".equals(action)) {
            messageBody = "Dear User, unfortunately your course '" + courseName + "' has been rejected.";
        }

        // Send email
        try {
            sendEmail(userEmail, subject, messageBody);
        } catch (MailException e) {
            e.printStackTrace();
            // You might want to log the exception or send an error response
        }
    }

    private void sendEmail(String to, String subject, String body) throws MailException {
        MimeMessageHelper message = new MimeMessageHelper(javaMailSender.createMimeMessage());
        try {
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("no-reply@yourdomain.com");  // Change to your domain's email
            javaMailSender.send(message.getMimeMessage());
        } catch (Exception e) {
//            throw new MailException("Failed to send email", e);
        }
    }

    // Method to fetch user email (you should implement this according to your app's structure)
    private String getUserEmail(Long userId) {
        // Get the user from the database based on the userId
        // For example, use your UserService:
        // User user = userService.findById(userId);
        // return user.getEmail();
        return "user@example.com"; // Replace with actual email fetch logic
    }

    // Method to fetch course name (you should implement this according to your app's structure)
    private String getCourseName(Long courseId) {
        // Get the course from the database based on the courseId
        // For example, use your CourseService:
        // Course course = courseService.findById(courseId);
        // return course.getCourseName();
        return "Sample Course"; // Replace with actual course name fetch logic
    }
}
