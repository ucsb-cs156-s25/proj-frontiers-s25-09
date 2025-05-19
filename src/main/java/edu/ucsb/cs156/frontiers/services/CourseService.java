package edu.ucsb.cs156.frontiers.services;

import edu.ucsb.cs156.frontiers.entities.Course;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    public List<Course> getAllCourses() {
        // TODO: Replace with real DB call later
        return List.of(); // Empty list just for now
    }
}
