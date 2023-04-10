package in.codifi.api.restservice;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import in.codifi.api.config.ApplicationProperties;
import in.codifi.api.utilities.EkycConstants;

@ApplicationScoped
public class SmsRestService {
	@Inject
	@RestClient
	ISmsRestService iSmsRestService;
	@Inject
	ApplicationProperties props;

	/**
	 * Method to send otp to Mobile Number
	 * 
	 * @author Nila
	 * @param otp
	 * @param mobile Number
	 * @return
	 */

	public void sendOTPtoMobile(int otp, long mobileNumber) {
		try {
			String Text = otp + " " + props.getSmsText();
			String message = iSmsRestService.SendSms(props.getSmsFeedId(), props.getSmsSenderId(),
					props.getSmsUserName(), props.getSmsPassword(), String.valueOf(mobileNumber), Text);
			System.out.println(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * method to send IVR link as sms
	 * 
	 * @param feedId
	 * @param senderId
	 * @param userName
	 * @param password
	 * @param mobileNumber
	 * @param message
	 * @return
	 */
	public void sendIvrLinktoMobile(String Url, long mobileNumber) {
		try {
			String Text = EkycConstants.IVR_MSG + Url + " " + " .NIDHI";
			iSmsRestService.SendLink(props.getSmsFeedId(), props.getSmsSenderId(), props.getSmsUserName(),
					props.getSmsPassword(), String.valueOf(mobileNumber), Text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
