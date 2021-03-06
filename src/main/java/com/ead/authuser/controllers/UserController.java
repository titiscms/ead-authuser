package com.ead.authuser.controllers;

import com.ead.authuser.configs.security.AuthenticationCurrentService;
import com.ead.authuser.configs.security.UserDetailsImpl;
import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(path = "/users")
public class UserController {

    public static final String MSG_USER_NOT_FOUND = "Error: User not found!";

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationCurrentService authenticationCurrentService;

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(SpecificationTemplate.UserSpec spec,
                                                       @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
                                                       Authentication authentication) {
        UserDetails userDetails = (UserDetailsImpl) authentication.getPrincipal();
        log.info("Authentication {}", userDetails.getUsername());
        Page<UserModel> userModelPage = userService.findAll(spec, pageable);
        if (!userModelPage.isEmpty()) {
            for (UserModel userModel : userModelPage.toList()) {
                userModel.add(linkTo(methodOn(UserController.class).getOneUser(userModel.getUserId())).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping(path = "/{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable UUID userId) {
        log.debug("GET getOneUser userId received {} ", userId);
        UUID currentUserId = authenticationCurrentService.getCurrentUser().getUserId();
        if (currentUserId.equals(userId)) {
            Optional<UserModel> userModelOptional = userService.findById(userId);
            if (userModelOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MSG_USER_NOT_FOUND);
            }
            UserModel userModel = userModelOptional.get();
            log.debug("GET getOneUser userId retrieved {} ", userModel.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(userModel);
        } else {
            throw new AccessDeniedException("Forbiden");
        }
    }

    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable UUID userId) {
        log.debug("DELETE deleteUser userId received {} ", userId);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MSG_USER_NOT_FOUND);
        }
        userService.deleteUser(userModelOptional.get());
        log.debug("DELETE deleteUser userId deleted {} ", userId);
        log.info("User deleted successfully userId {}", userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted successfully!");
    }

    @PutMapping(path = "/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable UUID userId, @RequestBody @Validated(UserDto.UserView.UserPut.class) @JsonView(UserDto.UserView.UserPut.class) UserDto userDto) {
        log.debug("PUT updateUser userDto {} received + userId {} ", userDto.toString(), userId);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MSG_USER_NOT_FOUND);
        }
        UserModel userModel = userModelOptional.get();
        userModel.setFullName(userDto.getFullName());
        userModel.setPhoneNumber(userDto.getPhoneNumber());
        userModel.setCpf(userDto.getCpf());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.updateUser(userModel);
        log.debug("PUT updateUser userId updated {} ", userModel.getUserId());
        log.info("User updated successfully userId {}", userModel.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

    @PutMapping(path = "/{userId}/password")
    public ResponseEntity<Object> updatePassword(@PathVariable UUID userId, @RequestBody @Validated(UserDto.UserView.PasswordPut.class) @JsonView(UserDto.UserView.PasswordPut.class) UserDto userDto) {
        log.debug("PUT updatePassword userDto {} received + userId {} ", userDto.toString(), userId);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MSG_USER_NOT_FOUND);
        }
        if (!userModelOptional.get().getPassword().equals(userDto.getOldPassword())) {
            log.warn("Mismatched old password userId {} ", userDto.getUserID());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Mismatched old password!");
        }
        UserModel userModel = userModelOptional.get();
        userModel.setPassword(userDto.getPassword());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.updatePassword(userModel);
        log.debug("PUT updateUser userId updated {} ", userModel.getUserId());
        log.info("Password updated successfully userId {}", userModel.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully!");
    }

    @PutMapping(path = "/{userId}/image")
    public ResponseEntity<Object> updateImage(@PathVariable UUID userId, @RequestBody @Validated(UserDto.UserView.ImagePut.class) @JsonView(UserDto.UserView.ImagePut.class) UserDto userDto) {
        log.debug("PUT updateImage userDto {} received + userId {} ", userDto.toString(), userId);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MSG_USER_NOT_FOUND);
        }
        UserModel userModel = userModelOptional.get();
        userModel.setImageUrl(userDto.getImageUrl());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.updateUser(userModel);
        log.debug("PUT updateImage userId updated {} ", userModel.getUserId());
        log.info("Image updated successfully userId {}", userModel.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

}
