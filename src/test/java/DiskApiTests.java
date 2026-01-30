
import data.TestData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.DiskResourceModel;
import org.junit.*;
import steps.DiskSteps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DiskApiTests extends BaseApiTest {

    private DiskResourceModel rootFolder;
    private DiskResourceModel copyFolder;

    @Before
    public void beforeEach() {
        // тест 401 тоже будет зависеть от того, что токен задан
        Assume.assumeTrue(
                "Не задан OAuth-токен в переменной окружения " + TestData.TOKEN_ENV,
                token != null && !token.isBlank()
        );

        rootFolder = new DiskResourceModel(TestData.root());
        copyFolder = new DiskResourceModel(rootFolder.getPath() + "-copy");
    }

    @After
    public void afterEach() {
        safeDelete(copyFolder);
        safeDelete(rootFolder);
    }

    @Test
    @DisplayName("Создание папки на Диске")
    @Description("PUT /resources — создание папки на Яндекс.Диске")
    public void createFolder_shouldReturn201or409() {
        Response response = DiskSteps.createFolder(rootFolder, token);

        assertThat(
                "Создание папки должно вернуть 201 (создано) или 409 (уже существует)",
                response.statusCode(),
                anyOf(is(201), is(409))
        );
    }

    @Test
    @DisplayName("Получение метаданных папки")
    @Description("GET /resources — получение метаданных созданной папки")
    public void getFolderMetadata_shouldReturn200_andDirType() {
        createRootFolder();

        Response response = DiskSteps.getResource(rootFolder, token);

        assertThat(response.statusCode(), is(200));

        String type = response.jsonPath().getString("type");
        assertThat(type, is("dir"));

        String name = response.jsonPath().getString("name");
        String expectedName = rootFolder.getPath().replace("app:/", "");
        assertThat(name, is(expectedName));

        String diskPath = response.jsonPath().getString("path");
        assertThat(diskPath, startsWith("disk:/"));

    }

    @Test
    @DisplayName("Копирование папки")
    @Description("POST /resources/copy — копирование папки")
    public void copyFolder_shouldCreateCopy() {
        createRootFolder();

        Response copyResponse = DiskSteps.copyResource(rootFolder, copyFolder, true, token);

        assertThat(
                "Копирование должно вернуть 201 или 202",
                copyResponse.statusCode(),
                anyOf(is(201), is(202))
        );

        DiskSteps.awaitIfAsync(copyResponse, token);

        Response meta = DiskSteps.getResource(copyFolder, token);
        assertThat(meta.statusCode(), is(200));
        assertThat(meta.jsonPath().getString("type"), is("dir"));

    }

    @Test
    @DisplayName("Удаление папки")
    @Description("DELETE /resources — удаление папки")
    public void deleteFolder_shouldRemoveResource() {
        createRootFolder();

        Response deleteResponse = DiskSteps.deleteResource(rootFolder, true, token);

        assertThat(
                "Удаление должно вернуть 202 или 204",
                deleteResponse.statusCode(),
                anyOf(is(202), is(204))
        );

        DiskSteps.awaitIfAsync(deleteResponse, token);

        Response afterDelete = DiskSteps.getResource(rootFolder, token);
        assertThat(
                "После удаления папка должна быть недоступна",
                afterDelete.statusCode(),
                is(404)
        );
    }

    @Test
    @DisplayName("Запрос без авторизации")
    @Description("GET /resources — запрос без OAuth-токена должен возвращать 401")
    public void unauthorizedRequest_shouldReturn401() {
        Response response = DiskSteps.getResourceWithoutAuth();

        assertThat(
                "Запрос без токена должен возвращать 401",
                response.statusCode(),
                is(401)
        );
    }

    private void createRootFolder() {
        Response r = DiskSteps.createFolder(rootFolder, token);
        assertThat("Создание папки должно вернуть 201 или 409", r.statusCode(), anyOf(is(201), is(409)));
    }

    private void safeDelete(DiskResourceModel resource) {
        try {
            Response deleteResponse = DiskSteps.deleteResource(resource, true, token);
            DiskSteps.awaitIfAsync(deleteResponse, token);
        } catch (Exception ignored) {
            // best-effort cleanup
        }
    }
}
