package coLaon.ClaonBack.service;

import coLaon.ClaonBack.common.utils.JwtUtil;
import coLaon.ClaonBack.config.dto.JwtDto;
import coLaon.ClaonBack.user.domain.OAuth2Provider;
import coLaon.ClaonBack.user.domain.User;
import coLaon.ClaonBack.user.dto.OAuth2UserInfoDto;
import coLaon.ClaonBack.user.dto.SignInRequestDto;
import coLaon.ClaonBack.user.dto.SignUpRequestDto;
import coLaon.ClaonBack.user.dto.UserResponseDto;
import coLaon.ClaonBack.user.repository.UserRepository;
import coLaon.ClaonBack.user.service.OAuth2UserInfoProvider;
import coLaon.ClaonBack.user.service.OAuth2UserInfoProviderSupplier;
import coLaon.ClaonBack.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    OAuth2UserInfoProviderSupplier oAuth2UserInfoProviderSupplier;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    OAuth2UserInfoProvider oAuth2UserInfoProvider;

    @InjectMocks
    UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        this.user = User.of(
                "test",
                "test@gmail.com",
                "1234567890",
                "test",
                "경기도",
                "성남시",
                "",
                "123456",
                "test"
        );
    }

    @Test
    @DisplayName("Success case for sign in for completed user")
    void successSignInForCompletedUser() {
        // given
        SignInRequestDto signInRequestDto = new SignInRequestDto(
                "testCode"
        );

        OAuth2UserInfoDto oAuth2UserInfoDto = OAuth2UserInfoDto.of(
                "1234567890",
                "test@gmail.com"
        );

        JwtDto jwtDto = JwtDto.of(
                "access-token",
                "refresh-token",
                true
        );

        given(this.oAuth2UserInfoProviderSupplier.getProvider(OAuth2Provider.of("google"))).willReturn(this.oAuth2UserInfoProvider);
        given(this.oAuth2UserInfoProvider.getUserInfo(signInRequestDto.getCode())).willReturn(oAuth2UserInfoDto);

        given(this.userRepository.findByEmailAndOAuthId(oAuth2UserInfoDto.getEmail(), oAuth2UserInfoDto.getOAuthId())).willReturn(Optional.of(this.user));
        given(this.jwtUtil.createToken(this.user.getId(), true)).willReturn(jwtDto);

        // when
        JwtDto result = this.userService.signIn("google", signInRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result)
                .extracting("accessToken", "refreshToken")
                .contains("access-token", "refresh-token");
    }

    @Test
    @DisplayName("Success case for sign in for first access user")
    void successSignInForFirstAccessUser() {
        User firstAccessUser = User.of(
                "test",
                "test@gmail.com",
                "1234567890",
                "nickname",
                null,
                null,
                null,
                null,
                null
        );

        try (MockedStatic<User> mockedUser = mockStatic(User.class)) {
            // given
            SignInRequestDto signInRequestDto = new SignInRequestDto(
                    "testCode"
            );

            OAuth2UserInfoDto oAuth2UserInfoDto = OAuth2UserInfoDto.of(
                    "1234567890",
                    "test@gmail.com"
            );

            JwtDto jwtDto = JwtDto.of(
                    "access-token",
                    "refresh-token",
                    false
            );

            given(this.oAuth2UserInfoProviderSupplier.getProvider(OAuth2Provider.of("google"))).willReturn(this.oAuth2UserInfoProvider);
            given(this.oAuth2UserInfoProvider.getUserInfo(signInRequestDto.getCode())).willReturn(oAuth2UserInfoDto);

            given(this.userRepository.findByEmailAndOAuthId(oAuth2UserInfoDto.getEmail(), oAuth2UserInfoDto.getOAuthId())).willReturn(Optional.empty());

            mockedUser.when(() -> User.createNewUser("test@gmail.com", "1234567890")).thenReturn(firstAccessUser);
            given(this.userRepository.save(firstAccessUser)).willReturn(firstAccessUser);

            given(this.jwtUtil.createToken(this.user.getId(), true)).willReturn(jwtDto);

            // when
            JwtDto result = this.userService.signIn("google", signInRequestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result)
                    .extracting("accessToken", "refreshToken")
                    .contains("access-token", "refresh-token");
        }
    }

    @Test
    @DisplayName("Success case for sign up")
    void successSignUp() {
        // given
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto(
                "test",
                "경기도",
                "성남시",
                "",
                "123456",
                "test"
        );

        given(this.userRepository.findById("test")).willReturn(Optional.of(this.user));
        given(this.userRepository.findByNickname("test")).willReturn(Optional.empty());

        given(this.userRepository.save(this.user)).willReturn(this.user);

        // when
        UserResponseDto userResponseDto = this.userService.signUp(this.user.getId(), signUpRequestDto);

        // then
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto)
                .extracting("email", "nickname")
                .contains("test@gmail.com", "test");
    }
}
