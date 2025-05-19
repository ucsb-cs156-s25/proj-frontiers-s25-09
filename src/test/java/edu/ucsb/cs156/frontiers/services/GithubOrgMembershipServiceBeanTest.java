package edu.ucsb.cs156.frontiers.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.mockito.ArgumentCaptor;
import java.util.List;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
public class GithubOrgMembershipServiceBeanTest {

    @Mock
    private RestTemplate restTemplate;

    private GithubOrgMembershipServiceBean service;

    @BeforeEach
    public void setUp() {
        service = new GithubOrgMembershipServiceBean();
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "githubToken", "test-token");
    }

    @Test
    public void testIsMemberReturnsTrueWhenUserIsMember() {
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        boolean result = service.isMember("test-org", "test-user");

        assertTrue(result);
    }

    @Test
    public void testIsMemberReturnsFalseWhenResponseIsNotOK() {
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        boolean result = service.isMember("test-org", "test-user");

        assertFalse(result);
    }

    @Test
    public void testIsMemberReturnsFalseWhenUserNotFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        boolean result = service.isMember("test-org", "test-user");

        assertFalse(result);
    }

    @Test
    public void testIsMemberReturnsFalseWhenForbidden() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.Forbidden.create(HttpStatus.FORBIDDEN, "Forbidden", null, null, null));

        boolean result = service.isMember("test-org", "test-user");

        assertFalse(result);
    }

    @Test
    public void testIsMemberThrowsExceptionWhenUnauthorized() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.Unauthorized.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        assertThrows(RuntimeException.class, () -> service.isMember("test-org", "test-user"));
    }

    @Test
    public void testIsMemberThrowsExceptionForOtherHttpErrors() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null));

        assertThrows(RuntimeException.class, () -> service.isMember("test-org", "test-user"));
    }

    @Test
    public void testIsMemberThrowsExceptionForGeneralErrors() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThrows(RuntimeException.class, () -> service.isMember("test-org", "test-user"));
    }

    @Test
    public void testIsMemberReturnsFalseWhenOrgIsNull() {
        boolean result = service.isMember(null, "test-user");

        assertFalse(result);
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    public void testIsMemberReturnsFalseWhenOrgIsEmpty() {
        boolean result = service.isMember("", "test-user");

        assertFalse(result);
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    public void testIsMemberReturnsFalseWhenOrgIsWhitespace() {
        boolean result = service.isMember("   ", "test-user");

        assertFalse(result);
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    public void testIsMemberReturnsFalseWhenUsernameIsNull() {
        boolean result = service.isMember("test-org", null);

        assertFalse(result);
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    public void testIsMemberReturnsFalseWhenUsernameIsEmpty() {
        boolean result = service.isMember("test-org", "");

        assertFalse(result);
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    public void testIsMemberReturnsFalseWhenUsernameIsWhitespace() {
        boolean result = service.isMember("test-org", "   ");

        assertFalse(result);
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    public void testIsMemberThrowsExceptionWhenTokenIsNull() {
        ReflectionTestUtils.setField(service, "githubToken", null);

        assertThrows(IllegalStateException.class, () -> service.isMember("test-org", "test-user"));
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    public void testIsMemberThrowsExceptionWhenTokenIsEmpty() {
        ReflectionTestUtils.setField(service, "githubToken", "");

        assertThrows(IllegalStateException.class, () -> service.isMember("test-org", "test-user"));
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    public void testIsMemberThrowsExceptionWhenTokenIsWhitespace() {
        ReflectionTestUtils.setField(service, "githubToken", "   ");

        assertThrows(IllegalStateException.class, () -> service.isMember("test-org", "test-user"));
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    public void testIsMemberTrimsOrgAndUsername() {
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        service.isMember("  test-org  ", "  test-user  ");

        verify(restTemplate).exchange(
                eq("https://api.github.com/orgs/test-org/members/test-user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
    
    @Test
public void testIsMemberSetsCorrectHeaders() {
    // given
    ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(response);

    // when
    service.isMember("test-org", "test-user");

    // then - verify the headers are set correctly
    verify(restTemplate).exchange(
            eq("https://api.github.com/orgs/test-org/members/test-user"),
            eq(HttpMethod.GET),
            argThat(httpEntity -> {
                HttpHeaders headers = httpEntity.getHeaders();
                
                // Verify Bearer token is set
                String authHeader = headers.getFirst("Authorization");
                assertNotNull(authHeader, "Authorization header should be present");
                assertTrue(authHeader.startsWith("Bearer "), "Authorization header should start with 'Bearer '");
                assertTrue(authHeader.contains("test-token"), "Authorization header should contain the token");
                
                // Verify Accept header is set
                List<MediaType> acceptHeaders = headers.getAccept();
                assertFalse(acceptHeaders.isEmpty(), "Accept header should be present");
                assertTrue(acceptHeaders.contains(MediaType.APPLICATION_JSON), "Accept header should include application/json");
                
                // Verify User-Agent header is set
                String userAgentHeader = headers.getFirst("User-Agent");
                assertNotNull(userAgentHeader, "User-Agent header should be present");
                assertEquals("Frontiers-App", userAgentHeader, "User-Agent should be 'Frontiers-App'");
                
                return true;
            }),
            eq(String.class)
    );
}

@Test
public void testIsMemberHeadersWithNullChecks() {
    // given
    ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
    
    // Capture the HttpEntity to inspect headers
    ArgumentCaptor<HttpEntity<String>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), httpEntityCaptor.capture(), eq(String.class)))
            .thenReturn(response);

    // when
    service.isMember("test-org", "test-user");

    // then
    HttpEntity<String> capturedEntity = httpEntityCaptor.getValue();
    HttpHeaders headers = capturedEntity.getHeaders();
    
    // Ensure all required headers are present and not null
    assertNotNull(headers.getFirst("Authorization"));
    assertNotNull(headers.getAccept());
    assertNotNull(headers.getFirst("User-Agent"));
    
    // Verify the specific values
    assertEquals("Bearer test-token", headers.getFirst("Authorization"));
    assertEquals("Frontiers-App", headers.getFirst("User-Agent"));
    assertTrue(headers.getAccept().contains(MediaType.APPLICATION_JSON));
}

@Test
public void testIsMemberAlwaysSetsHeaders() {
    // Test that headers are set even when the request fails
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

    // when
    service.isMember("test-org", "test-user");

    // then - verify headers were still set (method was called)
    verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.GET),
            argThat(httpEntity -> {
                HttpHeaders headers = httpEntity.getHeaders();
                return headers.getFirst("Authorization") != null &&
                       headers.getFirst("User-Agent") != null &&
                       !headers.getAccept().isEmpty();
            }),
            eq(String.class)
    );
}
}