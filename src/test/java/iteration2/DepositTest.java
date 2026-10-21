package iteration2;

import base.BaseTest;
import generators.RandomData;
import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.DepositRequest;
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

import static org.assertj.core.api.Assertions.within;
import static requests.steps.AccountSteps.*;
import static requests.steps.AdminSteps.createUser;

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

        long accountId = createAccount(userSpec);

        // депозит
        deposit(userSpec, accountId, amount);

        // проверка через шаг
        double balanceAfter = getBalances(userSpec).get(accountId);

        // ожидаем, что баланс равен сумме депозита
        softly.assertThat(balanceAfter).isCloseTo(amount, within(RandomData.MONEY_TOLERANCE));
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

        long accountId = createAccount(userSpec);

        // запоминаем баланс до операции
        double balanceBefore = getBalances(userSpec).get(accountId);

        new CrudRequester(
                userSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest(expectedError.getMessage()))
                .post(DepositRequest.builder()
                        .id(accountId)
                        .balance(amount)
                        .build());

        // проверяем, что баланс не изменился
        double balanceAfter = getBalances(userSpec).get(accountId);

        softly.assertThat(balanceAfter).isCloseTo(balanceBefore, within(RandomData.MONEY_TOLERANCE));
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

        new CrudRequester(
                userSpec,
                Endpoint.DEPOSIT,
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

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsUnauthorized())
                .post(depositRequest);
    }

}