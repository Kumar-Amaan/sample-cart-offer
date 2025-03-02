package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.ApplyOfferResponse;
import com.springboot.controller.OfferRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {


	@Test
	public void testFlatXAmountOffOffer() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX",10,segments);
		boolean offerAdded = addOffer(offerRequest);
		Assert.assertTrue(offerAdded);
		int cartValue = 200;
		int expectedCartValue = cartValue - 10;
		int updatedCartValue = applyOffer(cartValue, 1, 1);
		Assert.assertEquals(expectedCartValue,updatedCartValue);
	}

	@Test
	public void testFlatXPercentageOffOffer() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p2");
		OfferRequest offerRequest = new OfferRequest(1, "FLATX%", 10, segments);
		boolean offerAdded = addOffer(offerRequest);
		Assert.assertTrue(offerAdded);
		int cartValue = 200;
		int expectedCartValue = (int) (cartValue * 0.9);
		int updatedCartValue = applyOffer(cartValue, 2, 1);
		Assert.assertEquals(expectedCartValue, updatedCartValue);
	}

	@Test
	public void testInvalidOffer() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p3");
		OfferRequest invalidOfferRequest = new OfferRequest(100, "FLATX", 10, segments);
		boolean offerAdded = addOffer(invalidOfferRequest);
		Assert.assertTrue(offerAdded);
		int cartValue = 200;
		//taking different restaurant id
		int updatedCartValue = applyOffer(cartValue, 3, 3);
		Assert.assertEquals(cartValue, updatedCartValue);
	}

	@Test
	public void testNoOffersApplied() throws Exception {
		int cartValue = 200;
		int updatedCartValue = applyOffer(cartValue, 3, 1);
		Assert.assertEquals(cartValue, updatedCartValue);
	}

	@Test
	public void testNegativeCartValue() throws Exception {
		int cartValue = -100;
		int updatedCartValue = applyOffer(cartValue, 1, 1);
		Assert.assertTrue(updatedCartValue < 0);
	}

	@Test
	public void testOfferNotApplicableToUserSegment() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p7");
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, segments);
		boolean offerAdded = addOffer(offerRequest);
		Assert.assertTrue(offerAdded);
		int cartValue = 200;
		int updatedCartValue = applyOffer(cartValue, 7, 1);
		Assert.assertEquals(cartValue, updatedCartValue);
	}

	@Test
	public void testMultipleOffersChooseBest() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p4");
		addOffer(new OfferRequest(1, "FLATX%", 20, segments));
		addOffer(new OfferRequest(1, "FLATX", 10, segments));
		int cartValue = 200;
		int expectedCartValue = (int) (cartValue * 0.8);
		int updatedCartValue = applyOffer(cartValue, 4, 1);
		Assert.assertEquals(expectedCartValue, updatedCartValue);
	}

	@Test
	public void testOfferForDifferentRestaurant() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p2");
		addOffer(new OfferRequest(2, "FLATX", 10, segments));

		int cartValue = 200;
		int updatedCartValue1 = applyOffer(cartValue, 2, 2);
		int updatedCartValue2 = applyOffer(cartValue, 2, 1);
		Assert.assertNotEquals(cartValue, updatedCartValue1);
		Assert.assertEquals(cartValue, updatedCartValue2);
	}


	public boolean addOffer(OfferRequest offerRequest) throws Exception {
		String urlString = "http://localhost:9001/api/v1/offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();
		String POST_PARAMS = mapper.writeValueAsString(offerRequest);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();

		int responseCode = con.getResponseCode();
		return responseCode == HttpURLConnection.HTTP_OK;
	}

	public int applyOffer(int cartValue, int userId, int restaurantId) throws Exception {
		String urlString = "http://localhost:9001/api/v1/cart/apply_offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();
		String jsonRequest = "{\"cart_value\":" + cartValue + ",\"user_id\": " + userId + ", \"restaurant_id\": " + restaurantId + "}";
		OutputStream os = con.getOutputStream();
		os.write(jsonRequest.getBytes());
		os.flush();
		os.close();

		if(con.getResponseCode()==200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder response = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return mapper.readValue(response.toString(), ApplyOfferResponse.class).getCart_value();
		}
		System.out.println("ERROR OCCURRED WITH STATUS :{}"+ con.getResponseCode());
		return 0;
	}

}
