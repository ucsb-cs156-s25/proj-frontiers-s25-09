package edu.ucsb.cs156.frontiers.services;

public interface GithubOrgMembershipService {
    boolean isMember(String org, String username);
}
