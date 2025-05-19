package edu.ucsb.cs156.frontiers.jobs;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import edu.ucsb.cs156.frontiers.entities.Course;
import edu.ucsb.cs156.frontiers.services.CourseService;
import edu.ucsb.cs156.frontiers.services.GithubOrgMembershipService;
import edu.ucsb.cs156.frontiers.services.jobs.JobContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GitHubOrgStatusJobTest {

    @Mock
    private CourseService courseService;

    @Mock
    private GithubOrgMembershipService githubOrgMembershipService;

    @Mock
    private JobContext jobContext;

    private GitHubOrgStatusJob job;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        job = GitHubOrgStatusJob.builder()
        .courseService(courseService)
        .githubOrgMembershipService(githubOrgMembershipService)
        .build();
    }

    @Test
    public void testAcceptWithNoCourses() throws Exception {
        // given
        when(courseService.getAllCourses()).thenReturn(new ArrayList<>());

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("=== GitHub Org Status Job Started ===");
        verify(jobContext).log("Found 0 courses to process");
        verify(jobContext).log("=== GitHub Org Status Job Completed ===");
    }

    @Test
    public void testAcceptWithCourseNoGithubConfig() throws Exception {
        // given
        Course course = Course.builder()
                .id(1L)
                .orgName(null)
                .installationId(null)
                .rosterStudents(new ArrayList<>())
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Skipping course 1 - not set up with GitHub org and app.");
    }

    @Test
    public void testAcceptWithValidCourse() throws Exception {
        // given
        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(new ArrayList<>())
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Processing student roster for course 1");
        verify(jobContext).log("Processing staff roster for course 1");
    }
}