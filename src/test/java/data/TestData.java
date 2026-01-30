package data;

import java.util.UUID;

public class TestData {
    public static final String BASE_URI = "https://cloud-api.yandex.net";
    public static final String TOKEN_ENV = "YANDEX_OAUTH";

    public static String root() {
        return "app:/autotests-" + UUID.randomUUID();
    }
}

