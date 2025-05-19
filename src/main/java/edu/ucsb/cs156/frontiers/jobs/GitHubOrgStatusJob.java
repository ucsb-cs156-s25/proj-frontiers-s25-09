package edu.ucsb.cs156.frontiers.jobs;

import edu.ucsb.cs156.frontiers.entities.Course;
import edu.ucsb.cs156.frontiers.entities.CourseStaff;
import edu.ucsb.cs156.frontiers.entities.RosterStudent;
import edu.ucsb.cs156.frontiers.entities.User;
import edu.ucsb.cs156.frontiers.services.CourseService;
import edu.ucsb.cs156.frontiers.services.GithubOrgMembershipService;
import edu.ucsb.cs156.frontiers.services.jobs.JobContext;
import edu.ucsb.cs156.frontiers.services.jobs.JobContextConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GitHubOrgStatusJob implements JobContextConsumer {

    @Autowired
    private CourseService courseService;

    @Autowired
    private GithubOrgMembershipService githubOrgMembershipService;

    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    public void setGithubOrgMembershipService(GithubOrgMembershipService githubOrgMembershipService) {
        this.githubOrgMembershipService = githubOrgMembershipService;
    }

    @Override
    public void accept(JobContext ctx) throws Exception {
        List<Course> courses = courseService.getAllCourses();
        ctx.log("=== GitHub Org Status Job Started ===");
        ctx.log("Found " + courses.size() + " courses to process");

        for (Course course : courses) {
            ctx.log("Processing course " + course.getId() + 
                   " - orgName: " + course.getOrgName() + 
                   " - installationId: " + course.getInstallationId());
                   
            if (!isGithubConfigured(course)) {
                ctx.log("Skipping course " + course.getId() + " - not set up with GitHub org and app.");
                continue;
            }

            processStudentRoster(ctx, course);
            processStaffRoster(ctx, course);
        }
        
        ctx.log("=== GitHub Org Status Job Completed ===");
    }

    private boolean isGithubConfigured(Course course) {
        return course.getOrgName() != null && !course.getOrgName().trim().isEmpty() &&
               course.getInstallationId() != null && !course.getInstallationId().trim().isEmpty();
    }

    private void processStudentRoster(JobContext ctx, Course course) {
        ctx.log("Processing student roster for course " + course.getId());
        
        for (RosterStudent student : course.getRosterStudents()) {
            if (hasGithubId(student)) {
                checkAndUpdateStudentStatus(ctx, course, student);
            } else {
                ctx.log("Student " + getStudentIdentifier(student) + " - no GitHub ID, skipping");
            }
        }
    }

    private void processStaffRoster(JobContext ctx, Course course) {
        ctx.log("Processing staff roster for course " + course.getId());
        
        for (CourseStaff staff : course.getCourseStaff()) {
            if (hasGithubId(staff)) {
                checkAndUpdateStaffStatus(ctx, course, staff);
            } else {
                ctx.log("Staff " + getStaffIdentifier(staff) + " - no GitHub ID, skipping");
            }
        }
    }

    private boolean hasGithubId(RosterStudent student) {
        return student.getGithubId() != null && student.getGithubId() != 0;
    }

    private boolean hasGithubId(CourseStaff staff) {
        User user = staff.getUser();
        return user != null && user.getGithubId() != 0;
    }

    private void checkAndUpdateStudentStatus(JobContext ctx, Course course, RosterStudent student) {
        try {
            String githubId = String.valueOf(student.getGithubId());
            boolean isMember = githubOrgMembershipService.isMember(course.getOrgName(), githubId);
            
            String status = isMember ? "MEMBER" : "NOT_MEMBER";
            ctx.log("Student " + getStudentIdentifier(student) + " → " + status);
        } catch (Exception e) {
            ctx.log("Error checking student " + getStudentIdentifier(student) + ": " + e.getMessage());
        }
    }

    private void checkAndUpdateStaffStatus(JobContext ctx, Course course, CourseStaff staff) {
        try {
            User user = staff.getUser();
            String githubId = String.valueOf(user.getGithubId());
            boolean isMember = githubOrgMembershipService.isMember(course.getOrgName(), githubId);
            
            String status = isMember ? "MEMBER" : "NOT_MEMBER";
            ctx.log("Staff " + getStaffIdentifier(staff) + " → " + status);
        } catch (Exception e) {
            ctx.log("Error checking staff " + getStaffIdentifier(staff) + ": " + e.getMessage());
        }
    }

    private String getStudentIdentifier(RosterStudent student) {
        if (student == null) {
            return "unknown";
        }
        
        if (student.getEmail() != null && !student.getEmail().trim().isEmpty()) {
            return student.getEmail();
        }
        
        User user = student.getUser();
        if (user != null) {
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                return user.getEmail();
            }
            if (user.getGithubLogin() != null && !user.getGithubLogin().trim().isEmpty()) {
                return user.getGithubLogin();
            }
        }
        
        return "ID:" + (student.getId() != null ? student.getId() : "unknown");
    }

    private String getStaffIdentifier(CourseStaff staff) {
        if (staff == null) {
            return "unknown";
        }
        
        User user = staff.getUser();
        if (user != null) {
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                return user.getEmail();
            }
            if (user.getGithubLogin() != null && !user.getGithubLogin().trim().isEmpty()) {
                return user.getGithubLogin();
            }
        }
        
        return "ID:" + (staff.getId() != null ? staff.getId() : "unknown");
    }
}