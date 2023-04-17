package in.codifi.api.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nsdl.esign.preverifiedNo.controller.EsignApplication;

import in.codifi.api.config.ApplicationProperties;
import in.codifi.api.model.ResponseModel;
import in.codifi.api.service.spec.IEsignGeneratorService;
import in.codifi.api.utilities.CommonMethods;

@ApplicationScoped
public class EsignGeneratorService implements IEsignGeneratorService {
	@Inject
	ApplicationProperties props;

	@Inject
	CommonMethods commonMethods;
	
	private static final Logger logger = LogManager.getLogger(DocumentService.class);
	/**
	 * Method to get xml for E sign
	 */
	@Override
	public ResponseModel xmlGenerator(@NotNull long applicationId) {
		ResponseModel responseModel = new ResponseModel();
		String response = "";
		try {
			String ekycID = Long.toString(applicationId);
			String pdfReadServerPath = props.getEsignSamplePDF();
			String aspId = props.getEsignAspId();
			String authMode = "1";
			String responseUrl = "https://ekyc.nidhihq.com/ekyc-rest/user/testEsign";
			String p12CertificatePath = props.getEsignLocation();
			String p12CertiPwd = props.getEsignPassword();
			String tickImagePath = props.getEsignTickImage();
			int serverTime = 15;
			String alias = props.getEsignAlias();
			int pageNumberToInsertSignatureStamp = 1;
			String nameToShowOnSignatureStamp = "Test";
			String locationToShowOnSignatureStamp = "Madurai";
			String reasonForSign = "";
			int xCo_ordinates = 110;
			int yCo_ordinates = 100;
			int signatureWidth = 150;
			int signatureHeight = 100;
			String pdfPassword = "";
			String txn = "";
//			String respSignatureType  = ""; 
			try {
				EsignApplication eSignApp = new EsignApplication();
				response = eSignApp.getEsignRequestXml(ekycID, pdfReadServerPath, aspId, authMode, responseUrl,
						p12CertificatePath, p12CertiPwd, tickImagePath, serverTime, alias,
						pageNumberToInsertSignatureStamp, nameToShowOnSignatureStamp, locationToShowOnSignatureStamp,
						reasonForSign, xCo_ordinates, yCo_ordinates, signatureWidth, signatureHeight, pdfPassword, txn);
				System.out.println(response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("An error occurred: " + e.getMessage());
			commonMethods.sendErrorMail("An error occurred while processing your request, In xmlGenerator.","ERR-001");
			responseModel = commonMethods.constructFailedMsg(e.getMessage());
		}
		responseModel.setResult(response);
		return responseModel;
	}

}
