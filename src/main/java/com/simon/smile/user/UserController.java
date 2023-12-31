package com.simon.smile.user;

import com.simon.smile.common.Result;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidParameterException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("${api.base-url}/users")
@RequiredArgsConstructor
@Tag(name = "User Manager")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @Operation(summary = "Create user")
    @ApiResponse(responseCode = "201", description = "Create user success")
    @ApiResponse(responseCode = "400", description = "Invalid parameter")
    public Result createUser(@Parameter(name = "appUser", description = "User info")
                             @RequestBody @Valid AppUser appUser) {
        validatePassword(appUser.getPassword());
        validateUsernameNotPresent(appUser.getUsername());
        validateEmailNotPresent(appUser.getEmail());
        setNickname(appUser);
        appUser.setRoles(Roles.ROLE_USER.getRole()).setEnabled(true);
        AppUser savedUser = userService.create(appUser);
        return Result.success("Create user success")
                .setData(userToUserDtoConverter.convert(savedUser));
    }

    private final UserToUserDtoConverter userToUserDtoConverter;

    @DeleteMapping("/{id}")
    public Result deleteUserById(@PathVariable Integer id) {
        userService.deleteById(id);
        return Result.success("Delete user success");
    }

    @PostMapping("/filter")
    public Result filterUsers(@RequestBody AppUser appUser) {
        Stream<UserDto> filterUserList = userService.filter(appUser)
                .stream().map(userToUserDtoConverter::convert);
        return Result.success("Find user(s) success")
                .setData(filterUserList);
    }

    @GetMapping("/{id}")
    public Result findUserById(@PathVariable Integer id) {
        return Result.success("Find user success")
                .setData(userToUserDtoConverter.convert(userService.findById(id)));
    }

    @GetMapping
    public Result findUsers() {
        List<UserDto> userDtoList = userService.findAll()
                .stream()
                .map(userToUserDtoConverter::convert)
                .toList();
        return Result.success("Find all users success")
                .setData(userDtoList);
    }

    @GetMapping("/current_user")
    public Result getCurrentUser(HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        String name = userPrincipal.getName();
        AppUser appUser = userService.findByUsername(name).orElseThrow();
        return Result.success("Retrieve current user success")
                .setData(userToUserDtoConverter.convert(appUser));
    }

    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Integer id, @RequestBody @Valid AppUser appUser) {
        if (Objects.nonNull(appUser.getPassword())) {
            validatePassword(appUser.getPassword());
            appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        } else {
            appUser.setPassword(userService.findById(id).getPassword());
        }
        setNickname(appUser);
        return Result.success("Update user success")
                .setData(userToUserDtoConverter.convert(userService.update(id, appUser)));
    }

    private void validateEmailNotPresent(String email) {
        if (userService.findByEmail(email).isPresent()) {
            throw new InvalidParameterException("email already exists");
        }
    }

    private void validateUsernameNotPresent(String username) {
        if (userService.findByUsername(username).isPresent()) {
            throw new InvalidParameterException("username already exists");
        }
    }

    private void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("password is required");
        }
        /*
          - (?=.*[0-9])：at least a number
          - (?=.*[a-z])：at least a lower letter
          - (?=.*[A-Z])：at least a upper letter
          - (?=\\S+$)：no spaces
          - .{8,20}：at least 8 characters, at most 20 characters
         */
        String passwordRegex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,20}$";
        if (!password.matches(passwordRegex)) {
            throw new IllegalArgumentException("Password is not strong enough; 1. At least a number; 2. A least a lower letter; 3. At least a upper letter; 4. No spaces; 5. At least 8 characters, at most 20 characters");
        }
    }

    private void setNickname(AppUser appUser) {
        if (StringUtils.isEmpty(appUser.getNickname())) {
            appUser.setNickname(appUser.getUsername());
        }
    }
}
