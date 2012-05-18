package test.repository;

import static aQute.lib.deployer.repository.api.Decision.*;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import aQute.lib.deployer.repository.api.CheckResult;
import aQute.lib.deployer.repository.providers.ObrContentProvider;

public class TestObrRecognition extends TestCase {
	
	public void testRejectNamespace() throws Exception {
		String testdata = "<?xml version='1.0' encoding='utf-8'?>" +
				"<repository increment='0' name='index1' xmlns='http://www.osgi.org/xmlns/repository/v1.0.0'>" +
				"<resource>";
		ByteArrayInputStream stream = new ByteArrayInputStream(testdata.getBytes());
		assertEquals(reject, new ObrContentProvider().checkStream("xxx", stream).getDecision());
	}
	
	public void testAcceptNamespace() throws Exception {
		String testdata = "<?xml version='1.0'?>" +
				"<repository xmlns='http://www.osgi.org/xmlns/obr/v1.0.0'>";
		ByteArrayInputStream stream = new ByteArrayInputStream(testdata.getBytes());
		assertEquals(accept, new ObrContentProvider().checkStream("xxx", stream).getDecision());
	}
	
	public void testRejectRootElementName() throws Exception {
		String testdata = "<?xml version='1.0' encoding='utf-8'?>" +
				"<repo name='index1'/>";
		ByteArrayInputStream stream = new ByteArrayInputStream(testdata.getBytes());
		assertEquals(reject, new ObrContentProvider().checkStream("xxx", stream).getDecision());
	}
	
	public void testUndecidable() throws Exception {
		String testdata = "<?xml version='1.0' encoding='utf-8'?>" +
				"<repository name='index1'/>";
		ByteArrayInputStream stream = new ByteArrayInputStream(testdata.getBytes());
		assertEquals(undecided, new ObrContentProvider().checkStream("xxx", stream).getDecision());
	}
	
	public void testUnparseable() throws Exception {
		String testdata = "<?xml version='1.0' encoding='utf-8'?>" +
				"<repository name='index1'>";
		ByteArrayInputStream stream = new ByteArrayInputStream(testdata.getBytes());
		CheckResult result = new ObrContentProvider().checkStream("xxx", stream);
		assertEquals(reject, result.getDecision());
		assertTrue(result.getException() != null && result.getException() instanceof XMLStreamException);
	}
	
	
	public void testAcceptStylesheet() throws Exception {
		String testdata = "<?xml version='1.0' encoding='utf-8'?>" +
				"<?xml-stylesheet type='text/xsl' href='http://www2.osgi.org/www/obr2html.xsl'?>" +
				"<repository...";
		ByteArrayInputStream stream = new ByteArrayInputStream(testdata.getBytes());
		CheckResult result = new ObrContentProvider().checkStream("xxx", stream);
		assertEquals(accept, result.getDecision());
	}
	
	public void testRejectOnRepositoryChildElementName() throws Exception {
		String testdata;
		ByteArrayInputStream stream;
		CheckResult result;
		
		// Definitely wrong
		testdata = "<repository><XXX/><repo...";
		stream = new ByteArrayInputStream(testdata.getBytes());
		result = new ObrContentProvider().checkStream("xxx", stream);
		assertEquals(reject, result.getDecision());
		assertNull(result.getException());

		// Okay but not enough to decide for sure
		testdata = "<repository><resource/></repository>";
		stream = new ByteArrayInputStream(testdata.getBytes());
		result = new ObrContentProvider().checkStream("xxx", stream);
		assertEquals(undecided, result.getDecision());
		
		// Okay but not enough to decide for sure
		testdata = "<repository><referral/></repository>";
		stream = new ByteArrayInputStream(testdata.getBytes());
		result = new ObrContentProvider().checkStream("xxx", stream);
		assertEquals(undecided, result.getDecision());
	}
	
	public void testRejectOnCapabilityChildElementName() throws Exception {
		String testdata;
		ByteArrayInputStream stream;
		CheckResult result;
		
		// Definitely wrong
		testdata = "<repository><resource><capability><XXX/></capability></resource><repo...";
		stream = new ByteArrayInputStream(testdata.getBytes());
		result = new ObrContentProvider().checkStream("xxx", stream);
		assertEquals(reject, result.getDecision());
		assertNull(result.getException());

		// Definitely right
		testdata = "<repository><resource><capability><p/></capability></resource><repo...";
		stream = new ByteArrayInputStream(testdata.getBytes());
		result = new ObrContentProvider().checkStream("xxx", stream);
		assertEquals(accept, result.getDecision());

		// Arbitrary elements under resource are allowed
		testdata = "<repository><resource><XXX/><YYY/><capability><p/></capability><ZZZ/></resource><repo...";
		stream = new ByteArrayInputStream(testdata.getBytes());
		result = new ObrContentProvider().checkStream("xxx", stream);
		assertEquals(accept, result.getDecision());
	}
	
	public void testAcceptExtensionElementOtherNamespace() throws Exception {
		String testdata;
		ByteArrayInputStream stream;
		CheckResult result;
		
		// Arbitrary elements under resource are allowed
		testdata = "<?xml version='1.0'?>" +
				"<repository><resource><foo:XXX xmlns:foo='http://org.example/ns'/><YYY/><capability><p/></capability><ZZZ/></resource><repo...";
		stream = new ByteArrayInputStream(testdata.getBytes());
		result = new ObrContentProvider().checkStream("xxx", stream);
		assertEquals(accept, result.getDecision());
	}
}