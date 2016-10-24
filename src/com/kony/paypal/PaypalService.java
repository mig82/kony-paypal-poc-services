package com.kony.paypal;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kony.paypal.clients.JsonRestClient;
import com.kony.paypal.exceptions.ParamMissingException;
//import com.paypal.base.rest.PayPalRESTException;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;


public class PaypalService implements JavaService2 {

	@Override
	public Object invoke(String methodId, Object[] maps, DataControllerRequest request, DataControllerResponse response) {
		Result result = new Result();
		try {
			result = invoke(methodId, maps);
		}
		catch (ParamMissingException e) {
			e.printStackTrace();
			result.setParam(new Param("opstatus", "20001", "number"));
			result.setParam(new Param("errmsg", "Parameter '" + e.getMessage() + "' is mandatory for service '" + methodId + "' invocation", "string"));
		}
		catch (PayPalRESTException e) {
			e.printStackTrace();
			result.setParam(new Param("opstatus", "20002", "number"));
			result.setParam(new Param("errmsg", "PayPalRESTException message: '" + e.getMessage() + "', exception: " + e.toString(), "string"));
		}
		return result;
	}
	
	private Result invoke(String methodId, Object[] maps) throws ParamMissingException, PayPalRESTException {
		
		Result result = new Result();
		
		System.out.println("Called method:'" + methodId + "'");
		
		int mapCount = maps.length;
		
		Map<String, String> cfgMap, inMap, outMap;
		if(mapCount >= 1) cfgMap = (Map<String, String>)maps[0];
		
		if(mapCount >= 2) {
			inMap  = (Map<String, String>)maps[1];
			
			
			if(methodId.startsWith("getToken")){
				result = getToken();
			}
			else if(methodId.startsWith("postPaypalSale")){
				result = postPaypalSale(inMap);
			}
			else if(methodId.startsWith("executePayment")){
				result = executePayment(inMap);
			}
			else{
				result.setParam(new Param("opstatus", "10102", "number"));
				result.setParam(new Param("errmsg", "Service " + methodId + " does not exist", "string"));
			}
			
		}
		//if(mapCount >= 3) outMap = (Map)maps[2];
		
		return result;
	}
	
	private Result getToken() throws PayPalRESTException{
		Result result = new Result();
		JsonRestClient cli = new JsonRestClient();
		String accessToken = cli.getAccessToken();
		result.setParam(new Param("access_token", accessToken, "string"));
		result.setParam(new Param("opstatus", "0", "number"));
		return result;
	}
	
	private Result postPaypalSale(Map<String, String> inMap) throws PayPalRESTException, ParamMissingException{
		Result result = new Result();
		JsonRestClient cli = new JsonRestClient();
		
		String accessToken = getParam(inMap, "access_token");
		String stringifiedPayment = inMap.get("payment");
		
		Payment payment = null;
		if(stringifiedPayment != null){
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
			Payment payment0 = gson.fromJson(stringifiedPayment, Payment.class);
			payment = cli.createPayment(accessToken, payment0);
		}
		else{
			String paymentMethod = getParam(inMap, "payment_method");
			String intent = getParam(inMap, "intent");
			String description = getParam(inMap, "description");
			String total = getParam(inMap, "total");
			String currency = getParam(inMap, "currency");
			String returnUrl = getParam(inMap, "return_url");
			String cancelUrl = getParam(inMap, "cancel_url");
			
			payment = cli.createPayment(paymentMethod, intent, accessToken, description, total, currency, returnUrl, cancelUrl);
		}
		
		result.setParam(new Param("payment", payment.toString(), "string")); //IMPORTANT: This is how we make up for not being able to return a JSON object. Client app must JSON.parse() this.
		
		result.setParam(new Param("opstatus", "0", "number"));
		return result;
	}
	
	private Result executePayment(Map<String, String> inMap) throws PayPalRESTException, ParamMissingException{
		Result result = new Result();
		JsonRestClient cli = new JsonRestClient();
		
		String accessToken = getParam(inMap, "access_token");
		String paymentId = getParam(inMap, "payment_id");
		String payerId = getParam(inMap, "payer_id");
		
		Payment payment = cli.executePayment(accessToken, paymentId, payerId);
		result.setParam(new Param("payment_id", payment.getId(), "string"));
		result.setParam(new Param("state", payment.getState(), "string"));
		
		result.setParam(new Param("opstatus", "0", "number"));
		return result;
	}
	
	protected String getParam(Map<String, String> map, String paramKey) throws ParamMissingException{
		String param = map.get(paramKey);
		if(param == null) throw new ParamMissingException(paramKey);
		return param;
	}

	/**
	 * @param args
	 * @throws PayPalRESTException 
	 * @throws ParamMissingException 
	 */
	public static void main(String[] args) throws ParamMissingException, PayPalRESTException {
		
		
		System.out.println("*** Testing PaypalService ***");
		PaypalService srv = new PaypalService();
		
		
			
			//String accessToken = srv.getToken();
			Object[] maps = new HashMap[3];
			maps[0] = new HashMap<String, String>();
			
			Result result0 = srv.invoke("getToken", maps);
			String accessToken = result0.getParamList().get(0).getValue();
			System.out.println("accessToken: " + accessToken);
			System.out.println("--------------------------------------End of getToken test----------------------------------------------------");
			
			
			
			Map<String, String> inMap1 = new HashMap<String, String>();
			inMap1.put("access_token", result0.getParamList().get(0).getValue());
			
			
			/**
			 * Note that the service can either take in a stringified JSON object as specified
			 * by PayPal's REST API or the separated parameters that would be necessary for building
			 * a Payment object.
			 * This is meant to showcase a workaround for sending JSON input to the Kony Server.
			 * To test, try uncommenting one of the examples below and executing this main method.*/
			
			/**Example 1: Calling the method with all the necessary parameters to build a Payment object*/
			/*
			inMap1.put("payment_method", "paypal");
			inMap1.put("intent", "sale");
			inMap1.put("description", "this is a test");
			inMap1.put("total", "1");
			inMap1.put("currency", "CAD");
			inMap1.put("return_url", "http://example.com?authorised=true");
			inMap1.put("cancel_url", "http://example.com?authorised=false");*/
			/**-------End of data for Example 1-------*/
			
			/**Example 2: calling the method with a single parameter containing a stringified JSON object for the payment*/
			inMap1.put("payment", "{intent:'sale',"+
					"payer:{payment_method:'paypal'},"+
					"transactions:[{"+
						"amount:{currency:'EUR',total:1.99},"+
						"description:'JediLightSabre(green)'}],"+
					"redirect_urls:{return_url:'http://example.com/your_redirect_url.html',cancel_url:'http://example.com/your_cancel_url.html'}}");
			/**-------End of of data for Example 2-------*/
			
			maps[1] = inMap1;
			Result result1 = srv.invoke("postPaypalSale", maps);
			
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
			Payment payment = gson.fromJson(result1.getParamList().get(0).getValue() , Payment.class);;
			System.out.println("Payment created: " + payment.toString());
			System.out.println("-------------------------------------End of postPaypalSale test-----------------------------------------------------");
			
			
			
			Map<String, String> inMap2 = new HashMap<String, String>();
			inMap2.put("access_token", accessToken);
			inMap2.put("payment_id", payment.getId());
			inMap2.put("payer_id", "H4MJ38MDMQ746");
			maps[1] = inMap2;
			Result result2 = srv.invoke("executePayment", maps);
			System.out.println("Payment executed: " + result2);
			System.out.println("-------------------------------------End of executePayment test-----------------------------------------------------");
		
		
	}
}
