package edu.ucsb.cs156.frontiers.controllers;

import edu.ucsb.cs156.frontiers.ControllerTestCase;
import edu.ucsb.cs156.frontiers.controllers.CSRFController;
import edu.ucsb.cs156.frontiers.repositories.UserRepository;
import edu.ucsb.cs156.frontiers.testconfig.TestConfig;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.aspectj.lang.annotation.Before;

import edu.ucsb.cs156.frontiers.services.CourseService;
import edu.ucsb.cs156.frontiers.services.GithubOrgMembershipService;

@ActiveProfiles("development")
@WebMvcTest(controllers = CSRFController.class)
@Import(TestConfig.class)
public class CSRFControllerTests extends ControllerTestCase {

  @MockitoBean
  UserRepository userRepository;

  @MockitoBean
  CourseService courseService;

  @MockitoBean
  GithubOrgMembershipService githubOrgMembershipService;

  @Test
  public void csrf_returns_ok() throws Exception {
    MvcResult response = mockMvc.perform(get("/csrf"))
              .andExpect(status().isOk())
              .andReturn();

    String responseString = response.getResponse().getContentAsString();
    assertTrue(responseString.contains("parameterName"));
    assertTrue(responseString.contains("_csrf"));
    assertTrue(responseString.contains("headerName"));
    assertTrue(responseString.contains("X-XSRF-TOKEN"));

  }

}
