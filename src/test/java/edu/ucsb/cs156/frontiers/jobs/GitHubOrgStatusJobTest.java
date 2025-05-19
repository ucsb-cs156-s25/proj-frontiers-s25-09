package edu.ucsb.cs156.frontiers.jobs;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import edu.ucsb.cs156.frontiers.entities.Course;
import edu.ucsb.cs156.frontiers.entities.CourseStaff;
import edu.ucsb.cs156.frontiers.entities.RosterStudent;
import edu.ucsb.cs156.frontiers.entities.User;
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
    public void testAcceptWithCourseEmptyOrgName() throws Exception {
        // given
        Course course = Course.builder()
                .id(2L)
                .orgName("")
                .installationId("12345")
                .rosterStudents(new ArrayList<>())
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Skipping course 2 - not set up with GitHub org and app.");
    }

    @Test
    public void testAcceptWithCourseEmptyInstallationId() throws Exception {
        // given
        Course course = Course.builder()
                .id(3L)
                .orgName("test-org")
                .installationId("")
                .rosterStudents(new ArrayList<>())
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Skipping course 3 - not set up with GitHub org and app.");
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

    @Test
    public void testAcceptWithStudentWithGithubId() throws Exception {
        // given
        User studentUser = User.builder()
                .email("student@example.com")
                .githubLogin("student123")
                .build();
        
        RosterStudent student = RosterStudent.builder()
                .id(1L)
                .githubId(12345)
                .email("student@test.com")
                .user(studentUser)
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(Arrays.asList(student))
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
        when(githubOrgMembershipService.isMember("test-org", "12345")).thenReturn(true);

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Student student@test.com → MEMBER");
    }

    @Test
    public void testAcceptWithStudentNotMember() throws Exception {
        // given
        RosterStudent student = RosterStudent.builder()
                .id(1L)
                .githubId(12345)
                .email("student@test.com")
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(Arrays.asList(student))
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
        when(githubOrgMembershipService.isMember("test-org", "12345")).thenReturn(false);

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Student student@test.com → NOT_MEMBER");
    }

    @Test
    public void testAcceptWithStudentNoGithubId() throws Exception {
        // given
        RosterStudent student = RosterStudent.builder()
                .id(1L)
                .githubId(null)
                .email("student@test.com")
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(Arrays.asList(student))
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Student student@test.com - no GitHub ID, skipping");
    }

    @Test
    public void testAcceptWithStudentZeroGithubId() throws Exception {
        // given
        RosterStudent student = RosterStudent.builder()
                .id(1L)
                .githubId(0)
                .email("student@test.com")
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(Arrays.asList(student))
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Student student@test.com - no GitHub ID, skipping");
    }

    @Test
    public void testAcceptWithStaffWithGithubId() throws Exception {
        // given
        User staffUser = User.builder()
                .email("staff@example.com")
                .githubId(54321)
                .githubLogin("staff123")
                .build();
        
        CourseStaff staff = CourseStaff.builder()
                .id(1L)
                .user(staffUser)
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(new ArrayList<>())
                .courseStaff(Arrays.asList(staff))
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
        when(githubOrgMembershipService.isMember("test-org", "54321")).thenReturn(true);

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Staff staff@example.com → MEMBER");
    }

    @Test
    public void testAcceptWithStaffNotMember() throws Exception {
        // given
        User staffUser = User.builder()
                .email("staff@example.com")
                .githubId(54321)
                .build();
        
        CourseStaff staff = CourseStaff.builder()
                .id(1L)
                .user(staffUser)
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(new ArrayList<>())
                .courseStaff(Arrays.asList(staff))
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
        when(githubOrgMembershipService.isMember("test-org", "54321")).thenReturn(false);

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Staff staff@example.com → NOT_MEMBER");
    }

    @Test
    public void testAcceptWithStaffNoUser() throws Exception {
        // given
        CourseStaff staff = CourseStaff.builder()
                .id(1L)
                .user(null)
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(new ArrayList<>())
                .courseStaff(Arrays.asList(staff))
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Staff ID:1 - no GitHub ID, skipping");
    }

    @Test
    public void testAcceptWithStaffZeroGithubId() throws Exception {
        // given
        User staffUser = User.builder()
                .email("staff@example.com")
                .githubId(0)
                .build();
        
        CourseStaff staff = CourseStaff.builder()
                .id(1L)
                .user(staffUser)
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(new ArrayList<>())
                .courseStaff(Arrays.asList(staff))
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Staff staff@example.com - no GitHub ID, skipping");
    }

    @Test
    public void testAcceptWithStudentMembershipError() throws Exception {
        // given
        RosterStudent student = RosterStudent.builder()
                .id(1L)
                .githubId(12345)
                .email("student@test.com")
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(Arrays.asList(student))
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
        when(githubOrgMembershipService.isMember("test-org", "12345"))
            .thenThrow(new RuntimeException("API Error"));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Error checking student student@test.com: API Error");
    }

    @Test
    public void testAcceptWithStaffMembershipError() throws Exception {
        // given
        User staffUser = User.builder()
                .email("staff@example.com")
                .githubId(54321)
                .build();
        
        CourseStaff staff = CourseStaff.builder()
                .id(1L)
                .user(staffUser)
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(new ArrayList<>())
                .courseStaff(Arrays.asList(staff))
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
        when(githubOrgMembershipService.isMember("test-org", "54321"))
            .thenThrow(new RuntimeException("API Error"));

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Error checking staff staff@example.com: API Error");
    }

    @Test
    public void testGetStudentIdentifierVariations() throws Exception {
        // Test student with email
        RosterStudent studentWithEmail = RosterStudent.builder()
                .id(1L)
                .githubId(12345)
                .email("student@test.com")
                .build();

        // Test student with user email but no direct email
        User userWithEmail = User.builder()
                .email("user@example.com")
                .githubLogin("userlogin")
                .build();
        
        RosterStudent studentWithUserEmail = RosterStudent.builder()
                .id(2L)
                .githubId(23456)
                .email(null)
                .user(userWithEmail)
                .build();

        // Test student with github login but no email
        User userWithLogin = User.builder()
                .email(null)
                .githubLogin("githubuser")
                .build();
        
        RosterStudent studentWithGithubLogin = RosterStudent.builder()
                .id(3L)
                .githubId(34567)
                .email(null)
                .user(userWithLogin)
                .build();

        // Test student with only ID
        RosterStudent studentWithOnlyId = RosterStudent.builder()
                .id(4L)
                .githubId(45678)
                .email(null)
                .user(null)
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(Arrays.asList(studentWithEmail, studentWithUserEmail, 
                                             studentWithGithubLogin, studentWithOnlyId))
                .courseStaff(new ArrayList<>())
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
        when(githubOrgMembershipService.isMember(anyString(), anyString())).thenReturn(true);

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Student student@test.com → MEMBER");
        verify(jobContext).log("Student user@example.com → MEMBER");
        verify(jobContext).log("Student githubuser → MEMBER");
        verify(jobContext).log("Student ID:4 → MEMBER");
    }

    @Test
    public void testGetStaffIdentifierVariations() throws Exception {
        // Test staff with email
        User staffWithEmail = User.builder()
                .email("staff@example.com")
                .githubId(54321)
                .githubLogin("staffuser")
                .build();
        
        CourseStaff staffWithEmailUser = CourseStaff.builder()
                .id(1L)
                .user(staffWithEmail)
                .build();

        // Test staff with github login but no email
        User staffWithLogin = User.builder()
                .email(null)
                .githubId(65432)
                .githubLogin("githubstaff")
                .build();
        
        CourseStaff staffWithGithubLogin = CourseStaff.builder()
                .id(2L)
                .user(staffWithLogin)
                .build();

        // Test staff with only ID
        CourseStaff staffWithOnlyId = CourseStaff.builder()
                .id(3L)
                .user(null)
                .build();

        Course course = Course.builder()
                .id(1L)
                .orgName("test-org")
                .installationId("12345")
                .rosterStudents(new ArrayList<>())
                .courseStaff(Arrays.asList(staffWithEmailUser, staffWithGithubLogin, staffWithOnlyId))
                .build();
        
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
        when(githubOrgMembershipService.isMember("test-org", "54321")).thenReturn(true);
        when(githubOrgMembershipService.isMember("test-org", "65432")).thenReturn(true);

        // when
        job.accept(jobContext);

        // then
        verify(jobContext).log("Staff staff@example.com → MEMBER");
        verify(jobContext).log("Staff githubstaff → MEMBER");
        verify(jobContext).log("Staff ID:3 - no GitHub ID, skipping");
    }
}