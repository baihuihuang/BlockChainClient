/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project3task3client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Baihui
 */
public class Project3Task3Client {

    private static BigInteger e = new BigInteger("65537");
    private static BigInteger d = new BigInteger("339177647280468990599683753475404338964037287357290649639740920420195763493261892674937712727426153831055473238029100340967145378283022484846784794546119352371446685199413453480215164979267671668216248690393620864946715883011485526549108913");
    private static BigInteger n = new BigInteger("2688520255179015026237478731436571621031218154515572968727588377065598663770912513333018006654248650656250913110874836607777966867106290192618336660849980956399732967369976281500270286450313199586861977623503348237855579434471251977653662553");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int choice = 0;
        do {
            System.out.print("Block Chain Menu\n");
            System.out.print("1. Add a transaction to the blockchain\n");
            System.out.print("2. Verify the blockchain\n");
            System.out.print("3. View the blockchain\n");
            System.out.print("4. Exit\n");

            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1: // add block 
                    System.out.println("Enter difficulty");
                    int difficulty = Integer.parseInt(input.nextLine());
                    System.out.println("Enter Transaction");
                    String transaction = input.nextLine();

                    MessageDigest md = null;

                    try {
                        // hash the input using SHA-265
                        md = MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException ex) {
                        ex.getStackTrace();
                    }
                    // Hash the transaction
                    byte[] hashValue = md.digest(transaction.getBytes());

                    // put one significant byte in the front 
                    byte[] extraByte = new byte[hashValue.length + 1];
                    extraByte[0] = 0;
                    for (int i = 0; i < hashValue.length; i++) {
                        extraByte[i + 1] = hashValue[i];
                    }
                    BigInteger signature = new BigInteger(DatatypeConverter.printHexBinary(extraByte), 16);
                    
                    // Encrypt the data using the private key
                    BigInteger encrypted = signature.modPow(d, n);
                    
                    // Concat transaction and data
                    String data = transaction + "#" + encrypted.toString();

                    // Call doPost
                    doPost(difficulty,data);

                    break;
                case 2: // verify the block chain 
                    long start = System.currentTimeMillis();
                    System.out.println("Verifying");
                    doGet("verify");

                    long end = System.currentTimeMillis();
                    System.out.println("Total execution time to verify the chain is " + (end - start) + " milliseconds");

                    break;
                case 3: // Print out block chain 
                    System.out.println("View the BlockChain");
                    doGet("view");
                    break;
                case 4: // exit
                    System.exit(0);
                    break;

            }

         
        } while (choice != 4);

    }

    public static int doPost(int difficulty, String data) {
        int status = 0;

        try {
            // Make call to a particular URL
            URL url = new URL("http://localhost:8090/Project3Task3Server/BlockChainServlet/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set request method to POST and send name value pair
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            // write to POST data area
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data + "," + difficulty);
           
            out.close();

            // read result from server
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result = br.readLine();

            System.out.println(result);

            // get HTTP response code sent by server
            status = conn.getResponseCode();

            //close the connection
            conn.disconnect();
        } // handle exceptions
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return HTTP status
        return status;
    }

    public static int doGet(String name) {
        // Make an HTTP GET passing the name on the URL line
        String response = "";
        HttpURLConnection conn;
        int status = 0;
        try {
            // pass the name on the URL line
            URL url = new URL("http://localhost:8090/Project3Task3Server/BlockChainServlet" + "//" + name);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // tell the server what format we want back
            conn.setRequestProperty("Accept", "text/plain");

            
            // wait for response
            status = conn.getResponseCode();

            // If things went poorly, don't try to read any response, just return.
            if (status != 200) {
                // not using msg
                String msg = conn.getResponseMessage();
                return conn.getResponseCode();
            }
            String output = "";

            // things went well so let's read the result
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            while ((output = br.readLine()) != null) {
                response += output + "\n";
            }

            // display results
            System.out.println(response);

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return HTTP status to caller
        return status;
    }

}
