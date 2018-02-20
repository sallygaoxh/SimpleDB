package simpledb;

import java.util.*;

import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {
	OpIterator child;
	int afield;
	int gfield;
	Aggregator.Op aop;
	OpIterator iterator = null;
	Aggregator aggregator = null;
	
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntegerAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     * 
     * 
     * @param child
     *            The OpIterator that is feeding us tuples.
     * @param afield
     *            The column over which we are computing an aggregate.
     * @param gfield
     *            The column over which we are grouping the result, or -1 if
     *            there is no grouping
     * @param aop
     *            The aggregation operator to use
     */
    public Aggregate(OpIterator child, int afield, int gfield, Aggregator.Op aop) {
	// some code goes here
    	this.child = child;
    	this.afield = afield;
    	this.gfield = gfield;
    	this.aop = aop;	
    	if (gfield!=Aggregator.NO_GROUPING){
    		if (this.child.getTupleDesc().getFieldType(afield)==Type.INT_TYPE){
    			aggregator = new IntegerAggregator(gfield, this.child.getTupleDesc().getFieldType(gfield), afield, aop);
    		}
    		else {
    			aggregator = new StringAggregator(gfield, this.child.getTupleDesc().getFieldType(gfield), afield, aop);			
    		}
    	}
    	else{
    		if (this.child.getTupleDesc().getFieldType(afield)==Type.INT_TYPE){
    			aggregator = new IntegerAggregator(gfield, Type.STRING_TYPE, afield, aop);
    		}
    		else {
    			aggregator = new StringAggregator(gfield, Type.STRING_TYPE, afield, aop);			
    		}
    	}
    	try {
			this.child.open();
		} catch (DbException e) {
			e.printStackTrace();
		} catch (TransactionAbortedException e) {
			e.printStackTrace();
		}
		try {
			while(this.child.hasNext()){
				Tuple next = this.child.next();
				this.aggregator.mergeTupleIntoGroup(next);
			}
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (DbException e) {
			e.printStackTrace();
		} catch (TransactionAbortedException e) {
			e.printStackTrace();
		}
		iterator = aggregator.iterator();
    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link simpledb.Aggregator#NO_GROUPING}
     * */
    public int groupField() {
	// some code goes here
    	if(this.gfield == Aggregator.NO_GROUPING)
    		return Aggregator.NO_GROUPING;
    	else
    		return this.gfield;
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples. If not, return
     *         null;
     * */
    public String groupFieldName() {
	// some code goes here
    	if (this.gfield != Aggregator.NO_GROUPING)
    		return this.child.getTupleDesc().getFieldName(gfield);
    	else
    		return null;
    }

    /**
     * @return the aggregate field
     * */
    public int aggregateField() {
	// some code goes here
    	return this.afield;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     * */
    public String aggregateFieldName() {
	// some code goes here
    	return this.child.getTupleDesc().getFieldName(afield);
    }

    /**
     * @return return the aggregate operator
     * */
    public Aggregator.Op aggregateOp() {
	// some code goes here
    	return this.aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
    	return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
	    TransactionAbortedException {
	// some code goes here

		this.child.rewind();
		this.iterator.open();
		super.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate. If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
	// some code goes here
    	if (this.iterator.hasNext())
    		return this.iterator.next();
    	else 
    		return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
	// some code goes here
    	this.iterator.rewind();
    	this.child.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * 
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
	// some code goes here
    	Type[] typeAr;
    	String[] fieldAr;
    	TupleDesc td;

    	if (gfield == Aggregator.NO_GROUPING) {
    		typeAr = new Type[1];
    		fieldAr = new String[1];
    		typeAr[0] = this.child.getTupleDesc().getFieldType(afield);
    		fieldAr[0] = this.child.getTupleDesc().getFieldName(afield);
    	} else {
    		typeAr = new Type[2];
    		fieldAr = new String[2];
    		typeAr[0] = this.child.getTupleDesc().getFieldType(gfield);
    		typeAr[1] = this.child.getTupleDesc().getFieldType(afield);
    		fieldAr[0] = this.child.getTupleDesc().getFieldName(gfield);
    		fieldAr[1] = Aggregate.nameOfAggregatorOp(aop)+" "+this.child.getTupleDesc().getFieldName(afield);
    	}
    	td = new TupleDesc(typeAr);
    	return td;
    }

    public void close() {
	// some code goes here
    	super.close();
    	iterator.close();
    	child.close();
    }

    @Override
    public OpIterator[] getChildren() {
	// some code goes here
    	return new OpIterator[] { this.child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
	// some code goes here
    	if (this.child!=children[0])
    	{
    	    this.child = children[0];
    	}
    }
    
}
