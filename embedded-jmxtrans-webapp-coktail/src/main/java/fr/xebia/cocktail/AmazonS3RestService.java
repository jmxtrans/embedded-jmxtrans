package fr.xebia.cocktail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazon.s3.AWSAuthConnection;
import com.amazon.s3.QueryStringAuthGenerator;
import com.amazon.s3.Response;
import com.amazon.s3.S3Object;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;


/**
 * Created with IntelliJ IDEA.
 * User: slemesle
 * Date: 05/06/12
 * Time: 13:01
 * To change this template use File | Settings | File Templates.
 */
@Service
public class AmazonS3RestService {
    
    private static final Logger LOG = LoggerFactory.getLogger(AmazonS3RestService.class);

    private Map<String, String> contentTypeByFileExtension;

    private Map<String, String> defaultFileExtensionByContentType;

    private final Random random = new Random();

    @Value("${aws_s3_bucket_base_url}")
    private String amazonS3BucketBaseUrl;

    @Value("${aws_s3_bucket_name}")
    private String amazonS3BucketName;


    private final AWSAuthConnection conn;
    private final QueryStringAuthGenerator generator;
    
    @Inject
    public AmazonS3RestService(@Value("${AWS_ACCESS_KEY_ID}") String awsAccessKey, @Value("${AWS_SECRET_KEY}") String awsSecretKey) {

        conn =
                new AWSAuthConnection(awsAccessKey, awsSecretKey);
        generator =
                new QueryStringAuthGenerator(awsAccessKey, awsSecretKey);
        
        
        contentTypeByFileExtension = Maps.newHashMapWithExpectedSize(4);
        contentTypeByFileExtension.put("jpg", "image/jpeg");
        contentTypeByFileExtension.put("jpeg", "image/jpeg");
        contentTypeByFileExtension.put("png", "image/png");
        contentTypeByFileExtension.put("gif", "image/gif");

        defaultFileExtensionByContentType = Maps.newHashMapWithExpectedSize(3);
        defaultFileExtensionByContentType.put("image/jpeg", "jpg");
        defaultFileExtensionByContentType.put("image/png", "png");
        defaultFileExtensionByContentType.put("image/gif", "gif");
    }


    @Nullable
    public String findContentType(String fileName) {
        String fileExtension = Iterables.getLast(Splitter.on('.').split(fileName), null);
        fileExtension = Strings.nullToEmpty(fileExtension).toLowerCase();
        return contentTypeByFileExtension.get(fileExtension);
    }

    @Nonnull
    public String storeFile(byte[] buffer, Map objectMetadata) {
        String extension = defaultFileExtensionByContentType.get(objectMetadata.get("Content-Type"));
        String fileName = Math.abs(random.nextLong()) + "." + extension;
        S3Object object =  new S3Object(buffer, objectMetadata);
        try {
            Response r =conn.put(amazonS3BucketName, fileName, object, objectMetadata);
            LOG.info(r.connection.getResponseMessage());
        } catch (IOException e) {
            LOG.error("Problem on put file to S3", e);
        }

        return amazonS3BucketBaseUrl + fileName;
    }
}
