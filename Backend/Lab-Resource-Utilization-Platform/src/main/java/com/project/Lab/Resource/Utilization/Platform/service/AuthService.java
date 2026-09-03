package com.project.Lab.Resource.Utilization.Platform.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import com.project.Lab.Resource.Utilization.Platform.dto.AuthResponse;
import com.project.Lab.Resource.Utilization.Platform.dto.GoogleLoginRequest;
import com.project.Lab.Resource.Utilization.Platform.dto.LoginRequest;
import com.project.Lab.Resource.Utilization.Platform.dto.RegisterRequest;

import com.project.Lab.Resource.Utilization.Platform.entity.Institution;
import com.project.Lab.Resource.Utilization.Platform.entity.Role;
import com.project.Lab.Resource.Utilization.Platform.entity.User;

import com.project.Lab.Resource.Utilization.Platform.repository.InstitutionRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.RoleRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.UserRepository;

import com.project.Lab.Resource.Utilization.Platform.security.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private GoogleTokenService googleTokenService;


    // ==========================================================
    // REGISTER
    // ==========================================================

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(
                request.getEmail().trim().toLowerCase()
        ).isPresent()) {

            return new AuthResponse(
                    null,
                    "Email already exists",
                    null
            );
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() ->
                        new RuntimeException("Role not found")
                );

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        user.setName(
                request.getFirstName().trim() + " " +
                        request.getLastName().trim()
        );

        user.setEmail(
                request.getEmail().trim().toLowerCase()
        );

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setPhone(request.getPhone());

        Integer institutionId = request.getInstitutionId();

        if (institutionId == null &&
                request.getInstitutionName() != null &&
                !request.getInstitutionName().trim().isEmpty()) {

            Institution institution =
                    institutionRepository
                            .findByNameIgnoreCase(
                                    request.getInstitutionName().trim()
                            )
                            .orElseGet(() -> {

                                Institution newInstitution =
                                        new Institution();

                                newInstitution.setName(
                                        request.getInstitutionName().trim()
                                );

                                newInstitution.setCreatedAt(
                                        LocalDateTime.now()
                                );

                                return institutionRepository.save(
                                        newInstitution
                                );
                            });

            institutionId = institution.getInstitutionId();
        }

        user.setInstitutionId(institutionId);

        user.setRole(role);

        user.setIsActive(true);

        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "role",
                role.getRoleName()
        );

        String token =
                jwtService.generateToken(
                        claims,
                        user.getEmail()
                );

        return new AuthResponse(
                token,
                "Registration Successful",
                role.getRoleName()
        );
    }


    // ==========================================================
    // LOGIN
    // ==========================================================

    public AuthResponse login(LoginRequest request) {

        User user =
                userRepository.findByEmail(
                                request.getEmail()
                                        .trim()
                                        .toLowerCase()
                        )
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        "Invalid Email or Password"
                                )
                        );

        if (!Boolean.TRUE.equals(user.getIsActive())) {

            throw new RuntimeException(
                    "User account is disabled."
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new BadCredentialsException(
                    "Invalid Email or Password"
            );
        }

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "role",
                user.getRole().getRoleName()
        );

        String token =
                jwtService.generateToken(
                        claims,
                        user.getEmail()
                );

        return new AuthResponse(
                token,
                "Login Successful",
                user.getRole().getRoleName()
        );
    }


    // ==========================================================
    // GOOGLE LOGIN
    // ==========================================================

    public AuthResponse googleLogin(
            GoogleLoginRequest request
    ) {

        // ------------------------------------------------------
        // VALIDATE REQUEST
        // ------------------------------------------------------

        if (request == null ||
                request.getCredential() == null ||
                request.getCredential().trim().isEmpty()) {

            throw new BadCredentialsException(
                    "Google credential is required"
            );
        }


        // ------------------------------------------------------
        // VERIFY GOOGLE ID TOKEN
        // ------------------------------------------------------

        GoogleIdToken.Payload payload =
                googleTokenService.verifyToken(
                        request.getCredential().trim()
                );


        // ------------------------------------------------------
        // GET VERIFIED GOOGLE INFORMATION
        // ------------------------------------------------------

        String googleId =
                payload.getSubject();

        String email =
                payload.getEmail();

        String name =
                (String) payload.get("name");

        String firstName =
                (String) payload.get("given_name");

        String lastName =
                (String) payload.get("family_name");


        if (googleId == null ||
                googleId.isBlank() ||
                email == null ||
                email.isBlank()) {

            throw new BadCredentialsException(
                    "Invalid Google account information"
            );
        }

        email = email.trim().toLowerCase();


        // ------------------------------------------------------
        // FIND USER USING GOOGLE ID
        // ------------------------------------------------------

        User user =
                userRepository.findByGoogleId(
                        googleId
                ).orElse(null);


        // ------------------------------------------------------
        // IF GOOGLE ID NOT FOUND,
        // CHECK EXISTING ACCOUNT USING EMAIL
        // ------------------------------------------------------

        if (user == null) {

            user =
                    userRepository.findByEmail(
                            email
                    ).orElse(null);
        }


        // ------------------------------------------------------
        // EXISTING USER
        // ------------------------------------------------------

        if (user != null) {

            // --------------------------------------------------
            // LINK GOOGLE ACCOUNT
            // --------------------------------------------------

            if (user.getGoogleId() == null ||
                    user.getGoogleId().isBlank()) {

                user.setGoogleId(googleId);

                userRepository.save(user);
            }


            // --------------------------------------------------
            // CHECK ACCOUNT STATUS
            // --------------------------------------------------

            if (!Boolean.TRUE.equals(
                    user.getIsActive()
            )) {

                throw new RuntimeException(
                        "User account is disabled."
                );
            }
        }


        // ------------------------------------------------------
        // NEW GOOGLE USER
        // ------------------------------------------------------

        else {

            Role studentRole =
                    roleRepository
                            .findByRoleName("STUDENT")
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "STUDENT role not found"
                                    )
                            );

            user = new User();

            user.setGoogleId(googleId);

            user.setEmail(email);

            user.setFirstName(
                    firstName != null
                            ? firstName
                            : ""
            );

            user.setLastName(
                    lastName != null
                            ? lastName
                            : ""
            );

            user.setName(
                    name != null && !name.isBlank()
                            ? name
                            : email
            );

            /*
             * Google users authenticate through Google.
             * No local password is required.
             */
            user.setPassword(null);

            user.setIsActive(true);

            user.setCreatedAt(
                    LocalDateTime.now()
            );

            user.setRole(studentRole);

            userRepository.save(user);
        }


        // ------------------------------------------------------
        // GENERATE APPLICATION JWT
        // ------------------------------------------------------

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "role",
                user.getRole().getRoleName()
        );

        String token =
                jwtService.generateToken(
                        claims,
                        user.getEmail()
                );


        // ------------------------------------------------------
        // RESPONSE
        // ------------------------------------------------------

        return new AuthResponse(
                token,
                "Google Login Successful",
                user.getRole().getRoleName()
        );
    }
}