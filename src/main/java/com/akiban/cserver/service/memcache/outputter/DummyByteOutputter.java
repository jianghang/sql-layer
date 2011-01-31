/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.cserver.service.memcache.outputter;

import com.akiban.cserver.RowData;
import com.akiban.cserver.RowDefCache;
import com.akiban.cserver.api.HapiGetRequest;
import com.akiban.cserver.api.HapiProcessor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public final class DummyByteOutputter implements HapiProcessor.Outputter {
    private static final DummyByteOutputter instance= new DummyByteOutputter();

    public static DummyByteOutputter instance() {
        return instance;
    }

    private DummyByteOutputter() {}

    @Override
    public void output(HapiGetRequest request, RowDefCache rowDefCache, List<RowData> rows, OutputStream outputStream) throws IOException {
        outputStream.write("DUMMY DATA".getBytes());
    }
}
