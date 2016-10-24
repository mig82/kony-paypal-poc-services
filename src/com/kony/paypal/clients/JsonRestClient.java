package com.kony.paypal.clients;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRestClient {

	Map<String, String> sdkConfig;
	String clientId, secret;
	//String accessToken;
	
	Logger log;
	
	//private static final String METHOD_PAYPAL = "paypal";
	//private static final String INTENT_SALE = "sale";
	
	
	public JsonRestClient() {
		log = LoggerFactory.getLogger(JsonRestClient.class);
		sdkConfig = new HashMap<String, String>();
		sdkConfig.put("mode", "sandbox");
		
		//clientId = "EBWKjlELKMYqRNQ6sYvFo64FtaRLRR5BdHEESmha49TM";
		//secret = "EO422dn3gQLgDbuwqTjzrFgFtaRLRR5BdHEESmha49TM";
		clientId = "AfWuZnOvMXIEdXDlCM8fKtLpgXJgRHI_Np8t63Gdpl5YX-MHMkQCMyrEmKXd_BrkRsv6J__1WDLLZ_6p";
		secret = "EHB9xs9l_0LgTCr9UfMo9LQ129IW21mCVfsyWCDExI3N1MtZa79pwhkKIhViGtWBboe3Aot4MN4LRn3a";
	}
	
	public String getAccessToken() throws PayPalRESTException{
		
		OAuthTokenCredential credential = new OAuthTokenCredential(clientId, secret, sdkConfig);
		String accessToken = credential.getAccessToken();
		
		log.info("Access Token: {}", accessToken);
		return accessToken;
	}
	
	/*public Payment postPaypalSale(String description, String total, String currency, String returnUrl, String cancelUrl) throws PayPalRESTException{
		return createPayment(METHOD_PAYPAL, INTENT_SALE, getAccessToken(), description, total, currency, returnUrl, cancelUrl);
	}*/
	
	public Payment createPayment(String accessToken, Payment payment) throws PayPalRESTException{
		
		APIContext apiContext = new APIContext(accessToken);
		apiContext.setConfigurationMap(sdkConfig);

		Payment createdPayment = payment.create(apiContext);;
		return createdPayment;
	}
	
	public Payment createPayment(String paymentMethod, String intent, String accessToken, String description, String total, String currency, String returnUrl, String cancelUrl) throws PayPalRESTException{
		
		//APIContext apiContext = new APIContext(accessToken);
		//apiContext.setConfigurationMap(sdkConfig);

		Amount amount = new Amount();
		amount.setCurrency(currency); //amount.setCurrency("USD");
		amount.setTotal(total); //amount.setTotal("12");

		Transaction transaction = new Transaction();
		transaction.setDescription(description); //transaction.setDescription("creating a payment");
		transaction.setAmount(amount);

		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);

		Payer payer = new Payer();
		payer.setPaymentMethod(paymentMethod); //payer.setPaymentMethod("paypal");

		Payment payment = new Payment();
		payment.setIntent(intent); //payment.setIntent("sale");
		payment.setPayer(payer);
		payment.setTransactions(transactions);
		
		RedirectUrls redirectUrls = new RedirectUrls();
		redirectUrls.setCancelUrl(cancelUrl);
		redirectUrls.setReturnUrl(returnUrl);
		payment.setRedirectUrls(redirectUrls);

		//Payment createdPayment = payment.create(apiContext);;
		//return createdPayment;
		return createPayment(accessToken, payment);
	}
	
	public Payment executePayment(String accessToken, String paymentId, String payerId) throws PayPalRESTException{
		
		APIContext apiContext = new APIContext(accessToken);
		apiContext.setConfigurationMap(sdkConfig);

		Payment payment = new Payment();
		payment.setId(paymentId);
		PaymentExecution paymentExecution = new PaymentExecution();
		paymentExecution.setPayerId(payerId);
		Payment executedPayment = payment.execute(apiContext, paymentExecution);
		return executedPayment;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		JsonRestClient client = new JsonRestClient();
		System.out.println("*** Testing JsonRestClient ***");
		
		String cancelUrl = "https://devtools-paypal.com/guide/pay_paypal?cancel=true";
		String returnUrl = "https://devtools-paypal.com/guide/pay_paypal?success=true";
		
		try {
			String accessToken = client.getAccessToken();
			System.out.println("accessToken: " + accessToken);
			Payment payment0 = client.createPayment("paypal", "sale", accessToken, "Jedi Light Saber (Purple)", "1.00", "EUR", returnUrl, cancelUrl);
			System.out.println("created: " + payment0);
			Payment payment1 = client.executePayment(accessToken, payment0.getId(), "H4MJ38MDMQ746");
			System.out.println("executed: " + payment1);
		} catch (PayPalRESTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
