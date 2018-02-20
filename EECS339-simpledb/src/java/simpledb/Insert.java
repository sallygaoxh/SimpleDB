package simpledb;

import java.io.IOException;

/**
 * Inserts tuples read from the child operator into the tableId specified in the
 * constructor
 */
public class Insert extends Operator {
    private OpIterator child;
    private TransactionId t;
    private int tableId;
    private static final long serialVersionUID = 1L;
    int callsNext = 0;

    /**
     * Constructor.
     *
     * @param t
     *            The transaction running the insert.
     * @param child
     *            The child operator from which to read tuples to be inserted.
     * @param tableId
     *            The table in which to insert tuples.
     * @throws DbException
     *             if TupleDesc of child differs from table into which we are to
     *             insert.
     */
    public Insert(TransactionId t, OpIterator child, int tableId)
            throws DbException {
        // some code goes here
    	this.tableId = tableId;
    	this.t = t;
    	this.child =child;
    	if (!child.getTupleDesc().equals(Database.getCatalog().getTupleDesc(tableId))){
    		throw new DbException("TupleDesc of child differs from table into which we are to insert");
    	}
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return new TupleDesc(new Type[] {Type.INT_TYPE});
    }

    public void open() throws DbException, TransactionAbortedException {
        // some code goes here
    	child.open();
    	super.open();
    }

    public void close() {
        // some code goes here
    	child.close();
    	super.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
    	child.rewind();
    }

    /**
     * Inserts tuples read from child into the tableId specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     *
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
    	this.callsNext++;
    	int count = 0;
    	while (child.hasNext()) {
			Tuple tuple = child.next();
			try {
				Database.getBufferPool().insertTuple(t, tableId, tuple);
			} catch (IOException e) {
				e.printStackTrace();
			}
			count++;
		}
    	
    	Tuple result = new Tuple(this.getTupleDesc());
    	result.setField(0, new IntField(count));
    	if(callsNext==1)
    		return result;
    	else
    		return null;
    }

    @Override
    public OpIterator[] getChildren() {
        // some code goes here
        return new OpIterator[] {this.child};
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
