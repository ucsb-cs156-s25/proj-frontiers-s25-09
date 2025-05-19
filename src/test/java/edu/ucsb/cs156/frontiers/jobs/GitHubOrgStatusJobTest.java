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
import java.lang.reflect.Method;


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
    
    @Test
public void testGetStudentIdentifierWithAllNullFields() throws Exception {
    RosterStudent student = RosterStudent.builder()
            .id(null)
            .githubId(null)
            .email(null)
            .user(null)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

    job.accept(jobContext);

    verify(jobContext).log("Student ID:unknown - no GitHub ID, skipping");
}

@Test
public void testGetStaffIdentifierWithAllNullFields() throws Exception {
    CourseStaff staff = CourseStaff.builder()
            .id(null)
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

    job.accept(jobContext);

    verify(jobContext).log("Staff ID:unknown - no GitHub ID, skipping");
}

@Test 
public void testGetStudentIdentifierWithNullIdReturnsUnknown() throws Exception {
    RosterStudent student = RosterStudent.builder()
            .id(null)
            .githubId(12345)
            .email(null)
            .user(null)
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

    job.accept(jobContext);

    verify(jobContext).log("Student ID:unknown → MEMBER");
}

@Test 
public void testGetStaffIdentifierWithNullIdReturnsUnknown() throws Exception {
    User staffUser = User.builder()
            .email(null)
            .githubId(54321)
            .githubLogin(null)
            .build();
    
    CourseStaff staff = CourseStaff.builder()
            .id(null)
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

    job.accept(jobContext);

    verify(jobContext).log("Staff ID:unknown → MEMBER");
}

@Test
public void testIsGithubConfiguredWithWhitespaceOrgName() throws Exception {
    Course course = Course.builder()
            .id(1L)
            .orgName("   ")
            .installationId("valid-installation")
            .rosterStudents(new ArrayList<>())
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

    job.accept(jobContext);

    verify(jobContext).log("Skipping course 1 - not set up with GitHub org and app.");
}

@Test
public void testIsGithubConfiguredWithWhitespaceInstallationId() throws Exception {
    Course course = Course.builder()
            .id(1L)
            .orgName("valid-org")
            .installationId("   ")
            .rosterStudents(new ArrayList<>())
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

    job.accept(jobContext);

    verify(jobContext).log("Skipping course 1 - not set up with GitHub org and app.");
}
@Test
public void testGetStaffIdentifierWithNullStaffObject() throws Exception {
    // Use reflection to invoke the private method directly with null
    java.lang.reflect.Method getStaffIdentifierMethod = 
        GitHubOrgStatusJob.class.getDeclaredMethod("getStaffIdentifier", CourseStaff.class);
    getStaffIdentifierMethod.setAccessible(true);
    
    String result = (String) getStaffIdentifierMethod.invoke(job, (CourseStaff) null);
    
    assertEquals("unknown", result);
}

@Test
public void testGetStudentIdentifierWithDirectEmail() throws Exception {
    RosterStudent student = RosterStudent.builder()
            .id(10L)
            .githubId(10101)
            .email("direct@email.com")  // Direct email on student
            .user(null)  // No user object
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "10101")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student direct@email.com → MEMBER");
}

// Test student with whitespace email (should be trimmed)
@Test
public void testGetStudentIdentifierWithWhitespaceEmail() throws Exception {
    RosterStudent student = RosterStudent.builder()
            .id(11L)
            .githubId(11111)
            .email("  valid@email.com  ")  // Email with whitespace
            .user(null)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "11111")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student   valid@email.com   → MEMBER");
}

// Test staff with valid user email
@Test
public void testGetStaffIdentifierWithUserEmail() throws Exception {
    User staffUser = User.builder()
            .email("staff@email.com")
            .githubId(22222)
            .githubLogin(null)
            .build();
    
    CourseStaff staff = CourseStaff.builder()
            .id(7L)
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
    when(githubOrgMembershipService.isMember("test-org", "22222")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Staff staff@email.com → MEMBER");
}

// Test staff with valid github login (no email)
@Test
public void testGetStaffIdentifierWithGithubLogin() throws Exception {
    User staffUser = User.builder()
            .email(null)  // No email
            .githubId(33333)
            .githubLogin("staffgithub")
            .build();
    
    CourseStaff staff = CourseStaff.builder()
            .id(8L)
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
    when(githubOrgMembershipService.isMember("test-org", "33333")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Staff staffgithub → MEMBER");
}

// Test isGithubConfigured with null values
@Test
public void testIsGithubConfiguredWithNullOrgName() throws Exception {
    Course course = Course.builder()
            .id(3L)
            .orgName(null)  
            .installationId("valid-installation")
            .rosterStudents(new ArrayList<>())
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

    job.accept(jobContext);

    verify(jobContext).log("Skipping course 3 - not set up with GitHub org and app.");
}

// Test isGithubConfigured with null installation ID
@Test
public void testIsGithubConfiguredWithNullInstallationId() throws Exception {
    Course course = Course.builder()
            .id(4L)
            .orgName("valid-org")
            .installationId(null)  
            .rosterStudents(new ArrayList<>())
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

    job.accept(jobContext);

    verify(jobContext).log("Skipping course 4 - not set up with GitHub org and app.");
}
@Test
public void testGetStudentIdentifierWithNullStudentObject() throws Exception {
    // Use reflection to test the private method with null
    java.lang.reflect.Method getStudentIdentifierMethod = 
        GitHubOrgStatusJob.class.getDeclaredMethod("getStudentIdentifier", RosterStudent.class);
    getStudentIdentifierMethod.setAccessible(true);
    
    String result = (String) getStudentIdentifierMethod.invoke(job, (RosterStudent) null);
    
    assertEquals("unknown", result);
}

@Test
public void testGetStudentIdentifierWithEmptyEmail() throws Exception {
  
    User userWithEmail = User.builder()
            .email("user@example.com")
            .githubLogin("userlogin")
            .build();
    
    RosterStudent student = RosterStudent.builder()
            .id(5L)
            .githubId(55555)
            .email("") 
            .user(userWithEmail)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "55555")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student user@example.com → MEMBER");
}

@Test
public void testGetStudentIdentifierWithWhitespaceOnlyEmail() throws Exception {

    User userWithEmail = User.builder()
            .email("fallback@example.com")
            .githubLogin("fallbacklogin")
            .build();
    
    RosterStudent student = RosterStudent.builder()
            .id(6L)
            .githubId(66666)
            .email("   ")  
            .user(userWithEmail)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "66666")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student fallback@example.com → MEMBER");
}

@Test
public void testGetStudentIdentifierWithUserEmptyEmail() throws Exception {

    User userWithEmptyEmail = User.builder()
            .email("") 
            .githubLogin("githubuser")
            .build();
    
    RosterStudent student = RosterStudent.builder()
            .id(7L)
            .githubId(77777)
            .email(null)  
            .user(userWithEmptyEmail)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "77777")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student githubuser → MEMBER");
}

@Test
public void testGetStudentIdentifierWithUserWhitespaceEmail() throws Exception {
    // Test student where user has whitespace-only email (should fallback to github login)
    User userWithWhitespaceEmail = User.builder()
            .email("   ")  // Whitespace-only user email
            .githubLogin("anotheruser")
            .build();
    
    RosterStudent student = RosterStudent.builder()
            .id(8L)
            .githubId(88888)
            .email(null)
            .user(userWithWhitespaceEmail)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "88888")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student anotheruser → MEMBER");
}

@Test
public void testGetStudentIdentifierWithUserEmptyGithubLogin() throws Exception {
    // Test student where user has empty github login (should fallback to ID)
    User userWithEmptyLogin = User.builder()
            .email(null)
            .githubLogin("")  // Empty github login
            .build();
    
    RosterStudent student = RosterStudent.builder()
            .id(9L)
            .githubId(99999)
            .email(null)
            .user(userWithEmptyLogin)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "99999")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student ID:9 → MEMBER");
}

@Test
public void testGetStudentIdentifierWithUserWhitespaceGithubLogin() throws Exception {
    // Test student where user has whitespace-only github login (should fallback to ID)
    User userWithWhitespaceLogin = User.builder()
            .email(null)
            .githubLogin("   ")  // Whitespace-only github login
            .build();
    
    RosterStudent student = RosterStudent.builder()
            .id(10L)
            .githubId(101010)
            .email(null)
            .user(userWithWhitespaceLogin)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "101010")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student ID:10 → MEMBER");
}

@Test
public void testGetStaffIdentifierWithEmptyEmail() throws Exception {
    // Test staff with empty email (should fallback to github login)
    User staffUserWithEmptyEmail = User.builder()
            .email("")  // Empty email
            .githubId(111111)
            .githubLogin("staffuser")
            .build();
    
    CourseStaff staff = CourseStaff.builder()
            .id(5L)
            .user(staffUserWithEmptyEmail)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(new ArrayList<>())
            .courseStaff(Arrays.asList(staff))
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "111111")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Staff staffuser → MEMBER");
}

@Test
public void testGetStaffIdentifierWithWhitespaceEmail() throws Exception {
    // Test staff with whitespace-only email (should fallback to github login)
    User staffUserWithWhitespaceEmail = User.builder()
            .email("   ")  // Whitespace-only email
            .githubId(222222)
            .githubLogin("staffuser2")
            .build();
    
    CourseStaff staff = CourseStaff.builder()
            .id(6L)
            .user(staffUserWithWhitespaceEmail)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(new ArrayList<>())
            .courseStaff(Arrays.asList(staff))
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "222222")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Staff staffuser2 → MEMBER");
}

@Test
public void testGetStaffIdentifierWithEmptyGithubLogin() throws Exception {
    // Test staff with empty github login (should fallback to ID)
    User staffUserWithEmptyLogin = User.builder()
            .email(null)
            .githubId(333333)
            .githubLogin("")  // Empty github login
            .build();
    
    CourseStaff staff = CourseStaff.builder()
            .id(7L)
            .user(staffUserWithEmptyLogin)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(new ArrayList<>())
            .courseStaff(Arrays.asList(staff))
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "333333")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Staff ID:7 → MEMBER");
}


@Test
public void testGetStudentIdentifierGithubLoginNull() throws Exception {
    User user = User.builder()
            .email(null)
            .githubLogin(null)  
            .build();
    
    RosterStudent student = RosterStudent.builder()
            .id(100L)
            .githubId(100100)
            .email(null)
            .user(user)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "100100")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student ID:100 → MEMBER");
}

@Test
public void testGetStudentIdentifierGithubLoginEmpty() throws Exception {
    User user = User.builder()
            .email(null)
            .githubLogin("") 
            .build();
    
    RosterStudent student = RosterStudent.builder()
            .id(101L)
            .githubId(101101)
            .email(null)
            .user(user)
            .build();

    Course course = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(Arrays.asList(student))
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));
    when(githubOrgMembershipService.isMember("test-org", "101101")).thenReturn(true);

    job.accept(jobContext);

    verify(jobContext).log("Student ID:101 → MEMBER");
}
@Test
public void testAcceptLogsStartMessage() throws Exception {
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
    verify(jobContext).log("=== GitHub Org Status Job Started ===");
    verify(jobContext).log("Found 1 courses to process");
    verify(jobContext).log("Processing course 1 - orgName: test-org - installationId: 12345");
    verify(jobContext).log("Processing student roster for course 1");
    verify(jobContext).log("Processing staff roster for course 1");
    verify(jobContext).log("=== GitHub Org Status Job Completed ===");
}

@Test 
public void testAcceptLogsCorrectNumberOfCourses() throws Exception {
    // given
    Course course1 = Course.builder()
            .id(1L)
            .orgName("test-org-1")
            .installationId("111")
            .rosterStudents(new ArrayList<>())
            .courseStaff(new ArrayList<>())
            .build();
            
    Course course2 = Course.builder()
            .id(2L)
            .orgName("test-org-2") 
            .installationId("222")
            .rosterStudents(new ArrayList<>())
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(course1, course2));

    // when
    job.accept(jobContext);

    // then
    verify(jobContext).log("=== GitHub Org Status Job Started ===");
    verify(jobContext).log("Found 2 courses to process");
    verify(jobContext).log("=== GitHub Org Status Job Completed ===");
}

@Test
public void testAcceptLogsSkippedMessage() throws Exception {
    // given 
    Course configuredCourse = Course.builder()
            .id(1L)
            .orgName("test-org")
            .installationId("12345")
            .rosterStudents(new ArrayList<>())
            .courseStaff(new ArrayList<>())
            .build();
            
    Course unconfiguredCourse = Course.builder()
            .id(2L)
            .orgName(null)
            .installationId(null)
            .rosterStudents(new ArrayList<>())
            .courseStaff(new ArrayList<>())
            .build();
    
    when(courseService.getAllCourses()).thenReturn(Arrays.asList(configuredCourse, unconfiguredCourse));

    // when
    job.accept(jobContext);

    // then
    verify(jobContext).log("=== GitHub Org Status Job Started ===");
    verify(jobContext).log("Found 2 courses to process");
    verify(jobContext).log("Processing course 1 - orgName: test-org - installationId: 12345");
    verify(jobContext).log("Skipping course 2 - not set up with GitHub org and app.");
    verify(jobContext).log("=== GitHub Org Status Job Completed ===");
}
}