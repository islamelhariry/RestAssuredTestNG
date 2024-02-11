package endpoints;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import listeners.RetryAnalyzer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.Map;
import static finals.FavQsConstants.*;

public class FavQuote {

    @BeforeClass
    void startUp(){
        // Set base URI
        RestAssured.baseURI = BASE_URI;
    }

    @DataProvider(name = "favoriteQuoteTestData")
    public Object[][] favoriteQuoteTestDataProvider() {
        return new Object[][] {
                {10, "fav"},
                {10,"unfav"},
                {15, "unfav"},
                {15, "fav"},
                {15, "unfav"},
                {65892,"fav"}
        };
    }

    @Test(dataProvider = "favoriteQuoteTestData", retryAnalyzer = RetryAnalyzer.class)
    public void favoriteQuoteTest(int quoteId, String flag) {
        String quoteEndpoint = String.format(FAV_QUOTE_ENDPOINT,quoteId,flag);
        Response response = RestAssured
                .given()
                    .header(HEADER_AUTHORIZATION, AUTHORIZATION_TOKEN + TOKEN)
                    .header(USER_SESSION_TOKEN, SESSION)
                .when()
                    .put(quoteEndpoint)
                .then()
                    .extract()
                    .response();

        response
                .then()
                .statusCode(200);
        response.prettyPrint();

        validateFavoriteQuoteResponse(response, flag);
    }

    // Method to validate the data obtained from the API response
    public void validateFavoriteQuoteResponse(Response response, String mark) {
        // Parse the JSON response and extract relevant data
        Map<String, Object> responseBody = response.jsonPath().getMap("$");

        // Validate required attributes
        Assert.assertNotNull(responseBody.get("id"), "ID is null");
        Assert.assertEquals(responseBody.get("dialogue"), false, "Dialogue flag is not false");
        Assert.assertEquals(responseBody.get("private"), false, "Private flag is not false");
        Assert.assertNotNull(responseBody.get("tags"), "Tags are null");
        Assert.assertNotNull(responseBody.get("url"), "URL is null");
        Assert.assertNotNull(responseBody.get("favorites_count"), "Favorites count is null");
        Assert.assertNotNull(responseBody.get("upvotes_count"), "Upvotes count is null");
        Assert.assertNotNull(responseBody.get("downvotes_count"), "Downvotes count is null");
        Assert.assertNotNull(responseBody.get("author"), "Author is null");
        Assert.assertNotNull(responseBody.get("author_permalink"), "Author permalink is null");
        Assert.assertNotNull(responseBody.get("body"), "Body is null");

        // Validate user details
        Map<String, Object> userDetails = (Map<String, Object>) responseBody.get("user_details");
        Assert.assertNotNull(userDetails, "User details are null");
        if(mark.equals("endpoints/"))
            Assert.assertEquals(userDetails.get("favorite"), true, "Favorite flag is not true");
        else if(mark.equals("unfav/"))
            Assert.assertEquals(userDetails.get("favorite"), false, "Favorite flag is not false");

        Assert.assertEquals(userDetails.get("upvote"), false, "Upvote flag is not false");
        Assert.assertEquals(userDetails.get("downvote"), false, "Downvote flag is not false");
        Assert.assertEquals(userDetails.get("hidden"), false, "Hidden flag is not false");
    }
}