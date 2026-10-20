package com.rikai.backend.service;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.user.ChangePasswordRequest;
import com.rikai.backend.dto.request.user.UserCreationRequest;
import com.rikai.backend.dto.request.user.UserUpdateRequest;
import com.rikai.backend.dto.response.user.UserResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.UserMapper;
import com.rikai.backend.model.Department;
import com.rikai.backend.model.Enum.RoleType;
import com.rikai.backend.model.Roles;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.DepartmentRepository;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.RefreshTokenRepository;
import com.rikai.backend.repository.RolesRepository;
import com.rikai.backend.repository.UsersRepository;
import com.rikai.backend.service.auth.IAuthenticationService;
import com.rikai.backend.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private InternRepository internRepository;

    @Mock
    private IAuthenticationService authenticationService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private Users currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new Users();
        currentUser.setId(UUID.randomUUID());
        currentUser.setEmail("thanh@gmail.com");
        currentUser.setFullName("Thanh Nguyen");
        currentUser.setPasswordHash("hashed_old_password");
        currentUser.setIsActive(true);
    }

    @Test
    @DisplayName("Change password successfully: updates password hash, saves user, and revokes refresh tokens")
    void changePassword_Success() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("NewPass123")
                .confirmPassword("NewPass123")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        // check old password is correct
        when(passwordEncoder.matches("OldPass123", "hashed_old_password")).thenReturn(true);
        // check new password is not same as old password
        when(passwordEncoder.matches("NewPass123", "hashed_old_password")).thenReturn(false);
        
        when(passwordEncoder.encode("NewPass123")).thenReturn("hashed_new_password");
        when(usersRepository.save(currentUser)).thenReturn(currentUser);

        userService.changePassword(request);

        assertEquals("hashed_new_password", currentUser.getPasswordHash());
        verify(usersRepository).save(currentUser);
        verify(refreshTokenRepository).deleteByUser(currentUser);
    }

    @Test
    @DisplayName("Change password fails: confirmPassword does not match newPassword throws PASSWORD_CONFIRM_NOT_MATCH")
    void changePassword_PasswordConfirmNotMatch_ThrowsException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("NewPass123")
                .confirmPassword("DifferentPass123")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);

        AppException exception = assertThrows(AppException.class, () -> userService.changePassword(request));
        assertEquals(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH, exception.getErrorCode());

        verifyNoInteractions(passwordEncoder);
        verify(usersRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    @Test
    @DisplayName("Change password fails: incorrect old password throws PASSWORD_NOT_CORRECT")
    void changePassword_IncorrectOldPassword_ThrowsException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("WrongOldPass123")
                .newPassword("NewPass123")
                .confirmPassword("NewPass123")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(passwordEncoder.matches("WrongOldPass123", "hashed_old_password")).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> userService.changePassword(request));
        assertEquals(ErrorCode.PASSWORD_NOT_CORRECT, exception.getErrorCode());

        verify(passwordEncoder, never()).encode(any());
        verify(usersRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    @Test
    @DisplayName("Change password fails: new password is same as old password throws PASSWORD_SAME_AS_OLD")
    void changePassword_SameAsOldPassword_ThrowsException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("SamePass123")
                .newPassword("SamePass123")
                .confirmPassword("SamePass123")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(passwordEncoder.matches("SamePass123", "hashed_old_password")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> userService.changePassword(request));
        assertEquals(ErrorCode.PASSWORD_SAME_AS_OLD, exception.getErrorCode());

        verify(passwordEncoder, never()).encode(any());
        verify(usersRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    @Test
    @DisplayName("Change password fails: token revocation error propagates exception")
    void changePassword_TokenRevocationFails_ThrowsException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("NewPass123")
                .confirmPassword("NewPass123")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(passwordEncoder.matches("OldPass123", "hashed_old_password")).thenReturn(true);
        when(passwordEncoder.matches("NewPass123", "hashed_old_password")).thenReturn(false);
        when(passwordEncoder.encode("NewPass123")).thenReturn("hashed_new_password");
        when(usersRepository.save(currentUser)).thenReturn(currentUser);
        doThrow(new RuntimeException("Token delete failure")).when(refreshTokenRepository).deleteByUser(currentUser);   

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.changePassword(request));
        assertEquals("Token delete failure", exception.getMessage());
        verify(usersRepository).save(currentUser);
    }

    @Test
    @DisplayName("Update self user successfully: does not alter isActive, passwordHash, email, fullName, and does not invoke userMapper.updateUser")
    void updateSelfUser_Success_DoesNotChangePasswordOrIsActive() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFullName("Changed Doe");
        request.setEmail("changed@gmail.com");
        request.setPassword("NewPassword123");
        request.setActive(false);
        request.setDepartmentId(null);

        UserResponse mockResponse = UserResponse.builder()
                .id(currentUser.getId().toString())
                .email(currentUser.getEmail())
                .fullName(currentUser.getFullName())
                .isActive(currentUser.getIsActive())
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(usersRepository.save(currentUser)).thenReturn(currentUser);
        when(userMapper.toUserResponse(currentUser)).thenReturn(mockResponse);

        UserResponse result = userService.updateSelfUser(request);

        assertNotNull(result);
        assertTrue(currentUser.getIsActive());
        assertEquals("hashed_old_password", currentUser.getPasswordHash());
        assertEquals("thanh@gmail.com", currentUser.getEmail());
        assertEquals("Thanh Nguyen", currentUser.getFullName());
        verify(userMapper, never()).updateUser(any(), any());
        verify(usersRepository).save(currentUser);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Update self user with department: resolves department and updates user")
    void updateSelfUser_WithDepartment_Success() {
        Long deptId = 5L;
        Department department = Department.builder()
                .id(deptId)
                .title("RTE")
                .build();

        UserUpdateRequest request = new UserUpdateRequest();
        request.setDepartmentId(deptId);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(department));
        when(usersRepository.save(currentUser)).thenReturn(currentUser);
        when(userMapper.toUserResponse(currentUser)).thenReturn(new UserResponse());

        userService.updateSelfUser(request);

        assertEquals(department, currentUser.getDepartment());
        verify(departmentRepository).findById(deptId);
        verify(usersRepository).save(currentUser);
    }

    @Test
    @DisplayName("Update self user with invalid department: throws DEPARTMENT_NOT_EXISTED")
    void updateSelfUser_DepartmentNotExisted_ThrowsException() {
        Long invalidDeptId = 999L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setDepartmentId(invalidDeptId);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(departmentRepository.findById(invalidDeptId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> userService.updateSelfUser(request));
        assertEquals(ErrorCode.DEPARTMENT_NOT_EXISTED, exception.getErrorCode());

        verify(usersRepository, never()).save(any());
    }

    @Test
    @DisplayName("User change password successfully: resets mustChangePassword flag to false")
    void changePassword_Success_ClearsMustChangePasswordFlag() {
        currentUser.setMustChangePassword(true);
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("NewPass123")
                .confirmPassword("NewPass123")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(passwordEncoder.matches("OldPass123", "hashed_old_password")).thenReturn(true);
        when(passwordEncoder.matches("NewPass123", "hashed_old_password")).thenReturn(false);
        when(passwordEncoder.encode("NewPass123")).thenReturn("hashed_new_password");

        userService.changePassword(request);

        assertFalse(currentUser.getMustChangePassword());
    }

    @Test
    @DisplayName("Create user: sets mustChangePassword to true and encodes the provided password")
    void createUser_Success_SetsMustChangePasswordTrue() {
        UserCreationRequest request = UserCreationRequest.builder()
                .password("Abc12345")
                .fullName("Nguyen Van A")
                .roleName(RoleType.MENTOR)
                .departmentId(1L)
                .build();
        Roles role = Roles.builder().roleName("MENTOR").build();
        Department department = Department.builder().id(1L).title("RTE").build();

        when(rolesRepository.findByRoleName("MENTOR")).thenReturn(Optional.of(role));
        when(userMapper.toUser(request)).thenReturn(new Users());
        when(passwordEncoder.encode("Abc12345")).thenReturn("hashed_password");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(Users.class))).thenReturn(new UserResponse());

        userService.createUser(request);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(usersRepository).save(captor.capture());
        assertTrue(captor.getValue().getMustChangePassword());
        assertEquals("hashed_password", captor.getValue().getPasswordHash());
        verify(passwordEncoder).encode("Abc12345");
    }

    @Test
    @DisplayName("Update user with password: sets mustChangePassword and revokes only that user's refresh tokens")
    void updateUser_WithPassword_SetsFlagAndRevokesTokens() {
        UUID targetId = UUID.randomUUID();
        Users target = new Users();
        target.setId(targetId);
        target.setPasswordHash("hashed_old_password");
        Users otherUser = new Users();
        otherUser.setId(UUID.randomUUID());
        Department department = Department.builder().id(1L).title("RTE").build();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPassword("NewPass123");
        request.setDepartmentId(1L);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(usersRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(passwordEncoder.encode("NewPass123")).thenReturn("hashed_new_password");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(usersRepository.save(target)).thenReturn(target);
        when(userMapper.toUserResponse(target)).thenReturn(new UserResponse());

        userService.updateUser(targetId, request);

        assertTrue(target.getMustChangePassword());
        assertEquals("hashed_new_password", target.getPasswordHash());
        verify(refreshTokenRepository).deleteByUser(target);
        verify(refreshTokenRepository, never()).deleteByUser(otherUser);
    }

    @Test
    @DisplayName("Update user without password: keeps mustChangePassword unchanged and does not revoke tokens")
    void updateUser_WithoutPassword_KeepsFlagAndTokens() {
        UUID targetId = UUID.randomUUID();
        Users target = new Users();
        target.setId(targetId);
        target.setPasswordHash("hashed_old_password");
        target.setMustChangePassword(false);
        Department department = Department.builder().id(1L).title("RTE").build();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setDepartmentId(1L);

        when(usersRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(usersRepository.save(target)).thenReturn(target);
        when(userMapper.toUserResponse(target)).thenReturn(new UserResponse());

        userService.updateUser(targetId, request);

        assertFalse(target.getMustChangePassword());
        assertEquals("hashed_old_password", target.getPasswordHash());
        verify(passwordEncoder, never()).encode(any());
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    @Test
    @DisplayName("Update own account with password: throws USE_CHANGE_PASSWORD_INSTEAD and changes nothing")
    void updateUser_SelfWithPassword_ThrowsUseChangePasswordInstead() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPassword("NewPass123");
        request.setDepartmentId(1L);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);

        AppException exception = assertThrows(AppException.class,
                () -> userService.updateUser(currentUser.getId(), request));
        assertEquals(ErrorCode.USE_CHANGE_PASSWORD_INSTEAD, exception.getErrorCode());

        assertEquals("hashed_old_password", currentUser.getPasswordHash());
        verify(usersRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    @Test
    @DisplayName("Update own account without password: still succeeds")
    void updateUser_SelfWithoutPassword_Success() {
        Department department = Department.builder().id(1L).title("RTE").build();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setDepartmentId(1L);

        when(usersRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(usersRepository.save(currentUser)).thenReturn(currentUser);
        when(userMapper.toUserResponse(currentUser)).thenReturn(new UserResponse());

        userService.updateUser(currentUser.getId(), request);

        verify(usersRepository).save(currentUser);
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }
}
