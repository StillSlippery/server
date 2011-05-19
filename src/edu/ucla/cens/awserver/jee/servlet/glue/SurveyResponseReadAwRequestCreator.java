package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;

/**
 * Builds an AwRequest for /app/survey_response/read.
 * 
 * @author selsky
 */
public class SurveyResponseReadAwRequestCreator implements AwRequestCreator {
	
	public SurveyResponseReadAwRequestCreator() {
		
	}
	
	public AwRequest createFrom(HttpServletRequest request) {

		String startDate = request.getParameter("start_date");
		String endDate = request.getParameter("end_date");
		String userList = request.getParameter("user_list");
		String client = request.getParameter("client");
		String campaignUrn = request.getParameter("campaign_urn");
		String authToken = request.getParameter("auth_token");
		String promptIdList = request.getParameter("prompt_id_list");
		String surveyIdList = request.getParameter("survey_id_list");
		String columnList = request.getParameter("column_list");
		String outputFormat = request.getParameter("output_format");
		String prettyPrint = request.getParameter("pretty_print");
		String suppressMetadata = request.getParameter("suppress_metadata");
		String privacyState = request.getParameter("privacy_state");
		String returnId = request.getParameter("return_id");
		String sortOrder = request.getParameter("sort_order");
		
		SurveyResponseReadAwRequest awRequest = new SurveyResponseReadAwRequest();
		
		awRequest.setStartDate(startDate);
		awRequest.setEndDate(endDate);
		awRequest.setUserToken(authToken);
		awRequest.setClient(client);
		awRequest.setCampaignUrn(campaignUrn);
		awRequest.setUserListString(userList);
		awRequest.setPromptIdListString(promptIdList);
		awRequest.setSurveyIdListString(surveyIdList);
		awRequest.setColumnListString(columnList);
		awRequest.setOutputFormat(outputFormat);
		awRequest.setPrettyPrintAsString(prettyPrint);
		awRequest.setSuppressMetadataAsString(suppressMetadata);
		awRequest.setReturnIdAsString(returnId);
		awRequest.setSortOrder(sortOrder);
		awRequest.setPrivacyState(privacyState);
		
		// temporarily using this frankenstein approach before migrating completely to toValidate()
		awRequest.addToValidate(InputKeys.SUPPRESS_METADATA, suppressMetadata, true);
		awRequest.addToValidate(InputKeys.PRETTY_PRINT, prettyPrint, true);
		awRequest.addToValidate(InputKeys.RETURN_ID, returnId, true);
		awRequest.addToValidate(InputKeys.SORT_ORDER, sortOrder, true);
		
		
        NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread _ this means that 
                                     // it will be in every log message for the current thread
		return awRequest;
	}
}
