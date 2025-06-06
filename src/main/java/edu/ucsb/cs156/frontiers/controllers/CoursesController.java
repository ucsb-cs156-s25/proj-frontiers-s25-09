package edu.ucsb.cs156.frontiers.controllers;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.ucsb.cs156.frontiers.entities.Course;
import edu.ucsb.cs156.frontiers.entities.RosterStudent;
import edu.ucsb.cs156.frontiers.errors.EntityNotFoundException;
import edu.ucsb.cs156.frontiers.errors.InvalidInstallationTypeException;
import edu.ucsb.cs156.frontiers.models.CurrentUser;
import edu.ucsb.cs156.frontiers.models.RosterStudentDTO;
import edu.ucsb.cs156.frontiers.repositories.CourseRepository;
import edu.ucsb.cs156.frontiers.repositories.RosterStudentRepository;
import edu.ucsb.cs156.frontiers.services.OrganizationLinkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsb.cs156.frontiers.enums.OrgStatus;

@Tag(name = "Course")
@RequestMapping("/api/courses")
@RestController
@Slf4j
public class CoursesController extends ApiController {
    
    @Autowired
    private CourseRepository courseRepository;

    @Autowired private OrganizationLinkerService linkerService;

    @Autowired
    private RosterStudentRepository rosterStudentRepository;
     /**
     * This method creates a new Course.
     * 
    * @param orgName the name of the organization
    * @param courseName the name of the course
    * @param term the term of the course
    * @param school the school of the course
    * @return the created course
     */

    @Operation(summary = "Create a new course")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Course postCourse(
            @Parameter(name = "orgName") @RequestParam String orgName,
            @Parameter(name = "courseName") @RequestParam String courseName,
            @Parameter(name = "term") @RequestParam String term,
            @Parameter(name = "school") @RequestParam String school
           )
            {
        //get current date right now and set status to pending
        CurrentUser currentUser = getCurrentUser();
        Course course = Course.builder()
                .orgName(orgName)
                .courseName(courseName)
                .term(term)
                .school(school)
                .creator(currentUser.getUser())
                .build();
        Course savedCourse = courseRepository.save(course);

        return savedCourse;
    }

      /**
     * This method returns a list of courses.
     * @return a list of all courses.
     */
    @Operation(summary = "List all courses")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public Iterable<Course> allCourses(
    ) {
        Iterable<Course> courses = courseRepository.findAll();
        return courses;
    }


    /**
     * <p>This is the outgoing method, redirecting from Frontiers to GitHub to allow a Course to be linked to a GitHub Organization.
     * It redirects from Frontiers to the GitHub app installation process, and will return with the {@link #addInstallation(Optional, String, String, Long) addInstallation()} endpoint
     * </p>
     * @param courseId id of the course to be linked to
     * @return dynamically loaded url to install Frontiers to a Github Organization, with the courseId marked as the state parameter, which GitHub will return.
     *
     */
    @Operation(summary = "Authorize Frontiers to a Github Course")
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @GetMapping("/redirect")
    public ResponseEntity<Void> linkCourse(@Parameter Long courseId) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeySpecException {
        String newUrl = linkerService.getRedirectUrl();
        newUrl += "/installations/new?state="+courseId;
        //found this convenient solution here: https://stackoverflow.com/questions/29085295/spring-mvc-restcontroller-and-redirect
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, newUrl).build();
    }


    /**
     *
     * @param installation_id id of the incoming GitHub Organization installation
     * @param setup_action whether the permissions are installed or updated. Required RequestParam but not used by the method.
     * @param code token to be exchanged with GitHub to ensure the request is legitimate and not spoofed.
     * @param state id of the Course to be linked with the GitHub installation.
     * @return ResponseEntity, returning /success if the course was successfully linked or /noperms if the user does not have the permission to install the application on GitHub. Alternately returns 403 Forbidden if the user is not the creator.
     */
    @Operation(summary = "Link a Course to a Github Course")
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @GetMapping("link")
    public ResponseEntity<Void> addInstallation(@Parameter(name = "installationId") @RequestParam Optional<String> installation_id,
                                                @Parameter(name = "setupAction") @RequestParam String setup_action,
                                                @Parameter(name = "code") @RequestParam String code,
                                                @Parameter(name = "state") @RequestParam Long state) throws NoSuchAlgorithmException, InvalidKeySpecException, JsonProcessingException {
        if(installation_id.isEmpty()) {
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, "/courses/nopermissions").build();
        }else {
            Course course = courseRepository.findById(state).orElseThrow(() -> new EntityNotFoundException(Course.class, state));
            if(!(course.getCreator().getId() ==getCurrentUser().getUser().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }else{
                String orgName = linkerService.getOrgName(installation_id.get());
                course.setInstallationId(installation_id.get());
                course.setOrgName(orgName);
                courseRepository.save(course);
                return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, "/admin/courses?success=True&course=" + state).build();
            }
        }
    }

    /**
     * This method returns a list of students in the roster for a given course,
     * with each student represented as a RosterStudentDTO including GitHub org status.
     * 
     * @param courseId the ID of the course
     * @return a list of RosterStudentDTOs for the given course
     */
    @Operation(summary = "Get list of students in a course roster, including orgStatus")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PROFESSOR')")
    @GetMapping("/roster")
    public List<RosterStudentDTO> getRosterForCourse(
            @Parameter(name = "courseId") @RequestParam Long courseId
    ) {
        Iterable<RosterStudent> studentsIterable = rosterStudentRepository.findByCourseId(courseId);
        List<RosterStudent> students = StreamSupport.stream(studentsIterable.spliterator(), false).collect(Collectors.toList());
        return students.stream()
                .map(RosterStudentDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * This method handles the InvalidInstallationTypeException.
     * @param e the exception
     * @return a map with the type and message of the exception
     */
    @ExceptionHandler({ InvalidInstallationTypeException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleInvalidInstallationType(Throwable e) {
        return Map.of(
                "type", e.getClass().getSimpleName(),
                "message", e.getMessage()
        );
    }



    /**
     * This method looks up a current user and gets their email.
     * With that email, it that email on every course roster,
     * and when it appears, notes/stores course id
     * We then return all courses for those course ids, with relevant
     * fields for the student.
     * 
     * Relevant fields are: id, installationId, orgName, courseName, term, school
     * For each course, return the status that the student is in
     */
    
    @Operation(summary = "Get all courses for a student")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/student")
    public ResponseEntity<List<StudentCourseView>> getCoursesForStudent() {
        String email = getCurrentUser().getUser().getEmail();

        List<StudentCourseView> results = courseRepository
                .findAllByRosterStudents_Email(email)
.stream()
            .map(c -> new StudentCourseView(c, email))
                .toList();

        return ResponseEntity.ok(results);
    }

    
    // Lightweight projection of Course entity with only student-relevant fields
    public static record StudentCourseView(
            Long id,
            String installationId,
            String orgName,
            String courseName,
            String term,
            String school,
            String status) {

        
        // Creates view from Course entity and student email
        public StudentCourseView(Course c, String email) {
            this(
                c.getId(),
                c.getInstallationId(),
                c.getOrgName(),
                c.getCourseName(),
                c.getTerm(),
                c.getSchool(),
                mapStatus(
                    c.getRosterStudents()
                     .stream()
                     .filter(s -> s.getEmail().equals(email))
                     .findFirst()
                     .orElse(null)));
        }

        
        // Maps OrgStatus enum to human-readable status message
        private static String mapStatus(RosterStudent rs) {
            if (rs == null) return "Not yet requested an invitation";

            return switch (rs.getOrgStatus()) {
                case NONE -> "Not yet requested an invitation";
                case INVITED -> "Has requested an invitation but isn't yet a member";
                case MEMBER -> "Is a member of the org";
                case OWNER -> "Is an admin in the org";
                case EXPIRED -> "Invitation has expired";
            };
        }
    }

}
