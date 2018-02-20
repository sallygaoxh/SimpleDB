package simpledb;

import java.io.*;
import java.util.*;

import com.sun.org.apache.bcel.internal.generic.NEW;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {
    private File f;
	private TupleDesc td;
    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
    	this.f = f;
    	this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
    	return this.f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
    	int id;
    	id = f.getAbsoluteFile().hashCode();
    	return id;
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
    	// Page Id starts from 0    
    	
    	HeapPageId heapPageId = (HeapPageId)pid;
    	
    	int pageSize = BufferPool.getPageSize();
    	Page result = null;
    	//if the page does not exist 
    	if (((pid.getPageNumber()+1)*pageSize)>f.length()||pid.getPageNumber()<0)
    		throw new IllegalArgumentException();
    	else {
    		byte[] data = new byte[pageSize];
    		try {
    			RandomAccessFile raf = new RandomAccessFile(f, "r");
    			raf.seek((long)(pid.getPageNumber()*pageSize));
    			raf.readFully(data);   		
				result = new HeapPage(heapPageId, data);
				raf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return result;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
    	HeapPageId pid = (HeapPageId)page.getId();	
    	int pageSize = BufferPool.getPageSize();

		byte[] data = new byte[pageSize];
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			long offset = (pid.getPageNumber()*pageSize);
			data = page.getPageData();
			raf.seek(offset);
			raf.write(data, 0, pageSize); 		
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
		//calculated by compare the length of the file and each page
    	int num = 0;
    	num = (int) Math.floor(f.length()/BufferPool.getPageSize());
        return num;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
    	PageId pid = null;
    	ArrayList<Page> result = new ArrayList<Page>();
    	for (int pgNo = 0; pgNo<this.numPages();pgNo++){
    		pid = new HeapPageId(this.getId(), pgNo);
    		HeapPage page = (HeapPage)Database.getBufferPool().getPage(tid,pid,Permissions.READ_ONLY);
    		int tupleNo = -1;
    		for (int i = 0;i<page.numSlots;i++){
    			if (!page.isSlotUsed(i)){
    				tupleNo = i;
    				break;
    			}
    		}
    		if(tupleNo>=0){
    			page.insertTuple(t);
    			result.add(page);
    			return result;
    		}
    	}
    	//if all the pages are full
    	byte[] data = new byte[BufferPool.getPageSize()];
    	for (int i = 0;i<BufferPool.getPageSize();i++){
    		data[i] = (byte) 0x0;
    	}
    	HeapPage page = new HeapPage(new HeapPageId(this.getId(), this.numPages()), data);
        page.insertTuple(t);
        this.writePage(page);
        result.add(page);
		return result;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
    	PageId pid = t.getRecordId().getPageId();
    	ArrayList<Page> result = new ArrayList<Page>();

		HeapPage page = (HeapPage)Database.getBufferPool().getPage(tid,pid,Permissions.READ_ONLY);

		page.deleteTuple(t);
		result.add(page);
	
		return result;
    }

    // see DbFile.java for javadocs
    /**
     * Returns an iterator over all the tuples stored in this DbFile. The
     * iterator must use {@link BufferPool#getPage}, rather than
     * {@link #readPage} to iterate through the pages.
     *
     * @return an iterator over all the tuples stored in this DbFile.
     */
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here

    	int numPages = this.numPages();
    	int tableId = this.getId();

        return new HeapFileIterator(numPages,tableId,tid);
    }
    private class HeapFileIterator implements DbFileIterator{
    	int pgNo = 0;
    	HeapPageId pid = null;
    	HeapPage nowPage = null;
    	Iterator<Tuple> iterator = null;
    	int numPages,tableId;
    	TransactionId tid;
    	
    	private HeapFileIterator(int numPages, int tableId, TransactionId tid) {
			this.numPages = numPages;
			this.tableId = tableId;
			this.tid = tid;
		}
    	
		@Override
		public void rewind() throws DbException, TransactionAbortedException {
			// TODO Auto-generated method stub
			close();
            open();
		}
		
		@Override
		public void open() throws DbException, TransactionAbortedException {
			// TODO Auto-generated method stub
			pgNo = 0;
			pid = new HeapPageId(tableId, pgNo);
	    	nowPage = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_ONLY);
	    	iterator = nowPage.iterator();
		}
		
		@Override
		public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
			// TODO Auto-generated method stub
	    	if(iterator!=null){
	    		if(iterator.hasNext())
	    			return iterator.next();
	    		else if(pgNo<numPages-1){
	    			pgNo ++;
	    			
					pid = new HeapPageId(tableId, pgNo);
					nowPage = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_ONLY);
					if(nowPage!=null){
						iterator = nowPage.iterator();
			    		if(iterator!=null)
			    			return iterator.next();
			    		else
			    			throw new NoSuchElementException("No tuples can be returned");
					}
					else
						throw new NoSuchElementException("No tuples can be returned");
	    		}
	    		else {
	    			throw new NoSuchElementException("No tuples can be returned");
				}
	    	}
	    	else {
	    		throw new NoSuchElementException("No tuples can be returned");
			}
		}
		
		@Override
		public boolean hasNext() throws DbException, TransactionAbortedException {
			// TODO Auto-generated method stub
			if(iterator!=null){
				if(iterator.hasNext())
					return true;
				else if(pgNo<numPages-1){
//					pgNo ++;
					pid = new HeapPageId(tableId, pgNo+1);
					nowPage = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_ONLY);
			    	if(nowPage!=null){
			    		if(nowPage.iterator()!=null)
			    			return nowPage.iterator().hasNext();
			    		else
			    			return false;
			    	}
			    	else
			    		return false;
				}
				else 
					return false;
			}
			else 
				return false;
		}
		
		@Override
		public void close() {
			// TODO Auto-generated method stub
			iterator = null;
		}
    }
}

