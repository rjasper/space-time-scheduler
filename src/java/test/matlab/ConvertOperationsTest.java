package matlab;

import static org.junit.Assert.*;
import static matlab.ConvertOperations.*;
import static jts.geom.PolygonFixtures.*;
import static world.DynamicObstacleFixtures.*;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import jts.geom.factories.StaticJstFactories;
import matlab.data.DynamicObstacleData;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;

import org.junit.*;

import world.DynamicObstacle;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class ConvertOperationsTest {
	
	private static MatlabProxyFactory factory;
	
	private MatlabProxy proxy;
	
	private static MatlabProxyFactory getFactory() {
		return factory;
	}

	private static void setFactory(MatlabProxyFactory factory) {
		ConvertOperationsTest.factory = factory;
	}

	private MatlabProxy getProxy() {
		return proxy;
	}

	private void setProxy(MatlabProxy proxy) {
		this.proxy = proxy;
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
		.setUsePreviouslyControlledSession(true)
		.setMatlabStartingDirectory(new File("src/matlab"))
//		.setHidden(true) // messes with starting directory
		.build();
		
		setFactory(new MatlabProxyFactory(options));
	}
	
	@Before
	public void setUp() throws MatlabConnectionException {
		MatlabProxyFactory factory = getFactory();
		MatlabProxy proxy = factory.getProxy();
		
		setProxy(proxy);
	}
	
	@After
	public void tearDown() throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		m.feval("clear", "all");
		m.disconnect();
	}

	@Test
	public void testConvertPolygon() throws ParseException, MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		WKTReader wkt = StaticJstFactories.wktReader();
		Polygon p1 = (Polygon) wkt.read("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
		
		AccessOperations acc = new AccessOperations(m);
		
		double[] data1 = j2mPolygon(p1);
		acc.assignPolygon("polygon", data1);
		double[] data2 = acc.retrievePolygon("polygon");
		Polygon p2 = m2jPolygon(data2);
		
		assertTrue(p1.equalsTopo(p2));
	}
	
	@Test
	public void testConvertStaticObstacle() throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Collection<Polygon> obstacles1 = new Vector<>(2);
		obstacles1.add(triangle());
		obstacles1.add(square());
		
		AccessOperations acc = new AccessOperations(m);
		
		Object[] data1 = j2mStaticObstaclesData(obstacles1);
		acc.assignStaticObstacles("Os", data1);
		Object[] data2 = acc.retrieveStaticObstacles("Os");
		Collection<Polygon> obstacles2 = m2jStaticObstacles(data2);
		
		Iterator<Polygon> it1 = obstacles1.iterator();
		Iterator<Polygon> it2 = obstacles2.iterator();
		
		while (it1.hasNext() && it2.hasNext())
			assertTrue(it1.next().equalsTopo(it2.next()));
		
		assertTrue(!it1.hasNext() && !it2.hasNext());
	}
	
	@Test
	public void testConvertDynamicObsticals() throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Collection<DynamicObstacle> obstacles1 = new Vector<>(2);
		obstacles1.add(movingTriangle());
		obstacles1.add(movingSquare());
		
		AccessOperations acc = new AccessOperations(m);
		
		Collection<DynamicObstacleData> data1 = j2mDynamicObstacles(obstacles1);
		acc.assignDynamicObstacles("Om", data1);
		Collection<DynamicObstacleData> data2 = acc.retrieveDynamicObstacles("Om");
		Collection<DynamicObstacle> obstacles2 = m2jDynamicObstacles(data2);
		
		Iterator<DynamicObstacle> it1 = obstacles1.iterator();
		Iterator<DynamicObstacle> it2 = obstacles2.iterator();
		
		while (it1.hasNext() && it2.hasNext()) {
			DynamicObstacle o1 = it1.next();
			DynamicObstacle o2 = it2.next();
			
			assertTrue(o1.getPolygon().equalsTopo(o2.getPolygon()));
			assertTrue(o1.getPath().equalsExact(o2.getPath()));
		}

		assertTrue(!it1.hasNext() && !it2.hasNext());
	}

}
