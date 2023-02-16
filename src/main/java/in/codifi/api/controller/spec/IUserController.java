package in.codifi.api.controller.spec;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.wildfly.common.annotation.NotNull;

import in.codifi.api.entity.ApplicationUserEntity;
import in.codifi.api.model.ResponseModel;

public interface IUserController {

	/**
	 * test Method
	 */
	@Path("/testMethod")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Test")
	public ResponseModel test();

	/**
	 * Method to send otp to mobile number
	 * 
	 * @author prade
	 * @param userEntity
	 * @return
	 */
	@Path("/sendSmsOtp")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Method to send OTP to validate Mobile Number")
	public ResponseModel sendSmsOtp(@RequestBody ApplicationUserEntity userEntity);

	/**
	 * Method to validate sms OTP
	 * 
	 * @author prade
	 * @param userEntity
	 * @return
	 */
	@Path("/verifySmsOtp")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Method to verify Mobile Otp")
	public ResponseModel verifySmsOtp(@RequestBody ApplicationUserEntity userEntity);

	/**
	 * Method to send otp to Email Address
	 * 
	 * @author prade
	 * @param userEntity
	 * @return
	 */
	@Path("/sendMailOtp")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Method to send OTP to validate Email Id")
	public ResponseModel sendMailOtp(@RequestBody ApplicationUserEntity userEntity);

	/**
	 * Method to validate sms OTP
	 * 
	 * @author prade
	 * @param userEntity
	 * @return
	 */
	@Path("/verifyEmailOtp")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Method to verify email Otp")
	public ResponseModel verifyEmailOtp(@RequestBody ApplicationUserEntity userEntity);

	/**
	 * Method to get User Details
	 * 
	 * @author prade
	 * @param applicationId
	 * @return
	 */
	@Path("/getUsrDetails")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Method to get Profile Details")
	ResponseModel getUserDetailsByAppId(@NotNull @QueryParam("applicationId") long applicationId);

	/**
	 * Method to Update Stage to skip
	 * 
	 * @author prade
	 * @param applicationId
	 * @return
	 */
	@Path("/updateStage")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Method to get Profile Details")
	ResponseModel updateStage(@NotNull @QueryParam("applicationId") long applicationId,
			@QueryParam("stage") double stage);

}
