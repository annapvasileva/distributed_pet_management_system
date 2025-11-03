package ru.annapvasileva.controllers.users;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.annapvasileva.persistence.Roles;
import ru.annapvasileva.services.users.UserDto;
import ru.annapvasileva.services.users.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    @PreAuthorize("permitAll()")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request.getUsername(), request.getPassword(), Roles.ROLE_USER, request.getOwnerId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createAdmin(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request.getUsername(), request.getPassword(), Roles.ROLE_ADMIN, request.getOwnerId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("(hasRole('USER') and #username == authentication.name) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUserByUsername(username);

        return ResponseEntity.noContent().build();
    }

    @PutMapping
    @PreAuthorize("(hasRole('USER') and #userDto.username == authentication.name) or hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto) {
        return userService.updateUser(userDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
