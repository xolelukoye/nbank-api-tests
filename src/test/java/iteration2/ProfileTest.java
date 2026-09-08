package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;


public class ProfileTest extends BaseTest {

    @Test
    void userCanChangeName() {
        String requestBody = """
                {
                "name": "Ivan Ivanov"
                }
                """;

        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .put("http://localhost:4111/api/v1/customer/profile")
        .then()
                .statusCode(HttpStatus.SC_OK);
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
        String requestBody = String.format(
                """
                {
                "name": "%s"
                }
                """, name);

        given()
                .header("Authorization", user1Token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void userCannotChangeNameWithoutToken() {
        String requestBody = """
                {
                "name": "Ivan Ivanov"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}
