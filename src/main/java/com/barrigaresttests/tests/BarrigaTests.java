package com.barrigaresttests.tests;

import com.barrigaresttests.core.BaseTest;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class BarrigaTests extends BaseTest {
    private String TOKEN = "JWT ";

    @Before
    public void login(){
        Map<String, String> login = new HashMap<>();

        login.put("email", "mateus19sousa@gmail.com");
        login.put("senha", "123456");

        TOKEN = TOKEN + given()
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().path("token");
    }

    @Test
    public void shouldNotAccessWithoutAToken(){
        given()
                .when()
                    .get("/contas")
                .then()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void shouldCreateNewAccount(){
        given()
                    .header("Authorization", TOKEN)
                    .body("{ \"nome\": \"mateus tests " + UUID.randomUUID() + "\"}")
                .when()
                    .post("/contas")
                .then()
                    .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void shouldPutAccount(){
        given()
                    .header("Authorization", TOKEN)
                    .body("{ \"nome\": \"mateus tests atualizado\"}")
                .when()
                    .put("/contas/2187017")
                .then()
                    .statusCode(HttpStatus.SC_OK)
                .body("nome", Matchers.is("mateus tests atualizado"));
    }

    @Test
    public void shouldNotCreateNewAccountWithSameName(){
        given()
                    .header("Authorization", TOKEN)
                    .body("{ \"nome\": \"mateus tests\"}")
                .when()
                    .post("/contas")
                .then()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .body("error", Matchers.is("Já existe uma conta com esse nome!"));
    }
}
