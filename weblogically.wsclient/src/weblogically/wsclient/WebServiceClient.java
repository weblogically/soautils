package weblogically.wsclient;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import oracle.webservices.ClientConstants;
import oracle.webservices.ImplType;
import oracle.webservices.WsMetaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>You must specify one of either endpoint or wsdl. If endpoint is provided
 * then WSDL will default to endpoint?WSDL. If the 
 * <pre>
 * // Fetch the WSDL (including any imports recursive)
 * WebServiceClient getwsdl -wsdl=<wsdl_url> -dest=<filename>
 * 
 * // Generate some sample payload
 * WebServiceClient gensample -wsdl=<wsdl_url> -service=<service_name> -port=<port_name> -operation=<operation_name>
 * 
 * // Invoke a service call
 * WebServiceClient [-wsdl=<wsdlURL>] [-policy=<policy_name>]
 * 
 * </pre>
 * @author weblogically.blogspot.com
 *
 */

public class WebServiceClient {
	
	String endpointAddress = "http://";
	
	public static void call() throws IOException {
		
		String endpointAddress = "http://localhost:7080/ExamplesWebServices/HelloService";
		
		// TODO: We need to get this by parsing the WSDL
		QName serviceName = new QName("http://www.oracle.com/uk/ocs/examples/services/hello/", "HelloService");
		
		// TODO: We need to get this from the parsed WSDL
		QName portName = new QName("http://www.oracle.com/uk/ocs/examples/services/hello/", "HelloPort");
		
		Service service = Service.create(serviceName);
		
		// TODO: We need to read the binding from the WSDL
		service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
		
		Dispatch <Source> sourceDispatch = service.createDispatch(portName, Source.class, Service.Mode.MESSAGE);
		
		FileInputStream fin = new FileInputStream("request.xml");
		InputStreamReader isr = new InputStreamReader(fin);
		
		// TODO: invoke is for 2way sync. Teh actual type needs to come from the WSDL
		Source result = sourceDispatch.invoke(new StreamSource(isr));
		
		String rstr = sourceToXMLString(result);
		System.out.println("result==\n" + rstr);
		
	}
	
	private static String sourceToXMLString(Source result) {
		String xmlResult = null;
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			OutputStream out = new ByteArrayOutputStream();
			StreamResult streamResult = new StreamResult();
			streamResult.setOutputStream(out);
			transformer.transform(result, streamResult);
			xmlResult = streamResult.getOutputStream().toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return xmlResult;
	}
	
	public void processArgs(String[] args) {
		if (args.length != 2) {
			
		}
	}
	
	public static void printPath(Class clazz) {
		System.out.println("class: " + clazz.getName());
		String resourceName = clazz.getName().replace(".", "/") + ".class";
		System.out.println("res name:" + resourceName);
		System.out.println("from:  " + clazz.getClassLoader().getResource(resourceName));
	}
	
	public static void jrfCall() throws IOException, ParserConfigurationException, SAXException {
		
		printPath(oracle.j2ee.ws.common.jaxws.SoapMessageConverter.class);
		
		boolean messageMode = true;
		
		String endpointAddress = "http://localhost:7080/ExamplesWebServices/HelloService";
		
		// TODO: We need to get this by parsing the WSDL
		QName serviceQName = new QName("http://www.oracle.com/uk/ocs/examples/services/hello/", "HelloService");
		
		// TODO: We need to get this from the parsed WSDL
		QName portQName = new QName("http://www.oracle.com/uk/ocs/examples/services/hello/", "HelloPort");
		
		String policyAttachmentXmlDocString = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			    "<oracle-webservice-clients>\n" +
			    "    <webservice-client>\n" +
			    "        <port-info>\n" +
			    "            <policy-references>\n" +
		       // "                <policy-reference uri=\"oracle/wss11_saml_token_client_policy\" category=\"security\"/>" +
		        "            </policy-references>" +
		        "        </port-info>" +
		        "    </webservice-client>" +
		        "</oracle-webservice-clients>";
		
		ByteArrayInputStream in = new ByteArrayInputStream(policyAttachmentXmlDocString.getBytes());

		//Element clientConfigElem = this.fileToElement(clientPolicyStream);
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(in);
	    Element docElem = doc.getDocumentElement();
	    
	    URL serviceWsdlUrl = new URL(endpointAddress + "?WSDL");
	    

	    Service service = WsMetaFactory.newInstance(ImplType.JRF).createClientFactory().create(serviceWsdlUrl, serviceQName);
	    service.addPort(portQName, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
	    
	    Dispatch <Source> dispatch;
	    if (messageMode)
	    	dispatch = service.createDispatch(portQName, Source.class, Service.Mode.MESSAGE);
	    else
	    	dispatch = service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD);

		Map<String,Object> requestContext = ((BindingProvider) dispatch).getRequestContext();
		requestContext.put(ClientConstants.CLIENT_CONFIG , docElem);

		FileInputStream fin;
		if (messageMode)
			fin = new FileInputStream("request.xml");
		else
			fin = new FileInputStream("request_payload.xml");
		InputStreamReader isr = new InputStreamReader(fin);
		//Document requestDoc = builder.parse(fin);
		
		
		// TODO: invoke is for 2way sync. Teh actual type needs to come from the WSDL
		Source result = dispatch.invoke(new StreamSource(isr));
		//Source result = sourceDispatch.invoke();
		
	
		
		String rstr = sourceToXMLString(result);
		System.out.println("result==\n" + rstr);

	}
	
	public static void wsdeel(String resourceUrl) throws ParserConfigurationException, SAXException, IOException, XPathException, TransformerException {
		
		System.out.println("Loading the WSDL from URL: " + resourceUrl);
		
		URL url = new URL(resourceUrl);
		
		System.out.println("Opening the InputStream from the URL...");
		
		InputStream in = url.openStream();
		
		System.out.println("Loading the WSDL document from the InputStream...");
		
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder;
	    Document doc = null;
	    XPathExpression expr = null;
	    builder = factory.newDocumentBuilder();
	    doc = builder.parse(in);
	    
	    System.out.println("Loaded the WSDL document");
	    
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    StringWriter writer = new StringWriter();
	    transformer.transform(new DOMSource(doc), new StreamResult(writer));
	    String output = writer.getBuffer().toString();
	    System.out.println(output);
	    
	    // First question. WSDL version
	    
	    // create an XPathFactory
	    XPathFactory xFactory = XPathFactory.newInstance();

	    // create an XPath object
	    XPath xpath = xFactory.newXPath();
	    
	    // setup a namespace context
	    SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
	    namespaceContext.setNamespacePrefix("wsdl", "http://schemas.xmlsoap.org/wsdl/");
	    xpath.setNamespaceContext(namespaceContext);
	    
	    // create the XPath to
	    XPathExpression xpathExpr = xpath.compile("/wsdl:definitions/wsdl:service[@name='HelloService']");
	    
	    // run the query and get a Node
	    Node serviceNode = (Node) xpathExpr.evaluate(doc, XPathConstants.NODE);   
	    Element serviceElement = (Element) serviceNode;
	    System.out.println(serviceElement);
	    
	    
	    XPathExpression portXPathExpr = xpath.compile("wsdl:port[@name='HelloPort']");
	    Node portNode = (Node) portXPathExpr.evaluate(serviceNode, XPathConstants.NODE);
	    Element portElement = (Element) portNode;
	    System.out.println("portNode=" + portNode);
	    System.out.println(portElement.getAttribute("binding"));
	    
	    
	}


	public static void main(String[] args) throws Exception {
		wsdeel("http://localhost:7080/ExamplesWebServices/HelloService?WSDL");
		//call(); // works with just Java classes but breaks with JRF
		jrfCall();
	}

}
