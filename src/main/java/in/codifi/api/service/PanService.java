package in.codifi.api.service;

import java.util.Optional;

import javax.inject.Inject;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import in.codifi.api.entity.ApplicationUserEntity;
import in.codifi.api.entity.ProfileEntity;
import in.codifi.api.helper.KRAHelper;
import in.codifi.api.helper.PanHelper;
import in.codifi.api.model.ResponseModel;
import in.codifi.api.repository.ApplicationUserRepository;
import in.codifi.api.service.spec.IPanService;
import in.codifi.api.utilities.CommonMethods;
import in.codifi.api.utilities.EkycConstants;
import in.codifi.api.utilities.MessageConstants;
import in.codifi.api.utilities.StringUtil;

@Service
public class PanService implements IPanService {
	@Inject
	ApplicationUserRepository repository;
	@Inject
	PanHelper panHelper;
	@Inject
	CommonMethods commonMethods;
	@Inject
	KRAHelper kraHelper;

	/**
	 * Method to get PAN details
	 */
	@Override
	public ResponseModel getPanDetails(ApplicationUserEntity userEntity) {
		ResponseModel responseModel = new ResponseModel();
		try {
			Optional<ApplicationUserEntity> isUserPresent = repository.findById(userEntity.getId());
			ApplicationUserEntity panNumberPresent = repository.findByPanNumber(userEntity.getPanNumber());
			if (isUserPresent.isPresent() && (panNumberPresent == null
					|| panNumberPresent != null && userEntity.getId() == panNumberPresent.getId())) {
				String result = panHelper.getPanDetailsFromNSDL(userEntity.getPanNumber(), userEntity.getId());
				if (result != null && !result.equalsIgnoreCase("")) {
					responseModel = panHelper.saveResult(result, isUserPresent.get());
				} else {
					responseModel = commonMethods.constructFailedMsg(MessageConstants.INVALID_PAN_MSG);
				}
			} else {
				if (!isUserPresent.isPresent()) {
					responseModel = commonMethods.constructFailedMsg(MessageConstants.USER_ID_INVALID);
				} else {
					responseModel = commonMethods.constructFailedMsg(MessageConstants.PAN_ALREADY_AVAILABLE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			responseModel = commonMethods.constructFailedMsg(e.getMessage());
		}
		return responseModel;
	}

	/**
	 * Method to save Date of Birth
	 */
	@Override
	public ResponseModel saveDob(ApplicationUserEntity userEntity) {
		ResponseModel responseModel = new ResponseModel();
		ProfileEntity profileEntity = null;
		Optional<ApplicationUserEntity> isUserPresent = repository.findById(userEntity.getId());
		if (isUserPresent.isPresent()) {
			ApplicationUserEntity oldUserEntity = isUserPresent.get();
			oldUserEntity.setDob(userEntity.getDob());
			oldUserEntity.setStage(2.2);
			ApplicationUserEntity savingEntity = repository.save(oldUserEntity);
			try {
				if (StringUtil.isNotNullOrEmpty(savingEntity.getPanNumber())
						&& StringUtil.isNotNullOrEmpty(savingEntity.getDob())) {
					JSONObject pancardResponse = kraHelper.getPanCardStatus(savingEntity.getPanNumber());
					if (pancardResponse != null) {
						if (pancardResponse.has("APP_NAME")) {
							int panCardStatus = pancardResponse.getInt("APP_STATUS");
							if (panCardStatus == 002) {
								JSONObject panCardDetails = kraHelper.getPanCardDetails(savingEntity.getPanNumber(),
										savingEntity.getDob());
								if (panCardDetails != null) {
									if (panCardDetails.has("APP_NAME")) {
										profileEntity = kraHelper.updateDetailsFromKRA(panCardDetails,
												userEntity.getId());
									} else {
										if (panCardDetails.has("ERROR_MSG")) {
											responseModel = commonMethods
													.constructFailedMsg(pancardResponse.getString("ERROR_MSG"));
											responseModel.setPage(EkycConstants.PAGE_AADHAR);
										} else {
											responseModel = commonMethods
													.constructFailedMsg(MessageConstants.KRA_FAILED);
											responseModel.setPage(EkycConstants.PAGE_AADHAR);
										}
									}
								} else {
									responseModel = commonMethods
											.constructFailedMsg(MessageConstants.INTERNAL_SERVER_ERROR);
									responseModel.setPage(EkycConstants.PAGE_AADHAR);
								}
							}
						} else {
							if (pancardResponse.has("ERROR_MSG")) {
								responseModel = commonMethods
										.constructFailedMsg(pancardResponse.getString("ERROR_MSG"));
								responseModel.setPage(EkycConstants.PAGE_AADHAR);
							} else {
								responseModel = commonMethods.constructFailedMsg(MessageConstants.KRA_FAILED);
								responseModel.setPage(EkycConstants.PAGE_AADHAR);
							}
						}
					} else {
						responseModel = commonMethods.constructFailedMsg(MessageConstants.INTERNAL_SERVER_ERROR);
						responseModel.setPage(EkycConstants.PAGE_AADHAR);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				responseModel = commonMethods.constructFailedMsg(e.getMessage());
			}
			if (profileEntity != null) {
				responseModel.setMessage(EkycConstants.SUCCESS_MSG);
				responseModel.setStat(EkycConstants.SUCCESS_STATUS);
				responseModel.setResult(profileEntity);
				responseModel.setPage(EkycConstants.PAGE_PROFILE);
			} else if (savingEntity != null) {
				responseModel.setMessage(EkycConstants.SUCCESS_MSG);
				responseModel.setStat(EkycConstants.SUCCESS_STATUS);
				responseModel.setResult(savingEntity);
				responseModel.setPage(EkycConstants.PAGE_AADHAR);
			} else {
				responseModel = commonMethods.constructFailedMsg(MessageConstants.ERROR_WHILE_SAVING_DOB);
			}
		} else {
			responseModel = commonMethods.constructFailedMsg(MessageConstants.USER_ID_INVALID);
		}

		return responseModel;
	}
}
