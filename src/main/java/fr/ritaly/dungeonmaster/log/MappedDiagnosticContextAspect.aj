/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.ritaly.dungeonmaster.log;

import org.apache.log4j.MDC;

aspect MappedDiagnosticContextAspect {
	pointcut classes(): within(fr.ritaly.*.*);

	pointcut constructors(): classes() && execution(new(..));

	pointcut methods(): classes() && execution(* *(..));

	private String depth = ">";

	before(): constructors() {
		depth = "-" + depth;

		MDC.put("depth", depth);
		// System.out.println("" + thisJoinPointStaticPart.getSignature());
	}

	after(): constructors() {
		depth = depth.substring(1);

		MDC.put("depth", depth);
	}
	
	before(): methods() {
		depth = "-" + depth;

		MDC.put("depth", depth);
	}

	after(): methods() {
		depth = depth.substring(1);

		MDC.put("depth", depth);
	}
}