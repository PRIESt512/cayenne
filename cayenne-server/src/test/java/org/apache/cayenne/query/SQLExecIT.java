/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.apache.cayenne.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.cayenne.DataRow;
import org.apache.cayenne.QueryResult;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.test.jdbc.DBHelper;
import org.apache.cayenne.unit.UnitDbAdapter;
import org.apache.cayenne.unit.di.server.CayenneProjects;
import org.apache.cayenne.unit.di.server.ServerCase;
import org.apache.cayenne.unit.di.server.UseServerRuntime;
import org.junit.Test;

@UseServerRuntime(CayenneProjects.TESTMAP_PROJECT)
public class SQLExecIT extends ServerCase {

    @Inject
    private DataContext context;

    @Inject
    private DBHelper dbHelper;

    @Inject
    private UnitDbAdapter unitDbAdapter;

    @Test
    public void test_DataMapNameRoot() throws Exception {
        int inserted = SQLExec.query("testmap", "INSERT INTO ARTIST (ARTIST_ID, ARTIST_NAME) VALUES (1, 'a')").update(
                context);
        assertEquals(1, inserted);
    }

    @Test
    public void test_DefaultRoot() throws Exception {
        int inserted = SQLExec.query("INSERT INTO ARTIST (ARTIST_ID, ARTIST_NAME) VALUES (1, 'a')").update(context);
        assertEquals(1, inserted);
    }

    @Test
    public void test_ParamsArray_Single() throws Exception {

        int inserted = SQLExec.query("INSERT INTO ARTIST (ARTIST_ID, ARTIST_NAME) VALUES (1, #bind($name))")
                .paramsArray("a3").update(context);

        assertEquals(1, inserted);
        assertEquals("a3", dbHelper.getString("ARTIST", "ARTIST_NAME").trim());
    }

    @Test
    public void test_ExecuteSelect() throws Exception {
        int inserted = SQLExec.query("INSERT INTO ARTIST (ARTIST_ID, ARTIST_NAME) VALUES (1, 'a')").update(context);
        assertEquals(1, inserted);

        QueryResult result = SQLExec.query("SELECT * FROM ARTIST").execute(context);
        assertEquals(2, result.size());
        assertTrue(result.isList());
        assertEquals(1, result.firstList().size());

        DataRow row = (DataRow)result.firstList().get(0);
        if(unitDbAdapter.isLowerCaseNames()) {
            assertTrue(row.containsKey("artist_id"));
            assertEquals(1L, ((Number)row.get("artist_id")).longValue());
            assertEquals("a", row.get("artist_name"));
        } else {
            assertTrue(row.containsKey("ARTIST_ID"));
            assertEquals(1L, ((Number)row.get("ARTIST_ID")).longValue());
            assertEquals("a", row.get("ARTIST_NAME"));
        }
    }

    @Test
    public void test_ParamsArray_Multiple() throws Exception {

        int inserted = SQLExec.query("INSERT INTO ARTIST (ARTIST_ID, ARTIST_NAME) VALUES (#bind($id), #bind($name))")
                .paramsArray(55, "a3").update(context);

        assertEquals(1, inserted);
        assertEquals(55L, dbHelper.getLong("ARTIST", "ARTIST_ID"));
        assertEquals("a3", dbHelper.getString("ARTIST", "ARTIST_NAME").trim());
    }
}
