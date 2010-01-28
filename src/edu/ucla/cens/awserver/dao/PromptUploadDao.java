package edu.ucla.cens.awserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.PromptDataPacket;
import edu.ucla.cens.awserver.domain.PromptDataPacket.PromptResponseDataPacket;


/**
 * DAO for handling persistence of uploaded prompt data.
 * 
 * @author selsky
 */
public class PromptUploadDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(PromptUploadDao.class);
	
	private final String _selectSql = "select id from prompt" +
			                          " where campaign_prompt_group_id = ?" +
			                          " and campaign_prompt_version_id = ?" +
			                          " and prompt_config_id = ?";
	
	private final String _insertSql = "insert into prompt_response" +
	                                  " (prompt_id, user_id, utc_time_stamp, utc_epoch_millis, phone_timezone," +
	                                  " latitude, longitude, json_data) " +
	                                  " values (?,?,?,?,?,?,?,?)";
	
	public PromptUploadDao(DataSource datasource) {
		super(datasource);
	}
	
	/**
	 * Inserts prompt upload DataPackets into the DB. 
	 */
	public void execute(AwRequest request) {
		_logger.info("beginning prompt upload persistence");
		
		List<DataPacket> dataPackets = (List<DataPacket>) request.getAttribute("dataPackets");
		int numberOfPackets = dataPackets.size();
		
		_logger.info("found " + numberOfPackets + " packets");
		
		final int userId = request.getUser().getId();
		
		for(int i = 0; i < numberOfPackets; i++) {
			
			final PromptDataPacket promptDataPacket = (PromptDataPacket) dataPackets.get(i);
			List<PromptResponseDataPacket> promptResponses = promptDataPacket.getResponses();
			int numberOfResponses = promptResponses.size();
			
			_logger.info("found " + numberOfResponses + " responses");
			
			JdbcTemplate template = new JdbcTemplate(getDataSource());
			
			for(int j = 0; j < numberOfResponses; j++) {
				
				_logger.info("in loop at index " + j);
				
				final PromptResponseDataPacket response = promptResponses.get(j);
				
				try {
				
					final int promptId = template.queryForInt(
						_selectSql, new Object[]{request.getAttribute("campaignPromptGroupId"), 
								                 request.getAttribute("campaignPromptVersionId"), 
								                 response.getPromptConfigId()}
				    );
					
					_logger.info("found prompt id  " + promptId);
					
					// Now insert the response 
					
					// TODO -- check for duplicates for any exception thrown, log duplicates
					// exception handling
					
					int numberOfRowsUpdated = template.update( 
						new PreparedStatementCreator() {
							public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
								PreparedStatement ps = connection.prepareStatement(_insertSql);
								ps.setInt(1, promptId);
								ps.setInt(2, userId);
								ps.setTimestamp(3, Timestamp.valueOf(promptDataPacket.getUtcDate()));
								ps.setLong(4, promptDataPacket.getUtcTime());
								ps.setString(5, promptDataPacket.getTimezone());
								ps.setDouble(6, promptDataPacket.getLatitude().equals(Double.NaN) ? null : promptDataPacket.getLatitude());
								ps.setDouble(7, promptDataPacket.getLongitude().equals(Double.NaN) ? null : promptDataPacket.getLongitude());
								ps.setString(8, response.getResponse());
								
								return ps;
							}
						}
				    );
					
					_logger.info("number of rows updated = " + numberOfRowsUpdated);
					
				
				} catch(IncorrectResultSizeDataAccessException irsdae) { // the _selectSql query did not return one row, a bad data problem.
					
					throw new DataAccessException(irsdae);
					
				} catch(org.springframework.dao.DataAccessException dae) {
					
					throw new DataAccessException(dae);
				}
				
			}
		}
	}
}