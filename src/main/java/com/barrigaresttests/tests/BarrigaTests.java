package com.barrigaresttests.tests;

import com.barrigaresttests.core.BaseTest;
import com.barrigaresttests.core.Movimentation;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static io.restassured.RestAssured.given;

public class BarrigaTests extends BaseTest {

    private String TOKEN = "JWT ";
    private Movimentation MOVIMENTATION;

    @Before
    public void setupLoginAndMovimentation(){
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

        MOVIMENTATION = new Movimentation();

        MOVIMENTATION.setConta_id(2187017);
        MOVIMENTATION.setDescricao("Testes de Api");
        MOVIMENTATION.setEnvolvido("Envolvido");
        MOVIMENTATION.setTipo("REC");
        MOVIMENTATION.setData_transacao("01/05/2020");
        MOVIMENTATION.setData_pagamento("01/01/2020");
        MOVIMENTATION.setValor(100f);
        MOVIMENTATION.setStatus(true);
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

    @Test
    public void shouldCreateNewMovimentation(){
        given()
                    .header("Authorization", TOKEN)
                    .body(MOVIMENTATION)
                .when()
                    .post("/transacoes")
                .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .body("id", Matchers.notNullValue())
                    .body("status", Matchers.is(MOVIMENTATION.getStatus()))
                    .body("descricao", Matchers.is(MOVIMENTATION.getDescricao()))
                    .body("envolvido", Matchers.is(MOVIMENTATION.getEnvolvido()))
                    .body("tipo", Matchers.is(MOVIMENTATION.getTipo()))
                    .body("conta_id", Matchers.is(MOVIMENTATION.getConta_id()));
    }

    @Test
    public void shouldNotCreateNewMovimentationWithRequestBodyInvalid(){
        given()
                    .header("Authorization", TOKEN)
                    .body("{}")
                .when()
                    .post("/transacoes")
                .then()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .body("$", Matchers.hasSize(8))
                    .body("msg", Matchers.hasItems(
                            "Data da Movimentação é obrigatório",
                            "Data do pagamento é obrigatório",
                            "Descrição é obrigatório",
                            "Interessado é obrigatório",
                            "Valor é obrigatório",
                            "Valor deve ser um número",
                            "Conta é obrigatório",
                            "Situação é obrigatório"
                    ));
    }

    @Test
    public void shouldNotCreateNewMovimentationWithFutureDateTransaction(){
        Date new_data_transacao = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        new_data_transacao.setYear(125);

        MOVIMENTATION.setData_transacao(sdf.format(new_data_transacao));

        given()
                    .header("Authorization", TOKEN)
                    .body(MOVIMENTATION)
                .when()
                    .post("/transacoes")
                .then()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .body("$", Matchers.hasSize(1))
                    .body("msg", Matchers.hasItems("Data da Movimentação deve ser menor ou igual à data atual"));
    }

    @Test
    public void shouldNotRemoveAccountWithMovimentations(){
        given()
                    .header("Authorization", TOKEN)
                .when()
                    .delete("/contas/2187017")
                .then()
                    .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body("constraint", Matchers.is("transacoes_conta_id_foreign"));
    }

    @Test
    public void shouldCalculateCurrentValueOfAccount(){
        given()
                    .header("Authorization", TOKEN)
                .when()
                    .get("/saldo")
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("find{it.conta_id == 2187017}.saldo", Matchers.notNullValue());
    }

    @Test
    public void shouldRemoveMovimentation(){
        Integer movimentation_id = given()
                    .header("Authorization", TOKEN)
                    .body(MOVIMENTATION)
                .when()
                    .post("/transacoes")
                .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract().path("id");

        given()
                    .header("Authorization", TOKEN)
                .when()
                .delete("/transacoes/" + movimentation_id)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
