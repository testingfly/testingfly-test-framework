package com.javaexcel.automation.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;


public class HttpClient {

	/***
	 * This method install the 
	 * required certificates (if any) then establish a connection to the provided URI
	 * Converts the required data to be posted on the server into byte array and then 
	 * using Http's post method submit the data into the server.
	 * 
	 *  After processed writes the response from server into the response file.
	 * @param url
	 * @param inputXmlFilePath
	 * @param responseXmlFilePath
	 * @param certificatePassword
	 * @param certificateFilePath
	 */
	@SuppressWarnings({ "unused", "deprecation" })
	public void postDataToServer(String url,String inputXmlFilePath,String responseXmlFilePath,String certificateFilePath,String certificatePassword,String rootJksfilePath,String rootJksPwd,Map<String,Object> map){
	
		File oFile= null;
		SSLContext sslcontext;
		PrintWriter pw = null;
		try {

			oFile = new File(responseXmlFilePath);

			if (oFile.exists()) {
				System.out.println("File Already exists with name in the path :" + responseXmlFilePath);
				oFile.delete();
				System.out.println("delete the file in the path" + responseXmlFilePath);
			}
			if (certificateFilePath != null && !certificateFilePath.isEmpty() && certificatePassword != null
					&& !certificatePassword.isEmpty()) {

				if (rootJksfilePath != null && !rootJksfilePath.isEmpty() && rootJksPwd != null && !rootJksPwd.isEmpty()) {
					sslcontext = installCert(certificateFilePath, certificatePassword, rootJksfilePath, rootJksPwd, oFile);
				} else {
					sslcontext = installCert(certificatePassword, certificateFilePath, oFile);
				}

				HttpClients.custom().setSslcontext(sslcontext).build();
			} else {
				sslcontext = SSLContext.getInstance("TLSv1");
				sslcontext.init(null, getTrustAllCert(), new java.security.SecureRandom());
			}
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
					new String[] { "TLSv1", "SSLv3" }, null, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);// BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			CloseableHttpClient httpclient = HttpClients.custom()
					.setSSLSocketFactory((LayeredConnectionSocketFactory) sslsf).build();

			byte[] data = read(new File(inputXmlFilePath));
			ByteArrayEntity bArrayEntity = new ByteArrayEntity(data);
			HttpPost httppost = new HttpPost(url);
			httppost.addHeader("Content-type", "text/xml; charset=UTF-8");
			
			//If header values are passed as part of request, get the header Name and header value from the request and added to the HttpPost method
			if(map !=null){
			for(Map.Entry<String, Object> entry : map.entrySet()) {
			
				httppost.addHeader(entry.getKey(), (String.valueOf(entry.getValue())));
			}
				
			}
		
			httppost.setEntity((HttpEntity) bArrayEntity);
			CloseableHttpResponse closeableHttpResponse = httpclient.execute((HttpUriRequest) httppost);

			HttpEntity entity = closeableHttpResponse.getEntity();
			
			// Get Response
			InputStream is = entity.getContent();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}

			System.out.println("Response From Server\n" + response.toString());

			if (entity != null) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(oFile));
				bw.write(response.toString());
				bw.close();
			}
			rd.close();

			closeableHttpResponse.close();

		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			try {
				pw = new PrintWriter(new FileWriter(oFile), true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace(pw);

		} 
	}
	
	

	/***
	 * Reads a file convert its contents to byte array.
	 * 
	 * @param file
	 * @return array of byte of file content
	 * @throws IOException
	 */
	
	public static byte[] read(File file) throws IOException {
		ByteArrayOutputStream ous= null;
		InputStream ios = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(file);
			int read = 0;
			while ((read = ios.read(buffer)) != -1) {
				ous.write(buffer, 0, read);
			}
		} finally {
			try {
				if (ous != null) {
					ous.close(); }
			} catch (IOException var6_8) {}
			try {
				if (ios != null) {
					ios.close();
				}
			} catch (IOException var6_9) {	}
		}
		return ous.toByteArray();
	}

	/***
	 * Resolve certificate issue to connect to SSL layer
	 * 
	 * @return
	 */
	public static TrustManager[] getTrustAllCert() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}

		} };
		return trustAllCerts;
	}
/***
 * Install the certificate using the provided Cert details
 * @param certificatePassword
 * @param certificateFilePath
 * @return
 */
@SuppressWarnings("deprecation")
public static SSLContext installCert(String certificatePassword,String certificateFilePath,File oFile){
	
	KeyStore trustStore;
	//File oFile= null;
	PrintWriter pw = null;
	FileInputStream instream= null ;
	SSLContext  sslcontext= null;
	
	try {
		trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		instream = new FileInputStream(new File(certificateFilePath));
		trustStore.load(instream, certificatePassword.toCharArray());
		
		sslcontext= SSLContexts.custom()
				.loadTrustMaterial(trustStore, (TrustStrategy) new TrustSelfSignedStrategy())
				.loadKeyMaterial(trustStore, certificatePassword.toCharArray()).build();
	} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException |IOException | CertificateException e) {
		 try {
			pw= new PrintWriter(new FileWriter(oFile),true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		e.printStackTrace(pw);

	} finally {
		try {
			instream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 return sslcontext;
	 } 


/***
 * This method install the pfx file and root jsk file . 
 * Install the cert to access the secured Urls.
 * 
 * 
 * @param certificatePassword
 * @param certificateFilePath
 * @param jksfilePath
 * @param jkspwd
 * @param oFile
 * @return
 */
private SSLContext installCert( String certificateFilePath,String certificatePassword, String jksfilePath,
		String jkspwd, File oFile) {

	String keyPassphrase =certificatePassword;
	String pass = jkspwd;
	SSLContext  sslcontext= null;
	try {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		File filo = new File(jksfilePath);
		keyStore.load(new FileInputStream(certificateFilePath), keyPassphrase.toCharArray());
		
		sslcontext= SSLContexts.custom()
		       .loadTrustMaterial(filo,pass.toCharArray()) 
		       .loadKeyMaterial(keyStore, keyPassphrase.toCharArray())
		        .build();
	} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException
			| CertificateException | IOException e) {
		e.printStackTrace();
	}
	
	
	 return sslcontext;
	 } 

}
