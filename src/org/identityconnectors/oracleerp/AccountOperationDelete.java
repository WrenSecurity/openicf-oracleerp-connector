/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.     
 * 
 * The contents of this file are subject to the terms of the Common Development 
 * and Distribution License("CDDL") (the "License").  You may not use this file 
 * except in compliance with the License.
 * 
 * You can obtain a copy of the License at 
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.oracleerp;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.dbcommon.SQLUtil;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.operations.DeleteOp;


/**
 * The Account CreateOp implementation of the SPI
 * 
 * { call "+cfg.app()+"fnd_user_pkg.disableuser(?) }
 *  
 * @author Petr Jung
 * @version $Revision 1.0$
 * @since 1.0
 */
final class AccountOperationDelete extends Operation implements DeleteOp {

    /**
     * Setup logging.
     */
    static final Log log = Log.getLog(AccountOperationDelete.class);
    
    OracleERPConnector co;
    
    /**
     * @param conn
     * @param cfg
     */
    protected AccountOperationDelete(OracleERPConnection conn, OracleERPConfiguration cfg, OracleERPConnector co) {
        super(conn, cfg);
        this.co = co;
    }

    /* (non-Javadoc)
     * @see org.identityconnectors.framework.spi.operations.DeleteOp#delete(org.identityconnectors.framework.common.objects.ObjectClass, org.identityconnectors.framework.common.objects.Uid, org.identityconnectors.framework.common.objects.OperationOptions)
     */
    public void delete(ObjectClass objClass, Uid uid, OperationOptions options) {
        final String sql = "{ call "+cfg.app()+"fnd_user_pkg.disableuser(?) }";
        log.info("delete");
        CallableStatement cs = null;
        try {
            cs = conn.prepareCall(sql);
            final String identity = AttributeUtil.getAsStringValue(uid).toUpperCase();
            cs.setString(1, identity);
            cs.execute();
            
            conn.commit();
            // No Result ??
        } catch (SQLException e) {
            if (e.getErrorCode() == 20001 || e.getErrorCode() == 1403) {
                final String msg = "SQL Exception trying to delete Oracle user '{0}' ";
                SQLUtil.rollbackQuietly(conn);
                throw new IllegalArgumentException(MessageFormat.format(msg, uid),e);
            } else {
              throw new UnknownUidException(uid, objClass);
            }
        } finally {
            SQLUtil.closeQuietly(cs);
            cs = null;
        }
    }
}
