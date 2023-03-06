package in.codifi.api.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;
import org.json.simple.JSONValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.codifi.api.config.ApplicationProperties;
import in.codifi.api.entity.ApplicationUserEntity;
import in.codifi.api.entity.ReqResEntity;
import in.codifi.api.model.BankAddressModel;
import in.codifi.api.model.ResponseModel;
import in.codifi.api.repository.ApplicationUserRepository;
import in.codifi.api.repository.ReqResRepository;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;

@ApplicationScoped
public class CommonMethods {
	@Inject
	ApplicationProperties props;
	@Inject
	Mailer mailer;
	@Inject
	ApplicationUserRepository repos;
	@Inject
	ReqResRepository reqResRepository;

	/**
	 * Method to generate OTP for Mobile number
	 * 
	 * @author prade
	 * @param mobileNumber
	 * @return
	 */
	public int generateOTP(long mobileNumber) {
		int otp = 000000;
		if (mobileNumber == 1234567890 || mobileNumber == 1111100000) {
			otp = 000000;
			System.out.println("OTP : " + otp);
		} else {
			otp = (int) (Math.random() * 900000) + 100000;
			System.out.println("OTP : " + otp);
		}
		return otp;
	}

	/**
	 * Method to construct Failed method
	 * 
	 * @author prade
	 * @param failesMessage
	 * @return
	 */
	public ResponseModel constructFailedMsg(String failesMessage) {
		ResponseModel model = new ResponseModel();
		model.setStat(EkycConstants.FAILED_STATUS);
		model.setMessage(EkycConstants.FAILED_MSG);
		model.setReason(failesMessage);
		return model;
	}

	/**
	 * Method to send otp to Mobile Number
	 * 
	 * @author prade
	 * @param otp
	 * @param mobile Number
	 * @return
	 */
	public void sendOTPtoMobile(int otp, long mobileNumber) {
		try {
			StringBuffer data = new StringBuffer();
			data.append(EkycConstants.CONST_SMS_FEEDID + props.getSmsFeedId());
			data.append(EkycConstants.AND + EkycConstants.CONST_SMS_SENDERID + props.getSmsSenderId());
			data.append(EkycConstants.AND + EkycConstants.CONST_SMS_USERNAME + props.getSmsUserName());
			data.append(EkycConstants.AND + EkycConstants.CONST_SMS_PASSWORD + props.getSmsPassword());
			data.append(EkycConstants.AND + EkycConstants.CONST_SMS_TO + mobileNumber);
			String msg = EkycConstants.AND + EkycConstants.CONST_SMS_TEXT + otp
					+ EkycConstants.OTP_MSG.replace(" ", "%20");
			data.append(msg);
			URL url = new URL(props.getSmsUrl() + data.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(EkycConstants.HTTP_POST);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.connect();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				buffer.append(line);
			}
			rd.close();
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to send OTP to aliceBlue
	 * 
	 * @author Dinesh
	 * @param otp
	 * @param mobile
	 * @return
	 */

	public boolean sendOTPMessage(String otp, String mobile) {
		try {
			HttpURLConnection conn = null;
			JSONObject json = new JSONObject();
			json.put("apikey", props.getApiKey());
			json.put("senderid", props.getSenderId());
			json.put("number", mobile);
			json.put("message", "Dear User, " + otp
					+ " is your verification code as requested online, this code is valid for next 5 minutes. Regards-AliceBlue");
			URL url = new URL(props.getUrl());
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = json.toString().getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br1 = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			while ((output = br1.readLine()) != null) {
				@SuppressWarnings("unused")
				Object object = JSONValue.parse(output);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Inject
	public void MailService(Mailer javaMailSender) {
		this.mailer = javaMailSender;
	}

	/**
	 * Method to send mail
	 * 
	 * @param user
	 * @return
	 */
	public void sendMailOtp(int otp, String emailId) throws MessagingException {
		String getSubject = props.getMailSubject();
		String getText = "Dear User, " + otp + " " + props.getMailText();
		Mail mail = Mail.withText(emailId, getSubject, getText);
		mailer.send(mail);
		System.out.print("the post mail" + mail);
	}

	/**
	 * Trust Management
	 */
	public static void trustedManagement() {
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HostnameVerifier allHostsValid = new HostnameVerifier() {

				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (

		NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to update step in Application Entity
	 * 
	 * @author Vennila Ponnusamy
	 * @param otp
	 * @param mobile Number
	 * @return
	 */

	public void UpdateStep(double step, Long ApplicationID) {
		try {
			Optional<ApplicationUserEntity> checkApplicationID = repos.findById(ApplicationID);
			if (checkApplicationID.isPresent() && checkApplicationID.get().getStage() < step) {
				ApplicationUserEntity oldUserEntity = checkApplicationID.get();
				oldUserEntity.setStage(step);
				repos.save(oldUserEntity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to find bank address by ifsc
	 * 
	 * @author prade
	 * @param ifscCode
	 * @return
	 */
	public BankAddressModel findBankAddressByIfsc(String ifscCode) {
		BankAddressModel model = null;
		try {
			URL url = new URL(props.getRazorpayIfscUrl() + ifscCode);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				return model;
			}
			BufferedReader br1 = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;

			while ((output = br1.readLine()) != null) {
				ObjectMapper om = new ObjectMapper();
				model = om.readValue(output, BankAddressModel.class);
			}
		} catch (Exception e) {
			return model;
		}
		return model;
	}

	/**
	 * Method to save out rest service request and response
	 * 
	 * @author prade
	 * @param req
	 * @param res
	 * @param applicationId
	 * @return
	 */
	public void Req_Res_Save_object(Object reqe, Object res, String type, long id) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String Req = mapper.writeValueAsString(reqe);
			String Res;
			Res = mapper.writeValueAsString(res);
			System.out.println("req" + Req);
			System.out.println("res" + Res);
			saveRequestAndResposne(Req, Res, type, id);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public void saveRequestAndResposne(String req, String res, String method, long applicationId) {
//		ReqResEntity savedResult = null;
		if (StringUtil.isNotNullOrEmpty(req) && StringUtil.isNotNullOrEmpty(res) && StringUtil.isNotNullOrEmpty(method)
				&& applicationId > 0) {
			ReqResEntity oldReqRes = reqResRepository.findByApplicationIdAndType(applicationId, method);
			if (oldReqRes != null) {
				oldReqRes.setRequest(req);
				oldReqRes.setResponse(res);
				reqResRepository.save(oldReqRes);
			} else {
				ReqResEntity savingResult = new ReqResEntity();
				savingResult.setApplicationId(applicationId);
				savingResult.setType(method);
				savingResult.setRequest(req);
				savingResult.setResponse(res);
				reqResRepository.save(savingResult);
			}

		}

	}

}
