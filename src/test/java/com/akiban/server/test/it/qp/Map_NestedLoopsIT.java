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

package com.akiban.server.test.it.qp;


import com.akiban.qp.expression.ExpressionRow;
import com.akiban.qp.expression.IndexBound;
import com.akiban.qp.expression.IndexKeyRange;
import com.akiban.qp.expression.RowBasedUnboundExpressions;
import com.akiban.qp.operator.Operator;
import com.akiban.qp.row.BindableRow;
import com.akiban.qp.row.Row;
import com.akiban.qp.row.RowBase;
import com.akiban.qp.rowtype.RowType;
import com.akiban.server.api.dml.SetColumnSelector;
import com.akiban.server.api.dml.scan.NewRow;
import com.akiban.server.expression.Expression;
import com.akiban.server.expression.std.BoundFieldExpression;
import com.akiban.server.expression.std.Comparison;
import com.akiban.server.expression.std.FieldExpression;
import com.akiban.server.expression.std.LiteralExpression;
import com.akiban.server.types.AkType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.akiban.qp.operator.API.*;
import static com.akiban.server.expression.std.Expressions.*;

public class Map_NestedLoopsIT extends OperatorITBase
{
    @Before
    public void before()
    {
        super.before();
        NewRow[] db = new NewRow[]{
            createNewRow(customer, 1L, "northbridge"), // two orders, two addresses
            createNewRow(order, 100L, 1L, "ori"),
            createNewRow(order, 101L, 1L, "ori"),
            createNewRow(address, 1000L, 1L, "111 1000 st"),
            createNewRow(address, 1001L, 1L, "111 1001 st"),
            createNewRow(customer, 2L, "foundation"), // two orders, one address
            createNewRow(order, 200L, 2L, "david"),
            createNewRow(order, 201L, 2L, "david"),
            createNewRow(address, 2000L, 2L, "222 2000 st"),
            createNewRow(customer, 3L, "matrix"), // one order, two addresses
            createNewRow(order, 300L, 3L, "tom"),
            createNewRow(address, 3000L, 3L, "333 3000 st"),
            createNewRow(address, 3001L, 3L, "333 3001 st"),
            createNewRow(customer, 4L, "atlas"), // two orders, no addresses
            createNewRow(order, 400L, 4L, "jack"),
            createNewRow(order, 401L, 4L, "jack"),
            createNewRow(customer, 5L, "highland"), // no orders, two addresses
            createNewRow(address, 5000L, 5L, "555 5000 st"),
            createNewRow(address, 5001L, 5L, "555 5001 st"),
            createNewRow(customer, 6L, "flybridge"), // no orders or addresses
            // Add a few items to test Product_ByRun rejecting unexpected input. All other tests remove these items.
            createNewRow(item, 1000L, 100L),
            createNewRow(item, 1001L, 100L),
            createNewRow(item, 1010L, 101L),
            createNewRow(item, 1011L, 101L),
            createNewRow(item, 2000L, 200L),
            createNewRow(item, 2001L, 200L),
            createNewRow(item, 2010L, 201L),
            createNewRow(item, 2011L, 201L),
            createNewRow(item, 3000L, 300L),
            createNewRow(item, 3001L, 300L),
            createNewRow(item, 4000L, 400L),
            createNewRow(item, 4001L, 400L),
            createNewRow(item, 4010L, 401L),
            createNewRow(item, 4011L, 401L),
        };
        use(db);
    }

    // Test argument validation

    @Test(expected = IllegalArgumentException.class)
    public void testLeftInputNull()
    {
        map_NestedLoops(null, groupScan_Default(coi), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRightInputNull()
    {
        map_NestedLoops(groupScan_Default(coi), null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeInputBindingPosition()
    {
        map_NestedLoops(groupScan_Default(coi), groupScan_Default(coi), -1);
    }

    // Test operator execution

    @Test
    public void testIndexLookup()
    {
        Operator plan =
            map_NestedLoops(
                indexScan_Default(itemOidIndexRowType, false),
                ancestorLookup_Nested(coi, itemOidIndexRowType, Collections.singleton(itemRowType), 0),
                0);
        RowBase[] expected = new RowBase[]{
            row(itemRowType, 1000L, 100L),
            row(itemRowType, 1001L, 100L),
            row(itemRowType, 1010L, 101L),
            row(itemRowType, 1011L, 101L),
            row(itemRowType, 2000L, 200L),
            row(itemRowType, 2001L, 200L),
            row(itemRowType, 2010L, 201L),
            row(itemRowType, 2011L, 201L),
            row(itemRowType, 3000L, 300L),
            row(itemRowType, 3001L, 300L),
            row(itemRowType, 4000L, 400L),
            row(itemRowType, 4001L, 400L),
            row(itemRowType, 4010L, 401L),
            row(itemRowType, 4011L, 401L),
        };
        compareRows(expected, cursor(plan, queryContext));
    }

    @Test
    public void testInnerJoin()
    {
        // customer order inner join, done as a general join
        Operator project =
            project_Default(
                select_HKeyOrdered(
                    filter_Default(
                        groupScan_Default(coi),
                        Collections.singleton(orderRowType)),
                    orderRowType,
                    compare(
                            field(orderRowType, 1) /* order.cid */,
                            Comparison.EQ,
                            boundField(customerRowType, 0, 0) /* customer.cid */)),
                orderRowType,
                Arrays.asList(boundField(customerRowType, 0, 0) /* customer.cid */, field(orderRowType, 0) /* order.oid */));
        Operator plan =
            map_NestedLoops(
                filter_Default(
                    groupScan_Default(coi),
                    Collections.singleton(customerRowType)),
                project,
                0);
        RowType projectRowType = project.rowType();
        RowBase[] expected = new RowBase[]{
            row(projectRowType, 1L, 100L),
            row(projectRowType, 1L, 101L),
            row(projectRowType, 2L, 200L),
            row(projectRowType, 2L, 201L),
            row(projectRowType, 3L, 300L),
            row(projectRowType, 4L, 400L),
            row(projectRowType, 4L, 401L),
        };
        compareRows(expected, cursor(plan, queryContext));
    }

    @Test
    public void testOuterJoin()
    {
        // customer order outer join, done as a general join
        Operator project = project_Default(
            select_HKeyOrdered(
                filter_Default(
                    groupScan_Default(coi),
                    Collections.singleton(orderRowType)),
                orderRowType,
                compare(
                        field(orderRowType, 1) /* order.cid */,
                        Comparison.EQ,
                        boundField(customerRowType, 0, 0) /* customer.cid */)),
            orderRowType,
            Arrays.asList(boundField(customerRowType, 0, 0) /* customer.cid */, field(orderRowType, 0) /* order.oid */));
        RowType projectRowType = project.rowType();
        Operator plan =
            map_NestedLoops(
                filter_Default(
                    groupScan_Default(coi),
                    Collections.singleton(customerRowType)),
                ifEmpty_Default(project,
                                projectRowType,
                                Arrays.asList(boundField(customerRowType, 0, 0), literal(null))),
                0);
        RowBase[] expected = new RowBase[]{
            row(projectRowType, 1L, 100L),
            row(projectRowType, 1L, 101L),
            row(projectRowType, 2L, 200L),
            row(projectRowType, 2L, 201L),
            row(projectRowType, 3L, 300L),
            row(projectRowType, 4L, 400L),
            row(projectRowType, 4L, 401L),
            row(projectRowType, 5L, null),
            row(projectRowType, 6L, null),
        };
        compareRows(expected, cursor(plan, queryContext));
    }

    @Test
    // Inspired by bug 869396
    public void testIndexScanUnderMapNestedLoopsUsedAsInnerLoopOfAnotherMapNestedLoops()
    {
        RowType cidValueRowType = schema.newValuesType(AkType.INT);
        IndexBound cidBound =
            new IndexBound(
                new RowBasedUnboundExpressions(
                    customerCidIndexRowType,
                    Arrays.asList((Expression) new BoundFieldExpression(
                        1,
                        new FieldExpression(cidValueRowType, 0)))),
                new SetColumnSelector(0));
        IndexKeyRange cidRange = IndexKeyRange.bounded(customerCidIndexRowType, cidBound, true, cidBound, true);
        Operator plan =
            map_NestedLoops(
                valuesScan_Default(
                        bindableExpressions(intRow(cidValueRowType, 1),
                                intRow(cidValueRowType, 2),
                                intRow(cidValueRowType, 3),
                                intRow(cidValueRowType, 4),
                                intRow(cidValueRowType, 5)),
                    cidValueRowType),
                map_NestedLoops(
                    indexScan_Default(customerCidIndexRowType, false, cidRange),
                    ancestorLookup_Nested(coi, customerCidIndexRowType, Collections.singleton(customerRowType), 0),
                    0),
                1);
        RowBase[] expected = new RowBase[]{
            row(customerRowType, 1L, "northbridge"),
            row(customerRowType, 2L, "foundation"),
            row(customerRowType, 3L, "matrix"),
            row(customerRowType, 4L, "atlas"),
            row(customerRowType, 5L, "highland"),
        };
        compareRows(expected, cursor(plan, queryContext));
    }

    private Row intRow(RowType rowType, int x)
    {
        return new ExpressionRow(rowType, queryContext,
                                 Arrays.asList((Expression) new LiteralExpression(AkType.INT, x)));
    }

    private Collection<? extends BindableRow> bindableExpressions(Row... rows) {
        List<BindableRow> result = new ArrayList<BindableRow>();
        for (Row row : rows) {
            result.add(BindableRow.of(row));
        }
        return result;
    }
}
