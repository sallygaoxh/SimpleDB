package simpledb;

import java.io.IOException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {
	private TransactionId tid;
	private OpIterator child;
    private static final long serialVersionUID = 1L;
    int callsNext = 0;

    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     * 
     * @param t
     *            The transaction this delete runs in
     * @param child
     *            The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, OpIterator child) {
        // some code goes here
    	this.tid = t;
    	this.child = child;
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return new TupleDesc(new Type[] {Type.INT_TYPE});
    }

    public void open() throws DbException, TransactionAbortedException {
        // some code goes here
    	this.child.open();
    	super.open();
    }

    public void close() {
        // some code goes here
    	super.close();
    	this.child.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
    	this.child.rewind();
    }

    /**
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     * 
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
    	this.callsNext++;
    	int count = 0;
    	while (child.hasNext()) {
			Tuple tuple = child.next();
			try {
				Database.getBufferPool().deleteTuple(tid, tuple);
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
