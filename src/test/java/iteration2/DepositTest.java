package iteration2;

import base.BaseTest;
import generators.RandomData;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import models.AccountResponse;
import models.CreateUserRequest;
import models.DepositRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.GetAllAccountsOfUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class DepositTest extends BaseTest {

    static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of(4999.99),
                Arguments.of(5000.0),
                Arguments.of(1.0),
                Arguments.of(0.01)
        );
    }

    @ParameterizedTest(name = "User1 can deposit {0}")
    @MethodSource("validDepositAmounts")
    void user1CanDeposit(double amount) {
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
        // забираем токен юзера
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                userRequest.getUsername(), userRequest.getPassword());
        // создание счета от имени пользователя
        ValidatableResponse createResponse = new CreateAccountRequester(
                userSpec,
                ResponseSpecs.entityWasCreated())
                .post(null);
        // забираем его id и баланс
        var json = createResponse.extract().jsonPath();
        long accountId = json.getLong("id");
        double balanceBefore = json.getDouble("balance");

        // депозит
        DepositRequest depositRequest = DepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();
        // получение баланса после депозита
        double balanceAfter = new DepositRequester(
                userSpec,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .jsonPath()
                .getDouble("balance");

        softly.assertThat(balanceAfter).isEqualTo(balanceBefore + amount);
    }

    static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(5001.01, "Deposit amount cannot exceed 5000"),
                Arguments.of(5001, "Deposit amount cannot exceed 5000"),
                Arguments.of(0, "Deposit amount must be at least 0.01"),
                Arguments.of(-100, "Deposit amount must be at least 0.01")
        );
    }

    @ParameterizedTest(name = "User cannot deposit {0}")
    @MethodSource("invalidDepositAmounts")
    void userCannotDeposit(double amount, String expectedError) {
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
        // забираем токен юзера
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                userRequest.getUsername(), userRequest.getPassword());
        // создание счета от имени пользователя
        ValidatableResponse createResponse = new CreateAccountRequester(
                userSpec,
                ResponseSpecs.entityWasCreated())
                .post(null);
        // забираем его id и баланс
        var json = createResponse.extract().jsonPath();
        long accountId = json.getLong("id");
        String accountNumber = json.getString("accountNumber");
        double balanceBefore = json.getDouble("balance");
        // невалидный депозит
        DepositRequest invalidDeposit = DepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new DepositRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequest(expectedError))
                .post(invalidDeposit);

        // смотрим все аккаунты пользователя и находим нужный
        List<AccountResponse> accounts = new GetAllAccountsOfUserRequester(
                userSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList("", AccountResponse.class);

        double balanceAfter = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account not found: " + accountNumber))
                .getBalance();

        softly.assertThat(balanceAfter).isEqualTo(balanceBefore);
    }

    @Test
    void userCannotDepositToNonExistentAccount() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        RequestSpecification userSpec = RequestSpecs.authAsUser(
                userRequest.getUsername(), userRequest.getPassword());

        // депозит на несуществующий id
        DepositRequest depositRequest = DepositRequest.builder()
                .id(Long.MAX_VALUE)
                .balance(100.0)
                .build();

        new DepositRequester(
                userSpec,
                ResponseSpecs.requestReturnsForbidden("Unauthorized access to account"))
                .post(depositRequest);
    }

    @Test
        // проверяем только отсутствие токена
    void userCannotDepositWithoutToken() {
        DepositRequest depositRequest = DepositRequest.builder()
                .id(Long.MAX_VALUE)
                .balance(100.0)
                .build();

        new DepositRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .post(depositRequest);
    }

}