package steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.DiskResourceModel;

import static io.restassured.RestAssured.given;

public class DiskSteps {

    public static final String API_PREFIX = "/v1/disk";

    private static final String RESOURCES = API_PREFIX + "/resources";
    private static final String COPY = API_PREFIX + "/resources/copy";

    @Step("PUT: Создать папку path={folder.path}")
    public static Response createFolder(DiskResourceModel folder, String token) {
        return given()
                .log().all()
                .header("Authorization", "OAuth " + token)
                .queryParam("path", folder.getPath())
                .when()
                .put(RESOURCES)
                .then()
                .log().all()
                .extract().response();
    }

    @Step("GET: Получить метаданные path={resource.path}")
    public static Response getResource(DiskResourceModel resource, String token) {
        return given()
                .log().all()
                .header("Authorization", "OAuth " + token)
                .queryParam("path", resource.getPath())
                .when()
                .get(RESOURCES)
                .then()
                .log().all()
                .extract().response();
    }

    @Step("DELETE: Удалить ресурс path={resource.path} permanently={permanently}")
    public static Response deleteResource(DiskResourceModel resource, boolean permanently, String token) {
        return given()
                .log().all()
                .header("Authorization", "OAuth " + token)
                .queryParam("path", resource.getPath())
                .queryParam("permanently", permanently)
                .when()
                .delete(RESOURCES)
                .then()
                .log().all()
                .extract().response();
    }

    @Step("POST: Скопировать ресурс from={from.path} -> path={to.path} overwrite={overwrite}")
    public static Response copyResource(DiskResourceModel from, DiskResourceModel to, boolean overwrite, String token) {
        return given()
                .log().all()
                .header("Authorization", "OAuth " + token)
                .queryParam("from", from.getPath())
                .queryParam("path", to.getPath())
                .queryParam("overwrite", overwrite)
                .when()
                .post(COPY)
                .then()
                .log().all()
                .extract().response();
    }

    // для проверки 401
    @Step("GET: Запросить ресурс без авторизации (ожидаем 401)")
    public static Response getResourceWithoutAuth() {
        return given()
                .log().all()
                .queryParam("path", "app:/any") // любой путь, чтобы запрос был корректным
                .when()
                .get(RESOURCES)
                .then()
                .log().all()
                .extract().response();
    }

    @Step("WAIT: Если операция асинхронная (202 + href) — дождаться завершения")
    public static void awaitIfAsync(Response response, String token) {
        if (response == null) return;

        // ждём только для 202 (Accepted)
        if (response.statusCode() != 202) return;

        // если тело пустое — ничего не ждём
        String body = response.getBody() != null ? response.getBody().asString() : null;
        if (body == null || body.isBlank()) return;

        String href;
        try {
            href = response.jsonPath().getString("href");
        } catch (Exception e) {
            return; // тело не JSON или не распарсилось
        }

        if (href != null && !href.isBlank()) {
            awaitOperationSuccess(href, token);
        }
    }

    @Step("WAIT: Дождаться завершения операции href={operationHref}")
    public static void awaitOperationSuccess(String operationHref, String token) {
        for (int i = 0; i < 30; i++) {
            Response r = given()
                    .log().all()
                    .urlEncodingEnabled(false) //
                    .header("Authorization", "OAuth " + token)
                    .when()
                    .get(operationHref)
                    .then()
                    .log().all()
                    .extract().response();

            //  404 считаем временным и повторяем
            if (r.statusCode() == 404) {
                sleep(1000);
                continue;
            }

            if (r.statusCode() != 200) {
                throw new AssertionError("Статус операции вернул " + r.statusCode() + ": " + r.asString());
            }

            String status = r.jsonPath().getString("status");
            if ("success".equalsIgnoreCase(status)) return;
            if ("failed".equalsIgnoreCase(status)) {
                throw new AssertionError("Операция завершилась с ошибкой: " + r.asString());
            }

            sleep(1000);
        }

        throw new AssertionError("Операция не завершилась за отведённое время: " + operationHref);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Ожидание операции прервано");
        }
    }
}
