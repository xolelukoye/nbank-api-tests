package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class TransferTest extends BaseTest {

    @Test
    void userCannotTransferMoreThanBalance() {
        String topUpRequestBody = String.format(
                """
                {
                "id": %s,
                "balance": 500
                }
                """, user1Account1);
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(topUpRequestBody)
                .when()
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // переводим со счета на счет
        String transferRequestBody = String.format(
                """
                {
                "senderAccountId": %s,
                "receiverAccountId": %s,
                "amount": 501
                }
                """, user1Account1, user1Account2);
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(transferRequestBody)
                .when()
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    static Stream<Arguments> validTransfers() {
        return Stream.of(
                Arguments.of(user1Account1, user1Account2, 1000),
                Arguments.of(user1Account1, user1Account2, 9999.99),
                Arguments.of(user1Account1, user1Account2, 10000),
                Arguments.of(user1Account1, user1Account2, 0.01),
                Arguments.of(user1Account1, user2Account1, 500)
        );
    }

    @ParameterizedTest(name = "User can transfer {2} from account {0} to account {1}")
    @MethodSource("validTransfers")
    void user1CanTransfer(long senderAccountId, long receiverAccountId, double amount) {

        // пополняем счет, чтобы было что переводить
        String topUpRequestBody = String.format(
                """
                {
                "id": %s,
                "balance": %s
                }
                """, senderAccountId, amount);
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(topUpRequestBody)
        .when()
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // переводим со счета на счет
        String transferRequestBody = String.format(
                """
                {
                "senderAccountId": %s,
                "receiverAccountId": %s,
                "amount": %s
                }
                """, senderAccountId, receiverAccountId, amount);
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(transferRequestBody)
                .when()
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(-100),
                Arguments.of(0),
                Arguments.of(10000.01),
                Arguments.of(10001)
        );
    }

    @ParameterizedTest(name = "User cannot transfer {0}")
    @MethodSource("invalidTransferAmounts")
    void userCannotTransfer(double amount) {
        String requestBody = String.format(
                """
                {
                "senderAccountId": %s,
                "receiverAccountId": %s,
                "amount": %s
                }
                """, user1Account1, user1Account2, amount);
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void userCannotTransferFromNonExistentAccount() {
        String requestBody = String.format(
                """
                {
                "senderAccountId": 24,
                "receiverAccountId": %s,
                "amount": 500
                }
                """, user1Account2);
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void userCannotTransferToNonExistentAccount() {
        String requestBody = String.format(
                """
                {
                "senderAccountId": %s,
                "receiverAccountId": 5,
                "amount": 500
                }
                """, user1Account1);
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void userCannotTransferWithoutToken() {
        String requestBody = String.format(
                """
                {
                "senderAccountId": %s,
                "receiverAccountId": %s,
                "amount": 500
                }
                """, user1Account1, user1Account2);
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

}
