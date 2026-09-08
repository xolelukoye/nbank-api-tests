package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

import static io.restassured.RestAssured.given;

public class BaseTest {

    protected static String user1Token;
    protected static String user2Token;

    protected static long user1Account1;
    protected static long user1Account2;
    protected static long user2Account1;

    @BeforeAll
    public static void setUp() {
        // logging
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));

        // admin log in
        String adminToken =
            given()
                    .contentType(ContentType.JSON)
                    .body("""
                    {
                    "username": "admin",
                    "password": "admin"
                    }
                    """)
            .when()
                    .post("http://localhost:4111/api/v1/auth/login")
            .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .header("Authorization");

        // create a new user1
            given()
                    .header("Authorization", adminToken)
                    .contentType(ContentType.JSON)
                    .body("""
                    {
                    "username": "user1",
                    "password": "verysTRongPassword33$",
                    "role": "USER"
                    }
                    """)
            .when()
                    .post("http://localhost:4111/api/v1/admin/users")
            .then()
                    .statusCode(HttpStatus.SC_OK);

        // create a new user2
            given()
                    .header("Authorization", adminToken)
                    .contentType(ContentType.JSON)
                    .body("""
                    {
                    "username": "user2",
                    "password": "verysTRongPassword34$",
                    "role": "USER"
                    }
                    """)
            .when()
                    .post("http://localhost:4111/api/v1/admin/users")
            .then()
                    .statusCode(HttpStatus.SC_OK);

        // user1 can log in
            user1Token =
            given()
                    .contentType(ContentType.JSON)
                    .body("""
                    {
                    "username": "user1",
                    "password": "verysTRongPassword33$"
                    }
                    """)
            .when()
                    .post("http://localhost:4111/api/v1/auth/login")
            .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .header("Authorization");

        // user2 can log in
            user2Token =
            given()
                    .contentType(ContentType.JSON)
                    .body("""
                    {
                    "username": "user2",
                    "password": "verysTRongPassword34$"
                    }
                    """)
            .when()
                    .post("http://localhost:4111/api/v1/auth/login")
            .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .header("Authorization");

        // user1 create account1
            user1Account1 =
            given()
                    .header("Authorization", user1Token)
            .when()
                    .post("http://localhost:4111/api/v1/accounts")
            .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .path("id");

        // user1 create account2
            user1Account2 =
            given()
                    .header("Authorization", user1Token)
            .when()
                    .post("http://localhost:4111/api/v1/accounts")
            .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .path("id");

        // user2 create account1
            user2Account1 =
            given()
                    .header("Authorization", user2Token)
            .when()
                    .post("http://localhost:4111/api/v1/accounts")
            .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .path("id");
    }

}
