package iteration2;

import base.BaseTest;
import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.CustomerResponse;
import models.ProfileResponse;
import models.UpdateProfileRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.GetProfileRequester;
import requests.UpdateProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;


public class ProfileTest extends BaseTest {

    @Test
    void userCanChangeName() {
        // создаем пользователя
        CreateUserRequest user = createUser();
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                user.getUsername(), user.getPassword());

        // меняем имя через put
        String newName = "Ivan Ivanov";

        ProfileResponse updateResponse = new UpdateProfileRequester(
                userSpec, ResponseSpecs.requestReturnsOK())
                .put(UpdateProfileRequest.builder().name(newName).build())
                .extract()
                .as(ProfileResponse.class);

        // проверка имени в ответе и сообщения об успехе
        softly.assertThat(updateResponse.getCustomer().getName())
                .isEqualTo(newName);

        softly.assertThat(updateResponse.getMessage())
                .isEqualTo("Profile updated successfully");

        // читаем профиль через get
        CustomerResponse profile = new GetProfileRequester(
                userSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(CustomerResponse.class);

        // проверяем, что имя изменилось
        softly.assertThat(profile.getName())
                .isEqualTo(newName);
    }

    static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of("Ivan"),
                Arguments.of("   "),
                Arguments.of("Ivan123 Ivanov"),
                Arguments.of("Ivan @Ivanov"),
                Arguments.of("Ivan Ivanovich Ivanov"),
                Arguments.of("")
        );
    }

    @ParameterizedTest(name = "User cannot change name to \"{0}\"")
    @MethodSource("invalidNames")
    void userCannotChangeName(String name) {
        // создаем пользователя
        CreateUserRequest user = createUser();
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                user.getUsername(), user.getPassword());

        // имя до
        CustomerResponse before = new GetProfileRequester(
                userSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(CustomerResponse.class);

        // попытка заменить имя на невалидное
        new UpdateProfileRequester(userSpec, ResponseSpecs.requestReturnsBadRequest())
                .put(UpdateProfileRequest.builder()
                        .name(name).build());

        // проверка, что имя не изменилось
        CustomerResponse after = new GetProfileRequester(userSpec,
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(CustomerResponse.class);

        softly.assertThat(after.getName()).isEqualTo(before.getName());
    }

    @Test
    void userCannotChangeNameWithoutToken() {
        new UpdateProfileRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .put(UpdateProfileRequest.builder()
                        .name("Ivan Ivanov")
                        .build());
    }
}