package endpoints;

import finals.FavQsConstants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import listeners.RetryAnalyzer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static finals.FavQsConstants.*;

public class ListQuotes {
    @BeforeClass
    void startUp(){
        // Set base URI
        RestAssured.baseURI = BASE_URI;
   }

    @DataProvider(name = "listQuotesTestData")
    public Object[][] listQuotesTestDataProvider() {
        return new Object[][] {
                {""},
                {"?filter=funny"},
                {"?filter=government&type=tag"},
                {"?filter=Beverly+Sills&type=author"},
                {"?filter=gose&type=user"},
                {"?hidden=1"}
        };
    }

    @Test(dataProvider = "listQuotesTestData", retryAnalyzer = RetryAnalyzer.class)
    public void listQuotesTest(String filter) {
        Response response = RestAssured
                .given()
                    .header(FavQsConstants.HEADER_AUTHORIZATION, AUTHORIZATION_TOKEN + TOKEN)
                .when()
                    .get(LIST_QUOTES_ENDPOINT+filter)
                .then()
                    .extract()
                    .response();

        response.prettyPrint();
        response
                .then()
                    .statusCode(200);
        validateListQuotesResponse(response, filter);

    }

    @DataProvider(name = "listPrivateQuotesTestData")
    public Object[][] listPrivateQuotesTestDataProvider() {
        return new Object[][] {
                {"?private=1"},
                {"?filter=little+book&private=1"}
        };
    }

    @Test(dataProvider = "listPrivateQuotesTestData", retryAnalyzer = RetryAnalyzer.class)
    public void listPrivateQuotesTest(String filter) {
        Response response = RestAssured
                .given()
                .header(FavQsConstants.HEADER_AUTHORIZATION, AUTHORIZATION_TOKEN + TOKEN)
                .when()
                .get(LIST_QUOTES_ENDPOINT+filter)
                .then()
                .extract()
                .response();

        response.prettyPrint();
        response
                .then()
                .statusCode(200);
        validatePrivateListQuotesResponse(response);

    }

    // Method to validate the data obtained from the API response
    public void validateListQuotesResponse(Response response, String filter) {
        // Parse the JSON response and extract relevant data
        List<Map<String, Object>> quotes = response.jsonPath().getList("quotes");

        // Validate if the quotes list is not null and contains at least one quote
        Assert.assertNotNull(quotes, "Quotes list is null");
        Assert.assertTrue(quotes.size() > 0, "No quotes found");

        // Iterate through each quote and validate its attributes
        for (Map<String, Object> quote : quotes) {
            // Validate required attributes like id, body, author, etc.
            Assert.assertNotNull(quote.get("id"), "Quote id is null");
            Assert.assertNotNull(quote.get("body"), "Quote body is null");
            Assert.assertNotNull(quote.get("author"), "Quote author is null");

            // Validate other attributes like tags, favorites_count, upvotes_count, etc.
             Assert.assertNotNull(quote.get("tags"), "Quote tags are null");
             Assert.assertNotNull(quote.get("favorites_count"), "Favorites count is null");
             Assert.assertNotNull(quote.get("upvotes_count"), "Upvotes count is null");
             Assert.assertNotNull(quote.get("downvotes_count"), "Downvotes count is null");
        }
    }

    public void validatePrivateListQuotesResponse(Response response){
        Integer errorCode = response.jsonPath().get("error_code");
        String message = response.jsonPath().get("message");

        // Validate the error message
        Assert.assertEquals(errorCode, 20);
        Assert.assertEquals(message,"User session not found.", "No quotes found");
    }
}
