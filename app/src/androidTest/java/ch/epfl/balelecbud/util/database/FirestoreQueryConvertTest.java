package ch.epfl.balelecbud.util.database;

import com.google.common.collect.Lists;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.junit.Test;

import java.util.LinkedList;

import ch.epfl.balelecbud.util.database.FirestoreQueryConverter;
import ch.epfl.balelecbud.util.database.MyQuery;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class FirestoreQueryConvertTest {

    private String collectionName = "myCollectionName";
    private String leftOperandName = "myLeftOperandName";
    private Integer rightOperandValue = 12;

    private MyQuery.WhereClause clauseLessThan = new MyQuery.WhereClause(leftOperandName, MyQuery.WhereClause.Operator.LESS_THAN, rightOperandValue);
    private MyQuery.WhereClause clauseLessEquals = new MyQuery.WhereClause(leftOperandName, MyQuery.WhereClause.Operator.LESS_EQUAL, rightOperandValue);
    private MyQuery.WhereClause clauseGreaterThan = new MyQuery.WhereClause(leftOperandName, MyQuery.WhereClause.Operator.GREATER_THAN, rightOperandValue);
    private MyQuery.WhereClause clauseGreaterEquals = new MyQuery.WhereClause(leftOperandName, MyQuery.WhereClause.Operator.GREATER_EQUAL, rightOperandValue);
    private MyQuery.WhereClause clauseEquals = new MyQuery.WhereClause(leftOperandName, MyQuery.WhereClause.Operator.EQUAL, rightOperandValue);

    @Test
    public void whereLessThanConversionReturnsCorrectQuery(){
        MyQuery myQuery = new MyQuery(collectionName, Lists.newArrayList(clauseLessThan));
        Query convertedQuery = FirestoreQueryConverter.convert(myQuery);
        Query expectedQuery = FirebaseFirestore.getInstance().collection(collectionName).whereLessThan(leftOperandName, rightOperandValue);
        assertEquals(expectedQuery, convertedQuery);
    }

    @Test
    public void whereLessEqualsConversionReturnsCorrectQuery(){
        MyQuery myQuery = new MyQuery(collectionName, Lists.newArrayList(clauseLessEquals));
        Query convertedQuery = FirestoreQueryConverter.convert(myQuery);
        Query expectedQuery = FirebaseFirestore.getInstance().collection(collectionName).whereLessThanOrEqualTo(leftOperandName, rightOperandValue);
        assertEquals(expectedQuery, convertedQuery);
    }

    @Test
    public void whereGreaterThanConversionReturnsCorrectQuery(){
        MyQuery myQuery = new MyQuery(collectionName, Lists.newArrayList(clauseGreaterThan));
        Query convertedQuery = FirestoreQueryConverter.convert(myQuery);
        Query expectedQuery = FirebaseFirestore.getInstance().collection(collectionName).whereGreaterThan(leftOperandName, rightOperandValue);
        assertEquals(expectedQuery, convertedQuery);
    }

    @Test
    public void whereGreaterEqualsConversionReturnsCorrectQuery(){
        MyQuery myQuery = new MyQuery(collectionName, Lists.newArrayList(clauseGreaterEquals));
        Query convertedQuery = FirestoreQueryConverter.convert(myQuery);
        Query expectedQuery = FirebaseFirestore.getInstance().collection(collectionName).whereGreaterThanOrEqualTo(leftOperandName, rightOperandValue);
        assertEquals(expectedQuery, convertedQuery);
    }

    @Test
    public void whereEqualsConversionReturnsCorrectQuery(){
        MyQuery myQuery = new MyQuery(collectionName, Lists.newArrayList(clauseEquals));
        Query convertedQuery = FirestoreQueryConverter.convert(myQuery);
        Query expectedQuery = FirebaseFirestore.getInstance().collection(collectionName).whereEqualTo(leftOperandName, rightOperandValue);
        assertEquals(expectedQuery, convertedQuery);
    }
}