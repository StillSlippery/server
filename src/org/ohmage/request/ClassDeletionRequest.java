package org.ohmage.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.service.ClassServices;
import org.ohmage.service.ServiceException;
import org.ohmage.service.UserServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.ValidationException;

/**
 * <p>A request to delete a class. The requester must be an admin.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN}</td>
 *     <td>The unique identifier for the class to be deleted.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassDeletionRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassDeletionRequest.class);
	
	private final String classId;
	
	/**
	 * Builds this request based on the information in the HttpServletRequest.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the required
	 * 					  parameters.
	 */
	public ClassDeletionRequest(HttpServletRequest httpRequest) {
		super(CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN), httpRequest.getParameter(InputKeys.CLIENT));
		
		LOGGER.info("Creating a class deletion request.");
		
		String tempClassId = null;
		
		if(! failed) {
			try {
				tempClassId = ClassValidators.validateClassId(this, httpRequest.getParameter(InputKeys.CLASS_URN));
				if(tempClassId == null) {
					setFailed(ErrorCodes.CLASS_MISSING_ID, "Missing required class URN.");
					throw new ValidationException("Missing required key: " + InputKeys.CLASS_URN);
				}
			}
			catch(ValidationException e) {
				LOGGER.info("One of the parameters was invalid.", e);
			}
		}
		
		classId = tempClassId;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a class deletion request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			LOGGER.info("Checking that the user is an admin.");
			UserServices.verifyUserIsAdmin(this, user.getUsername());
			
			LOGGER.info("Checking that the class exists.");
			ClassServices.checkClassExistence(this, classId, true);
			
			LOGGER.info("Deleting the class.");
			ClassServices.deleteClass(this, classId);
		}
		catch(ServiceException e) {
			LOGGER.error("A Service threw an exception.", e);
		}
	}

	/**
	 * Responds to the deletion request. Returns success if it successfully 
	 * deleted the class or an error code and explanation if anything went
	 * wrong.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}