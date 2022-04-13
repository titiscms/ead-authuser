package com.ead.authuser.controllers;

import com.ead.authuser.dtos.AdminDto;
import com.ead.authuser.enums.RoleType;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.RoleModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.RoleService;
import com.ead.authuser.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(path = "/administrators")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping(path = "/subscription")
    public ResponseEntity<Object> saveSubscriptionAdmin(@RequestBody @Valid AdminDto adminDto) {
        Optional<UserModel> userModelOptional = userService.findById(adminDto.getUserId());
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        var userModel = userModelOptional.get();
        userModel.setUserType(UserType.ADMIN);
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        RoleModel roleModel = roleService.findByRoleName(RoleType.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        userModel.getRoles().add(roleModel);
        var userModelSaved = userService.updateUser(userModel);
        return ResponseEntity.status(HttpStatus.OK).body(userModelSaved);
    }

}
