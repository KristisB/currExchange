package study.currencyrates;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.*;
import java.io.IOException;
import java.util.HashMap;

public class LbConnect {
    String soapEndpointUrl = "https://www.lb.lt/webservices/FxRates/FxRates.asmx";
    String soapAction = "http://www.lb.lt/WebServices/FxRates/getFxRates";
    String date ="";

    public HashMap<String, Double> getFxRates(String date) {
        this.date=date;
        HashMap<String, Double> fxRates = new HashMap<>();
        fxRates = callSoapWebService(soapEndpointUrl, soapAction);
        return fxRates;

    }

    private HashMap<String, Double> callSoapWebService(String soapEndpointUrl, String soapAction) {
        HashMap<String, Double> fxRates = new HashMap<>();
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(soapAction), soapEndpointUrl);

            //parsing response
            Document doc = soapResponse.getSOAPBody().extractContentAsDocument();
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("FxRate");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element aElement = (Element) node;
                NodeList innerNodeList = aElement.getElementsByTagName("CcyAmt");
                for (int j = 0; j < innerNodeList.getLength(); j++) {
                    Node innerNode = innerNodeList.item(j);

                    if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) innerNode;
                        String curCode=eElement.getElementsByTagName("Ccy").item(0).getTextContent();
                        Double rate=Double.valueOf(eElement.getElementsByTagName("Amt").item(0).getTextContent());
                        fxRates.put(curCode,rate);
                        
                        System.out.println("Ccy: " + eElement.getElementsByTagName("Ccy").item(0).getTextContent());
                        System.out.println("Amt: " + eElement.getElementsByTagName("Amt").item(0).getTextContent());
                    }
                }
            }
            

        } catch (
                Exception e) {
            System.err.println("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
        return fxRates;
    }

    private SOAPMessage createSOAPRequest(String soapAction) throws SOAPException, IOException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        createSoapEnvelope(soapMessage);
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", soapAction);

        soapMessage.saveChanges();
        /* Print the request message, just for debugging purposes */
        System.out.println("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println("\n");

        return soapMessage;
    }

    private void createSoapEnvelope(SOAPMessage soapMessage) throws SOAPException {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String fxRates = "getFxRates";
        String fxRatesURI = "http://www.lb.lt/WebServices/FxRates";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(fxRates, fxRatesURI);
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");

            /*
            Creating SOAP message as per LB page instructions
            POST /webservices/fxrates/fxrates.asmx HTTP/1.1
            Host:
            Content-Type: text/xml; charset=utf-8
            Content-Length: length
            SOAPAction: "http://www.lb.lt/WebServices/FxRates/getFxRates"

            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getFxRates xmlns="http://www.lb.lt/WebServices/FxRates">
                  <tp>string</tp>
                  <dt>string</dt>
                </getFxRates>
              </soap:Body>
            </soap:Envelope>

             */

        // SOAP body
        SOAPBody soapBody = envelope.getBody();

        SOAPElement elementGetFxRates=soapBody.addChildElement(fxRates,"",fxRatesURI);
        SOAPElement elementTp = elementGetFxRates.addChildElement("tp");
        SOAPElement elementDt = elementGetFxRates.addChildElement("dt");
        elementTp.addTextNode("LT");
        elementDt.addTextNode(date);
    }
}