package iteration1;

import base.BaseTest;
import io.restassured.response.ValidatableResponse;
import models.AccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        // данные для нового пользователя
        CreateUserRequest userRequest = AdminSteps.createUser();

        // создание счета от имени пользователя
        ValidatableResponse createResponse = new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // достаем номер счета
        String createdAccountNumber = createResponse
                .extract()
                .path("accountNumber");

        // запрос на все аккаунты пользователя
        ValidatableResponse getAllAccountsResponse = new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.GET_ALL_CUSTOMER_ACCOUNTS,
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
