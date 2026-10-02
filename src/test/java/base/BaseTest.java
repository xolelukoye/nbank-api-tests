package base;

import generators.RandomData;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import models.AccountResponse;
import models.CreateUserRequest;
import models.DepositRequest;
import models.UserRole;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.GetAllAccountsOfUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }

    // создание юзера
    protected CreateUserRequest createUser() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        return userRequest;
    }

    // создание счета, извлечение id
    protected long createAccount(RequestSpecification userSpec) {
        ValidatableResponse response = new CreateAccountRequester(
                userSpec, ResponseSpecs.entityWasCreated())
                .post(null);
        return response.extract().jsonPath().getLong("id");
    }

    // чтение балансов всех аккаунтов пользователя
    protected Map<Long, Double> getBalances(RequestSpecification userSpec) {
        List<AccountResponse> accounts = new GetAllAccountsOfUserRequester(
                userSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", AccountResponse.class);

        return accounts.stream()
                .collect(Collectors.toMap(AccountResponse::getId, AccountResponse::getBalance));
    }

    // пополнение счета (депозит)
    protected void deposit(RequestSpecification userSpec, long accountId, double amount) {
        new DepositRequester(userSpec, ResponseSpecs.requestReturnsOK())
                .post(DepositRequest.builder()
                        .id(accountId)
                        .balance(amount)
                        .build());
    }

    // разбивает пополнение счета на 5000
    protected void fundAccount(RequestSpecification userSpec, long accountId, double totalAmount) {
        double remaining = totalAmount;
        while (remaining > 0) {
            double chunk = Math.min(remaining, 5000.0);
            deposit(userSpec, accountId, chunk);
            remaining -= chunk;
        }
    }
}
