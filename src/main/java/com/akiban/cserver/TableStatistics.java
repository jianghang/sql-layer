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

package com.akiban.cserver;

import java.util.ArrayList;
import java.util.List;

public class TableStatistics {

    public final static int LOW_CARDINALITY = 1;

    private final int rowDefId;

    private long rowCount;

    private long autoIncrementValue;

    private int meanRecordLength;

    private int blockSize;

    private long creationTime;

    private long updateTime;

    private List<Histogram> histograms = new ArrayList<Histogram>();

    public TableStatistics(final int rowDefId) {
        this.rowDefId = rowDefId;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public long getAutoIncrementValue() {
        return autoIncrementValue;
    }

    public void setAutoIncrementValue(long autoIncrementValue) {
        this.autoIncrementValue = autoIncrementValue;
    }

    public int getMeanRecordLength() {
        return meanRecordLength;
    }

    public void setMeanRecordLength(int meanRecordLength) {
        this.meanRecordLength = meanRecordLength;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public List<Histogram> getHistogramList() {
        return histograms;
    }

    public void addHistogram(Histogram histogram) {
        this.histograms.add(histogram);
    }

    public void clearHistograms() {
        this.histograms.clear();
    }

    public int getRowDefId() {
        return rowDefId;
    }

    public static class Histogram {

        private final int indexId;

        private List<HistogramSample> samples = new ArrayList<HistogramSample>();

        public Histogram(final int indexId) {
            this.indexId = indexId;

        }

        public int getIndexId() {
            return indexId;
        }

        public final List<HistogramSample> getHistogramSamples() {
            return samples;
        }

        public void addSample(final HistogramSample sample) {
            this.samples.add(sample);
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder("Histogram(" + indexId
                    + ")[");
            boolean first = true;
            for (final HistogramSample sample : samples) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(CServerUtil.NEW_LINE);
                sb.append("  ");
                sb.append(sample);
            }
            sb.append(CServerUtil.NEW_LINE);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class HistogramSample {

        public HistogramSample(final RowData rowData, final long rowCount) {
            this.rowData = rowData;
            this.rowCount = rowCount;
        }

        private final RowData rowData;

        public RowData getRowData() {
            return rowData;
        }

        public long getRowCount() {
            return rowCount;
        }

        private final long rowCount;

        public String toString() {
            return String.format("%,8d->%s", rowCount, rowData.toString());
        }

    }
}
