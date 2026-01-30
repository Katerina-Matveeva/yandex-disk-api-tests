import io.restassured.RestAssured;
import org.junit.BeforeClass;
import data.TestData;

public class BaseApiTest {

    protected static String token;

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = TestData.BASE_URI;
        token = System.getenv(TestData.TOKEN_ENV);

    }
}
