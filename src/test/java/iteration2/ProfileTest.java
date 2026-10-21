package iteration2;

import base.BaseTest;
import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.CustomerResponse;
import models.ProfileResponse;
import models.UpdateProfileRequest;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import specs.ErrorMessages;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static requests.steps.AdminSteps.createUser;
import static requests.steps.UserSteps.getProfile;
import static requests.steps.UserSteps.updateName;


public class ProfileTest extends BaseTest {

    @Test
    void userCanChangeName() {
        // создаем пользователя
        CreateUserRequest user = createUser();
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                user.getUsername(), user.getPassword());

        // меняем имя
        String newName = "Ivan Ivanov";

        // собираем запрос в переменную для ассерта
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .name(newName)
                .build();

        ProfileResponse updateResponse = updateName(userSpec, newName);

        // сравниваем отправленный запрос и вложенный объект customer из ответа
        ModelAssertions.assertThatModels(updateRequest, updateResponse.getCustomer()).match();

        // проверяем сообщение об успехе
        softly.assertThat(updateResponse.getMessage())
                .isEqualTo(ErrorMessages.PROFILE_UPDATED.getMessage());

        // читаем профиль
        CustomerResponse profile = getProfile(userSpec);

        // сравниваем запрос и профиль
        ModelAssertions.assertThatModels(updateRequest, profile).match();
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
        CustomerResponse before = getProfile(userSpec);

        // попытка заменить имя на невалидное
        new CrudRequester(userSpec,
                Endpoint.UPDATE_PROFILE,
                ResponseSpecs.requestReturnsBadRequest())
                .update(UpdateProfileRequest.builder()
                        .name(name)
                        .build());

        // проверка, что имя не изменилось
        CustomerResponse after = getProfile(userSpec);

        softly.assertThat(after.getName()).isEqualTo(before.getName());
    }

    @Test
    void userCannotChangeNameWithoutToken() {
        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.UPDATE_PROFILE,
                ResponseSpecs.requestReturnsUnauthorized())
                .update(UpdateProfileRequest.builder()
                        .name("Ivan Ivanov")
                        .build());
    }
}