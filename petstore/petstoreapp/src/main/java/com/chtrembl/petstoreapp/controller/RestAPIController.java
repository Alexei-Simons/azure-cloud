package com.chtrembl.petstoreapp.controller;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chtrembl.petstoreapp.model.Order;
import com.chtrembl.petstoreapp.model.User;
import com.chtrembl.petstoreapp.service.PetStoreService;

/**
 * REST controller to facilitate REST calls such as session keep alives
 * (progressive web apps)
 *
 */
@RestController
public class RestAPIController {

	@Autowired
	private User sessionUser;

	@Autowired
	private PetStoreService petStoreService;

	@GetMapping("/api/contactus")
	public String contactus() {

		this.sessionUser.getTelemetryClient().trackEvent(
				String.format("PetStoreApp user %s requesting contact us", this.sessionUser.getName()),
				this.sessionUser.getCustomEventProperties(), null);

		return "Please contact Azure PetStore at 401-555-5555. Thank you. Demo 6/13";
	}

	@GetMapping("/api/sessionid")
	public String sessionid() {

		return this.sessionUser.getSessionId();
	}

	// helper api call for soul machines dp demo...
	@PostMapping(value = "/api/updatecart", produces = MediaType.TEXT_HTML_VALUE)
	public String updatecart(Model model, OAuth2AuthenticationToken token, HttpServletRequest request,
			@RequestParam Map<String, String> params) {
		this.sessionUser.getTelemetryClient().trackEvent(
				String.format("PetStoreApp user %s requesting update cart", this.sessionUser.getName()),
				this.sessionUser.getCustomEventProperties(), null);
	
		int cartCount = 1;

		String operator = params.get("operator");
		if (StringUtils.isNotEmpty(operator)) {
			if ("minus".equals(operator)) {
				cartCount = -1;
			}
		}

		this.petStoreService.updateOrder(Long.valueOf(params.get("productId")), cartCount, false);
		
		Order order = this.petStoreService.retrieveOrder(this.sessionUser.getSessionId());
		model.addAttribute("order", order);
		int cartSize = 0;
		if (order != null && order.getProducts() != null && !order.isComplete()) {
			cartSize = order.getProducts().size();
		}
		this.sessionUser.setCartCount(cartSize);

		return "success";
	}

	// helper api call for soul machines dp demo...
	@GetMapping(value = "/api/cartcount", produces = MediaType.TEXT_HTML_VALUE)
	public String cartcount() {

		this.sessionUser.getTelemetryClient().trackEvent(
				String.format("PetStoreApp user %s requesting cart count", this.sessionUser.getName()),
				this.sessionUser.getCustomEventProperties(), null);

		return String.valueOf(this.sessionUser.getCartCount());
	}

	// helper api call for soul machines dp demo...
	@GetMapping(value = "/api/viewcart", produces = MediaType.TEXT_HTML_VALUE)
	public String viewcart() {
		this.sessionUser.getTelemetryClient().trackEvent(
				String.format("PetStoreApp user %s requesting view cart", this.sessionUser.getName()),
				this.sessionUser.getCustomEventProperties(), null);

		Order order = this.petStoreService.retrieveOrder(this.sessionUser.getSessionId());
		
		StringBuilder sb = new StringBuilder();
		sb.append("Your order contains a ");
		if (order != null && order.getProducts() != null && !order.isComplete()) {
			for (int i = 0; i < order.getProducts().size(); i++) {
				sb.append(order.getProducts().get(i).getName()).append(" with a quantity ").append(order.getProducts().get(i).getQuantity());
				if (i < order.getProducts().size() - 1) {
					sb.append(", a ");
				}
			}
		}
		
		return sb.toString();
	}

	@GetMapping(value = "/introspectionSimulation", produces = MediaType.APPLICATION_JSON_VALUE)
	public String introspectionSimulation(Model model, HttpServletRequest request,
			@RequestParam(name = "sessionIdToIntrospect") Optional<String> sessionIdToIntrospect) {
		boolean active = (sessionIdToIntrospect != null && sessionIdToIntrospect.isPresent()
				&& sessionIdToIntrospect.get() != null
				&& sessionIdToIntrospect.get().equals(request.getHeader("session-id")));

			return "{\n" + 
				"  \"active\": " + active + ",\n" + 
					"  \"scope\": \"read write email\",\n" + 
					"  \"client_id\": \""+request.getHeader("session-id")+"\",\n" + 
					"  \"username\": \""+request.getHeader("session-id")+"\",\n" + 
					"  \"exp\": 1911221039\n" + 
					"}";
		}
}
