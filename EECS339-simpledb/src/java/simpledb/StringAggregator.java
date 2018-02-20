package simpledb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import simpledb.Aggregator.Op;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {
	int gbfield;
	Type gbfieldtype;
	int afield;
	Op what;
//	ArrayList<Tuple> aggregatedTuples = new ArrayList<Tuple>();
	HashMap<Field, Tuple> aTuples = new HashMap<Field, Tuple>();
	Iterator tIterator = aTuples.entrySet().iterator();
    private static final long serialVersionUID = 1L;

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
    	this.afield = afield;
    	this.gbfield = gbfield;
    	this.gbfieldtype = gbfieldtype;
    	this.what = what;
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here   	
		Field key;
		Tuple result;
		if(this.gbfield == Aggregator.NO_GROUPING){
			key = new IntField(aTuples.size());
	    	Type[] typeAr;
	    	TupleDesc td;
    		typeAr = new Type[1];
    		typeAr[0] = tup.getField(afield).getType();
	    	result = new Tuple(new TupleDesc(typeAr));
	    	result.setField(0, tup.getField(afield));
	    	this.aTuples.put(key, result);
		}
		else{
	    	//the groupby field already recorded
			key = tup.getField(gbfield);
	    	if (aTuples.containsKey(key)){
	    		result = aTuples.get(key);
	    		
	    		if (this.what == Aggregator.Op.COUNT){
	    			int count = ((IntField)result.getField(1)).getValue();		
	    			count += 1;
	    			result.setField(1, new IntField(count));
	        		aTuples.put(key, result);	
	    		}
	    			
	    	}else{
		    	result = new Tuple(this.getTupleDesc());
		    	result.setField(0, tup.getField(gbfield));
		    	if (this.what == Aggregator.Op.COUNT){
		    		result.setField(1, new IntField(1));//count starts from 1
		    	}

	    		this.aTuples.put(key,result);
	    	}
		}
    }
    
    public TupleDesc getTupleDesc() {
    	Type[] typeAr;
    	TupleDesc td;

    	if (gbfield == Aggregator.NO_GROUPING) {
    		typeAr = new Type[1];
    		typeAr[0] = Type.STRING_TYPE;
    	} else {
    		typeAr = new Type[2];
    		typeAr[0] = this.gbfieldtype;
    		typeAr[1] = Type.INT_TYPE; //count,sum must be integer
    	}
    	td = new TupleDesc(typeAr);
    	return td;
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public OpIterator iterator() {
        // some code goes here
    	OpIterator iterator = new OpIterator() {	
//    		Iterator<Tuple> tupleIterator = aggregatedTuples.iterator();
    		TupleDesc tupleDesc = getTupleDesc();
			@Override
			public void rewind() throws DbException, TransactionAbortedException {
				// TODO Auto-generated method stub
				tIterator = aTuples.entrySet().iterator();
			}
			
			@Override
			public void open() throws DbException, TransactionAbortedException {
				// TODO Auto-generated method stub
				tIterator = aTuples.entrySet().iterator();
			}
			
			@Override
			public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
				// TODO Auto-generated method stub
//				if(tIterator.hasNext()){
					Map.Entry entry = (Map.Entry)tIterator.next();
					return (Tuple) entry.getValue();
//				}
//				else
//					throw new NoSuchElementException();
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
