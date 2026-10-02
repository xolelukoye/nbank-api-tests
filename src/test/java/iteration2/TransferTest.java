package iteration2;

import base.BaseTest;
import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.TransferRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.TransferRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.within;

public class TransferTest extends BaseTest {

    @Test
    void userCannotTransferMoreThanBalance() {
        // создаем пользователя
        CreateUserRequest userRequest = createUser();

        // забираем токен юзера
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                userRequest.getUsername(), userRequest.getPassword());

        // создаем два счета
        long senderId = createAccount(userSpec);
        long receiverId = createAccount(userSpec);

        // проверка, что аккаунты действительно созданы у данного юзера
        Map<Long, Double> balancesAfterCreation = getBalances(userSpec);
        softly.assertThat(balancesAfterCreation).containsKeys(senderId, receiverId);

        // пополняем счет отправителя на 500
        deposit(userSpec, senderId, 500.0);

        // получаем балансы до перевода
        Map<Long, Double> before = getBalances(userSpec);

        // переводим 501
        new TransferRequester(userSpec,
                ResponseSpecs.requestReturnsBadRequest("Invalid transfer: insufficient funds or invalid accounts"))
                .post(TransferRequest.builder()
                        .senderAccountId(senderId)
                        .receiverAccountId(receiverId)
                        .amount(501.0)
                        .build());

        // проверяем балансы - не изменились
        Map<Long, Double> after = getBalances(userSpec);

        softly.assertThat(after.get(senderId))
                .isCloseTo(before.get(senderId), within(0.0001));

        softly.assertThat(after.get(receiverId))
                .isCloseTo(before.get(receiverId), within(0.0001));
    }

    static Stream<Arguments> validTransfers() {
        return Stream.of(
                Arguments.of(0.01, 0.01),
                Arguments.of(1.0, 1.0),
                Arguments.of(1000.0, 1000.0),
                Arguments.of(5000.0, 4999.99),
                Arguments.of(5000.0, 5000.0),
                Arguments.of(10000.0, 5000.01),
                Arguments.of(10000.0, 10000.0));
    }

    @ParameterizedTest(name = "User can transfer {1} when balance is {0}")
    @MethodSource("validTransfers")
    void userCanTransfer(double balanceToFund, double amountToTransfer) {
        // создаем пользователя
        CreateUserRequest userRequest = createUser();

        // забираем токен юзера
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                userRequest.getUsername(), userRequest.getPassword());

        // создаем два счета
        long senderId = createAccount(userSpec);
        long receiverId = createAccount(userSpec);

        // проверка, что аккаунты действительно созданы у данного юзера
        Map<Long, Double> balancesAfterCreation = getBalances(userSpec);
        softly.assertThat(balancesAfterCreation).containsKeys(senderId, receiverId);

        // пополняем счет отправитель
        fundAccount(userSpec, senderId, balanceToFund);

        // получаем балансы до
        Map<Long, Double> before = getBalances(userSpec);
        double senderBefore = before.get(senderId);
        double receiverBefore = before.get(receiverId);

        // трансфер
        new TransferRequester(userSpec, ResponseSpecs.requestReturnsOK())
                .post(TransferRequest.builder()
                        .senderAccountId(senderId)
                        .receiverAccountId(receiverId)
                        .amount(amountToTransfer)
                        .build());
        // получаем балансы после
        Map<Long, Double> after = getBalances(userSpec);

        softly.assertThat(after.get(senderId))
                .isCloseTo(senderBefore - amountToTransfer, within(0.0001));

        softly.assertThat(after.get(receiverId))
                .isCloseTo(receiverBefore + amountToTransfer, within(0.0001));

    }

    @Test
    void userCanTransferToAnotherUser() {
        double amount = 500.0;

        // создаем двух пользователей
        CreateUserRequest user1 = createUser();
        RequestSpecification user1Spec = RequestSpecs.authAsUser(
                user1.getUsername(), user1.getPassword()
        );

        CreateUserRequest user2 = createUser();
        RequestSpecification user2Spec = RequestSpecs.authAsUser(
                user2.getUsername(), user2.getPassword()
        );

        // создаем счета
        long senderId = createAccount(user1Spec);
        long receiverId = createAccount(user2Spec);

        // проверяем что оба счета действительно создались у каждого юзера
        softly.assertThat(getBalances(user1Spec)).containsKey(senderId);
        softly.assertThat(getBalances(user2Spec)).containsKey(receiverId);

        // пополняем счет отправителя
        deposit(user1Spec, senderId, amount);

        // проверяем балансы до перевода
        double senderBefore = getBalances(user1Spec).get(senderId);
        double receiverBefore = getBalances(user2Spec).get(receiverId);

        // трансфер со счета юзера1 на счет юзера2
        new TransferRequester(user1Spec, ResponseSpecs.requestReturnsOK())
                .post(TransferRequest.builder()
                        .senderAccountId(senderId)
                        .receiverAccountId(receiverId)
                        .amount(amount)
                        .build());

        // проверяем балансы после трансфера
        double senderAfter = getBalances(user1Spec).get(senderId);
        double receiverAfter = getBalances(user2Spec).get(receiverId);

        softly.assertThat(senderAfter).isCloseTo(senderBefore - amount, within(0.0001));
        softly.assertThat(receiverAfter).isCloseTo(receiverBefore + amount, within(0.0001));

    }

    //
    static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(-100, "Transfer amount must be at least 0.01"),
                Arguments.of(0, "Transfer amount must be at least 0.01"),
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(10001, "Transfer amount cannot exceed 10000")
        );
    }

    @ParameterizedTest(name = "User cannot transfer {0}")
    @MethodSource("invalidTransferAmounts")
    void userCannotTransfer(double amount, String expectedError) {
        // создание юзера
        CreateUserRequest user = createUser();
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                user.getUsername(), user.getPassword());

        // создание двух счетов для юзера
        long senderId = createAccount(userSpec);
        long receiverId = createAccount(userSpec);

        // проверка, что счета создались
        Map<Long, Double> balances = getBalances(userSpec);
        softly.assertThat(balances)
                .containsKeys(senderId, receiverId);

        // пополнение счета
        deposit(userSpec, senderId, 5000.0);

        // балансы до
        Map<Long, Double> before = getBalances(userSpec);

        // невалидный трансфер
        new TransferRequester(
                userSpec, ResponseSpecs.requestReturnsBadRequest(expectedError))
                .post(TransferRequest.builder()
                        .senderAccountId(senderId)
                        .receiverAccountId(receiverId)
                        .amount(amount)
                        .build());

        // проверка балансов - не изменились
        Map<Long, Double> after = getBalances(userSpec);

        softly.assertThat(after.get(senderId))
                .isCloseTo(before.get(senderId), within(0.0001));

        softly.assertThat(after.get(receiverId))
                .isCloseTo(before.get(receiverId), within(0.0001));
    }

    @Test
    void userCannotTransferFromNonExistentAccount() {
        // создание юзера
        CreateUserRequest user = createUser();
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                user.getUsername(), user.getPassword());

        // создание счета
        long receiverId = createAccount(userSpec);

        // проверка, что счет создался
        Map<Long, Double> balances = getBalances(userSpec);
        softly.assertThat(balances)
                .containsKey(receiverId);

        // трансфер с несуществующего счета
        new TransferRequester(
                userSpec,
                ResponseSpecs.requestReturnsForbidden(
                        "Unauthorized access to account"))
                .post(TransferRequest.builder()
                        .senderAccountId(Long.MAX_VALUE)
                        .receiverAccountId(receiverId)
                        .amount(500.0)
                        .build());
    }

    @Test
    void userCannotTransferToNonExistentAccount() {
        // создание юзера
        CreateUserRequest user = createUser();
        RequestSpecification userSpec = RequestSpecs.authAsUser(
                user.getUsername(), user.getPassword());

        // создание счета
        long senderId = createAccount(userSpec);

        // проверка, что счет создался
        Map<Long, Double> balances = getBalances(userSpec);
        softly.assertThat(balances)
                .containsKey(senderId);

        // пополнение счета
        deposit(userSpec, senderId, 500.0);

        // трансфер на несуществующий счет
        new TransferRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequest(
                        "Invalid transfer: insufficient funds or invalid accounts"))
                .post(TransferRequest.builder()
                        .senderAccountId(senderId)
                        .receiverAccountId(Long.MAX_VALUE)
                        .amount(500.0)
                        .build());
    }

    @Test
    void userCannotTransferWithoutToken() {
        // отсутствие токена проверяется первым - пользователь и аккаунт не нужны
        // просто делаем перевод
        new TransferRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsUnauthorized())
                .post(TransferRequest.builder()
                        .senderAccountId(Long.MAX_VALUE)
                        .receiverAccountId(Long.MAX_VALUE)
                        .amount(500.0)
                        .build());
    }

}