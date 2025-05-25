package edu.ucsb.cs156.frontiers.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class GithubOrgMembershipServiceBean implements GithubOrgMembershipService {
    
    private final RestTemplate restTemplate;
    
    @Value("${github.token}")
    private String githubToken;

    public GithubOrgMembershipServiceBean() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public boolean isMember(String org, String username) {
        if (org == null || org.trim().isEmpty()) {
            log.warn("Organization name is null or empty");
            return false;
        }
        
        if (username == null || username.trim().isEmpty()) {
            log.warn("Username is null or empty");
            return false;
        }

        if (githubToken == null || githubToken.trim().isEmpty()) {
            log.error("GitHub token is not configured");
            throw new IllegalStateException("GitHub token is not configured");
        }

        String url = String.format("https://api.github.com/orgs/%s/members/%s", 
                                 org.trim(), username.trim());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "Frontiers-App");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.debug("Checking membership for user {} in org {}", username, org);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            boolean isMember = response.getStatusCode() == HttpStatus.OK;
            log.debug("User {} is {} member of org {}", username, isMember ? "a" : "not a", org);
            return isMember;
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("User {} not found in org {} (404)", username, org);
            return false;
        } catch (HttpClientErrorException.Forbidden e) {
            log.warn("Access forbidden when checking membership for user {} in org {}. " +
                    "This may indicate insufficient token permissions or private membership.", username, org);
            return false;
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("GitHub API authentication failed. Check token validity.");
            throw new RuntimeException("GitHub API authentication failed", e);
        } catch (HttpClientErrorException e) {
            log.error("GitHub API error (status: {}): {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("GitHub API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error checking GitHub org membership for user {} in org {}: {}", 
                     username, org, e.getMessage(), e);
            throw new RuntimeException("Error checking GitHub org membership: " + e.getMessage(), e);
        }
    }
}