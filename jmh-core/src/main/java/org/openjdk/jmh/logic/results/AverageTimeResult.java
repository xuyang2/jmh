/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.jmh.logic.results;

import org.openjdk.jmh.runner.parameters.TimeValue;
import org.openjdk.jmh.util.internal.ListStatistics;
import org.openjdk.jmh.util.internal.Statistics;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Result class that stores average operation time.
 */
public class AverageTimeResult extends Result {

    private final long operations;
    private final long durationNs;
    private final TimeUnit outputTimeUnit;

    public AverageTimeResult(ResultRole mode, String label, long operations, long durationNs, TimeUnit tu) {
        this(mode, label, operations, durationNs, tu, null);
    }

    AverageTimeResult(ResultRole mode, String label, long operations, long durationNs, TimeUnit tu, Statistics stat) {
        super(mode, label, stat);
        this.operations = operations;
        this.durationNs = durationNs;
        this.outputTimeUnit = tu;
    }

    @Override
    public String getScoreUnit() {
        return TimeValue.tuToString(outputTimeUnit) + "/op";
    }

    @Override
    public double getScore() {
        return 1.0D * durationNs / (operations * outputTimeUnit.toNanos(1));
    }

    public Aggregator<AverageTimeResult> getIterationAggregator() {
        return new ResultAggregator();
    }

    public Aggregator<AverageTimeResult> getRunAggregator() {
        return new ResultAggregator();
    }

    /**
     * Computes the aggregate result.
     * Regardless of aggregation, we need to compute the aggregate time as:
     *   average time = (all time) / (all operations)
     */
    static class ResultAggregator implements Aggregator<AverageTimeResult> {
        @Override
        public AverageTimeResult aggregate(Collection<AverageTimeResult> results) {
            ListStatistics stat = new ListStatistics();
            ResultRole role = null;
            String label = null;
            long operations = 0;
            long duration = 0;
            TimeUnit tu = null;
            for (AverageTimeResult r : results) {
                role = r.role;
                label = r.label;
                tu = r.outputTimeUnit;
                operations += r.operations;
                duration += r.durationNs;
                stat.addValue(r.getScore());
            }
            return new AverageTimeResult(role, label, operations, duration, tu, stat);
        }
    }

}
