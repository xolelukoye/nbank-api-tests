package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class DepositTest extends BaseTest {

    static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of(4999.99),
                Arguments.of(5000),
                Arguments.of(1),
                Arguments.of(0.01)
        );
    }

    @ParameterizedTest(name = "User1 can deposit {0}")
    @MethodSource("validDepositAmounts")
    void user1CanDeposit(double amount) {
        String requestBody = String.format(
                """
                        {
                        "id": %s,
                        "balance": %s
                        }
                        """, user1Account1, amount);
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("http://localhost:4111/api/v1/accounts/deposit")
        .then()
                .statusCode(HttpStatus.SC_OK);
    }

    static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(5001.01),
                Arguments.of(5001),
                Arguments.of(0),
                Arguments.of(-100)
        );
    }

    @ParameterizedTest(name = "User1 cannot deposit {0}")
    @MethodSource("invalidDepositAmounts")
    void user1CannotDeposit(double amount) {
        String requestBody = String.format(
                """
                        {
                        "id": %s,
                        "balance": %s
                        }
                        """, user1Account1, amount);
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("http://localhost:4111/api/v1/accounts/deposit")
        .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void user1CannotDepositToNonExistentAccount() {
        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                        "id": 38,
                        "balance": 100
                        }
                        """)
                .when()
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void user1CannotDepositWithoutToken() {
        String requestBody = String.format(
                """
                        {
                        "id": %s,
                        "balance": 100
                        }
                        """, user1Account1);
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

}
