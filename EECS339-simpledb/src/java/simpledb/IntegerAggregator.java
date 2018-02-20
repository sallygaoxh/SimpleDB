package simpledb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.sun.org.apache.bcel.internal.generic.NEW;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {
	int gbfield;
	Type gbfieldtype;
	int afield;
	Op what;
	int counter = 0;
	HashMap<Field, Tuple> aTuples = new HashMap<Field, Tuple>();
	HashMap<Field, Integer> gbFields_count = new HashMap<Field, Integer>();
	HashMap<Field, Integer> gbFields_sum = new HashMap<Field, Integer>();
	ArrayList<Tuple> aggregatedTuples =new ArrayList<Tuple>(); 
	Iterator tIterator = aTuples.entrySet().iterator();
    private static final long serialVersionUID = 1L;

    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *            the 0-based index of the group-by field in the tuple, or
     *            NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *            the type of the group by field (e.g., Type.INT_TYPE), or null
     *            if there is no grouping
     * @param afield
     *            the 0-based index of the aggregate field in the tuple
     * @param what
     *            the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
    	this.afield = afield;
    	this.gbfield = gbfield;
    	this.gbfieldtype = gbfieldtype;
    	this.what = what;
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here	    	
		Tuple result;
		Field key;
		if(this.gbfield == Aggregator.NO_GROUPING){
			counter++;
			key = new IntField(0);
			int currValue = ((IntField)tup.getField(afield)).getValue();
	    	Type[] typeAr;
	    	TupleDesc td;
    		typeAr = new Type[1];
    		typeAr[0] = Type.INT_TYPE;
	    	result = new Tuple(new TupleDesc(typeAr));
	    	if (this.what == Aggregator.Op.AVG){
		    	if (aTuples.isEmpty()){
		    		result.setField(0, new IntField(currValue));
		    		aTuples.put(key, result);
		    		gbFields_sum.put(key, currValue);
		    	}
		    	else{
		    		int sum = gbFields_sum.get(key);
		    		sum = sum + currValue;
		    		gbFields_sum.put(key, sum);
		    		int avg = (int) sum/counter;
		    		result.setField(0, new IntField(avg));
		    		aTuples.put(key, result);
		    	}
	    	}
	    	else if (this.what == Aggregator.Op.COUNT){
	    		result.setField(0, new IntField(counter));
	    		aTuples.put(key, result);
	    	}
	    	else{
		    	result.setField(0, tup.getField(afield));
				key = tup.getField(afield);
				this.aTuples.put(key, result);
				System.out.println("map size: "+aTuples.size());
	    	}

		}
		else{
			//the groupby field already recorded
			int currentAggregateValue = 0;
			int sum = 0;
			key = tup.getField(gbfield);
	    	if (this.aTuples.containsKey(key)){ 		
	    		result = this.aTuples.get(key);
	    		int value = ((IntField)this.aTuples.get(key).getField(1)).getValue();
	    		
	    		if (this.what == Aggregator.Op.COUNT){		
	        		int count = gbFields_count.get(key);
	        		count++;
	        		gbFields_count.put(key, count);
	        		value = count;
	    		}
	    		else if (this.what == Aggregator.Op.MAX){
	    			currentAggregateValue = ((IntField)tup.getField(afield)).getValue();
	    			if(currentAggregateValue>value)
	    				value = currentAggregateValue;
	    		}
	    		else if (this.what == Aggregator.Op.MIN){
	    			currentAggregateValue = ((IntField)tup.getField(afield)).getValue();
	    			if(currentAggregateValue<value)
	    				value = currentAggregateValue;
	    		}
	    		else if (this.what == Aggregator.Op.SUM){
	    			currentAggregateValue = ((IntField)tup.getField(afield)).getValue();
	    			value = value+currentAggregateValue;
	    		}
	    		else if (this.what == Aggregator.Op.AVG){
	    			currentAggregateValue = ((IntField)tup.getField(afield)).getValue();
	    			sum = gbFields_sum.get(key);
	        		sum += currentAggregateValue;
	        		gbFields_sum.put(key, sum);
	        		int count = gbFields_count.get(key);
	        		count++;
	        		gbFields_count.put(key, count);
	    			value = (int) sum/count;
	    		}
	    		
	    		result.setField(1, new IntField(value));
	    		this.aTuples.put(key,result);
	    		
	    	}
	    	//The groupby field has not been recorded.
	    	else{    			
		    	result = new Tuple(this.getTupleDesc());
		    	result.setField(0, tup.getField(gbfield));
	    		if (this.what == Aggregator.Op.COUNT){
	    			result.setField(1, new IntField(1));
	    			gbFields_count.put(key, 1);
	    		}
	    		else if (this.what == Aggregator.Op.AVG){
	    			result.setField(1, tup.getField(afield));
	    			currentAggregateValue = ((IntField)tup.getField(afield)).getValue();
	    			gbFields_sum.put(key, currentAggregateValue);
	    			gbFields_count.put(key, 1);
	    		}
	    		else {
	    			result.setField(1, tup.getField(afield));
				}
	    		this.aTuples.put(key,result);
	    	}
		}
    }
    
    public TupleDesc getTupleDesc() {
    	Type[] typeAr;
    	TupleDesc td;
    	if (this.gbfield == Aggregator.NO_GROUPING){
    		typeAr = new Type[1];
    		typeAr[0] = Type.INT_TYPE;
    	}
    	else{
			typeAr = new Type[2];
			typeAr[0] = this.gbfieldtype;
			typeAr[1] = Type.INT_TYPE;
    	}
    	td = new TupleDesc(typeAr);
    	return td;
    }

    /**
     * Create a OpIterator over group aggregate results.
     * 
     * @return a OpIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public OpIterator iterator() {
        // some code goes here   	
    	OpIterator iterator = new OpIterator() {	
    		TupleDesc tupleDesc = getTupleDesc();
			@Override
			public void rewind() throws DbException, TransactionAbortedException {
				tIterator = null;
				tIterator = aTuples.entrySet().iterator();
			}
			
			@Override
			public void open() throws DbException, TransactionAbortedException {
				tIterator = aTuples.entrySet().iterator();
			}
			
			@Override
			public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
//				if (tIterator.hasNext()){
					Map.Entry entry = (Map.Entry)tIterator.next();
					return (Tuple) entry.getValue();
//				}else
//					throw new NoSuchElementException();
//					return null;
			}
			
			@Override
			public boolean hasNext() throws DbException, TransactionAbortedException {
				// TODO Auto-generated method stub
				return tIterator.hasNext();
			}
			
			@Override
			public TupleDesc getTupleDesc() {
				// TODO Auto-generated method stub
				return tupleDesc;
			}
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				tIterator = null;
			}
		};
    	
        return iterator;
    }

}
