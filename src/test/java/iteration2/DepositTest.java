package iteration2;

import base.BaseTest;
import generators.RandomData;
import io.restassured.common.mapper.TypeRef;
import io.restassured.specification.RequestSpecification;
import models.AccountResponse;
import models.CreateUserRequest;
import models.DepositRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.GetAllAccountsOfUserRequester;
import specs.ErrorMessages;
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

    @ParameterizedTest(name = "User can deposit {0}")
    @MethodSource("validDepositAmounts")
    void userCanDeposit(double amount) {
        // данные для нового пользователя
        CreateUserRequest userRequest = createUser();

        // забираем токен юзера
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                userRequest.getUsername(), userRequest.getPassword());

        // создание счета от имени пользователя
        AccountResponse account = new CreateAccountRequester(
                userSpec, ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(AccountResponse.class);

        // депозит
        DepositRequest depositRequest = DepositRequest.builder()
                .id(account.getId())
                .balance(amount)
                .build();

        // получение баланса после депозита
        double balanceAfter = new DepositRequester(
                userSpec,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(AccountResponse.class)
                .getBalance();

        softly.assertThat(balanceAfter).isEqualTo(account.getBalance() + amount);
    }

    static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(5001.01, ErrorMessages.DEPOSIT_ABOVE_MAX),
                Arguments.of(5001, ErrorMessages.DEPOSIT_ABOVE_MAX),
                Arguments.of(0, ErrorMessages.DEPOSIT_BELOW_MIN),
                Arguments.of(-100, ErrorMessages.DEPOSIT_BELOW_MIN)
        );
    }

    @ParameterizedTest(name = "User cannot deposit {0}")
    @MethodSource("invalidDepositAmounts")
    void userCannotDeposit(double amount, ErrorMessages expectedError) {
        // данные для нового пользователя
        CreateUserRequest userRequest = createUser();

        // забираем токен юзера
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                userRequest.getUsername(), userRequest.getPassword());

        // создание счета от имени пользователя
        AccountResponse account = new CreateAccountRequester(
                userSpec,
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(AccountResponse.class);

        // невалидный депозит
        DepositRequest invalidDeposit = DepositRequest.builder()
                .id(account.getId())
                .balance(amount)
                .build();

        new DepositRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequest(expectedError.getMessage()))
                .post(invalidDeposit);

        // смотрим все аккаунты пользователя и находим нужный
        List<AccountResponse> accounts = new GetAllAccountsOfUserRequester(
                userSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(new TypeRef<List<AccountResponse>>() {
                });

        double balanceAfter = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(account.getAccountNumber()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account not found: " + account.getAccountNumber()))
                .getBalance();

        softly.assertThat(balanceAfter).isEqualTo(account.getBalance());
    }

    @Test
    void userCannotDepositToNonExistentAccount() {
        CreateUserRequest userRequest = createUser();

        RequestSpecification userSpec = RequestSpecs.authAsUser(
                userRequest.getUsername(), userRequest.getPassword());

        // депозит на несуществующий id
        DepositRequest depositRequest = DepositRequest.builder()
                .id(RandomData.getNonExistentAccountId())
                .balance(RandomData.getValidDepositAmount())
                .build();

        new DepositRequester(
                userSpec,
                ResponseSpecs.requestReturnsForbidden(ErrorMessages.UNAUTHORIZED_ACCESS.getMessage()))
                .post(depositRequest);
    }

    @Test
        // проверяем только отсутствие токена
    void userCannotDepositWithoutToken() {
        DepositRequest depositRequest = DepositRequest.builder()
                .id(RandomData.getNonExistentAccountId())
                .balance(RandomData.getValidDepositAmount())
                .build();

        new DepositRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .post(depositRequest);
    }

}