package iteration1;

import base.BaseTest;
import generators.RandomData;
import io.restassured.response.ValidatableResponse;
import models.AccountResponse;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.GetAllAccountsOfUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        // данные для нового пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя админом
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создание счета от имени пользователя
        ValidatableResponse createResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null);

        // достаем номер счета
        String createdAccountNumber = createResponse
                .extract()
                .path("accountNumber");

        // запрос на все аккаунты пользователя
        ValidatableResponse getAllAccountsResponse = new GetAllAccountsOfUserRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get();

        // данный запрос отдает массив, поэтому нужна десериализация в подготовленную модель
        List<AccountResponse> accounts = getAllAccountsResponse
                .extract()
                .jsonPath()
                .getList("", AccountResponse.class);

        // проверяем, что среди аккаунтов есть тот, который только что создали
        softly.assertThat(accounts)
                .extracting(AccountResponse::getAccountNumber)
                .contains(createdAccountNumber);
    }

}
