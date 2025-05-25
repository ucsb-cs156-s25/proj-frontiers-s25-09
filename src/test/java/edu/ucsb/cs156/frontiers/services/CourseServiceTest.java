package edu.ucsb.cs156.frontiers.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import edu.ucsb.cs156.frontiers.entities.Course;
import edu.ucsb.cs156.frontiers.repositories.CourseRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    private CourseService courseService;

    @BeforeEach
    public void setUp() {
        courseService = new CourseService();
        ReflectionTestUtils.setField(courseService, "courseRepository", courseRepository);
    }

    @Test
    public void testGetAllCourses() {
        Course course1 = Course.builder().id(1L).courseName("CS156").build();
        Course course2 = Course.builder().id(2L).courseName("CS32").build();
        List<Course> expectedCourses = Arrays.asList(course1, course2);

        when(courseRepository.findAll()).thenReturn(expectedCourses);

        List<Course> actualCourses = courseService.getAllCourses();

        assertEquals(expectedCourses, actualCourses);
        verify(courseRepository).findAll();
    }

    @Test
    public void testSaveCourse() {
        Course courseToSave = Course.builder().courseName("CS156").build();
        Course savedCourse = Course.builder().id(1L).courseName("CS156").build();

        when(courseRepository.save(courseToSave)).thenReturn(savedCourse);

        Course result = courseService.save(courseToSave);

        assertEquals(savedCourse, result);
        verify(courseRepository).save(courseToSave);
    }

    @Test
    public void testFindById() {
        Long courseId = 1L;
        Course expectedCourse = Course.builder().id(courseId).courseName("CS156").build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(expectedCourse));

        Optional<Course> result = courseService.findById(courseId);

        assertTrue(result.isPresent());
        assertEquals(expectedCourse, result.get());
        verify(courseRepository).findById(courseId);
    }

    @Test
    public void testFindByIdNotFound() {
        Long courseId = 1L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        Optional<Course> result = courseService.findById(courseId);

        assertFalse(result.isPresent());
        verify(courseRepository).findById(courseId);
    }

    @Test
    public void testDeleteCourse() {
        Course courseToDelete = Course.builder().id(1L).courseName("CS156").build();

        courseService.delete(courseToDelete);

        verify(courseRepository).delete(courseToDelete);
    }

    @Test
    public void testDeleteById() {
        Long courseId = 1L;

        courseService.deleteById(courseId);

        verify(courseRepository).deleteById(courseId);
    }
}