package ch.epfl.balelecbud.view.map;

import android.app.PendingIntent;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.location.LocationRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.balelecbud.BalelecbudApplication;
import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.model.Location;
import ch.epfl.balelecbud.model.MarkerType;
import ch.epfl.balelecbud.model.MyMap;
import ch.epfl.balelecbud.model.MyMarker;
import ch.epfl.balelecbud.model.PointOfInterest;
import ch.epfl.balelecbud.model.PointOfInterestType;
import ch.epfl.balelecbud.testUtils.TestAsyncUtils;
import ch.epfl.balelecbud.utility.authentication.MockAuthenticator;
import ch.epfl.balelecbud.utility.database.Database;
import ch.epfl.balelecbud.utility.database.MockDatabase;
import ch.epfl.balelecbud.utility.location.LocationClient;
import ch.epfl.balelecbud.utility.location.LocationUtils;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static ch.epfl.balelecbud.utility.database.MockDatabase.celine;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class MapViewWithPOITest {
    private final MockDatabase mockDB = MockDatabase.getInstance();
    private final MockAuthenticator mockAuth = MockAuthenticator.getInstance();
    private static final double SEARCH_RADIUS_IN_KM = 0.003;

    private final PointOfInterest atm = new PointOfInterest("credit suisse", PointOfInterestType.ATM,
            new Location(1, 2), SEARCH_RADIUS_IN_KM);

    @Before
    public void setup() {
        MockDatabase.getInstance().resetDatabase();
        BalelecbudApplication.setAppDatabase(mockDB);
        BalelecbudApplication.setAppAuthenticator(mockAuth);
        MapViewFragment.setMockCallback(mapboxMap -> { });
        LocationUtils.setLocationClient(new LocationClient() {
            @Override
            public void requestLocationUpdates(LocationRequest lr, PendingIntent intent) {

            }

            @Override
            public void removeLocationUpdates(PendingIntent intent) {

            }
        });
        mockAuth.setCurrentUser(celine);
        mockDB.resetDocument(Database.POINT_OF_INTEREST_PATH);
        mockDB.storeDocument(Database.POINT_OF_INTEREST_PATH, atm);
    }

    @After
    public void cleanUp() {
        mockDB.resetDocument(Database.POINT_OF_INTEREST_PATH);
    }

    private void assertNameAndLocation(MyMarker.Builder markerBuilder, TestAsyncUtils sync, PointOfInterest poi) {
        sync.assertEquals(poi.getName(), markerBuilder.getTitle());
        sync.assertEquals(poi.getLocation(), markerBuilder.getLocation());
        sync.assertEquals(MarkerType.getMarkerType(poi.getType()), MarkerType.ATM);
        sync.assertEquals(poi.getRadius(), SEARCH_RADIUS_IN_KM);
    }

    @Test
    public void onePoiShowsOneMarkerOnMap() throws Throwable {
        TestAsyncUtils sync = new TestAsyncUtils();

        MyMap mockMap = new MyMap() {
            @Override
            public MyMarker addMarker(MyMarker.Builder markerBuilder) {
                sync.assertNotNull(markerBuilder);
                assertNameAndLocation(markerBuilder, sync, atm);
                sync.call();
                return null;
            }

            @Override
            public void initialiseMap(boolean locationEnabled, Location defaultLocation, double zoom) {
                sync.assertThat(zoom, is((double) getApplicationContext().getResources().getInteger(R.integer.default_zoom)));
                sync.assertThat(defaultLocation, is(Location.DEFAULT_LOCATION));
                sync.call();
            }
        };

        FragmentScenario<MapViewFragment> scenario = FragmentScenario.launchInContainer(MapViewFragment.class);
        scenario.onFragment(fragment -> fragment.onMapReady(mockMap));

        sync.waitCall(2);
        sync.assertCalled(2);
        sync.assertNoFailedTests();
    }
}