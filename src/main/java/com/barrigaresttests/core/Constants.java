package com.barrigaresttests.core;
import io.restassured.http.ContentType;
public interface Constants {
    String API_BASE_URL = "https://barrigarest.wcaquino.me";
    Integer API_PORT = 443;//80 => http
    String BASE_PATH = "";
    ContentType API_CONTENT_TYPE = ContentType.JSON;
    Long API_TIMEOUT = 10000L;
}
