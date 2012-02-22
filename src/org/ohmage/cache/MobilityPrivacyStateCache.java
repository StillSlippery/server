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
package org.ohmage.cache;

import javax.sql.DataSource;

/**
 * Singleton cache for the indices and String values for Mobility privacy
 * states.
 * 
 * @author John Jenkins
 */
public final class MobilityPrivacyStateCache extends StringAndIdCache {
	// The column IDs for the query.
	private static final String ID_COLUMN = "Id";
	private static final String STATE_COLUMN = "privacy_state";
	
	private static final String SQL_GET_MOBILITY_PRIVACY_STATES_AND_IDS = 
		"SELECT " + ID_COLUMN + ", " + STATE_COLUMN + " " +
		"FROM mobility_privacy_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "mobilityPrivacyStateCache";
	
	// Known Mobility privacy states.
	public static final String PRIVACY_STATE_PRIVATE = "private";
	/**
	 * This will probably be used somewhere down the road, but for now it 
	 * shouldn't be used unless as an option in a WHERE clause where it will
	 * always return null.
	 * 
	 * @deprecated
	 */
	public static final String PRIVACY_STATE_SHARED = "shared";
	
	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static MobilityPrivacyStateCache instance;

	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private MobilityPrivacyStateCache(DataSource dataSource, long updateFrequency) {
		super(dataSource, updateFrequency, SQL_GET_MOBILITY_PRIVACY_STATES_AND_IDS, ID_COLUMN, STATE_COLUMN);
		
		instance = this;
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static MobilityPrivacyStateCache instance() {
		return instance;
	}
	
	/**
	 * Returns a human-readable name for this cache.
	 */
	@Override
	public String getName() {
		return CACHE_KEY;
	}
}