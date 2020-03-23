package ch.epfl.balelecbud.models;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.core.Is.is;

public class PointOfInterestTest {

    private PointOfInterest p1 = new PointOfInterest( new GeoPoint(24, 42), "credit suisse", "atm", "BXnkTQdLsOXoGJmMSeCS");

    @Test
    public void testEmptyConstructor() {
        new PointOfInterest();
    }

    @Test
    public void testGetName() {
        Assert.assertThat(p1.getName(), is("credit suisse"));
    }

    @Test
    public void testGetType() {
        Assert.assertThat(p1.getType(), is("atm"));
    }

    @Test
    public void testGetPoiToken() { Assert.assertThat(p1.getPoiToken(), is("BXnkTQdLsOXoGJmMSeCS"));}

    @Test
    public void testGetLocation() {
        Assert.assertThat(p1.getLocation(), is(new GeoPoint(24, 42)));
    }


}
