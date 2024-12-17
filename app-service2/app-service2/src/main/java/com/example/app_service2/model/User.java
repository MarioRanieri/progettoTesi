package com.example.app_service2.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false)
    private String username;

    @Column(nullable=false)
    private String password;

    @Column(nullable=false)
    private String email;

    
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="authorities", joinColumns=@JoinColumn(name="user_id")) 
    @Column(name="authority") 
    private Set<String> authorities;

    @Version
    @Column(name = "version")
    private Long version;

    // Costruttore di default
    public User() {}

    // Costruttore con username e password
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Costruttore con tutti i campi
    public User(String username, String password, String email, Set<String> authorities) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
