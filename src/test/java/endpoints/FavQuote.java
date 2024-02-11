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
                {10, "fav"}, // Marking one quote as favorite
                {10,"unfav"}, // Unmarking the same quote as favorite
                {15, "unfav"}, // Unmakring a non-favorite quote
                {15, "fav"}, // Marking the same quote as favorite
                {15, "unfav"}, //Unmakring it again as favorite
                {65892,"fav"}, // Marking a favorite quote as favorite again
                {65892,"fav"} // Marking a favorite quote as favorite again
        };
    }

    @Test(dataProvider = "favoriteQuoteTestData", retryAnalyzer = RetryAnalyzer.class)
    public void favoriteQuoteTest(int quoteId, String flag) {
        // Constructing the put request to include the quote id and the flag (mark)
        String quoteEndpoint = String.format(FAV_QUOTE_ENDPOINT,quoteId,flag);

        // Make the API call to mark or unmark a quote as favorite
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

        // Print response body to see the response
        response.prettyPrint();

        validateFavoriteQuoteResponse(response, flag);
    }

    // Method to validate the data obtained from the API response
    public void validateFavoriteQuoteResponse(Response response, String flag) {
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
        if(flag.equals("endpoints/"))
            Assert.assertEquals(userDetails.get("favorite"), true, "Favorite flag is not true");
        else if(flag.equals("unfav/"))
            Assert.assertEquals(userDetails.get("favorite"), false, "Favorite flag is not false");
        Assert.assertEquals(userDetails.get("upvote"), false, "Upvote flag is not false");
        Assert.assertEquals(userDetails.get("downvote"), false, "Downvote flag is not false");
        Assert.assertEquals(userDetails.get("hidden"), false, "Hidden flag is not false");
    }
}