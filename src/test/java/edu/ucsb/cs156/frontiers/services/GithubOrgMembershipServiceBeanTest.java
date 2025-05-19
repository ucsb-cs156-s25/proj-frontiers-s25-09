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
}