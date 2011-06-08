/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.ohmage.request.CampaignUpdateAwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;


public class CampaignUpdateValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(CampaignUpdateValidator.class);
	
	private DiskFileItemFactory _diskFileItemFactory;
	private int _fileSizeMax;
	
	/**
	 * Basic constructor that sets up the list of required parameters.
	 */
	public CampaignUpdateValidator(DiskFileItemFactory diskFileItemFactory, int fileSizeMax) {
		if(diskFileItemFactory == null) {
			throw new IllegalArgumentException("a DiskFileItemFactory is required");
		}
		_diskFileItemFactory = diskFileItemFactory;
		_fileSizeMax = fileSizeMax;
	}

	/**
	 * Ensures that all the required parameters exist and that each parameter
	 * is of a sane length.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		String requestType = httpRequest.getContentType();
		
		if(requestType.contains("multipart/form-data;") || requestType.contains("multipart/mixed;")) {
			return validateMultipartRequest(httpRequest);
		}
		else {
			return validateRegularRequest(httpRequest);
		}
	}
	
	/**
	 * If the HTTP request is just a regular request without the XML attached,
	 * it will be validated through this call which will build the actual 
	 * request object and set it as a new parameter. t will return false if
	 * the validation fails.
	 * 
	 * @param httpRequest The HTTP request as generated by JEE.
	 * 
	 * @return True iff the validation completely passes and a new request
	 * 		   object is generated and added to the 'httpRequest's list of
	 * 		   parameters.
	 */
	private boolean validateRegularRequest(HttpServletRequest httpRequest) {
		String token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String urn = httpRequest.getParameter(InputKeys.CAMPAIGN_URN);
		
		if(token == null) {
			_logger.warn("Missing " + InputKeys.AUTH_TOKEN);
		}
		else if(urn == null) {
			_logger.warn("Missing " + InputKeys.CAMPAIGN_URN);
		}
		else if(greaterThanLength("authToken", InputKeys.AUTH_TOKEN, token, 36)) {
			_logger.warn(InputKeys.AUTH_TOKEN + " is too long.");
			return false;
		}
		else if(greaterThanLength("campaignUrn", InputKeys.CAMPAIGN_URN, urn, 255)) {
			_logger.warn(InputKeys.CAMPAIGN_URN + " is too long.");
			return false;
		}
		
		CampaignUpdateAwRequest request = new CampaignUpdateAwRequest();
		request.setUserToken(token);
		request.setCampaignUrn(urn);
		
		String description = httpRequest.getParameter(InputKeys.DESCRIPTION);
		if((description != null) && (! "".equals(description))) {
			request.setDescription(description);
		}
		
		String runningState = httpRequest.getParameter(InputKeys.RUNNING_STATE);
		if((runningState != null) && (! "".equals(runningState))) {
			request.setRunningState(runningState);
		}
		
		String privacyState = httpRequest.getParameter(InputKeys.PRIVACY_STATE);
		if((privacyState != null) && (! "".equals(privacyState))) {
			request.setPrivacyState(privacyState);
		}
		
		String classUrnList = httpRequest.getParameter(InputKeys.CLASS_URN_LIST);
		if((classUrnList != null) && (! "".equals(classUrnList))) {
			request.setCommaSeparatedClasses(classUrnList);
		}
		
		String userRolesListAdd = httpRequest.getParameter(InputKeys.USER_ROLE_LIST_ADD);
		if((userRolesListAdd != null) && (! "".equals(userRolesListAdd))) {
			request.setUserRoleListAdd(userRolesListAdd);
		}
		
		String userRolesListRemove = httpRequest.getParameter(InputKeys.USER_ROLE_LIST_REMOVE);
		if((userRolesListRemove != null) && (! "".equals(userRolesListRemove))) {
			request.setUserRoleListRemove(userRolesListRemove);
		}
		
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		if(client == null) {
			return false;
		}
		
		httpRequest.setAttribute("awRequest", request);
		return true;
	}
	
	/**
	 * If the HTTP request is a multipart request which means that it should
	 * have XML attached to it, then this will handle the validation. It will
	 * create the actual request object and place it in the httpRequest as a
	 * new parameter. It will return false if the validation fails.
	 * 
	 * @param httpRequest The HTTP request as generated by JEE.
	 * 
	 * @return True iff the validation completely passes and a new request
	 * 		   object is generated and added to the 'httpRequest's list of
	 * 		   parameters.
	 */
	private boolean validateMultipartRequest(HttpServletRequest httpRequest) {
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(_diskFileItemFactory);
		upload.setHeaderEncoding("UTF-8");
		upload.setFileSizeMax(_fileSizeMax);
		
		// Parse the request
		List<?> uploadedItems = null;
		try {
		
			uploadedItems = upload.parseRequest(httpRequest);
		
		} catch(FileUploadException fue) { 			
			_logger.error("Caught exception while uploading XML to update a campaign.", fue);
			throw new IllegalStateException(fue);
		}
		
		// Get the number of items were in the request.
		int numberOfUploadedItems = uploadedItems.size();
		
		// Parse the request for each of the parameters.
		CampaignUpdateAwRequest request = new CampaignUpdateAwRequest();
		for(int i = 0; i < numberOfUploadedItems; i++) {
			FileItem fi = (FileItem) uploadedItems.get(i);
			if(fi.isFormField()) {
				String name = fi.getFieldName();
				String tmp = StringUtils.urlDecode(fi.getString());
				
				if(InputKeys.DESCRIPTION.equals(name)) {
					if(greaterThanLength("description", InputKeys.DESCRIPTION, tmp, 65535)) {
						return false;
					}
					else if(! "".equals(tmp)) {
						request.setDescription(tmp);
					}
				}
				else if(InputKeys.RUNNING_STATE.equals(name)) {
					if(greaterThanLength("runningState", InputKeys.RUNNING_STATE, tmp, 50)) {
						return false;
					}
					else if(! "".equals(tmp)) {
						request.setRunningState(tmp);
					}
				}
				else if(InputKeys.PRIVACY_STATE.equals(name)) {
					if(greaterThanLength("privacyState", InputKeys.PRIVACY_STATE, tmp, 50)) {
						return false;
					}
					else if(! "".equals(tmp)) {
						request.setPrivacyState(tmp);
					}
				}
				else if(InputKeys.CLASS_URN_LIST.equals(name)) {
					// Note: This is based on the maximum size of a campaign
					// times 100 plus 100 commas.
					if(greaterThanLength("classes", InputKeys.CLASS_URN_LIST, tmp, 25600)) {
						return false;
					}
					else if(! "".equals(tmp)) {
						request.setCommaSeparatedClasses(tmp);
					}
				}
				else if(InputKeys.AUTH_TOKEN.equals(name)) {
					if(greaterThanLength("authToken", InputKeys.AUTH_TOKEN, tmp, 36)) {
						return false;
					}
					else if(! "".equals(tmp)) {
						request.setUserToken(tmp);
					}
				}
				else if(InputKeys.CAMPAIGN_URN.equals(name)) {
					if(greaterThanLength("campaignUrn", InputKeys.CAMPAIGN_URN, tmp, 255)) {
						return false;
					}
					else if(! "".equals(tmp)) {
						request.setCampaignUrn(tmp);
					}
				}
				else if(InputKeys.USER_ROLE_LIST_ADD.equals(name)) {
					if(greaterThanLength(InputKeys.USER_ROLE_LIST_ADD, InputKeys.USER_ROLE_LIST_ADD, tmp, 25600)) {
						return false;
					}
					else if(! "".equals(tmp)) {
						request.setUserRoleListAdd(tmp);
					}
				}
				else if(InputKeys.USER_ROLE_LIST_REMOVE.equals(name)) {
					if(greaterThanLength(InputKeys.USER_ROLE_LIST_REMOVE, InputKeys.USER_ROLE_LIST_REMOVE, tmp, 25600)) {
						return false;
					}
					else if(! "".equals(tmp)) {
						request.setUserRoleListRemove(tmp);
					}
				}
			} else {
				// The XML data is not checked because its length is so variable and potentially huge.
				// The default setting for Tomcat is to disallow requests that are greater than 2MB, which may have to change in the future
				byte[] xml = fi.get();
				if(xml.length != 0) {
					request.setXmlAsByteArray(xml); // Gets the XML file.
				}
			}
		}
		
		if(request.getUserToken() == null) {
			_logger.warn("No token in the request.");
			return false;
		}
		else if(request.getCampaignUrn() == null) {
			_logger.warn("No campaign URN in the request.");
			return false;
		}
		httpRequest.setAttribute("awRequest", request);
		
		return true;
	}
}