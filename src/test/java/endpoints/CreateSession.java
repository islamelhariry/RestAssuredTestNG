package endpoints;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import listeners.RetryAnalyzer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static finals.FavQsConstants.*;
import static finals.FavQsConstants.TOKEN;

public class CreateSession {
    public static final String createSessionRequestBody = "{\n" +
            "  \"user\": {\n" +
            "    \"login\": \"%d\",\n" +
            "    \"password\": \"%s\"\n" +
            "  }\n" +
            "}";
    @BeforeClass
    void startUp(){
        // Set base URI
        RestAssured.baseURI = BASE_URI;
    }
    @DataProvider(name = "createSessionTestData")
    public Object[][] createSessionTestDataProvider() {
        return new Object[][] {
                {30521, "95e40514974e"}
        };
    }
    @Test(dataProvider = "createSessionTestData", retryAnalyzer = RetryAnalyzer.class)
    public void createSessionTest(int user, String password) {
        String requestBody = String.format(createSessionRequestBody, user, password);
        // Make the API call to create a session
        Response response = RestAssured
                .given()
                    .header(HEADER_AUTHORIZATION, AUTHORIZATION_TOKEN + TOKEN)
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                .when()
                    .post(CREATE_SESSION_ENDPOINT)
                .then()
                    .extract()
                    .response();

        // Print response body to get the session
        response.prettyPrint();
    }
}