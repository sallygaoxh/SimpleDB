package simpledb;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {
	
	private ArrayList<TDItem> TDItemList = new ArrayList<TDItem>();
//	private Type[] typeAr;
//	private String[] fieldAr; 
    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // some code goes here
    	return this.TDItemList.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // some code goes here
    		for (int i = 0;i<typeAr.length;i++){
    			this.TDItemList.add(new TDItem(typeAr[i],fieldAr[i]));
    		}
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here
	    	for (int i = 0;i<typeAr.length;i++)
	    		this.TDItemList.add(new TDItem(typeAr[i], ""));
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
//    	if(typeAr==null)
//    		return 0;
//    	else
//    		return typeAr.length;
    		if(this.TDItemList==null)
    			return 0;
    		else
    			return this.TDItemList.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
		try {
    		return this.TDItemList.get(i).fieldName;
    	}catch (Exception e) {
		throw new NoSuchElementException("The index is out of bound");
		}
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
    	try {
    		return this.TDItemList.get(i).fieldType;
    	}catch (Exception e) {
			throw new NoSuchElementException("The index is out of bound");
		}
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
    	int result = -1;
    	if (this.TDItemList==null)
    		throw new NoSuchElementException("no fields are named, so you can't find it");
    	else {
//	        	for(int i = 0;i<this.fieldAr.length;i++) {
    		for(int i = 0;i<this.TDItemList.size();i++) {
    			if (this.TDItemList.get(i).fieldName.equals(name)) {
    				result = i;
    				break;
    			}
        	}
        	if (result>=0)
        		return result;
        	else 
        		throw new NoSuchElementException(name+" is not a valid field name");
    	}
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
	    	int size = 0;
	    	for (int i = 0;i<this.TDItemList.size();i++){
//	    		size += typeAr[i].getLen();
	    		size += this.TDItemList.get(i).fieldType.getLen();
	    	}
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
    	Type[] newTypeAr = new Type[td1.numFields()+td2.numFields()];
    	String[] newFieldAr = new String[td1.numFields()+td2.numFields()];
    	for (int i = 0; i<td1.numFields(); i++)  {
    		newFieldAr[i] = td1.getFieldName(i);
    		newTypeAr[i] = td1.getFieldType(i);
    	}
    	for (int j = 0; j<td2.numFields();j++) {
    		newFieldAr[j+td1.numFields()] = td2.getFieldName(j);
    		newTypeAr[j+td1.numFields()] = td2.getFieldType(j);
    	}
    	TupleDesc newTd = new TupleDesc(newTypeAr,newFieldAr);
        return newTd;
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */

    public boolean equals(Object o) {
        if (o instanceof TupleDesc) {
        	if (((TupleDesc) o).numFields() == this.numFields()){
        		for (int i = 0; i<this.numFields();i++) {
        			if (!(this.getFieldType(i) == ((TupleDesc) o).getFieldType(i))){
        				System.out.println("field "+i+" mismatch");
        				return false;
        			}
        			else
        				continue;
        		}
        		return true;
        	}
        	else {
        		System.out.println("Number of fields mismatch");
        		return false;      		
        	}
        }
        else {
        	System.out.println("Not a TupleDescriptor");
        	return false;
        }
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        //throw new UnsupportedOperationException("unimplemented");
    	int result = 0;
    	for (int i = 0;i <this.TDItemList.size();i++){
    		result = result + (this.getFieldType(i) != null ? this.getFieldType(i).hashCode() : 0);
    	}
        return result;
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        // some code goes here
	    	String result = "";
	    	if(this.TDItemList!=null){
	    		for (int i = 0;i<this.numFields();i++) {
	    			result += this.getFieldType(i)+"("+this.getFieldName(i)+") "; 
	        	}
	    	}
        return result;
    }
}
