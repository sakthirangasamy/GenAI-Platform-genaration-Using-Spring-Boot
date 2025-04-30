package com.ai.demo.controller;

import com.ai.demo.entity.Course;
import com.ai.demo.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class CourseController {

    @Autowired
    private CourseService courseService;

    // Define the base directory to store videos
    private static final String UPLOAD_DIR = "videos";  // Folder to store videos

    @PostMapping("/api/courses")
    public String addCourse(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("level") String level,
            @RequestParam("studentsCount") int studentsCount,
            @RequestParam("description") String description,
            @RequestParam("videoFile") MultipartFile videoFile) throws IOException {

        // Save course to database (handle video file storage here)
        Course course = new Course();
        course.setName(name);
        course.setCategory(category);
        course.setLevel(level);
        course.setStudentsCount(studentsCount);
        course.setDescription(description);

        // Handle the video file and store it
        String videoPath = handleVideoFile(videoFile);

        course.setVideoPath(videoPath);  // Store the file path in the database
        courseService.saveCourse(course);

        return "redirect:/adminDashboard"; // Redirect to the course list
    }

    private String handleVideoFile(MultipartFile videoFile) throws IOException {
        if (videoFile.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Get the original filename
        String fileName = videoFile.getOriginalFilename();

        // Check if the file name is valid
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("Invalid file name");
        }

        // Define the path for storing videos (create the folder if it doesn't exist)
        Path uploadPath = Paths.get(UPLOAD_DIR);

        // Create the directory if it doesn't exist
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Define the full path to store the file
        Path filePath = uploadPath.resolve(fileName);

        // Save the file to the directory
        videoFile.transferTo(filePath);

        // Return the file path that will be saved in the database
        return filePath.toString();
    }
}
