package in.codifi.api.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.codifi.api.config.ApplicationProperties;
import in.codifi.api.entity.ApplicationUserEntity;
import in.codifi.api.entity.IvrEntity;
import in.codifi.api.helper.DocumentHelper;
import in.codifi.api.model.IvrModel;
import in.codifi.api.model.LivenessCheckReqModel;
import in.codifi.api.model.LivenessCheckResModel;
import in.codifi.api.model.ResponseModel;
import in.codifi.api.repository.ApplicationUserRepository;
import in.codifi.api.repository.IvrRepository;
import in.codifi.api.restservice.AryaLivenessCheck;
import in.codifi.api.service.spec.IIvrService;
import in.codifi.api.utilities.CommonMethods;
import in.codifi.api.utilities.EkycConstants;
import in.codifi.api.utilities.MessageConstants;
import in.codifi.api.utilities.StringUtil;

@ApplicationScoped
public class IvrService implements IIvrService {
	private static String OS = System.getProperty("os.name").toLowerCase();
	@Inject
	AryaLivenessCheck aryaLivenessCheck;
	@Inject
	DocumentHelper documentHelper;
	@Inject
	IvrRepository ivrRepository;
	@Inject
	CommonMethods commonMethods;
	@Inject
	ApplicationProperties props;
	@Inject
	ApplicationUserRepository userRepository;

	/**
	 * Method to upload IVR Document
	 */
	@Override
	public ResponseModel uploadIvr(IvrModel ivrModel) {
		String slash = EkycConstants.UBUNTU_FILE_SEPERATOR;
		if (OS.contains(EkycConstants.OS_WINDOWS)) {
			slash = EkycConstants.WINDOWS_FILE_SEPERATOR;
		}
		ResponseModel responseModel = new ResponseModel();
		try {
			List<String> errorList = checkIvrModel(ivrModel);
			if (StringUtil.isListNullOrEmpty(errorList)) {
				LivenessCheckReqModel reqModel = new LivenessCheckReqModel();
				reqModel.setDoc_base64(ivrModel.getImageUrl());
				reqModel.setReq_id(ivrModel.getApplicationId());
				LivenessCheckResModel model = aryaLivenessCheck.livenessCheck(reqModel);
				ObjectMapper mapper = new ObjectMapper();
				System.out.println(mapper.writeValueAsString(model));
				if (model != null && model.getDocJson() != null
						&& Double.parseDouble(model.getDocJson().getReal()) >= 0.50) {
					String ivrName = documentHelper.convertBase64ToImage(ivrModel.getImageUrl(),
							ivrModel.getApplicationId());
					IvrEntity oldRecord = ivrRepository.findByApplicationId(ivrModel.getApplicationId());
					IvrEntity updatedDocEntity = null;
					if (oldRecord != null) {
						oldRecord.setAttachement(ivrName);
						oldRecord.setDocumentType(EkycConstants.DOC_IVR);
						oldRecord.setTypeOfProof(EkycConstants.DOC_IVR);
						oldRecord.setAttachementUrl(
								props.getImageUrlPath() + ivrModel.getApplicationId() + slash + ivrName);
						oldRecord.setLatitude(ivrModel.getLatitude());
						oldRecord.setLongitude(ivrModel.getLongitude());
						updatedDocEntity = ivrRepository.save(oldRecord);
					} else {
						IvrEntity doc = new IvrEntity();
						doc.setApplicationId(ivrModel.getApplicationId());
						doc.setAttachement(ivrName);
						doc.setTypeOfProof(EkycConstants.DOC_IVR);
						doc.setDocumentType(EkycConstants.DOC_IVR);
						doc.setAttachementUrl(props.getImageUrlPath() + ivrModel.getApplicationId() + slash + ivrName);
						doc.setLatitude(ivrModel.getLatitude());
						doc.setLongitude(ivrModel.getLongitude());
						updatedDocEntity = ivrRepository.save(doc);
					}
					if (updatedDocEntity != null) {
						commonMethods.UpdateStep(EkycConstants.PAGE_IPV, ivrModel.getApplicationId());
						responseModel.setMessage(EkycConstants.SUCCESS_MSG);
						responseModel.setResult(updatedDocEntity);
					} else {
						responseModel = commonMethods.constructFailedMsg(MessageConstants.FAILED_IVR_DOC_UPLOAD);
					}
				} else {
					if (model != null && model.getDocJson() != null
							&& Double.parseDouble(model.getDocJson().getSpoof()) > 0.01) {
						responseModel = commonMethods.constructFailedMsg(MessageConstants.INVALID_IVR_INVALID);
						responseModel.setReason(model.getErrorMessage() + model.getDocJson().getSpoof());
						responseModel.setResult(model);
					} else {
						responseModel = commonMethods.constructFailedMsg(MessageConstants.ERROR_LIVENESS);
					}
				}
			} else {
				responseModel = commonMethods.constructFailedMsg(MessageConstants.INVALID_IVR_PARAMS);
				responseModel.setResult(errorList);
			}
		} catch (Exception e) {
			responseModel = commonMethods.constructFailedMsg(e.getMessage());
			e.printStackTrace();
		}
		return responseModel;
	}

	public List<String> checkIvrModel(IvrModel ivrModel) {
		List<String> errorList = new ArrayList<>();
		if (StringUtil.isNullOrEmpty(ivrModel.getImageUrl())) {
			errorList.add(MessageConstants.IVR_IMAGE_NULL);
		}
		if (StringUtil.isNullOrEmpty(ivrModel.getLatitude())) {
			errorList.add(MessageConstants.IVR_LAT_NULL);
		}
		if (StringUtil.isNullOrEmpty(ivrModel.getLongitude())) {
			errorList.add(MessageConstants.IVR_LON_NULL);
		}
		return errorList;
	}

	/**
	 * Method to generate IVR Link
	 */
	@Override
	public ResponseModel getIvrLink(@NotNull long applicationId) {
		ResponseModel responseModel = new ResponseModel();
		Optional<ApplicationUserEntity> isUserPresent = userRepository.findById(applicationId);
		if (isUserPresent.isPresent()) {
			String FirstName = isUserPresent.get().getFirstName();
			String RandomencodedUuid = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(UUID.randomUUID().toString().getBytes());
			String baseUrl = props.getIvrBaseUrl();
			String apiKey = props.getBitlyAccessToken();
			String url = baseUrl + EkycConstants.IVR_KEY + apiKey + EkycConstants.IVR_APPLICATIONID + applicationId
					+ EkycConstants.IVR_NAME + FirstName + EkycConstants.IVR_USER_DOMAIN_AND_RANDOMKEY
					+ RandomencodedUuid;
			try {
				String generateShortLink = generateShortLink(url);
				if (StringUtil.isNotNullOrEmpty(generateShortLink)) {
					IvrEntity oldRecord = ivrRepository.findByApplicationId(applicationId);
					if (oldRecord != null) {
						oldRecord.setUrl(generateShortLink);
						ivrRepository.save(oldRecord);
					} else {
						oldRecord = new IvrEntity();
						oldRecord.setApplicationId(applicationId);
						oldRecord.setUrl(generateShortLink);
						ivrRepository.save(oldRecord);
					}
					responseModel.setStat(EkycConstants.SUCCESS_STATUS);
					responseModel.setMessage(EkycConstants.SUCCESS_MSG);
					responseModel.setReason(generateShortLink);
					responseModel.setResult(generateShortLink);
				} else {
					responseModel.setStat(EkycConstants.FAILED_STATUS);
					responseModel.setMessage(EkycConstants.FAILED_MSG);
					responseModel.setReason(EkycConstants.IVR_FAILED_MESSAGE);
				}
			} catch (Exception e) {
				responseModel.setStat(EkycConstants.FAILED_STATUS);
				responseModel.setMessage(EkycConstants.FAILED_MSG);
				responseModel.setReason(EkycConstants.IVR_FAILED_MESSAGE);
				responseModel.setReason(e.getMessage());
				e.printStackTrace();
			}
		}
		return responseModel;
	}

	/**
	 * Method to shorten the url
	 * 
	 * @param longUrl
	 * @return
	 */
	public String generateShortLink(String longUrl) {
		HttpURLConnection conn = null;
		String shortUrl = "";
		try {
			String apiKey = props.getBitlyAccessToken();
			String apiUrl = String.format(props.getBitlyBaseUrl(), apiKey,
					URLEncoder.encode(longUrl, StandardCharsets.UTF_8));
			URL url = new URL(apiUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(EkycConstants.HTTP_GET);
			conn.setRequestProperty(EkycConstants.IVR_ACCEPT, EkycConstants.CONSTANT_APPLICATION_JSON);
			if (conn.getResponseCode() != 200) {
				BufferedReader errorReader = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
				String errorOutput;
				StringBuilder errorResponseBuilder = new StringBuilder();
				while ((errorOutput = errorReader.readLine()) != null) {
					errorResponseBuilder.append(errorOutput);
				}
				throw new RuntimeException(MessageConstants.FAILED_HTTP_CODE + conn.getResponseCode() + " : "
						+ errorResponseBuilder.toString());
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output;
			StringBuilder responseBuilder = new StringBuilder();
			while ((output = in.readLine()) != null) {
				responseBuilder.append(output);
			}
			JSONObject responseJson = new JSONObject(responseBuilder.toString());
			JSONObject urlObj = responseJson.getJSONObject(EkycConstants.URL);
			shortUrl = urlObj.getString(EkycConstants.SHORT_URL);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return shortUrl;
	}

	@Override
	public ResponseModel sendLink(ApplicationUserEntity userEntity, String type) {
		ResponseModel responseModel = new ResponseModel();
		String url = "";
		try {
			IvrEntity oldRecord = ivrRepository.findByApplicationId(userEntity.getId());
			if (oldRecord == null || StringUtil.isNullOrEmpty(oldRecord.getUrl())) {
				ResponseModel newModel = getIvrLink(userEntity.getId());
				if (newModel.getStat() == 1 && StringUtil.isEqual(newModel.getMessage(), EkycConstants.SUCCESS_MSG)) {
					url = newModel.getReason();
				}
			} else {
				url = oldRecord.getUrl();
			}
			if (StringUtil.isNotNullOrEmpty(url)) {
				if (type.equalsIgnoreCase(EkycConstants.IVR_SMS_KEY)) {
					if (userEntity.getMobileNo() != null && userEntity.getMobileNo() > 0) {
						commonMethods.sendIvrLinktoMobile(url, userEntity.getMobileNo());
					} else {
						responseModel = commonMethods.constructFailedMsg(MessageConstants.MOBILE_NUMBER_NULL);
					}
				} else if (type.equalsIgnoreCase(EkycConstants.IVR_EMAIL_KEY)) {
					if (StringUtil.isNotNullOrEmpty(userEntity.getEmailId())) {
						commonMethods.sendMailIvr(url, userEntity.getEmailId());
					} else {
						responseModel = commonMethods.constructFailedMsg(MessageConstants.EMAIL_ID_NULL);
					}
				}
				responseModel.setStat(EkycConstants.SUCCESS_STATUS);
				responseModel.setMessage(EkycConstants.SUCCESS_MSG);
			} else {
				responseModel.setStat(EkycConstants.FAILED_STATUS);
				responseModel.setMessage(EkycConstants.FAILED_MSG);
				responseModel.setReason(EkycConstants.IVR_FAILED_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			responseModel.setStat(EkycConstants.FAILED_STATUS);
			responseModel.setMessage(EkycConstants.FAILED_MSG);
			responseModel.setReason(e.getMessage());
		}
		return responseModel;
	}
}
