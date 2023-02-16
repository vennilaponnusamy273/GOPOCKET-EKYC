package in.codifi.api.service.spec;

import in.codifi.api.model.NomineeDocModel;
import in.codifi.api.model.ResponseModel;

public interface INomineeService {
	/**
	 * Method to get Nominee Details
	 * 
	 * @author prade
	 * 
	 * @param applicationId
	 * @return
	 */
	ResponseModel getNominee(long applicationId);

	/**
	 * Method to save and upload Nominee Proof
	 * 
	 * @author prade
	 * 
	 * @param fileModel
	 * @return
	 */

	ResponseModel uploadDocNominee(NomineeDocModel fileModel);

}