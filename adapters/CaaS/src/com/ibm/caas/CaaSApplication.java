/*
 ********************************************************************
 * Licensed Materials - Property of IBM                             *
 *                                                                  *
 * Copyright IBM Corp. 2015 All rights reserved.                    *
 *                                                                  *
 * US Government Users Restricted Rights - Use, duplication or      *
 * disclosure restricted by GSA ADP Schedule Contract with          *
 * IBM Corp.                                                        *
 *                                                                  *
 * DISCLAIMER OF WARRANTIES. The following [enclosed] code is       *
 * sample code created by IBM Corporation. This sample code is      *
 * not part of any standard or IBM product and is provided to you   *
 * solely for the purpose of assisting you in the development of    *
 * your applications. The code is provided "AS IS", without         *
 * warranty of any kind. IBM shall not be liable for any damages    *
 * arising out of your use of the sample code, even if they have    *
 * been advised of the possibility of such damages.                 *
 ********************************************************************
 */

package com.ibm.caas;

import java.util.logging.Logger;
import com.worklight.wink.extensions.MFPJAXRSApplication;

public class CaaSApplication extends MFPJAXRSApplication {

	static Logger logger = Logger.getLogger(CaaSApplication.class.getName());

	@Override
	protected void init() throws Exception {
		logger.info("Adapter initialized!");
	}

	@Override
	protected void destroy() throws Exception {
		logger.info("Adapter destroyed!");
	}

	@Override
	protected String getPackageToScan() {
		// The package of this class will be scanned (recursively) to find
		// JAX-RS resources.
		// It is also possible to override "getPackagesToScan" method in order
		// to return more than one package for scanning
		return getClass().getPackage().getName();
	}
}
