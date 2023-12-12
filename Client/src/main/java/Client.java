import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.methods.multipart.Part;


import java.awt.*;
import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Client extends Thread {

    private String url;
    private String path;
    private String reviewPath;
    private int iterations;
    private HttpClient client;
    private PostMethod postMethod;
    private PostMethod postLike;
    private PostMethod postDislike;
    private final boolean isRecordingThread;
    private final LinkedBlockingQueue<CallInfo> latencyQueue;



    public Client(String url, String path, int iterations, LinkedBlockingQueue<CallInfo> latencyQueue, boolean isRecordingThread) throws FileNotFoundException {
        this.url = url;
        this.path = "/"+path;
        this.reviewPath = "/"+path+"/review";
        this.iterations = iterations;
        this.latencyQueue = latencyQueue;
        this.isRecordingThread = isRecordingThread;

        client = new HttpClient();

        // Create a post method instance.
        postMethod = new PostMethod(url+this.path+"/albums");
        Part[] parts = {
                new StringPart("profile", "{\"artist\": \"Shakira\", \"title\": \"waka waka\", \"year\": \"2012\"}", "UTF-8"),
                new FilePart("image",new File("src/assets/nmtb.png"),"image/png", null)
        };
        MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts, postMethod.getParams());
        postMethod.setRequestEntity(requestEntity);
        postMethod.setRequestHeader("accept", "application/json");
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(5, false));
        // Create a get method instance
        postLike = new PostMethod(url);
        postDislike = new PostMethod(url);
    }

    public void run() {
        if (isRecordingThread) {recordingThread();}
        else {startupThread();}
    }

    public void startupThread() {
        int statusCode;

        for (int i = 0; i<iterations ; i++) {
            try {
                // Execute the method.
                statusCode = client.executeMethod(postMethod);
                if (statusCode != HttpStatus.SC_CREATED) {
                    System.out.println(postMethod.getURI());
                    System.out.println("Post album failed");
                    System.err.println("Method failed: " + postMethod.getStatusLine());
                    System.err.println(postMethod.getResponseBodyAsString());
                }
                ImageMetaData postInfo = JsonUtils.jsonToObject(postMethod.getResponseBodyAsString(),ImageMetaData.class);

                postLike.setPath(reviewPath+"/like/"+postInfo.getAlbumID());
                postDislike.setPath(reviewPath+"/dislike/"+postInfo.getAlbumID());

                statusCode = client.executeMethod(postLike);
                if (statusCode != HttpStatus.SC_CREATED) {
                    System.out.println("Like failed");
                    System.err.println("Method failed: " + postLike.getStatusLine());
                    System.err.println(postLike.getResponseBodyAsString());
                }
                postLike.releaseConnection();

                statusCode = client.executeMethod(postLike);
                if (statusCode != HttpStatus.SC_CREATED) {
                    System.out.println("Like failed");
                    System.err.println("Method failed: " + postLike.getStatusLine());
                    System.err.println(postLike.getResponseBodyAsString());
                }
                postLike.releaseConnection();

                statusCode = client.executeMethod(postDislike);
                if (statusCode != HttpStatus.SC_CREATED) {
                    System.out.println("Dislike failed");
                    System.err.println("Method failed: " + postDislike.getStatusLine());
                    System.err.println(postDislike.getResponseBodyAsString());

                }
                postDislike.releaseConnection();


            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Fatal transport error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Release the connection.
                postMethod.releaseConnection();
            }
        }
    }

    public void recordingThread() {
        int statusCode;
        long startTime;
        long endTime;
        long latency;

        //Iterates iterations times calling post then get method
        for (int i = 0; i<iterations ; i++) {
            try {
                // Execute the method.
                startTime = System.currentTimeMillis();
                statusCode = client.executeMethod(postMethod);
                if (statusCode != 201) {
                    System.out.println("Post album failed");
                    System.out.println("Error Message: "+statusCode);
                    System.out.println(postMethod.getResponseBodyAsString());
                }
                endTime = System.currentTimeMillis();
                latency = endTime - startTime;
                //Takes request information and passes instance of CallInfo to latency Queue
                latencyQueue.add(new CallInfo(startTime,"POST",latency,statusCode));

                //Get Album Key from server and adjust the like and dislike path
                ImageMetaData postInfo = JsonUtils.jsonToObject(postMethod.getResponseBodyAsString(),ImageMetaData.class);
                postLike.setPath(reviewPath+"/like/"+postInfo.getAlbumID());
                postDislike.setPath(reviewPath+"/dislike/"+postInfo.getAlbumID());

                startTime = System.currentTimeMillis();
                statusCode = client.executeMethod(postLike);
                if (statusCode != 201) {
                    System.out.println("Like failed");
                    System.out.println("Error Message: " + statusCode);
                    System.out.println(postLike.getResponseBodyAsString());
                }
                endTime = System.currentTimeMillis();
                latency = endTime - startTime;
                latencyQueue.add(new CallInfo(startTime, " PostLike", latency, statusCode));
                postLike.releaseConnection();

                startTime = System.currentTimeMillis();
                statusCode = client.executeMethod(postLike);
                if (statusCode != 201) {
                    System.out.println("Like failed");
                    System.out.println("Error Message: " + statusCode);
                    System.out.println(postLike.getResponseBodyAsString());
                }
                endTime = System.currentTimeMillis();
                latency = endTime - startTime;
                latencyQueue.add(new CallInfo(startTime, " PostLike", latency, statusCode));
                postLike.releaseConnection();

                // Send Dislike request and calculate latency
                startTime = System.currentTimeMillis();
                statusCode = client.executeMethod(postDislike);
                if (statusCode != 201) {
                    System.out.println("Dislike failed");
                    System.out.println("Error Message: " + statusCode);
                    System.out.println(postDislike.getResponseBodyAsString());
                }
                endTime = System.currentTimeMillis();
                latency = endTime - startTime;

                latencyQueue.add(new CallInfo(startTime, " PostDislike", latency, statusCode));
                postDislike.releaseConnection();

            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Fatal transport error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Release the connection.
                postMethod.releaseConnection();
            }
        }
    }
}